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

package nitezh.ministock.tests;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.dataaccess.FxChangeRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.dataaccess.YahooStockQuoteRepository;
import nitezh.ministock.tests.mocks.MockCache;


public class YahooStockQuoteRepositoryTests extends TestCase {

    private YahooStockQuoteRepository yahooRepository;

    public void setUp() {
        FxChangeRepository fxRepository = new FxChangeRepository();
        this.yahooRepository = new YahooStockQuoteRepository(fxRepository);
    }

    public void testRetrieveQuotesAsJson() {
        List<String> symbols = Arrays.asList("AAPL", "GOOG");
        JSONArray json = null;
        try {
            json = this.yahooRepository.retrieveQuotesAsJson(new MockCache(), symbols);
        } catch (JSONException ignored) {
        }

        assertNotNull(json);
        assertEquals(2, json.length());
        JSONObject aaplJson = json.optJSONObject(0);
        assertEquals("AAPL", aaplJson.optString("symbol"));
        assertEquals("NasdaqNM", aaplJson.optString("exchange"));
        assertEquals("Apple Inc.", aaplJson.optString("name"));
        JSONObject googJson = json.optJSONObject(1);
        assertEquals("GOOG", googJson.optString("symbol"));
        assertEquals("NasdaqNM", googJson.optString("exchange"));
        assertEquals("Google Inc.", googJson.optString("name"));
    }

    public void testGetQuotes() {
        List<String> symbols = Arrays.asList("AAPL", "GOOG");
        HashMap<String, StockQuote> stockQuotes = this.yahooRepository.getQuotes(new MockCache(), symbols);

        assertEquals(2, stockQuotes.size());
        StockQuote aaplQuote = stockQuotes.get("AAPL");
        assertEquals("AAPL", aaplQuote.getSymbol());
        assertEquals("NasdaqNM", aaplQuote.getExchange());
        assertEquals("Apple Inc.", aaplQuote.getName());
        StockQuote googQuote = stockQuotes.get("GOOG");
        assertEquals("GOOG", googQuote.getSymbol());
        assertEquals("NasdaqNM", googQuote.getExchange());
        assertEquals("Google Inc.", googQuote.getName());
        assertEquals(stockQuotes, stockQuotes);
    }
}