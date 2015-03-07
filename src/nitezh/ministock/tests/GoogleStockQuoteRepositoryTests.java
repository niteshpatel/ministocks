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

import nitezh.ministock.dataaccess.GoogleStockQuoteRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.tests.mocks.MockCache;


public class GoogleStockQuoteRepositoryTests extends TestCase {

    private GoogleStockQuoteRepository googleRepository;

    public void setUp() {
        this.googleRepository = new GoogleStockQuoteRepository();
    }

    public void testRetrieveQuotesAsJson() {
        List<String> symbols = Arrays.asList(".DJI", ".IXIC");
        JSONArray json = null;
        try {
            json = this.googleRepository.retrieveQuotesAsJson(new MockCache(), symbols);
        } catch (JSONException ignored) {
        }

        assertNotNull(json);
        assertEquals(2, json.length());
        JSONObject djiJson = json.optJSONObject(0);
        assertEquals(".DJI", djiJson.optString("t"));
        assertEquals("INDEXDJX", djiJson.optString("e"));
        JSONObject ixicJson = json.optJSONObject(1);
        assertEquals(".IXIC", ixicJson.optString("t"));
        assertEquals("INDEXNASDAQ", ixicJson.optString("e"));
    }

    public void testGetQuotes() {
        List<String> symbols = Arrays.asList(".DJI", ".IXIC");
        HashMap<String, StockQuote> stockQuotes = this.googleRepository.getQuotes(new MockCache(), symbols);

        assertEquals(2, stockQuotes.size());
        StockQuote djiQuote = stockQuotes.get(".DJI");
        assertEquals(".DJI", djiQuote.getSymbol());
        assertEquals("DJX", djiQuote.getExchange());
        StockQuote ixicQuote = stockQuotes.get(".IXIC");
        assertEquals(".IXIC", ixicQuote.getSymbol());
        assertEquals("NASDAQ", ixicQuote.getExchange());
        assertEquals(stockQuotes, stockQuotes);
    }
}