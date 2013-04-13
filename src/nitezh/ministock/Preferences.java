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

package nitezh.ministock;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.TimePicker;
import nitezh.ministock.widget.WidgetBase;

import java.util.concurrent.Callable;

public class Preferences extends PreferencesBase {

    private final String CHANGE_LOG =
            "• Fix issue with NASDAQ stock symbol.<br /><br />• Add option for larger widget font.<br /><br />• Other minor bug-fixes.<br /><br /><i>If you appreciate this app please rate it 5 stars in the Android market!</i>";

    @Override
    protected String getChangeLog() {
        return CHANGE_LOG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Hook the About preference to the About (Ministocks) activity
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
            findPreference(key).setOnPreferenceClickListener(
                    new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            mSymbolSearchKey = preference.getKey();

                            // Start search with current value as query
                            String query =
                                    preference
                                            .getSharedPreferences()
                                            .getString(mSymbolSearchKey, "");
                            startSearch(query, false, null, false);

                            return true;
                        }
                    });
        }

        // Hook up the help preferences
        Preference help_usage = findPreference("help_usage");
        help_usage
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showHelpUsage();
                        return true;
                    }
                });

        // Hook the Help preference to the Help activity
        Preference help_portfolio = findPreference("help_portfolio");
        help_portfolio
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showHelpPortfolio();
                        return true;
                    }
                });

        // Hook the Help preference to the Help activity
        Preference help_prices = findPreference("help_prices");
        help_prices
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

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

                // Update all widgets and quit
                WidgetBase.updateWidgets(
                        getApplicationContext(),
                        WidgetBase.VIEW_UPDATE);

                finish();
                return true;
            }
        });

        // Hook the Portfolio preference to the Portfolio activity
        Preference portfolio = findPreference("portfolio");
        portfolio.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Preferences.this, Portfolio.class);
                startActivity(intent);
                return true;
            }
        });

		/*
         * // Hook the Backup portfolio option to the backup portfolio method
		 * Preference backup = findPreference("backup_portfolio");
		 * backup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		 * 
		 * @Override public boolean onPreferenceClick(Preference preference) {
		 * UserData.backupPortfolio(getApplicationContext());
		 * 
		 * Intent intent = new Intent(Preferences.this, Portfolio.class);
		 * startActivity(intent); return true; } }); // Hook the Restore
		 * portfolio option to the restore portfolio method Preference restore =
		 * findPreference("restore_portfolio");
		 * restore.setOnPreferenceClickListener(new OnPreferenceClickListener()
		 * {
		 * 
		 * @Override public boolean onPreferenceClick(Preference preference) {
		 * UserData.restorePortfolio(getApplicationContext());
		 * 
		 * Intent intent = new Intent(Preferences.this, Portfolio.class);
		 * startActivity(intent); return true; } });
		 */

        // Hook Rate Ministocks preference to the market link
        Preference rate_app = findPreference("rate_app");
        rate_app.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                showFeedbackOption();
                return true;
            }
        });

        // Hook the Feedback preference to the Portfolio activity
        Preference feedback = findPreference("feedback");
        feedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Open the e-mail client with destination and subject
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");

                String[] to_addr = {"nitezh@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, to_addr);
                intent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.app_name) + " BUILD " + Utils.BUILD);
                intent.setType("message/rfc822");

                // In case we can't launch e-mail, show a dialog
                try {
                    startActivity(intent);
                    return true;

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Show dialog if launching e-mail fails
                Tools
                        .showSimpleDialog(
                                getApplicationContext(),
                                "Launching e-mail failed",
                                "We were unable to launch your e-mail client automatically.<br /><br />Our e-mail address for support and feedback is nitezh@gmail.com");
                return true;
            }
        });

        // Hook the Change history preference to the Change history dialog
        Preference change_history = findPreference("change_history");
        change_history
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showChangeLog();
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
        update_start
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showTimePickerDialog(preference, "00:00");
                        return true;
                    }
                });
        Preference update_end = findPreference("update_end");
        update_end
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showTimePickerDialog(preference, "23:59");
                        return true;
                    }
                });
    }

    @Override
    public void
    updateSummaries(SharedPreferences sharedPreferences, String key) {

        // Initialise the Stock summaries
        if (key.startsWith("Stock") && !key.endsWith("_summary")) {

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

            findPreference(key).setTitle(value);
            findPreference(key).setSummary(summary);

            // Initialise the ListPreference summaries
        } else if (key.startsWith("background")
                || key.startsWith("updated_colour")
                || key.startsWith("updated_display")
                || key.startsWith("text_style")) {

            String value = sharedPreferences.getString(key, "");
            findPreference(key).setSummary(
                    "Selected: "
                            + value.substring(0, 1).toUpperCase()
                            + value.substring(1));

        }

        // Initialise the Update interval
        else if (key.startsWith("update_interval")) {

            // Update summary based on selected value
            String displayValue = "30 minutes";
            String value = getAppPrefs().getString(key, "1800000");
            if (value.equals("300000")) {
                displayValue = "5 minutes";
            } else if (value.equals("900000")) {
                displayValue = "15 minutes";
            } else if (value.equals("1800000")) {
                displayValue = "30 minutes";
            } else if (value.equals("3600000")) {
                displayValue = "One hour";
            } else if (value.equals("10800000")) {
                displayValue = "Three hours";
            } else if (value.equals("86400000")) {
                displayValue = "Daily";
            }
            findPreference(key).setSummary("Selected: " + displayValue);

            // Update the value of the update interval
            updateFromGlobal(sharedPreferences, "update_interval", LIST_TYPE);
        }

        // Update time picker summaries
        else if (key.equals("update_start") || key.equals("update_end")) {
            String value = getAppPrefs().getString(key, null);

            mHour = 0;
            mMinute = 0;

            if (value != null) {
                String[] items = value.split(":");
                mHour = Integer.parseInt(items[0]);
                mMinute = Integer.parseInt(items[1]);
            }

            findPreference(key).setSummary(
                    "Time set: "
                            + Tools.timeDigitPad(mHour)
                            + ":"
                            + Tools.timeDigitPad(mMinute));

            // Update the value of the update limits
            updateFromGlobal(sharedPreferences, key, STRING_TYPE);
        } else if (key.equals("update_weekend")) {
            updateFromGlobal(sharedPreferences, key, CHECKBOX_TYPE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Update the widget when we quit the preferences, and if the dirty,
        // flag is true then do a web update, otherwise do a regular update
        if (mStocksDirty) {
            mStocksDirty = false;
            WidgetBase.updateWidgets(
                    getApplicationContext(),
                    WidgetBase.VIEW_UPDATE);

        } else {
            WidgetBase.update(
                    getApplicationContext(),
                    mAppWidgetId,
                    WidgetBase.VIEW_NO_UPDATE);
        }
        finish();
    }

    private void showHelpUsage() {
        String title = "Selecting widget views";
        String body =
                "The widget has multiple views that display different information.<br /><br />These views can be turned on from the Widget views menu in settings.<br /><br />Once selected, the views can be changed on your homescreen by touching the right-side of the widget.<br /><br />If a stock does not have information for a particular view, then the daily percentage change will instead be displayed for that stock in blue.<br /><br /><b>Daily change %</b><br /><br />Shows the current stock price with the daily percentage change.<br /><br /><b>Daily change (DA)</b><br /><br />Shows the current stock price with the daily price change.<br /><br /><b>Total change % (PF T%)</b><br /><br />Shows the current stock price with the total percentage change from the buy price in the portfolio.<br /><br /><b>Total change (PF TA)</b><br /><br />Shows the current stock price with the total price change from the buy price in the portfolio.<br /><br /><b>Total change AER % (PF AER)</b><br /><br />Shows the current stock price with the annualised percentage change using the buy price in the portfolio.<br /><br /><b>P/L daily change % (P/L D%)</b><br /><br />Shows your current holding value with the daily percentage change.<br /><br /><b>P/L daily change (P/L DA)</b><br /><br />Shows your current holding value with the daily price change.<br /><br /><b>P/L total change % (P/L T%)</b><br /><br />Shows your current holding value with the total percentage change from the buy cost in the portfolio.<br /><br /><b>P/L total change (P/L TA)</b><br /><br />Shows your current holding value with the total value change from the buy cost in the portfolio.<br /><br /><b>P/L total change AER (P/L AER)</b><br /><br />Shows your current holding value with the annualised percentage change using the buy cost in the portfolio.";
        Tools.showSimpleDialog(this, title, body);
    }

    private void showHelpPortfolio() {
        String title = "Using the portfolio";
        String body =
                "On the portfolio screen you will see all the stocks that you have entered in your widgets in one list.<br /><br />You can touch an item to enter your stock purchase details.<br /><br /><b>Entering purchase details</b><br /><br />Enter the price that you bought the stock for, this will then be used for the Portfolio and Profit and loss widget views.<br /><br />The Date is optional, and will be used for the AER rate on the portfolio AER and profit and loss AER views.<br /><br />The Quantity is optional, and will be used to calculate your holdings for the profit and loss views.  You may use negative values to simulate a short position.<br /><br />The High price limit and Low price limit are optional.  When the current price hits these limits, the price color will change in the widget.<br /><br /><b>Removing purchase details</b><br /><br />To remove purchase and alert details, long press the portfolio item and then choose the Clear details option.";
        Tools.showSimpleDialog(this, title, body);
    }

    private void showChangeLog() {
        String title = "BUILD " + Utils.BUILD;
        String body = CHANGE_LOG;
        Tools.showSimpleDialog(this, title, body);
    }

    private void showFeedbackOption() {

        @SuppressWarnings("rawtypes")
        Callable callable = new Callable() {

            @Override
            public Object call() throws Exception {
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=nitezh.ministock")));

                return new Object();
            }
        };

        Tools
                .alertWithCallback(
                        this,
                        "Rate Ministocks",
                        "Please support Ministocks by giving the application a 5 star rating in the android market.<br /><br />Motivation to continue to improve the product and add new features comes from positive feedback and ratings.",
                        "Rate it!",
                        "Close",
                        callable);
    }

}
