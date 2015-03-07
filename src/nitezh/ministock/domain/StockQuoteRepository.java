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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.Cache;
import nitezh.ministock.dataaccess.FxChangeRepository;
import nitezh.ministock.dataaccess.GoogleStockQuoteRepository;
import nitezh.ministock.dataaccess.YahooStockQuoteRepository;


public class StockQuoteRepository {

    private static final List<String> GOOGLE_SYMBOLS = Arrays.asList(".DJI", ".IXIC");

    private List<String> filterYahooSymbols(List<String> symbols) {
        List<String> filtered = new ArrayList<>();
        for (String symbol : symbols) {
            if (!GOOGLE_SYMBOLS.contains(symbol)) {
                filtered.add(symbol);
            }
        }
        return filtered;
    }

    private List<String> filterGoogleSymbols(List<String> symbols) {
        List<String> filtered = new ArrayList<>();
        for (String symbol : symbols) {
            if (GOOGLE_SYMBOLS.contains(symbol)) {
                filtered.add(symbol);
            }
        }
        return filtered;
    }


    public HashMap<String, StockQuote> getQuotes(Cache cache, List<String> symbols) {

        HashMap<String, StockQuote> allQuotes = new HashMap<>();

        // Get Yahoo data
        FxChangeRepository fxRepository = new FxChangeRepository();
        YahooStockQuoteRepository yahooRepository = new YahooStockQuoteRepository(fxRepository);
        HashMap<String, StockQuote> yahooQuotes = yahooRepository.getQuotes(cache, filterYahooSymbols(symbols));

        // Get Google data
        GoogleStockQuoteRepository googleRepository = new GoogleStockQuoteRepository();
        HashMap<String, StockQuote> googleQuotes = googleRepository.getQuotes(cache, filterGoogleSymbols(symbols));

        // Combine hashmaps
        if (yahooQuotes != null) {
            allQuotes.putAll(yahooQuotes);
        }
        if (googleQuotes != null) {
            allQuotes.putAll(googleQuotes);
        }

        return allQuotes;
    }
}
