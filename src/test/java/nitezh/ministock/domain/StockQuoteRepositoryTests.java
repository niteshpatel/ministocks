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

package nitezh.ministock.domain;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;
import nitezh.ministock.tests.mocks.MockCache;
import nitezh.ministock.tests.mocks.MockStorage;
import nitezh.ministock.tests.mocks.MockWidgetRepository;


public class StockQuoteRepositoryTests extends TestCase {

    private StockQuoteRepository stockRepository;

    public void setUp() {
        MockWidgetRepository mockWidgetRepository = new MockWidgetRepository();
        mockWidgetRepository.setWidgetsStockSymbols(new HashSet<>(Arrays.asList(
                "AAPL",
                "GOOG",
                "^DJI",
                "^IXIC")));
        this.stockRepository = new StockQuoteRepository(
                new MockStorage(), new MockCache(), mockWidgetRepository);
    }

    public void tearDown() {
        PortfolioStockRepository.setmDirtyPortfolioStockMap(true);
    }

    public void testGetLiveQuotes() {
        // Arrange
        List<String> symbols = Arrays.asList("AAPL", "GOOG", "^DJI", "^IXIC");

        // Act
        HashMap<String, StockQuote> quotes = this.stockRepository.getLiveQuotes(symbols);

        // Assert
        assertEquals(4, quotes.size());

        StockQuote aaplQuote = quotes.get("AAPL");
        assertEquals("AAPL", aaplQuote.getSymbol());
        assertTrue(Arrays.asList("NasdaqNM", "NMS").contains(aaplQuote.getExchange()));
        assertEquals("Apple Inc.", aaplQuote.getName());

        StockQuote googQuote = quotes.get("GOOG");
        assertEquals("GOOG", googQuote.getSymbol());
        assertTrue(Arrays.asList("NasdaqNM", "NMS").contains(googQuote.getExchange()));
        assertEquals("Alphabet Inc.", googQuote.getName());

        StockQuote djiQuote = quotes.get("^DJI");
        assertEquals("^DJI", djiQuote.getSymbol());
        assertEquals("DJX", djiQuote.getExchange());

        StockQuote ixicQuote = quotes.get("^IXIC");
        assertEquals("^IXIC", ixicQuote.getSymbol());
        assertEquals("NASDAQ", ixicQuote.getExchange());
    }

    public void testGetQuotes() {
        // Arrange
        List<String> symbols = Arrays.asList("AAPL", "GOOG", "^DJI", "^IXIC");

        // Act
        HashMap<String, StockQuote> quotes = this.stockRepository.getQuotes(symbols, true);

        // Assert
        assertEquals(4, quotes.size());

        StockQuote aaplQuote = quotes.get("AAPL");
        assertEquals("AAPL", aaplQuote.getSymbol());
        assertTrue(Arrays.asList("NasdaqNM", "NMS").contains(aaplQuote.getExchange()));
        assertEquals("Apple Inc.", aaplQuote.getName());

        StockQuote googQuote = quotes.get("GOOG");
        assertEquals("GOOG", googQuote.getSymbol());
        assertTrue(Arrays.asList("NasdaqNM", "NMS").contains(googQuote.getExchange()));
        assertEquals("Alphabet Inc.", googQuote.getName());

        StockQuote djiQuote = quotes.get("^DJI");
        assertEquals("^DJI", djiQuote.getSymbol());
        assertEquals("DJX", djiQuote.getExchange());

        StockQuote ixicQuote = quotes.get("^IXIC");
        assertEquals("^IXIC", ixicQuote.getSymbol());
        assertEquals("NASDAQ", ixicQuote.getExchange());
    }
}