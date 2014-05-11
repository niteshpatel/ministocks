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
import nitezh.ministock.DataSource.StockField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

class StockQuotes {

    private static final String BASE_URL = "http://download.finance.yahoo.com/d/quotes.csv";
    private static final String FX_URL = "http://ministocks-app.appspot.com/getcurrencydata";
    private static final String GOOGLE_URL = "http://finance.google.com/finance/info?client=ig&q=.DJI,.IXIC";
    private static final String FORMAT = "sd1t1l1c1p2xvn";
    private static final int COUNT_FIELDS = 9;

    public static HashMap<String, HashMap<StockField, String>> getQuotes(
            Context context,
            String[] symbols) {

        // Check if we have FX symbols
        // Check if we have DJI/IXIC (needs to come from Google)
        boolean hasFX = false;
        boolean hasGoogle = false;
        for (String s : symbols) {
            if (s.contains("="))
                hasFX = true;
            if (s.contains("^DJI") || s.contains("^IXIC"))
                hasGoogle = true;
        }

        // Get FX data if we need to
        JSONObject fxData = new JSONObject();
        if (hasFX) {
            try {
                fxData = new JSONObject(URLData.getURLData(context, FX_URL, 86400));
            } catch (JSONException ignored) {
            }
        }

        // Get Google data if we need to
        JSONArray gData = new JSONArray();
        if (hasGoogle) {
            try {
                gData = new JSONArray(URLData.getURLData(context, GOOGLE_URL, 300).replace("//", ""));
            } catch (JSONException ignored) {
            }
        }

        // Create empty HashMap to store the results
        HashMap<String, HashMap<StockField, String>> quotes = new HashMap<String, HashMap<StockField, String>>();

        // Convert the list of stock symbols into a url-encoded parameter
        StringBuilder sQuery = new StringBuilder();
        for (String s : symbols) {
            if (s.equals(""))
                continue;
            // If the query string is not empty add a '+'
            if (!sQuery.toString().equals(""))
                sQuery.append("+");
            sQuery.append(s);
        }

        // Build the data source url and get the data
        String url = BASE_URL + "?f=" + FORMAT + "&s=" + sQuery.toString();
        String response = URLData.getURLData(context, url, 300);

        // If the symbols list was invalid return empty
        if (response.equals("Missing Symbols List.") || response.equals("")) {
            return quotes;
        }

        // Prepare the map for population
        for (String s : symbols) {
            quotes.put(s, new HashMap<StockField, String>());
        }

        // Parse the response and return
        for (String line : response.split("\n")) {

            // Replace quotes and split values into an array
            String[] values = line.replace("\"", "").split(",", COUNT_FIELDS);

            // If we do not have at least 6 elements continue
            if (values.length < COUNT_FIELDS) {
                continue;
            }

            // Use ^DJI data from Google
            if (gData.length() > 0 && values[0].equals("INDU")) {

                JSONObject dji;
                try {
                    dji = gData.getJSONObject(0);
                    values[0] = "^DJI";
                    values[3] = ((String) dji.get("l_cur")).replace(",", "");
                    values[4] = (String) dji.get("c");
                    values[5] = (String) dji.get("cp");
                    values[6] = "DJI";
                    values[7] = "0";
                    values[8] = "Dow Jones Industrial Average";

                } catch (JSONException ignored) {
                }
            }

            // Use ^IXIC data from Google
            if (gData.length() > 0 && values[0].equals("^IXIC")) {

                JSONObject jsonObject;
                try {
                    jsonObject = gData.getJSONObject(1);
                    values[3] = ((String) jsonObject.get("l_cur")).replace(",", "");
                    values[4] = (String) jsonObject.get("c");
                    values[5] = (String) jsonObject.get("cp");
                    values[6] = "IXIC";
                    values[7] = "0";
                    values[8] = "NASDAQ Composite";

                } catch (JSONException ignored) {
                }
            }

            // Skip items that we don't stock for
            if (!quotes.containsKey(values[0])) {
                continue;
            }

            // Are we FX item
            Boolean isFX = values[0].contains("=");

            // Get additional FX data if applicable
            Double yesterdayPrice = null;
            if (isFX) {
                try {
                    JSONObject item = fxData.getJSONObject(values[0]);
                    yesterdayPrice = Double.parseDouble(item.getString("price"));

                } catch (Exception ignored) {
                }
            }

            // Set stock prices to 2 decimal places
            Double price = null;
            if (!values[3].equals("0.00")) {
                try {
                    price = Double.parseDouble(values[3]);
                    if (isFX)
                        quotes.get(values[0]).put(StockField.PRICE, Tools.getTrimmedDouble2(price, 6));
                    else
                        quotes.get(values[0]).put(StockField.PRICE, Tools.getTrimmedDouble(price, 6, 4));

                } catch (Exception e) {
                    try {
                        quotes.get(values[0]).put(StockField.PRICE, "0.00");

                    } catch (Exception e1) {
                        continue;
                    }
                }

                // Note that if the change or percent == "N/A" set to 0
                if ((values[4].equals("N/A") || values[4].equals("")) && yesterdayPrice == null) {
                    values[4] = "0.00";
                }
                if ((values[5].equals("N/A") || values[5].equals("")) && yesterdayPrice == null) {
                    values[5] = "0.00";
                }
            }

            // Changes are only set to 5 significant figures
            Double change = null;
            if (!values[4].equals("N/A") && !values[4].equals("")) {
                change = Double.parseDouble(values[4]);

            } else if (yesterdayPrice != null && price != null) {
                change = price - yesterdayPrice;
            }
            if (change != null) {
                if (price != null && (price < 10 || isFX))
                    quotes.get(values[0]).put(StockField.CHANGE, Tools.getTrimmedDouble(change, 5, 3));
                else
                    quotes.get(values[0]).put(StockField.CHANGE, Tools.getTrimmedDouble(change, 5));
            }

            // Percentage changes are only set to one decimal place
            Double pc = null;
            if (!values[5].equals("N/A") && !values[5].equals("")) {
                pc = Double.parseDouble(values[5].replace("%", ""));

            } else {
                if (change != null && price != null) {
                    pc = (change / price) * 100;
                }
            }
            if (pc != null) {
                quotes.get(values[0]).put(StockField.PERCENT, String.format("%.1f", pc) + "%");
            }

            // Add name and volume
            quotes.get(values[0]).put(StockField.EXCHANGE, values[6]);
            quotes.get(values[0]).put(StockField.VOLUME, values[7]);
            quotes.get(values[0]).put(StockField.NAME, values[8]);
        }

        // Return quotes
        return quotes;
    }
}
