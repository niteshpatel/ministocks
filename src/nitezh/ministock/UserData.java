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

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.WidgetRepository;


public class UserData {

    public static final String PORTFOLIO_JSON = "portfolioJson";
    public static final String WIDGET_JSON = "widgetJson";
    // Object for intrinsic lock
    public static final Object sFileBackupLock = new Object();
    private static final HashMap<String, HashMap<PortfolioField, String>> mPortfolioStockMap = new HashMap<>();
    // Cache markers
    private static boolean mDirtyPortfolioStockMap = true;

    public static void addAppWidgetSize(Context context, int appWidgetId, int widgetSize) {
        Storage appStorage = PreferenceTools.getAppPreferences(context);
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context, appStorage);
        Storage storage = widgetRepository.getWidgetStorage(appWidgetId);
        storage.putInt("widgetSize", widgetSize);
        storage.apply();
    }

    public static void addAppWidgetId(Context context, int appWidgetId, Integer widgetSize) {
        // Get the existing widgetIds from the preferences
        LocalStorage storage = PreferenceTools.getAppPreferences(context);

        // Add the new appWidgetId
        StringBuilder rawAppWidgetIds = new StringBuilder();
        rawAppWidgetIds.append(storage.getString("appWidgetIds", ""));
        if (!rawAppWidgetIds.toString().equals(""))
            rawAppWidgetIds.append(",");
        rawAppWidgetIds.append(String.valueOf(appWidgetId));

        // Update the preferences too
        storage.putString("appWidgetIds", rawAppWidgetIds.toString());
        storage.apply();

        // Only add the widget size if provided
        if (widgetSize != null)
            addAppWidgetSize(context, appWidgetId, widgetSize);
    }

    public static void delAppWidgetId(Context context, int appWidgetId) {
        // Get the existing widgetIds from the storage
        LocalStorage storage = PreferenceTools.getAppPreferences(context);
        ArrayList<String> newAppWidgetIds = new ArrayList<>();
        Collections.addAll(newAppWidgetIds, storage.getString("appWidgetIds", "").split(","));

        // Remove the one to remove
        newAppWidgetIds.remove(String.valueOf(appWidgetId));

        // Add the new appWidgetId
        StringBuilder appWidgetIds = new StringBuilder();
        for (String id : newAppWidgetIds)
            appWidgetIds.append(id).append(",");

        // Remove trailing comma
        if (appWidgetIds.length() > 0)
            appWidgetIds.deleteCharAt(appWidgetIds.length() - 1);

        // Update the storage too
        storage.putString("appWidgetIds", appWidgetIds.toString());
        storage.apply();
    }

    public static HashMap<String, HashMap<PortfolioField, String>> getPortfolioStockMap(Storage storage) {
        // If data is unchanged return cached version
        if (!mDirtyPortfolioStockMap)
            return mPortfolioStockMap;

        // Clear the old data
        mPortfolioStockMap.clear();

        // Use the Json data if present
        String rawJson = storage.getString(PORTFOLIO_JSON, "");
        if (rawJson.equals("")) {
            // If there is no Json data then use the old style data
            for (String rawStock : storage.getString("portfolio", "").split(",")) {
                String[] stockArray = rawStock.split(":");

                // Skip empties and invalid formatted stocks
                if (stockArray.length != 2)
                    continue;

                // Create stock map, ignoring any items with nulls
                String[] stockInfo = stockArray[1].split("\\|");
                if (stockInfo.length > 0 && stockInfo[0] != null) {
                    HashMap<PortfolioField, String> stockInfoMap = new HashMap<>();
                    for (PortfolioField f : PortfolioField.values()) {
                        String data = "";
                        if (stockInfo.length > f.ordinal() && !stockInfo[f.ordinal()].equals("empty")) {
                            data = stockInfo[f.ordinal()];
                        }
                        stockInfoMap.put(f, data);
                    }
                    mPortfolioStockMap.put(stockArray[0], stockInfoMap);
                }
            }
        } else {
            JSONObject json = null;
            try {
                json = new JSONObject(rawJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Parse the stock info from the raw string
            Iterator keys;
            if (json != null) {
                keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next().toString();
                    JSONObject itemData = new JSONObject();
                    try {
                        itemData = json.getJSONObject(key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    HashMap<PortfolioField, String> stockInfoMap = new HashMap<>();
                    for (PortfolioField f : PortfolioField.values()) {
                        String data = "";
                        try {
                            if (!itemData.get(f.name()).equals("empty")) {
                                data = itemData.get(f.name()).toString();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        stockInfoMap.put(f, data);
                    }
                    mPortfolioStockMap.put(key, stockInfoMap);
                }
            }
        }

        // Set marker clean and return
        mDirtyPortfolioStockMap = false;
        return mPortfolioStockMap;
    }

    public static void setPortfolioStockMap(Context context, HashMap<String, HashMap<PortfolioField, String>> stockMap) {
        // Convert the portfolio stock map into a Json string to store in preferences
        JSONObject json = new JSONObject();
        for (String symbol : stockMap.keySet()) {

            // Create the raw string, ignoring any items with nulls
            HashMap<PortfolioField, String> stockInfoMap = stockMap.get(symbol);
            if ((stockInfoMap.get(PortfolioField.PRICE) != null && !stockInfoMap.get(PortfolioField.PRICE).equals("")) || (stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY) != null && !stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY).equals(""))) {

                // Create a JSON object to store this data
                JSONObject itemData = new JSONObject();
                try {
                    json.put(symbol, itemData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (PortfolioField f : PortfolioField.values()) {
                    // Replace null dates with an empty string
                    String data = stockInfoMap.get(f);
                    if (data == null || data.equals(""))
                        data = "empty";
                    try {
                        itemData.put(f.name(), data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Commit changes to the preferences
        LocalStorage editor = PreferenceTools.getAppPreferences(context);
        editor.putString(PORTFOLIO_JSON, json.toString());
        editor.apply();

        // Set the cache flag as dirty
        mDirtyPortfolioStockMap = true;
    }

    public static HashMap<String, HashMap<PortfolioField, String>> getPortfolioStockMapForWidget(Storage appStorage, String[] symbols) {
        HashMap<String, HashMap<PortfolioField, String>> portfolioStockMapForWidget = new HashMap<>();
        HashMap<String, HashMap<PortfolioField, String>> portfolioStockMap = getPortfolioStockMap(appStorage);

        // Add stock details for any symbols that exist in the widget
        for (String symbol : Arrays.asList(symbols)) {
            HashMap<PortfolioField, String> stockInfoMap = portfolioStockMap.get(symbol);
            if (stockInfoMap != null && (stockInfoMap.get(PortfolioField.PRICE) != null || stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY) != null))
                portfolioStockMapForWidget.put(symbol, stockInfoMap);
        }
        return portfolioStockMapForWidget;
    }

    public static void cleanupPreferenceFiles(Context context, Storage storage) {
        // Remove old preferences if we are upgrading
        ArrayList<String> l = new ArrayList<>();

        // Shared preferences is never deleted
        l.add(context.getString(R.string.prefs_name) + ".xml");
        WidgetRepository repository = new AndroidWidgetRepository(context, storage);
        for (int id : repository.getIds())
            l.add(context.getString(R.string.prefs_name) + id + ".xml");

        // Remove files we do not have an active widget for
        String appDir = context.getFilesDir().getParentFile().getPath();
        File f_shared_preferences = new File(appDir + "/shared_prefs");

        // Check if shared_preferences exists
        // TODO: Work out why this is ever null and an alternative strategy
        if (f_shared_preferences.exists())
            for (File f : f_shared_preferences.listFiles())
                if (!l.contains(f.getName()))
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
    }

    public static void backupPortfolio(Context context) {
        // Get current portfolio from preferences
        String rawJson = PreferenceTools.getAppPreferences(context).getString(PORTFOLIO_JSON, "");

        // Store portfolio in internal storage
        writeInternalStorage(context, rawJson, PORTFOLIO_JSON);

        // Show confirmation to user
        DialogTools.showSimpleDialog(context, "Portfolio backed up", "Your portfolio settings have been backed up to internal storage.");
    }

    public static void restorePortfolio(Context context) {
        // Get portfolio from internal storage
        String rawJson = readInternalStorage(context, PORTFOLIO_JSON);

        // Store portfolio in preferences
        LocalStorage storage = PreferenceTools.getAppPreferences(context);
        storage.putString(PORTFOLIO_JSON, rawJson);
        storage.apply();
        mDirtyPortfolioStockMap = true;

        // Show confirmation to user
        DialogTools.showSimpleDialog(context, "Portfolio restored", "Your portfolio settings have been restored from internal storage.");
    }

    public static void backupWidget(Context context, int appWidgetId, String backupName) {
        try {
            // Get existing data collection from storage if present
            JSONObject backupContainer = new JSONObject();
            String rawJson = readInternalStorage(context, WIDGET_JSON);
            if (rawJson != null) {
                backupContainer = new JSONObject(rawJson);
            }
            // Now get data for current widget, append to existing data and write to storage
            JSONObject backupJson = PreferenceTools.getWidgetPreferencesAsJson(context, appWidgetId);
            backupContainer.put(backupName, backupJson);
            writeInternalStorage(context, backupContainer.toString(), WIDGET_JSON);
        } catch (JSONException ignored) {
        }
    }

    public static void restoreWidget(Context context, int appWidgetId, String backupName) {
        try {
            // Get existing data collection from storage
            JSONObject backupContainer = new JSONObject(readInternalStorage(context, WIDGET_JSON));

            // Update widget with preferences from backup
            PreferenceTools.setWidgetPreferencesFromJson(context, appWidgetId, backupContainer.getJSONObject(backupName));

            // Show confirmation to user
            DialogTools.showSimpleDialog(context, "Widget restored", "The current widget preferences have been restored from your selected backup.");

            // restart activity to force reload of preferences
            Activity activity = ((Activity) context);
            Intent intent = activity.getIntent();
            activity.finish();
            activity.startActivity(intent);
        } catch (JSONException ignored) {
        }
    }

    public static CharSequence[] getWidgetBackupNames(Context context) {
        // Get existing data collection from storage
        try {
            String rawJson = readInternalStorage(context, WIDGET_JSON);
            if (rawJson == null) {
                return null;
            }
            JSONObject backupContainer = new JSONObject(readInternalStorage(context, WIDGET_JSON));
            Iterator<String> iterator = backupContainer.keys();
            ArrayList<String> backupList = new ArrayList<>();
            while (iterator.hasNext()) {
                backupList.add(iterator.next());
            }
            CharSequence[] backupNames = new String[backupList.size()];
            return backupList.toArray(backupNames);
        } catch (JSONException ignored) {
        }
        return null;
    }

    private static void writeInternalStorage(Context context, String stringData, String filename) {
        try {
            synchronized (UserData.sFileBackupLock) {
                FileOutputStream fos;
                fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(stringData.getBytes());
                fos.close();
            }
            BackupManager backupManager = new BackupManager(context);
            backupManager.dataChanged();
        } catch (IOException ignored) {
        }
    }

    private static String readInternalStorage(Context context, String filename) {
        try {
            StringBuffer fileContent = new StringBuffer();
            synchronized (UserData.sFileBackupLock) {
                FileInputStream fis;
                fis = context.openFileInput(filename);
                byte[] buffer = new byte[1024];
                while (fis.read(buffer) != -1) {
                    fileContent.append(new String(buffer));
                }
            }
            return new String(fileContent);
        } catch (IOException ignored) {
        }
        return null;
    }

    public enum PortfolioField {
        PRICE, DATE, QUANTITY, LIMIT_HIGH, LIMIT_LOW, CUSTOM_DISPLAY, SYMBOL_2
    }
}
