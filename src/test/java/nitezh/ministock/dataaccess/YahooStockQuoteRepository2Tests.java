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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.mocks.MockCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class YahooStockQuoteRepository2Tests {

    private YahooStockQuoteRepository2 quoteRepository;

    @Before
    public void setUp() {
        FxChangeRepository fxRepository = new FxChangeRepository();
        quoteRepository = new YahooStockQuoteRepository2(fxRepository);
    }

    @Test
    public void retrieveQuotesAsJson() {
        // Skipif
        Assume.assumeTrue(System.getenv("CIRCLECI") == null);

        // Arrange
        List<String> symbols = Arrays.asList("AAPL", "GOOG");
        JSONArray json = null;

        // Act
        try {
            json = this.quoteRepository.retrieveQuotesAsJson(new MockCache(), symbols);
        } catch (JSONException ignored) {
        }

        // Assert
        assertNotNull(json);
        assertEquals(2, json.length());

        JSONObject aaplJson = json.optJSONObject(0);
        assertEquals("AAPL", aaplJson.optString("symbol"));
        assertTrue(Arrays.asList(
                "NasdaqNM",
                "NMS",
                "Nasdaq Global Select",
                "NasdaqGS"
        ).contains(aaplJson.optString("exchange")));

        assertEquals("Apple Inc.", aaplJson.optString("name"));

        JSONObject googJson = json.optJSONObject(1);
        assertEquals("GOOG", googJson.optString("symbol"));
        assertTrue(Arrays.asList(
                "NasdaqNM",
                "NMS",
                "Nasdaq Global Select",
                "NasdaqGS"
        ).contains(googJson.optString("exchange")));
        assertEquals("Alphabet Inc.", googJson.optString("name"));
    }

    @Test
    public void getQuotes() {
        // Skipif
        Assume.assumeTrue(System.getenv("CIRCLECI") == null);

        // Arrange
        List<String> symbols = Arrays.asList("AAPL", "GOOG");

        // Act
        HashMap<String, StockQuote> stockQuotes = quoteRepository.getQuotes(new MockCache(), symbols);

        // Assert
        assertEquals(2, stockQuotes.size());

        StockQuote aaplQuote = stockQuotes.get("AAPL");
        assertEquals("AAPL", aaplQuote.getSymbol());
        assertTrue(Arrays.asList(
                "NasdaqNM",
                "NMS",
                "Nasdaq Global Select",
                "NasdaqGS"
        ).contains(aaplQuote.getExchange()));
        assertEquals("Apple Inc.", aaplQuote.getName());

        StockQuote googQuote = stockQuotes.get("GOOG");
        assertEquals("GOOG", googQuote.getSymbol());
        assertTrue(Arrays.asList(
                "NasdaqNM",
                "NMS",
                "Nasdaq Global Select",
                "NasdaqGS"
        ).contains(googQuote.getExchange()));
        assertEquals("Alphabet Inc.", googQuote.getName());
    }
}
