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

package nitezh.ministock.dataaccess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.utils.Cache;
import nitezh.ministock.utils.UrlDataTools;


public class GoogleStockQuoteRepository {

    private static final String BASE_URL = "http://finance.google.com/finance?output=json&q=";

    public HashMap<String, StockQuote> getQuotes(Cache cache, List<String> symbols) {
        HashMap<String, StockQuote> quotes = new HashMap<>();
        JSONArray jsonArray;
        JSONObject quoteJson;

        for (String symbol : symbols) {
            try {
                jsonArray = this.retrieveQuotesAsJson(cache, Collections.singletonList(symbol));

                for (int i = 0; i < jsonArray.length(); i++) {
                    quoteJson = jsonArray.getJSONObject(i);
                    StockQuote quote = new StockQuote(
                            quoteJson.optString("t"),
                            quoteJson.optString("l_cur", quoteJson.optString("l")).replace(",", ""),
                            quoteJson.optString("c"),
                            quoteJson.optString("cp"),
                            quoteJson.optString("e").replace("INDEX", ""),
                            "0",
                            quoteJson.optString("e"),
                            Locale.US);
                    quotes.put(quote.getSymbol(), quote);
                }
            } catch (JSONException ignored) {
            }
        }

        return quotes;
    }

    private String buildRequestUrl(List<String> symbols) {
        StringBuilder sQuery = new StringBuilder();
        for (String s : symbols) {
            if (!s.equals("")) {
                if (!sQuery.toString().equals("")) {
                    sQuery.append(",");
                }
                sQuery.append(s);
            }
        }
        return String.format("%s%s", BASE_URL, sQuery);
    }

    JSONArray retrieveQuotesAsJson(Cache cache, List<String> symbols) throws JSONException {
        String url = this.buildRequestUrl(symbols);
        String data = UrlDataTools.getCachedUrlData(url, cache, 300);
        String json = data.replace("//", "").replaceAll("\\\\", "");

        return new JSONArray(json);
    }
}
