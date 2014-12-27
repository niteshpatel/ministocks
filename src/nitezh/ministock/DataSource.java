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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class DataSource {
    private static String mTimeStamp;
    private static HashMap<String, HashMap<StockField, String>> mCachedQuotes;

    public HashMap<String, HashMap<StockField, String>> getStockData(Context context, String[] symbols, boolean noCache) {
        HashMap<String, HashMap<StockField, String>> allQuotes = new HashMap<String, HashMap<StockField, String>>();

        // If fresh data is request, retrieve from the stock data provider
        if (noCache) {
            // Retrieve all widget symbols and additionally add ^DJI
            Set<String> widgetSymbols = UserData.getWidgetsStockSymbols(context);
            widgetSymbols.add("^DJI");
            widgetSymbols.addAll(UserData.getPortfolioStockMap(context).keySet());

            // Retrieve the data from the stock data provider
            allQuotes = StockQuotes.getQuotes(context, widgetSymbols.toArray(new String[widgetSymbols.size()]));
        }
        // If there is no information used the last retrieved info
        if (allQuotes.isEmpty()) {
            allQuotes = loadQuotes(context);
        }
        // Otherwise save the info
        else {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM HH:mm");
            String timeStamp = format.format(new Date()).toUpperCase();
            saveQuotes(context, allQuotes, timeStamp);
        }
        // Filter out quotes that are not for this widget and return
        HashMap<String, HashMap<StockField, String>> quotes = new HashMap<String, HashMap<StockField, String>>();
        for (String s : symbols)
            if (s != null && !s.equals(""))
                quotes.put(s, allQuotes.get(s));
        return quotes;
    }

    private HashMap<String, HashMap<StockField, String>> loadQuotes(Context context) {
        // If we have cached data on the class use that for efficiency
        if ((mCachedQuotes) != null)
            return mCachedQuotes;

        // Create empty HashMap to store the results
        HashMap<String, HashMap<StockField, String>> quotes = new HashMap<String, HashMap<StockField, String>>();

        // Load the saved quotes
        SharedPreferences preferences = Tools.getAppPreferences(context);
        String savedQuotes = preferences.getString("savedQuotes", "");
        String timeStamp = preferences.getString("savedQuotesTime", "");

        // Parse the string and convert to a hash map
        if (!savedQuotes.equals("")) {
            try {
                for (String line : savedQuotes.split("\n")) {
                    String[] values = line.split(";");
                    quotes.put(values[0], new HashMap<StockField, String>());
                    for (StockField f : StockField.values())
                        if (!values[f.ordinal() + 1].equals(""))
                            quotes.get(values[0]).put(f, values[f.ordinal() + 1]);
                }
            } catch (Exception e) {
                // Don't do anything if the stored data is dodgy
                quotes = new HashMap<String, HashMap<StockField, String>>();
            }
        }
        // Update the class cache and quotes timestamp
        mCachedQuotes = quotes;
        mTimeStamp = timeStamp;
        return quotes;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    private void saveQuotes(Context context, HashMap<String, HashMap<StockField, String>> quotes, String timeStamp) {
        // Convert the quotes into a string and save in share preferences
        StringBuilder savedQuotes = new StringBuilder();
        for (String symbol : quotes.keySet()) {
            // Ensures we do not add a line break to the last line
            if (!savedQuotes.toString().equals("")) {
                savedQuotes.append("\n");
            }
            savedQuotes.append(symbol);
            for (StockField f : StockField.values()) {
                savedQuotes.append(";");
                savedQuotes.append(quotes.get(symbol).get(f));
            }
        }
        // Update the class cache and quotes timestamp
        mCachedQuotes = quotes;
        mTimeStamp = timeStamp;

        // Save preferences
        Editor editor = Tools.getAppPreferences(context).edit();
        editor.putString("savedQuotes", savedQuotes.toString());
        editor.putString("savedQuotesTime", timeStamp);
        editor.commit();
    }

    public enum StockField {
        PRICE, CHANGE, PERCENT, EXCHANGE, VOLUME, NAME
    }
}
