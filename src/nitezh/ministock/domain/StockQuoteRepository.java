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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nitezh.ministock.StockSuggestions;
import nitezh.ministock.utils.Cache;
import nitezh.ministock.Storage;
import nitezh.ministock.dataaccess.FxChangeRepository;
import nitezh.ministock.dataaccess.GoogleStockQuoteRepository;
import nitezh.ministock.dataaccess.YahooStockQuoteRepository;


public class StockQuoteRepository {

    public static final List<String> GOOGLE_SYMBOLS = Arrays.asList(".DJI", ".IXIC");

    private static String mTimeStamp;
    private static HashMap<String, StockQuote> mCachedQuotes;
    private final YahooStockQuoteRepository yahooRepository;
    private final GoogleStockQuoteRepository googleRepository;

    private final Storage appStorage;
    private final Cache appCache;
    private final WidgetRepository widgetRepository;

    public StockQuoteRepository(Storage appStorage, Cache appCache, WidgetRepository widgetRepository) {
        this.yahooRepository = new YahooStockQuoteRepository(new FxChangeRepository());
        this.googleRepository = new GoogleStockQuoteRepository();
        this.appStorage = appStorage;
        this.appCache = appCache;
        this.widgetRepository = widgetRepository;
    }

    public HashMap<String, StockQuote> getLiveQuotes(List<String> symbols) {
        HashMap<String, StockQuote> allQuotes = new HashMap<>();

        symbols = this.convertRequestSymbols(symbols);
        List<String> yahooSymbols = new ArrayList<>(symbols);
        List<String> googleSymbols = new ArrayList<>(symbols);
        yahooSymbols.removeAll(GOOGLE_SYMBOLS);
        googleSymbols.retainAll(GOOGLE_SYMBOLS);

        HashMap<String, StockQuote> yahooQuotes = this.yahooRepository.getQuotes(this.appCache, yahooSymbols);
        HashMap<String, StockQuote> googleQuotes = this.googleRepository.getQuotes(this.appCache, googleSymbols);
        if (yahooQuotes != null) allQuotes.putAll(yahooQuotes);
        if (googleQuotes != null) allQuotes.putAll(googleQuotes);
        allQuotes = this.convertResponseQuotes(allQuotes);

        return allQuotes;
    }

    private HashMap<String, StockQuote> convertResponseQuotes(HashMap<String, StockQuote> quotes) {
        HashMap<String, StockQuote> newQuotes = new HashMap<>();
        for (String symbol : quotes.keySet()) {
            StockQuote quote = quotes.get(symbol);
            String newSymbol = quote.getSymbol()
                    .replace(".DJI", "^DJI")
                    .replace(".IXIC", "^IXIC");
            quote.setSymbol(newSymbol);
            if ("N/A".equalsIgnoreCase(quote.getName())) {
                String nameFromStockSuggestions = getNameFromStockSuggestions(newSymbol);
                if (!nameFromStockSuggestions.isEmpty()){
                    quote.setName(nameFromStockSuggestions);
                }
            }
            newQuotes.put(newSymbol, quote);
        }
        return newQuotes;
    }

    private String getNameFromStockSuggestions(String symbol) {
        String name = "";

        if (symbol != null && !symbol.isEmpty()) {
            List<Map<String, String>> suggestions = StockSuggestions.getSuggestions(symbol);
            for (Map<String, String> suggestion : suggestions) {
                if (symbol.equals(suggestion.get("symbol"))) {
                    name = suggestion.get("name");
                    break;
                }
            }
        }

        return name;
    }

    private List<String> convertRequestSymbols(List<String> symbols) {
        List<String> newSymbols = new ArrayList<>();
        for (String symbol : symbols) {
            newSymbols.add(symbol
                    .replace("^DJI", ".DJI")
                    .replace("^IXIC", ".IXIC"));
        }
        return newSymbols;
    }

    public HashMap<String, StockQuote> getQuotes(List<String> symbols, boolean noCache) {
        HashMap<String, StockQuote> quotes = new HashMap<>();

        if (noCache) {
            Set<String> widgetSymbols = this.widgetRepository.getWidgetsStockSymbols();
            widgetSymbols.add("^DJI");
            widgetSymbols.addAll(new PortfolioStockRepository(
                    this.appStorage, this.appCache, this.widgetRepository).getStocks().keySet());
            quotes = getLiveQuotes(new ArrayList<>(widgetSymbols));
        }

        if (quotes.isEmpty()) {
            quotes = loadQuotes();
        } else {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM HH:mm");
            String timeStamp = format.format(new Date()).toUpperCase();
            saveQuotes(quotes, timeStamp);
        }

        // Returns only quotes requested
        @SuppressWarnings("unchecked") HashMap<String, StockQuote> filteredQuotes =
                (HashMap<String, StockQuote>) quotes.clone();
        filteredQuotes.keySet().retainAll(symbols);
        return filteredQuotes;
    }

    private HashMap<String, StockQuote> loadQuotes() {
        if (mCachedQuotes != null) {
            return mCachedQuotes;
        }

        HashMap<String, StockQuote> quotes = new HashMap<>();
        String savedQuotes = this.appStorage.getString("savedQuotes", "");
        String timeStamp = this.appStorage.getString("savedQuotesTime", "");
        if (!savedQuotes.equals("")) {
            try {
                for (String line : savedQuotes.split("\n")) {
                    String[] values = line.split(";");
                    quotes.put(values[0], new StockQuote(
                            values[0],
                            values[1],
                            values[2],
                            values[3],
                            values[4],
                            values[5],
                            values[6]));
                }
            } catch (Exception e) {
                quotes = new HashMap<>();
            }
        }
        mCachedQuotes = quotes;
        mTimeStamp = timeStamp;

        return quotes;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    private void saveQuotes(HashMap<String, StockQuote> quotes, String timeStamp) {
        mCachedQuotes = quotes;
        mTimeStamp = timeStamp;

        StringBuilder savedQuotes = new StringBuilder();
        for (String symbol : quotes.keySet()) {
            StockQuote quote = quotes.get(symbol);
            savedQuotes.append(String.format("%s;%s;%s;%s;%s;%s;%s\n",
                    quote.getSymbol() != null ? quote.getSymbol() : "",
                    quote.getPrice() != null ? quote.getPrice() : "",
                    quote.getChange() != null ? quote.getChange() : "",
                    quote.getPercent() != null ? quote.getPercent() : "",
                    quote.getExchange() != null ? quote.getExchange() : "",
                    quote.getVolume() != null ? quote.getVolume() : "",
                    quote.getName() != null ? quote.getName() : ""));
        }
        this.appStorage.putString("savedQuotes", savedQuotes.toString().trim());
        this.appStorage.putString("savedQuotesTime", timeStamp);
        this.appStorage.apply();
    }
}
