/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel https://github.com/niteshpatel/ministocks

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

import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class PreferencesBase extends PreferenceActivity
        implements
        OnSharedPreferenceChangeListener {

    // Constants
    public static final int STRING_TYPE = 0;
    public static final int LIST_TYPE = 1;
    public static final int CHECKBOX_TYPE = 2;

    // Public variables
    public static int mAppWidgetId = 0;

    // Private
    protected static boolean mStocksDirty = false;
    protected static String mSymbolSearchKey = "";

    // Meant to be overridden
    private final String CHANGE_LOG = null;

    // Fields for time pickers
    protected TimePickerDialog.OnTimeSetListener mTimeSetListener;
    protected String mTimePickerKey = null;
    protected int mHour = 0;
    protected int mMinute = 0;

    protected String getChangeLog() {
        return CHANGE_LOG;
    }

    @Override
    public void onNewIntent(Intent intent) {

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            setPreference(
                    mSymbolSearchKey,
                    intent.getDataString(),
                    intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));

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

    public SharedPreferences getAppPrefs() {

        // Convenience method to get global preferences
        return super.getSharedPreferences(getString(R.string.prefs_name), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void removePref(PreferenceScreen screen, String name) {
        try {
            screen.removePreference(findPreference(name));

        } catch (Exception e) {
        }
    }

    private void removePref(String screenName, String name) {
        PreferenceScreen screen = (PreferenceScreen) findPreference(screenName);
        try {
            screen.removePreference(findPreference(name));

        } catch (Exception e) {
        }
    }

    private void showRecentChanges() {

        // Return if the change log has already been viewed
        if (getAppPrefs()
                .getString("change_log_viewed", "")
                .equals(Utils.BUILD)) {
            return;
        }

        // Cleanup preferences files
        UserData.cleanupPreferenceFiles(getApplicationContext());

        @SuppressWarnings("rawtypes")
        Callable callable = new Callable() {

            @Override
            public Object call() throws Exception {

                // Ensure we don't show this again
                SharedPreferences prefs = getAppPrefs();
                Editor editor = prefs.edit();
                editor.putString("change_log_viewed", Utils.BUILD);

                // Set first install if not set
                if (prefs.getString("install_date", "").equals("")) {
                    editor.putString("install_date", new SimpleDateFormat(
                            "yyyyMMdd").format(new Date()).toUpperCase());
                }
                editor.commit();

                return new Object();
            }
        };

        Tools.alertWithCallback(
                this,
                "BUILD " + Utils.BUILD,
                getChangeLog(),
                "Close",
                null,
                callable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        showRecentChanges();

        PreferenceScreen screen = getPreferenceScreen();
        SharedPreferences sharedPreferences = screen.getSharedPreferences();

        // Add this widgetId if we don't have it
        Set<Integer> appWidgetIds = new HashSet<Integer>();
        for (int i : UserData.getAppWidgetIds2(getBaseContext()))
            appWidgetIds.add(i);

        if (!appWidgetIds.contains(mAppWidgetId))
            UserData.addAppWidgetId(getBaseContext(), mAppWidgetId, null);

        // Hide preferences for certain widget sizes
        int widgetSize = sharedPreferences.getInt("widgetSize", 0);

        // Remove extra stocks
        if (widgetSize == 0 || widgetSize == 1) {
            PreferenceScreen stock_setup =
                    (PreferenceScreen) findPreference("stock_setup");
            for (int i = 5; i < 11; i++)
                removePref(stock_setup, "Stock" + i);
        }

        // Remove extra widget views
        if (widgetSize == 1 || widgetSize == 3) {
            PreferenceScreen widget_views =
                    (PreferenceScreen) findPreference("widget_views");
            removePref(widget_views, "show_percent_change");
            removePref(widget_views, "show_portfolio_change");
            removePref(widget_views, "show_profit_daily_change");
            removePref(widget_views, "show_profit_change");
        }

        // Hide Feedback option if not relevant
        String install_date = getAppPrefs().getString("install_date", null);
        if (Tools.elapsedDays(install_date, "yyyyMMdd") < 30)
            removePref("about_menu", "rate_app");

        // Initialise the summaries when the preferences screen loads
        Map<String, ?> map = sharedPreferences.getAll();
        for (String key : map.keySet())
            updateSummaries(sharedPreferences, key);

        // Update version number
        findPreference("version").setSummary("BUILD " + Utils.BUILD);

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

    public void
    updateSummaries(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences,
            String key) {

        // Perform some custom handling of some values
        if (key.startsWith("Stock") && !key.endsWith("_summary")) {
            updateStockValue(sharedPreferences, key);

            // Mark stock changed as dirty
            mStocksDirty = true;

        } else if (key.equals("update_interval")) {
            updateGlobalPref(sharedPreferences, key, LIST_TYPE);

            // Warning massage if necessary
            if (sharedPreferences.getString(key, "").equals("900000")
                    || sharedPreferences.getString(key, "").equals("300000")) {

                String title = "Short update interval";
                String body =
                        "Note that choosing a short update interval may drain your battery faster.";
                Tools.showSimpleDialog(this, title, body);
            }

        } else if (key.equals("update_start") || key.equals("update_end")) {
            updateGlobalPref(sharedPreferences, key, STRING_TYPE);

        } else if (key.equals("update_weekend")) {
            updateGlobalPref(sharedPreferences, key, CHECKBOX_TYPE);
        }

        // Update the summary whenever the preference is changed
        updateSummaries(sharedPreferences, key);
    }

    public void
    updateStockValue(SharedPreferences sharedPreferences, String key) {

        // Unregister the listener whenever a key changes
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        // Massages the value: remove whitespace and upper-case
        String value = sharedPreferences.getString(key, "");
        value = value.replace(" ", "");
        value = value.toUpperCase();

        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();

        // Also update the UI
        EditTextPreference preference =
                (EditTextPreference) findPreference(key);
        preference.setText(value);

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void updateFromGlobal(
            SharedPreferences sharedPreferences,
            String key,
            int valType) {

        // Unregister the listener whenever a key changes
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        // Update the widget prefs with the interval
        Editor editor = sharedPreferences.edit();

        if (valType == STRING_TYPE) {
            String value = getAppPrefs().getString(key, "");
            if (!value.equals("")) {
                editor.putString(key, value);
            }

        } else if (valType == LIST_TYPE) {
            String value = getAppPrefs().getString(key, "");
            if (!value.equals("")) {
                editor.putString(key, value);
                ((ListPreference) findPreference(key)).setValue(value);
            }

        } else if (valType == CHECKBOX_TYPE) {
            Boolean value = getAppPrefs().getBoolean(key, false);
            editor.putBoolean(key, value);
            ((CheckBoxPreference) findPreference(key)).setChecked(value);
        }
        editor.commit();

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void updateGlobalPref(
            SharedPreferences sharedPreferences,
            String key,
            int valType) {

        // Unregister the listener whenever a key changes
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        // Update the global preferences with the widget update interval
        Editor editor = getAppPrefs().edit();

        if (valType == STRING_TYPE || valType == LIST_TYPE)
            editor.putString(key, sharedPreferences.getString(key, ""));

        else if (valType == CHECKBOX_TYPE)
            editor.putBoolean(key, sharedPreferences.getBoolean(key, false));

        editor.commit();

        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    protected void showDisclaimer() {
        String title = "Disclaimer";
        String body =
                "Copyright © 2011 Nitesh Patel<br/><br />All rights reserved.<br /><br /> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
        Tools.showSimpleDialog(this, title, body);
    }

    protected void showHelp() {
        String title = "Entering stocks";
        String body =
                "<b>Entering stock symbols</b><br/><br />Stock symbols must be in the Yahoo format, which you can look up on the Yahoo Finance website.";
        Tools.showSimpleDialog(this, title, body);
    }

    protected void showHelpPrices() {
        String title = "Updating prices";
        String body =
                "You can set how often, and when the widget updates in the Advanced settings menu.  The setting applies globally to all the widgets.<br /><br />Stock price information is provided by Yahoo Finance, and there may be a delay (from real-time prices, to up to 30 mins) for some exchanges.<br /><br />Note that the time in the lower-left of the widget is the time that the data was retrieved from Yahoo, not the time of the live price.<br /><br />If an internet connection is not present when an update occurs, the widget will just use the last shown data, and the time for that data.<br /><br /><b>Update prices now feature</b><br /><br />This will update the prices in all your widgets, if there is an internet connection available.";
        Tools.showSimpleDialog(this, title, body);
    }

    protected void showTimePickerDialog(
            Preference preference,
            String defaultValue) {
        // Get the raw value from the preferences
        String value =
                preference.getSharedPreferences().getString(
                        preference.getKey(),
                        defaultValue);

        mHour = 0;
        mMinute = 0;

        if (value != null && !value.equals("")) {
            String[] items = value.split(":");
            mHour = Integer.parseInt(items[0]);
            mMinute = Integer.parseInt(items[1]);
        }

        mTimePickerKey = preference.getKey();
        new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, true)
                .show();
    }

    @Override
    protected void
    onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setTimePickerPreference(int hourOfDay, int minute) {

        // Set the preference value
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        Editor editor = prefs.edit();
        editor.putString(mTimePickerKey, String.valueOf(hourOfDay)
                + ":"
                + String.valueOf(minute));
        editor.commit();

        // Also update the UI
        updateSummaries(
                getPreferenceScreen().getSharedPreferences(),
                mTimePickerKey);
    }

    public void setPreference(String key, String value, String summary) {

        // Return if no key
        if (key.equals("")) {
            return;
        }

        // Set the stock value
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Ignore the remove and manual entry options
        if (value.endsWith("and close")) {
            value = "";

        } else if (value.startsWith("Use ")) {
            value = value.replace("Use ", "");
        }

        // Set dirty
        mStocksDirty = true;

        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.putString(key + "_summary", summary);
        editor.commit();
    }
}
