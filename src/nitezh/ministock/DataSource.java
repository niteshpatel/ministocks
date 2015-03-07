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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;


public class DataSource {

    private static String mTimeStamp;
    private static HashMap<String, StockQuote> mCachedQuotes;

    public HashMap<String, StockQuote> getStockData(Context context, String[] symbols, boolean noCache) {
        HashMap<String, StockQuote> allQuotes = new HashMap<>();

        // If fresh data is request, retrieve from the stock data provider
        if (noCache) {
            // Retrieve all widget symbols and additionally add ^DJI
            Set<String> widgetSymbols = UserData.getWidgetsStockSymbols(context);
            widgetSymbols.add("^DJI");
            widgetSymbols.addAll(UserData.getPortfolioStockMap(context).keySet());

            // Retrieve the data from the stock data provider
            allQuotes = new StockQuoteRepository().getQuotes(new PreferenceCache(context), Arrays.asList(widgetSymbols.toArray(new String[widgetSymbols.size()])));
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
        HashMap<String, StockQuote> quotes = new HashMap<>();
        for (String s : symbols) {
            if (s != null && !s.equals("")) {
                quotes.put(s, allQuotes.get(s));
            }
        }

        return quotes;
    }

    private HashMap<String, StockQuote> loadQuotes(Context context) {
        // If we have cached data on the class use that for efficiency
        if ((mCachedQuotes) != null)
            return mCachedQuotes;

        // Create empty HashMap to store the results
        HashMap<String, StockQuote> quotes = new HashMap<>();

        // Load the saved quotes
        SharedPreferences preferences = PreferenceTools.getAppPreferences(context);
        String savedQuotes = preferences.getString("savedQuotes", "");
        String timeStamp = preferences.getString("savedQuotesTime", "");

        // Parse the string and convert to a hash map
        if (!savedQuotes.equals("")) {
            try {
                for (String line : savedQuotes.split("\n")) {
                    String[] values = line.split(";");
                    quotes.put(values[0], new StockQuote(values[0], values[1], values[2], values[3], values[4], values[5], values[6]));
                }
            } catch (Exception e) {
                // Don't do anything if the stored data is dodgy
                quotes = new HashMap<>();
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

    private void saveQuotes(Context context, HashMap<String, StockQuote> quotes, String timeStamp) {
        // Convert the quotes into a string and save in share preferences
        StringBuilder savedQuotes = new StringBuilder();
        for (String symbol : quotes.keySet()) {
            // Ensures we do not add a line break to the last line
            if (!savedQuotes.toString().equals("")) {
                savedQuotes.append("\n");
            }
            savedQuotes.append(quotes.get(symbol).serialize());
        }

        // Update the class cache and quotes timestamp
        mCachedQuotes = quotes;
        mTimeStamp = timeStamp;

        // Save preferences
        Editor editor = PreferenceTools.getAppPreferences(context).edit();
        editor.putString("savedQuotes", savedQuotes.toString());
        editor.putString("savedQuotesTime", timeStamp);
        editor.apply();
    }

    public enum StockField {
        PRICE, CHANGE, PERCENT, EXCHANGE, VOLUME, NAME
    }
}
