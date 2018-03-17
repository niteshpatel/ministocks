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

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nitezh.ministock.DialogTools;
import nitezh.ministock.Storage;
import nitezh.ministock.UserData;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.StorageCache;

public class PortfolioStockRepository {
    public static final String PORTFOLIO_JSON = "portfolioJson";
    public static final String WIDGET_JSON = "widgetJson";
    private static final HashMap<String, PortfolioStock> mPortfolioStocks = new HashMap<>();
    private static boolean mDirtyPortfolioStockMap = true;
    private final WidgetRepository widgetRepository;
    private final Storage mAppStorage;
    public HashMap<String, StockQuote> stocksQuotes = new HashMap<>();
    HashMap<String, PortfolioStock> portfolioStocksInfo = new HashMap<>();
    private Set<String> widgetsStockSymbols = new HashSet<>();

    public PortfolioStockRepository(Storage appStorage, WidgetRepository widgetRepository) {
        this.mAppStorage = appStorage;
        this.widgetRepository = widgetRepository;

        this.widgetsStockSymbols = widgetRepository.getWidgetsStockSymbols();
        this.portfolioStocksInfo = getPortfolioStocksInfo(widgetsStockSymbols);
    }

    private static boolean isDirtyPortfolioStockMap() {
        return mDirtyPortfolioStockMap;
    }

    static void setDirtyPortfolioStockMap(boolean mDirtyPortfolioStockMap) {
        PortfolioStockRepository.mDirtyPortfolioStockMap = mDirtyPortfolioStockMap;
    }

    public void updateStocksQuotes() {
        if (this.stocksQuotes.isEmpty())
            this.stocksQuotes = getStocksQuotes();
    }

    private HashMap<String, StockQuote> getStocksQuotes() {
        StockQuoteRepository stockQuoteRepository = new StockQuoteRepository(
                this.mAppStorage, new StorageCache(this.mAppStorage), this.widgetRepository);

        Set<String> symbolSet = portfolioStocksInfo.keySet();

        return stockQuoteRepository
                .getQuotes(Arrays.asList(symbolSet.toArray(new String[symbolSet.size()])), false);
    }

    private HashMap<String, PortfolioStock> getPortfolioStocksInfo(Set<String> symbols) {
        HashMap<String, PortfolioStock> stocks = this.getStocks();
        for (String symbol : symbols) {
            if (!stocks.containsKey(symbol)) {
                stocks.put(symbol, null);
            }
        }

        return stocks;
    }

    public List<Map<String, String>> getDisplayInfo() {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

        List<Map<String, String>> info = new ArrayList<>();
        for (String symbol : this.getSortedSymbols()) {
            StockQuote quote = this.stocksQuotes.get(symbol);
            PortfolioStock stock = this.getStock(symbol);
            Map<String, String> itemInfo = new HashMap<>();

            populateDisplayNames(quote, stock, itemInfo);

            // Get the current price if we have the data
            String currentPrice = populateDisplayCurrentPrice(quote, itemInfo);

            if (hasInfoForStock(stock)) {
                String buyPrice = stock.getPrice();

                itemInfo.put("buyPrice", buyPrice);
                itemInfo.put("date", stock.getDate());

                populateDisplayHighLimit(stock, itemInfo);
                populateDisplayLowLimit(stock, itemInfo);

                itemInfo.put("quantity", stock.getQuantity());

                populateDisplayLastChange(numberFormat, symbol, quote, stock, itemInfo);
                populateDisplayTotalChange(numberFormat, symbol, stock, itemInfo, currentPrice, buyPrice);
                populateDisplayHoldingValue(numberFormat, symbol, stock, itemInfo, currentPrice);
            }
            itemInfo.put("symbol", symbol);
            info.add(itemInfo);
        }

        return info;
    }

    private boolean hasInfoForStock(PortfolioStock stock) {
        return !stock.getPrice().equals("");
    }

    private void populateDisplayHighLimit(PortfolioStock stock, Map<String, String> itemInfo) {
        String limitHigh = NumberTools.decimalPlaceFormat(stock.getHighLimit());
        itemInfo.put("limitHigh", limitHigh);
    }

