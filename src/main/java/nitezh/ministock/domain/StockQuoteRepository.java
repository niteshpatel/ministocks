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

import android.annotation.SuppressLint;

import com.google.common.collect.ImmutableBiMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nitezh.ministock.Storage;
import nitezh.ministock.dataaccess.FxChangeRepository;
import nitezh.ministock.dataaccess.GoogleStockQuoteRepository;
import nitezh.ministock.dataaccess.YahooStockQuoteRepository2;
import nitezh.ministock.utils.Cache;


public class StockQuoteRepository {

    private static final ImmutableBiMap<String, String> GOOGLE_SYMBOLS = new ImmutableBiMap.Builder<String, String>()
            .put(".DJI", "^DJI")
            .put(".IXIC", "^IXIC")
            .put("DWCPF", "^DWCPF")
            .build();

    private static String mTimeStamp;
    private static HashMap<String, StockQuote> mCachedQuotes;
    private final YahooStockQuoteRepository2 iexRepository;
    private final GoogleStockQuoteRepository googleRepository;

    private final Storage appStorage;
    private final Cache appCache;
    private final WidgetRepository widgetRepository;

    public StockQuoteRepository(Storage appStorage, Cache appCache, WidgetRepository widgetRepository) {
        this.iexRepository = new YahooStockQuoteRepository2(new FxChangeRepository());
        this.googleRepository = new GoogleStockQuoteRepository();
        this.appStorage = appStorage;
        this.appCache = appCache;
        this.widgetRepository = widgetRepository;
    }

    HashMap<String, StockQuote> getLiveQuotes(List<String> symbols) {
        HashMap<String, StockQuote> allQuotes = new HashMap<>();

        symbols = this.convertRequestSymbols(symbols);
        List<String> yahooSymbols = new ArrayList<>(symbols);
        List<String> googleSymbols = new ArrayList<>(symbols);
        yahooSymbols.removeAll(GOOGLE_SYMBOLS.keySet());
        googleSymbols.retainAll(GOOGLE_SYMBOLS.keySet());

        HashMap<String, StockQuote> yahooQuotes = this.iexRepository.getQuotes(this.appCache, yahooSymbols);
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

            String newSymbol = quote.getSymbol();
            for (String symbolToReplace : GOOGLE_SYMBOLS.keySet()) {
                newSymbol = newSymbol.replace(symbolToReplace, GOOGLE_SYMBOLS.get(symbolToReplace));
            }

            quote.setSymbol(newSymbol);
            newQuotes.put(newSymbol, quote);
        }
        return newQuotes;
    }

    private List<String> convertRequestSymbols(List<String> symbols) {
        List<String> newSymbols = new ArrayList<>();
        for (String symbol : symbols) {
            String newSymbol = symbol;
            for (String symbolToReplace : GOOGLE_SYMBOLS.inverse().keySet()) {
                newSymbol = newSymbol.replace(symbolToReplace, GOOGLE_SYMBOLS.inverse().get(symbolToReplace));
            }
            newSymbols.add(newSymbol);
        }
        return newSymbols;
    }

    public HashMap<String, StockQuote> getQuotes(List<String> symbols, boolean noCache) {
        HashMap<String, StockQuote> quotes = new HashMap<>();

        if (noCache) {
            Set<String> widgetSymbols = this.widgetRepository.getWidgetsStockSymbols();
            widgetSymbols.add("^DJI");
            widgetSymbols.addAll(new PortfolioStockRepository(
                    this.appStorage, this.widgetRepository).getStocks().keySet());
            quotes = getLiveQuotes(new ArrayList<>(widgetSymbols));
        }

        if (quotes.isEmpty()) {
            quotes = loadQuotes();
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd MMM HH:mm");
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
        if (mCachedQuotes != null && !mCachedQuotes.isEmpty()) {
            return mCachedQuotes;
        }

        HashMap<String, StockQuote> quotes = new HashMap<>();
        String savedQuotesString = this.appStorage.getString("savedQuotes", "");
        String timeStamp = this.appStorage.getString("savedQuotesTime", "");

        if (!savedQuotesString.equals("")) {
            JSONObject savedQuotes = new JSONObject();
            try {
                savedQuotes = new JSONObject(savedQuotesString);
            } catch (JSONException ignored) {
            }

            try {
                String key;
                JSONObject details;

                for (Iterator iter = savedQuotes.keys(); iter.hasNext(); ) {
                    key = (String) iter.next();
                    details = savedQuotes.getJSONObject(key);

                    quotes.put(key, new StockQuote(
                            details.getString("symbol"),
                            details.getString("price"),
                            details.getString("change"),
                            details.getString("percent"),
                            details.getString("exchange"),
                            details.getString("volume"),
                            details.getString("name"),
                            Locale.getDefault()
                    ));
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

        JSONObject savedQuotes = new JSONObject();
        for (String symbol : quotes.keySet()) {
            try {
                savedQuotes.put(symbol, new JSONObject());

            } catch (JSONException ignored) {
            }

            StockQuote quote = quotes.get(symbol);
            try {
                savedQuotes.getJSONObject(symbol).put("symbol", quote.getSymbol());
                savedQuotes.getJSONObject(symbol).put("price", quote.getPrice());
                savedQuotes.getJSONObject(symbol).put("change", quote.getChange());
                savedQuotes.getJSONObject(symbol).put("percent", quote.getPercent());
                savedQuotes.getJSONObject(symbol).put("exchange", quote.getExchange());
                savedQuotes.getJSONObject(symbol).put("volume", quote.getVolume());
                savedQuotes.getJSONObject(symbol).put("name", quote.getName());

            } catch (JSONException ignored) {
            }
        }

        this.appStorage.putString("savedQuotes", savedQuotes.toString());
        this.appStorage.putString("savedQuotesTime", timeStamp);
        this.appStorage.apply();
    }
}
