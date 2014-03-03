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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class UserData {

    private static final HashMap<String, HashMap<PortfolioField, String>> mPortfolioStockMap = new HashMap<String, HashMap<PortfolioField, String>>();
    // Cache markers
    private static boolean mDirtyPortfolioStockMap = true;

    public static void addAppWidgetSize(
            Context context,
            int appWidgetId,
            int widgetSize) {

        // Record widgetSize
        Editor editor = Tools.getWidgetPreferences(context, appWidgetId).edit();
        editor.putInt("widgetSize", widgetSize);
        editor.commit();
    }

    public static void addAppWidgetId(
            Context context,
            int appWidgetId,
            Integer widgetSize) {

        // Get the existing widgetIds from the preferences
        SharedPreferences preferences = Tools.getAppPreferences(context);

        // Add the new appWidgetId
        StringBuilder rawAppWidgetIds = new StringBuilder();
        rawAppWidgetIds.append(preferences.getString("appWidgetIds", ""));
        if (!rawAppWidgetIds.toString().equals(""))
            rawAppWidgetIds.append(",");

        rawAppWidgetIds.append(String.valueOf(appWidgetId));

        // Update the preferences too
        Editor editor = preferences.edit();
        editor.putString("appWidgetIds", rawAppWidgetIds.toString());
        editor.commit();

        // Only add the widget size if provided
        if (widgetSize != null)
            addAppWidgetSize(context, appWidgetId, widgetSize);
    }

    public static void delAppWidgetId(Context context, int appWidgetId) {

        // Get the existing widgetIds from the preferences
        SharedPreferences preferences = Tools.getAppPreferences(context);

        ArrayList<String> newAppWidgetIds = new ArrayList<String>();
        Collections.addAll(newAppWidgetIds, preferences.getString("appWidgetIds", "").split(","));

        // Remove the one to remove
        newAppWidgetIds.remove(String.valueOf(appWidgetId));

        // Add the new appWidgetId
        StringBuilder appWidgetIds = new StringBuilder();
        for (String id : newAppWidgetIds)
            appWidgetIds.append(id).append(",");

        // Remove trailing comma
        if (appWidgetIds.length() > 0)
            appWidgetIds.deleteCharAt(appWidgetIds.length() - 1);

        // Update the preferences too
        Editor editor = preferences.edit();
        editor.putString("appWidgetIds", appWidgetIds.toString());
        editor.commit();
    }

    public static int[] getAppWidgetIds2(Context context) {

        // Get the widgetIds from the preferences
        SharedPreferences prefs = Tools.getAppPreferences(context);

        StringBuilder rawAppWidgetIds = new StringBuilder();
        rawAppWidgetIds.append(prefs.getString("appWidgetIds", ""));

        // Create an array of appWidgetIds
        String[] appWidgetIds = rawAppWidgetIds.toString().split(",");
        int appWidgetIdsLength = 0;
        if (!rawAppWidgetIds.toString().equals(""))
            appWidgetIdsLength = appWidgetIds.length;

        int[] savedAppWidgetIds = new int[appWidgetIdsLength];
        for (int i = 0; i < appWidgetIds.length; i++)
            if (!appWidgetIds[i].equals(""))
                savedAppWidgetIds[i] = Integer.parseInt(appWidgetIds[i]);

        return savedAppWidgetIds;
    }

    public static Set<String> getWidgetsStockSymbols(Context context) {

        Set<String> widgetStockSymbols = new HashSet<String>();
        SharedPreferences widgetPreferences;

        // Add the stock symbols from the widget preferences
        for (int appWidgetId : getAppWidgetIds2(context)) {

            widgetPreferences = Tools.getWidgetPreferences(context, appWidgetId);
            if (widgetPreferences == null)
                continue;

            // If widget preferences were found, extract the stock symbols
            for (int i = 1; i < 11; i++) {

                String stockSymbol = widgetPreferences.getString("Stock" + i, "");
                if (!stockSymbol.equals(""))
                    widgetStockSymbols.add(stockSymbol);
            }
        }
        return widgetStockSymbols;
    }

    public static HashMap<String, HashMap<PortfolioField, String>>
    getPortfolioStockMap(Context context) {

        // If data is unchanged return cached version
        if (!mDirtyPortfolioStockMap)
            return mPortfolioStockMap;

        // Clear the old data
        mPortfolioStockMap.clear();

        // Use the Json data if present
        String rawJson = Tools.getAppPreferences(context).getString("portfolioJson", "");
        if (rawJson.equals("")) {

            // If there is no Json data then use the old style data
            for (String rawStock : Tools
                    .getAppPreferences(context)
                    .getString("portfolio", "")
                    .split(",")) {

                String[] stockArray = rawStock.split(":");

                // Skip empties and invalid formatted stocks
                if (stockArray.length != 2)
                    continue;

                // Create stock map, ignoring any items with nulls
                String[] stockInfo = stockArray[1].split("\\|");
                if (stockInfo.length > 0 && stockInfo[0] != null) {

                    HashMap<PortfolioField, String> stockInfoMap =
                            new HashMap<PortfolioField, String>();
                    for (PortfolioField f : PortfolioField.values()) {
                        String data = "";
                        if (stockInfo.length > f.ordinal()
                                && !stockInfo[f.ordinal()].equals("empty")) {
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
            Iterator keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                JSONObject itemData = new JSONObject();
                try {
                    itemData = json.getJSONObject(key);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HashMap<PortfolioField, String> stockInfoMap =
                        new HashMap<PortfolioField, String>();
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

        // Set marker clean and return
        mDirtyPortfolioStockMap = false;
        return mPortfolioStockMap;
    }

    public static void setPortfolioStockMap(
            Context context,
            HashMap<String, HashMap<PortfolioField, String>> stockMap) {

        // Convert the portfolio stock map into a Json string to store in preferences
        JSONObject json = new JSONObject();
        for (String symbol : stockMap.keySet()) {

            // Create the raw string, ignoring any items with nulls
            HashMap<PortfolioField, String> stockInfoMap = stockMap.get(symbol);
            if ((stockInfoMap.get(PortfolioField.PRICE) != null && !stockInfoMap
                    .get(PortfolioField.PRICE)
                    .equals(""))
                    || (stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY) != null && !stockInfoMap
                    .get(PortfolioField.CUSTOM_DISPLAY)
                    .equals(""))) {

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
        Editor editor = Tools.getAppPreferences(context).edit();
        editor.putString("portfolioJson", json.toString());
        editor.commit();

        // Set the cache flag as dirty
        mDirtyPortfolioStockMap = true;
    }

    public static HashMap<String, HashMap<PortfolioField, String>>
    getPortfolioStockMapForWidget(Context context, String[] symbols) {

        HashMap<String, HashMap<PortfolioField, String>> portfolioStockMapForWidget =
                new HashMap<String, HashMap<PortfolioField, String>>();
        HashMap<String, HashMap<PortfolioField, String>> portfolioStockMap =
                getPortfolioStockMap(context);

        // Add stock details for any symbols that exist in the widget
        for (String symbol : Arrays.asList(symbols)) {
            HashMap<PortfolioField, String> stockInfoMap =
                    portfolioStockMap.get(symbol);
            if (stockInfoMap != null
                    && (stockInfoMap.get(PortfolioField.PRICE) != null || stockInfoMap
                    .get(PortfolioField.CUSTOM_DISPLAY) != null))
                portfolioStockMapForWidget.put(symbol, stockInfoMap);
        }
        return portfolioStockMapForWidget;
    }

    public static void cleanupPreferenceFiles(Context context) {

        // Remove old preferences if we are upgrading
        ArrayList<String> l = new ArrayList<String>();

        // Shared preferences is never deleted
        l.add(context.getString(R.string.prefs_name) + ".xml");
        for (int id : UserData.getAppWidgetIds2(context))
            l.add(context.getString(R.string.prefs_name) + id + ".xml");

        // Remove files we do not have an active widget for
        String appDir = context.getFilesDir().getParentFile().getPath();
        File f_shared_preferences = new File(appDir + "/shared_prefs");

        // Check if shared_preferences exists
        // TODO: Work out why this is ever null and an alternative strategy
        if (f_shared_preferences.exists())
            for (File f : f_shared_preferences.listFiles())
                if (!l.contains(f.getName()))
                    f.delete();
    }

    public enum PortfolioField {
        PRICE, DATE, QUANTITY, LIMIT_HIGH, LIMIT_LOW, CUSTOM_DISPLAY, SYMBOL_2
    }
}