    private void populateDisplayLowLimit(PortfolioStock stock, Map<String, String> itemInfo) {
        String limitLow = NumberTools.decimalPlaceFormat(stock.getLowLimit());
        itemInfo.put("limitLow", limitLow);
    }

    private void populateDisplayHoldingValue(NumberFormat numberFormat, String symbol, PortfolioStock stock, Map<String, String> itemInfo, String currentPrice) {
        String holdingValue = "";
        try {
            Double holdingQuanta = NumberTools.parseDouble(stock.getQuantity());
            Double holdingPrice = numberFormat.parse(currentPrice).doubleValue();
            holdingValue = CurrencyTools.addCurrencyToSymbol(String.format(Locale.getDefault(), "%.0f", (holdingQuanta * holdingPrice)), symbol);
        } catch (Exception ignored) {
        }
        itemInfo.put("holdingValue", holdingValue);
    }

    private void populateDisplayLastChange(NumberFormat numberFormat, String symbol, StockQuote quote, PortfolioStock stock, Map<String, String> itemInfo) {
        String lastChange = "";
        try {
            if (quote != null) {
                lastChange = quote.getPercent();
                try {
                    Double change = numberFormat.parse(quote.getChange()).doubleValue();
                    Double totalChange = NumberTools.parseDouble(stock.getQuantity()) * change;
                    lastChange += " / " + CurrencyTools.addCurrencyToSymbol(String.format(Locale.getDefault(), "%.0f", (totalChange)), symbol);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        itemInfo.put("lastChange", lastChange);
    }

    private void populateDisplayTotalChange(NumberFormat numberFormat, String symbol, PortfolioStock stock, Map<String, String> itemInfo, String currentPrice, String buyPrice) {
        // Calculate total change, including percentage
        String totalChange = "";
        try {
            Double price = numberFormat.parse(currentPrice).doubleValue();
            Double buy = NumberTools.parseDouble(buyPrice);
            Double totalPercentChange = price - buy;
            totalChange = String.format(Locale.getDefault(), "%.0f", 100 * totalPercentChange / buy) + "%";

            // Calculate change
            try {
                Double quanta = NumberTools.parseDouble(stock.getQuantity());
                totalChange += " / " + CurrencyTools.addCurrencyToSymbol(String.format(Locale.getDefault(), "%.0f", quanta * totalPercentChange), symbol);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
        itemInfo.put("totalChange", totalChange);
    }

    private String populateDisplayCurrentPrice(StockQuote quote, Map<String, String> itemInfo) {
        String currentPrice = "";
        if (quote != null)
            currentPrice = quote.getPrice();
        itemInfo.put("currentPrice", currentPrice);

        return currentPrice;
    }

    private void populateDisplayNames(StockQuote quote, PortfolioStock stock, Map<String, String> itemInfo) {
        String defaultName = "No description";
        String name = defaultName;

        if (quote != null) {
            if (!stock.getCustomName().equals("")) {
                name = stock.getCustomName();
                itemInfo.put("customName", name);
            }
            if (name.equals(defaultName)) {
                name = quote.getName();
            }
        }
        itemInfo.put("name", name);
    }

    private PortfolioStock getStock(String symbol) {
        PortfolioStock stock = this.portfolioStocksInfo.get(symbol);
        if (stock == null) {
            stock = new PortfolioStock("", "", "", "", "", "", null);
        }
        this.portfolioStocksInfo.put(symbol, stock);

        return stock;
    }

    @SuppressWarnings("unused")
    public void backupPortfolio(Context context) {
        String rawJson = this.mAppStorage.getString(PORTFOLIO_JSON, "");
        UserData.writeInternalStorage(context, rawJson, PORTFOLIO_JSON);
        DialogTools.showSimpleDialog(context, "PortfolioActivity backed up",
                "Your portfolio settings have been backed up to internal mAppStorage.");
    }

    @SuppressWarnings("unused")
    public void restorePortfolio(Context context) {
        setDirtyPortfolioStockMap(true);
        String rawJson = UserData.readInternalStorage(context, PORTFOLIO_JSON);
        this.mAppStorage.putString(PORTFOLIO_JSON, rawJson).apply();
        DialogTools.showSimpleDialog(context, "PortfolioActivity restored",
                "Your portfolio settings have been restored from internal mAppStorage.");
    }

    private JSONObject getStocksJson() {
        JSONObject stocksJson = new JSONObject();
        try {
            stocksJson = new JSONObject(this.mAppStorage.getString(PORTFOLIO_JSON, ""));
        } catch (JSONException ignored) {
        }
        return stocksJson;
    }

    HashMap<String, PortfolioStock> getStocks() {
        if (!isDirtyPortfolioStockMap()) {
            return mPortfolioStocks;
        }
        mPortfolioStocks.clear();

        // Use the Json data if present
        Iterator keys;
        JSONObject json = this.getStocksJson();
        keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            JSONObject itemJson = new JSONObject();
            try {
                itemJson = json.getJSONObject(key);
            } catch (JSONException ignored) {
            }

            HashMap<PortfolioField, String> stockInfoMap = new HashMap<>();
            for (PortfolioField f : PortfolioField.values()) {
                String data = "";
                try {
                    if (!itemJson.get(f.name()).equals("empty")) {
                        data = itemJson.get(f.name()).toString();
                    }
                } catch (JSONException ignored) {
                }
                stockInfoMap.put(f, data);
            }

            PortfolioStock stock = new PortfolioStock(
                    stockInfoMap.get(PortfolioField.PRICE),
                    stockInfoMap.get(PortfolioField.DATE),
                    stockInfoMap.get(PortfolioField.QUANTITY),
                    stockInfoMap.get(PortfolioField.LIMIT_HIGH),
                    stockInfoMap.get(PortfolioField.LIMIT_LOW),
                    stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY),
                    stockInfoMap.get(PortfolioField.SYMBOL_2));
            mPortfolioStocks.put(key, stock);
        }
        setDirtyPortfolioStockMap(false);

        return mPortfolioStocks;
    }

    private void persist() {
        JSONObject json = new JSONObject();
        for (String symbol : this.portfolioStocksInfo.keySet()) {
            PortfolioStock item = this.portfolioStocksInfo.get(symbol);
            if (item.hasData()) {
                try {
                    json.put(symbol, item.toJson());
                } catch (JSONException ignored) {
                }
            }
        }
        this.mAppStorage.putString(PORTFOLIO_JSON, json.toString());
        this.mAppStorage.apply();
        setDirtyPortfolioStockMap(true);
    }

    public HashMap<String, PortfolioStock> getStocksForSymbols(List<String> symbols) {
        HashMap<String, PortfolioStock> stocksForSymbols = new HashMap<>();
        HashMap<String, PortfolioStock> stocks = this.getStocks();

        for (String symbol : symbols) {
            PortfolioStock stock = stocks.get(symbol);
            if (stock != null && stock.hasData()) {
                stocksForSymbols.put(symbol, stock);
            }
        }

        return stocksForSymbols;
    }

    private List<String> getSortedSymbols() {
        ArrayList<String> symbols = new ArrayList<>();
        symbols.addAll(this.portfolioStocksInfo.keySet());

        try {
            // Ensure symbols beginning with ^ appear first
            Collections.sort(symbols, new RuleBasedCollator("< '^' < a"));
        } catch (ParseException ignored) {
        }
        return symbols;
    }

    public void updateStock(String symbol, String price, String date, String quantity,
                            String limitHigh, String limitLow, String customDisplay) {
        PortfolioStock portfolioStock = new PortfolioStock(price, date, quantity,
                limitHigh, limitLow, customDisplay, null);
        this.portfolioStocksInfo.put(symbol, portfolioStock);
    }

    public void updateStock(String symbol) {
        this.updateStock(symbol, "", "", "", "", "", "");
    }

    void removeUnused() {
        List<String> symbols = new ArrayList<>(this.portfolioStocksInfo.keySet());
        for (String symbol : symbols) {
            String price = this.portfolioStocksInfo.get(symbol).getPrice();
            if ((price == null || price.equals("")) && !this.widgetsStockSymbols.contains(symbol)) {
                this.portfolioStocksInfo.remove(symbol);
            }
        }
    }

    public void saveChanges() {
        this.removeUnused();
        this.persist();
    }

    public enum PortfolioField {
        PRICE, DATE, QUANTITY, LIMIT_HIGH, LIMIT_LOW, CUSTOM_DISPLAY, SYMBOL_2
    }
}
