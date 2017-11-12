/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock.activities;

import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import nitezh.ministock.DialogTools;
import nitezh.ministock.R;
import nitezh.ministock.UserData;
import nitezh.ministock.activities.widget.WidgetProviderBase;
import nitezh.ministock.utils.DateTools;
import nitezh.ministock.utils.VersionTools;

import static android.content.SharedPreferences.Editor;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;


public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    // Constants
    private static final int STRING_TYPE = 0;
    private static final int LIST_TYPE = 1;
    private static final int CHECKBOX_TYPE = 2;
    // Public variables
    public static int mAppWidgetId = 0;
    // Private
    private static boolean mPendingUpdate = false;
    private static String mSymbolSearchKey = "";
    private final String CHANGE_LOG = ""
//            + "New features:<br/><br/>"
//            + "• TODO.<br/><br/>"
            + "Multiple bug fixes:<br/><br/>"
            + "• Allow comma when entering numbers";

    // Fields for time pickers
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private String mTimePickerKey = null;
    private int mHour = 0;
    private int mMinute = 0;

    private String getChangeLog() {
        return CHANGE_LOG;
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            setPreference(mSymbolSearchKey, intent.getDataString(), intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            startSearch(query, false, null, false);
        } else if (Intent.ACTION_EDIT.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            startSearch(query, false, null, false);
        }
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        // Each widget has its own set of preferences
        return super.getSharedPreferences(name + mAppWidgetId, mode);
    }

    private SharedPreferences getAppPreferences() {
        // Convenience method to get global preferences
        return super.getSharedPreferences(getString(R.string.prefs_name), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void removePref(PreferenceScreen screen, String name) {
        try {
            screen.removePreference(findPreference(name));
        } catch (Exception ignored) {
        }
    }

    private void removePref(String screenName, String name) {
        PreferenceScreen screen = (PreferenceScreen) findPreference(screenName);
        try {
            screen.removePreference(findPreference(name));
        } catch (Exception ignored) {
        }
    }

    private void showRecentChanges() {
        // Return if the change log has already been viewed
        if (getAppPreferences().getString("change_log_viewed", "").equals(VersionTools.BUILD)) {
            return;
        }
        // Cleanup preferences files
        UserData.cleanupPreferenceFiles(getApplicationContext());
        @SuppressWarnings("rawtypes") Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                // Ensure we don't show this again
                SharedPreferences preferences = getAppPreferences();
                Editor editor = preferences.edit();
                editor.putString("change_log_viewed", VersionTools.BUILD);

                // Set first install if not set
                if (preferences.getString("install_date", "").equals("")) {
                    editor.putString("install_date", new SimpleDateFormat("yyyyMMdd").format(new Date()).toUpperCase());
                }
                editor.apply();
                return new Object();
            }
        };
        DialogTools.alertWithCallback(this, "BUILD " + VersionTools.BUILD,
                getChangeLog(), "Close", null, callable, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRecentChanges();
        PreferenceScreen screen = getPreferenceScreen();
        SharedPreferences sharedPreferences = screen.getSharedPreferences();

        // Hide preferences for certain widget sizes
        int widgetSize = sharedPreferences.getInt("widgetSize", 0);

        // Remove extra stocks
        if (widgetSize == 0 || widgetSize == 1) {
            PreferenceScreen stock_setup = (PreferenceScreen) findPreference("stock_setup");
            for (int i = 5; i < 11; i++)
                removePref(stock_setup, "Stock" + i);
        }
        // Remove extra widget views
        if (widgetSize == 1 || widgetSize == 3) {
            PreferenceScreen widget_views = (PreferenceScreen) findPreference("widget_views");
            removePref(widget_views, "show_percent_change");
            removePref(widget_views, "show_portfolio_change");
            removePref(widget_views, "show_profit_daily_change");
            removePref(widget_views, "show_profit_change");
        }
        // Hide Feedback option if not relevant
        String install_date = getAppPreferences().getString("install_date", null);
        if (DateTools.elapsedDays(install_date) < 30)
            removePref("about_menu", "rate_app");

        // Initialise the summaries when the preferences screen loads
        Map<String, ?> map = sharedPreferences.getAll();
        for (String key : map.keySet())
            updateSummaries(sharedPreferences, key);

        // Update version number
        findPreference("version").setSummary("BUILD " + VersionTools.BUILD);

        // Force update of global preferences
        // TODO Ensure the items below are included in the above list
        // rather than updating these items twice (potentially)
        updateSummaries(sharedPreferences, "update_interval");
        updateSummaries(sharedPreferences, "update_start");
        updateSummaries(sharedPreferences, "update_end");
        updateSummaries(sharedPreferences, "update_weekend");

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Perform some custom handling of some values
        if (key.startsWith("Stock") && !key.endsWith("_summary")) {
            updateStockValue(sharedPreferences, key);

            // Mark stock changed as dirty
            mPendingUpdate = true;
        } else if (key.equals("update_interval")) {
            updateGlobalPref(sharedPreferences, key, LIST_TYPE);

            // Warning massage if necessary
            if (sharedPreferences.getString(key, "").equals("900000") || sharedPreferences.getString(key, "").equals("300000")) {
                String title = "Short update interval";
                String body = "Note that choosing a short update interval may drain your battery faster.";
                DialogTools.showSimpleDialog(this, title, body);
            }
        } else if (key.equals("update_start") || key.equals("update_end")) {
            updateGlobalPref(sharedPreferences, key, STRING_TYPE);
        } else if (key.equals("update_weekend")) {
            updateGlobalPref(sharedPreferences, key, CHECKBOX_TYPE);
        }
        // Update the summary whenever the preference is changed
        updateSummaries(sharedPreferences, key);
    }

    private void updateStockValue(SharedPreferences sharedPreferences, String key) {
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        // Massages the value: remove whitespace and upper-case
        String value = sharedPreferences.getString(key, "");
        value = value.replace(" ", "");
        value = value.toUpperCase();
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

        // Also update the UI
        EditTextPreference preference = (EditTextPreference) findPreference(key);
        preference.setText(value);

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void updateFromGlobal(SharedPreferences sharedPreferences, String key, int valType) {
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        // Update the widget preferences with the interval
        Editor editor = sharedPreferences.edit();
        if (valType == STRING_TYPE) {
            String value = getAppPreferences().getString(key, "");
            if (!value.equals("")) {
                editor.putString(key, value);
            }
        } else if (valType == LIST_TYPE) {
            String value = getAppPreferences().getString(key, "");
            if (!value.equals("")) {
                editor.putString(key, value);
                ((ListPreference) findPreference(key)).setValue(value);
            }
        } else if (valType == CHECKBOX_TYPE) {
            Boolean value = getAppPreferences().getBoolean(key, false);
            editor.putBoolean(key, value);
            ((CheckBoxPreference) findPreference(key)).setChecked(value);
        }
        editor.apply();

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void updateGlobalPref(SharedPreferences sharedPreferences, String key, int valType) {
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        // Update the global preferences with the widget update interval
        Editor editor = getAppPreferences().edit();
        if (valType == STRING_TYPE || valType == LIST_TYPE)
            editor.putString(key, sharedPreferences.getString(key, ""));
        else if (valType == CHECKBOX_TYPE)
            editor.putBoolean(key, sharedPreferences.getBoolean(key, false));
        editor.apply();

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void showDisclaimer() {
        String title = "License";
        String body = "The MIT License (MIT)<br/><br/>Copyright © 2013 Nitesh Patel<br/><br />Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:<br /><br />The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.<br/><br/>THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showAttributions() {
        String title = "Attributions";
        String body = "Data provided for free by IEX (https://iextrading.com/developer).";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showTermsOfService() {
        String title = "Terms of Service (data)";
        String body = "See https://iextrading.com/api-exhibit-a";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showHelp() {
        String title = "Entering stocks";
        String body = "<b>Entering stock symbols</b><br/><br />Stock symbols must be in the Yahoo format, which you can look up on the Yahoo Finance website.";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showHelpPrices() {
        String title = "Updating prices";
        String body = "You can set how often, and when the widget updates in the Advanced settings menu.  The setting applies globally to all the widgets.<br /><br />Stock price information is provided by Yahoo Finance, and there may be a delay (from real-time prices, to up to 30 minutes) for some exchanges.<br /><br />Note that the time in the lower-left of the widget is the time that the data was retrieved from Yahoo, not the time of the live price.<br /><br />If an internet connection is not present when an update occurs, the widget will just use the last shown data, and the time for that data.<br /><br /><b>Update prices now feature</b><br /><br />This will update the prices in all your widgets, if there is an internet connection available.";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showTimePickerDialog(Preference preference, String defaultValue) {
        // Get the raw value from the preferences
        String value = preference.getSharedPreferences().getString(preference.getKey(), defaultValue);
        mHour = 0;
        mMinute = 0;
        if (value != null && !value.equals("")) {
            String[] items = value.split(":");
            mHour = Integer.parseInt(items[0]);
            mMinute = Integer.parseInt(items[1]);
        }
        mTimePickerKey = preference.getKey();
        new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, true).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setTimePickerPreference(int hourOfDay, int minute) {
        // Set the preference value
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        Editor editor = preferences.edit();
        editor.putString(mTimePickerKey, String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
        editor.apply();

        // Also update the UI
        updateSummaries(getPreferenceScreen().getSharedPreferences(), mTimePickerKey);
    }

    private void setPreference(String key, String value, String summary) {
        // Return if no key
        if (key.equals("")) {
            return;
        }
        // Set the stock value
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();

        // Ignore the remove and manual entry options
        if (value.endsWith("and close")) {
            value = "";
        } else if (value.startsWith("Use ")) {
            value = value.replace("Use ", "");
        }
        // Set dirty
        mPendingUpdate = true;
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.putString(key + "_summary", summary);
        editor.apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Hook the About preference to the About (MinistocksActivity) activity
        Preference about = findPreference("about");
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDisclaimer();
                return true;
            }
        });
        // Hook up the help preferences
        Preference help = findPreference("help");
        help.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showHelp();
                return true;
            }
        });
        // Hook up the symbol search for the stock preferences
        for (int i = 1; i < 11; i++) {
            String key = "Stock" + i;
            findPreference(key).setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mSymbolSearchKey = preference.getKey();

                    // Start search with current value as query
                    String query = preference.getSharedPreferences().getString(mSymbolSearchKey, "");
                    startSearch(query, false, null, false);
                    return true;
                }
            });
        }
        // Hook up the help preferences
        Preference help_usage = findPreference("help_usage");
        help_usage.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showHelpUsage();
                return true;
            }
        });
        // Hook the Help preference to the Help activity
        Preference help_portfolio = findPreference("help_portfolio");
        help_portfolio.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showHelpPortfolio();
                return true;
            }
        });
        // Hook the Help preference to the Help activity
        Preference help_prices = findPreference("help_prices");
        help_prices.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showHelpPrices();
                return true;
            }
        });
        // Hook the Update preference to the Help activity
        Preference updateNow = findPreference("update_now");
        updateNow.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mPendingUpdate = true;
                finish();
                return true;
            }
        });
        // Hook the PortfolioActivity preference to the PortfolioActivity activity
        Preference portfolio = findPreference("portfolio");
        portfolio.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, PortfolioActivity.class);
                startActivity(intent);
                return true;
            }
        });

        /*
        // Hook the Backup portfolio option to the backup portfolio method
        Preference backup_portfolio = findPreference("backup_portfolio");
        backup_portfolio.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Storage storage = PreferenceStorage.getInstance(PreferencesActivity.this);
                Cache cache = new StorageCache(storage);
                WidgetRepository widgetRepository = new AndroidWidgetRepository(PreferencesActivity.this);
                new PortfolioStockRepository(storage, cache, widgetRepository).backupPortfolio(PreferencesActivity.this);
                return true;
            }
        });


        // Hook the Restore portfolio option to the restore portfolio method
        Preference restore_portfolio = findPreference("restore_portfolio");
        restore_portfolio.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Storage storage = PreferenceStorage.getInstance(PreferencesActivity.this);
                Cache cache = new StorageCache(storage);
                WidgetRepository widgetRepository = new AndroidWidgetRepository(PreferencesActivity.this);
                new PortfolioStockRepository(storage, cache, widgetRepository).restorePortfolio(PreferencesActivity.this);
                return true;
            }
        });
        */

        // Hook the Backup widget option to the backup widget method
        Preference backup_widget = findPreference("backup_widget");
        backup_widget.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogTools.InputAlertCallable callable = new DialogTools.InputAlertCallable() {
                    @Override
                    public Object call() throws Exception {
                        UserData.backupWidget(PreferencesActivity.this, mAppWidgetId, this.getInputValue());
                        return new Object();
                    }
                };
                DialogTools.inputWithCallback(PreferencesActivity.this,
                        "Backup this widget", "Please enter a name for this backup:",
                        "OK", "Cancel",
                        "Widget <" + mAppWidgetId + "> from " + DateTools.getNowAsString(),
                        callable);
                return true;
            }
        });

        // Hook the Restore widget option to the restore widget method
        Preference restore_widget = findPreference("restore_widget");
        restore_widget.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CharSequence[] backupNames = UserData.getWidgetBackupNames(PreferencesActivity.this);
                // If there are no backups show an appropriate dialog
                if (backupNames == null) {
                    DialogTools.showSimpleDialog(PreferencesActivity.this,
                            "No backups available", "There were no widget backups to restore.");
                    return true;
                }

                // If there are backups then show the list
                DialogTools.InputAlertCallable callable = new DialogTools.InputAlertCallable() {
                    @Override
                    public Object call() throws Exception {
                        UserData.restoreWidget(PreferencesActivity.this, mAppWidgetId, this.getInputValue());
                        return new Object();
                    }
                };
                DialogTools.choiceWithCallback(PreferencesActivity.this,
                        "Select a widget backup to restore", "Cancel", backupNames, callable);
                return true;
            }
        });

        // Hook the Delete widget backup option to the delete widget backup method
        Preference delete_widget_backup = findPreference("delete_widget_backup");
        delete_widget_backup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CharSequence[] backupNames = UserData.getWidgetBackupNames(PreferencesActivity.this);
                // If there are no backups show an appropriate dialog
                if (backupNames == null) {
                    DialogTools.showSimpleDialog(PreferencesActivity.this,
                            "No backups available to delete", "There were no widget backups to delete.");
                    return true;
                }

                // If there are backups then show the list
                DialogTools.InputAlertCallable callable = new DialogTools.InputAlertCallable() {
                    @Override
                    public Object call() throws Exception {
                        UserData.deleteWidgetBackup(PreferencesActivity.this, this.getInputValue());
                        return new Object();
                    }
                };
                DialogTools.choiceWithCallback(PreferencesActivity.this,
                        "Select a widget backup to delete", "Cancel", backupNames, callable);
                return true;
            }
        });

        // Hook Rate MinistocksActivity preference to the market link
        Preference rate_app = findPreference("rate_app");
        rate_app.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showFeedbackOption();
                return true;
            }
        });

        // Hook the Online help preference to the online help link
        Preference online_help = findPreference("online_help");
        online_help.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showOnlineHelp();
                return true;
            }
        });

        // Hook the Online faqs preference to the online faqs link
        Preference online_faqs = findPreference("online_faqs");
        online_faqs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showOnlineFaqs();
                return true;
            }
        });

        // Hook the Feedback preference to the PortfolioActivity activity
        Preference feedback = findPreference("feedback");
        feedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Open the e-mail client with destination and subject
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                String[] toAddress = {"nitezh@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, toAddress);
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " BUILD " + VersionTools.BUILD);
                intent.setType("message/rfc822");

                // In case we can't launch e-mail, show a dialog
                try {
                    startActivity(intent);
                    return true;
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // Show dialog if launching e-mail fails
                DialogTools.showSimpleDialog(getApplicationContext(), "Launching e-mail failed", "We were unable to launch your e-mail client automatically.<br /><br />Our e-mail address for support and feedback is nitezh@gmail.com");
                return true;
            }
        });
        // Hook the Change history preference to the Change history dialog
        Preference change_history = findPreference("change_history");
        change_history.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showChangeLog();
                return true;
            }
        });
        // Hook the Attributions preference to the Attributions dialog
        Preference attributions = findPreference("attributions");
        attributions.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAttributions();
                return true;
            }
        });
        // Hook the Terms of Service preference to the Terms of Service dialog
        Preference termsOfService = findPreference("termsOfService");
        termsOfService.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTermsOfService();
                return true;
            }
        });
        // Callback received when the user sets the time in the dialog
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setTimePickerPreference(hourOfDay, minute);
            }
        };
        // Hook the Update schedule preferences up
        Preference update_start = findPreference("update_start");
        update_start.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePickerDialog(preference, "00:00");
                return true;
            }
        });
        Preference update_end = findPreference("update_end");
        update_end.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePickerDialog(preference, "23:59");
                return true;
            }
        });
    }

    private void updateSummaries(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null) {

            // Initialise the Stock summaries
            if (sharedPreferences.contains(key) && key.startsWith("Stock") && !key.endsWith("_summary")) {
                // Update the summary based on the stock value
                String value = sharedPreferences.getString(key, "");
                String summary = sharedPreferences.getString(key + "_summary", "");

                // Set the title
                if (value.equals("")) {
                    value = key.replace("Stock", "Stock ");
                    summary = "Set symbol";
                }
                // Set the summary appropriately
                else if (summary.equals("")) {
                    summary = "No description";
                }

                preference.setTitle(value);
                preference.setSummary(summary);
            }

            // Initialise the ListPreference summaries
            else if (key.startsWith("background") || key.startsWith("updated_colour") || key.startsWith("updated_display") || key.startsWith("text_style")) {
                String value = sharedPreferences.getString(key, "");
                preference.setSummary("Selected: " + value.substring(0, 1).toUpperCase() + value.substring(1));
            }
            // Initialise the Update interval
            else if (key.startsWith("update_interval")) {
                // Update summary based on selected value
                String displayValue = "30 minutes";
                String value = getAppPreferences().getString(key, "1800000");
                switch (value) {
                    case "300000":
                        displayValue = "5 minutes";
                        break;
                    case "900000":
                        displayValue = "15 minutes";
                        break;
                    case "1800000":
                        displayValue = "30 minutes";
                        break;
                    case "3600000":
                        displayValue = "One hour";
                        break;
                    case "10800000":
                        displayValue = "Three hours";
                        break;
                    case "86400000":
                        displayValue = "Daily";
                        break;
                }
                preference.setSummary("Selected: " + displayValue);

                // Update the value of the update interval
                updateFromGlobal(sharedPreferences, "update_interval", LIST_TYPE);
            }
            // Update time picker summaries
            else if (key.equals("update_start") || key.equals("update_end")) {
                String value = getAppPreferences().getString(key, null);
                mHour = 0;
                mMinute = 0;
                if (value != null) {
                    String[] items = value.split(":");
                    mHour = Integer.parseInt(items[0]);
                    mMinute = Integer.parseInt(items[1]);
                }
                preference.setSummary("Time set: " + DateTools.timeDigitPad(mHour) + ":" + DateTools.timeDigitPad(mMinute));

                // Update the value of the update limits
                updateFromGlobal(sharedPreferences, key, STRING_TYPE);
            } else if (key.equals("update_weekend")) {
                updateFromGlobal(sharedPreferences, key, CHECKBOX_TYPE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Update the widget when we quit the preferences, and if the dirty,
        // flag is true then do a web update, otherwise do a regular update
        if (mPendingUpdate) {
            mPendingUpdate = false;
            WidgetProviderBase.updateWidgets(getApplicationContext(),
                    WidgetProviderBase.UpdateType.VIEW_UPDATE);
        } else {
            WidgetProviderBase.updateWidgetAsync(getApplicationContext(), mAppWidgetId,
                    WidgetProviderBase.UpdateType.VIEW_NO_UPDATE);
        }
        finish();
    }

    private void showHelpUsage() {
        String title = "Selecting widget views";
        String body = "The widget has multiple views that display different information.<br /><br />These views can be turned on from the AppWidgetProvider views menu in settings.<br /><br />Once selected, the views can be changed on your home screen by touching the right-side of the widget.<br /><br />If a stock does not have information for a particular view, then the daily percentage change will instead be displayed for that stock in blue.<br /><br /><b>Daily change %</b><br /><br />Shows the current stock price with the daily percentage change.<br /><br /><b>Daily change (DA)</b><br /><br />Shows the current stock price with the daily price change.<br /><br /><b>Total change % (PF T%)</b><br /><br />Shows the current stock price with the total percentage change from the buy price in the portfolio.<br /><br /><b>Total change (PF TA)</b><br /><br />Shows the current stock price with the total price change from the buy price in the portfolio.<br /><br /><b>Total change AER % (PF AER)</b><br /><br />Shows the current stock price with the annualised percentage change using the buy price in the portfolio.<br /><br /><b>P/L daily change % (P/L D%)</b><br /><br />Shows your current holding value with the daily percentage change.<br /><br /><b>P/L daily change (P/L DA)</b><br /><br />Shows your current holding value with the daily price change.<br /><br /><b>P/L total change % (P/L T%)</b><br /><br />Shows your current holding value with the total percentage change from the buy cost in the portfolio.<br /><br /><b>P/L total change (P/L TA)</b><br /><br />Shows your current holding value with the total value change from the buy cost in the portfolio.<br /><br /><b>P/L total change AER (P/L AER)</b><br /><br />Shows your current holding value with the annualised percentage change using the buy cost in the portfolio.";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showHelpPortfolio() {
        String title = "Using the portfolio";
        String body = "On the portfolio screen you will see all the stocks that you have entered in your widgets in one list.<br /><br />You can touch an item to enter your stock purchase details.<br /><br /><b>Entering purchase details</b><br /><br />Enter the price that you bought the stock for, this will then be used for the PortfolioActivity and Profit and loss widget views.<br /><br />The Date is optional, and will be used for the AER rate on the portfolio AER and profit and loss AER views.<br /><br />The Quantity is optional, and will be used to calculate your holdings for the profit and loss views.  You may use negative values to simulate a short position.<br /><br />The High price limit and Low price limit are optional.  When the current price hits these limits, the price color will change in the widget.<br /><br /><b>Removing purchase details</b><br /><br />To remove purchase and alert details, long press the portfolio item and then choose the Clear details option.";
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showChangeLog() {
        String title = "BUILD " + VersionTools.BUILD;
        String body = CHANGE_LOG;
        DialogTools.showSimpleDialog(this, title, body);
    }

    private void showOnlineHelp() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://niteshpatel.github.io/ministocks/help.html"));
        startActivity(browserIntent);
    }

    private void showOnlineFaqs() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://niteshpatel.github.io/ministocks/faq.html"));
        startActivity(browserIntent);
    }

    private void showFeedbackOption() {
        @SuppressWarnings("rawtypes") Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=nitezh.ministock")));
                return new Object();
            }
        };
        DialogTools.alertWithCallback(this, "Rate MinistocksActivity",
                "Please support MinistocksActivity by giving the application a 5 star rating in the android market.<br /><br />Motivation to continue to improve the product and add new features comes from positive feedback and ratings.",
                "Rate it!", "Close", callable, null);
    }
}
