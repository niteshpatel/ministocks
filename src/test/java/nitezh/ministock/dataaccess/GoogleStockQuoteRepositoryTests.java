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
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.mocks.MockCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class GoogleStockQuoteRepositoryTests {

    private GoogleStockQuoteRepository googleRepository;

    @Before
    public void setUp() {
        this.googleRepository = new GoogleStockQuoteRepository();
    }

    @Test
    public void retrieveDJIQuoteAsJson() {
        // Arrange
        List<String> symbols = Collections.singletonList(".DJI");
        JSONArray json = null;

        // Act
        try {
            json = googleRepository.retrieveQuotesAsJson(new MockCache(), symbols);
        } catch (JSONException ignored) {
        }

        // Assert
        assertNotNull(json);
        assertEquals(1, json.length());

        JSONObject djiJson = json.optJSONObject(0);
        assertEquals(".DJI", djiJson.optString("t"));
        assertEquals("INDEXDJX", djiJson.optString("e"));
    }

    @Test
    public void retrieveIXICQuoteAsJson() {
        // Arrange
        List<String> symbols = Collections.singletonList(".IXIC");
        JSONArray json = null;

        // Act
        try {
            json = googleRepository.retrieveQuotesAsJson(new MockCache(), symbols);
        } catch (JSONException ignored) {
        }

        // Assert
        assertNotNull(json);
        assertEquals(1, json.length());

        JSONObject ixicJson = json.optJSONObject(0);
        assertEquals(".IXIC", ixicJson.optString("t"));
        assertEquals("INDEXNASDAQ", ixicJson.optString("e"));
    }

    @Test
    public void getQuotes() {
        // Arrange
        List<String> symbols = Arrays.asList(".DJI", ".IXIC");

        // Act
        HashMap<String, StockQuote> stockQuotes = googleRepository.getQuotes(new MockCache(), symbols);

        // Assert
        assertEquals(2, stockQuotes.size());

        StockQuote djiQuote = stockQuotes.get(".DJI");
        assertEquals(".DJI", djiQuote.getSymbol());
        assertEquals("DJX", djiQuote.getExchange());

        StockQuote ixicQuote = stockQuotes.get(".IXIC");
        assertEquals(".IXIC", ixicQuote.getSymbol());
        assertEquals("NASDAQ", ixicQuote.getExchange());
    }
}