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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nitezh.ministock.DialogTools;
import nitezh.ministock.Storage;
import nitezh.ministock.UserData;

public class PortfolioStockRepository {
    public static final String PORTFOLIO_JSON = "portfolioJson";
    public static final String WIDGET_JSON = "widgetJson";
    private static final HashMap<String, PortfolioStock> mPortfolioStocks = new HashMap<>();
    // Cache markers
    private static boolean mDirtyPortfolioStockMap = true;
    private Storage storage;

    public PortfolioStockRepository(Storage storage) {
        this.storage = storage;
    }

    public void backupPortfolio(Context context) {
        String rawJson = this.storage.getString(PORTFOLIO_JSON, "");
        UserData.writeInternalStorage(context, rawJson, PORTFOLIO_JSON);
        DialogTools.showSimpleDialog(context, "PortfolioActivity backed up",
                "Your portfolio settings have been backed up to internal storage.");
    }

    public void restorePortfolio(Context context) {
        mDirtyPortfolioStockMap = true;
        String rawJson = UserData.readInternalStorage(context, PORTFOLIO_JSON);
        this.storage.putString(PORTFOLIO_JSON, rawJson);
        this.storage.apply();
        DialogTools.showSimpleDialog(context, "PortfolioActivity restored",
                "Your portfolio settings have been restored from internal storage.");
    }

    public JSONObject getStocksJson() {
        JSONObject stocksJson = new JSONObject();
        try {
            stocksJson = new JSONObject(this.storage.getString(PORTFOLIO_JSON, ""));
        } catch (JSONException ignored) {
        }
        return stocksJson;
    }

    public HashMap<String, PortfolioStock> getStocks() {
        if (!mDirtyPortfolioStockMap) {
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

            PortfolioStock stock = new PortfolioStock(key,
                    stockInfoMap.get(PortfolioField.PRICE),
                    stockInfoMap.get(PortfolioField.DATE),
                    stockInfoMap.get(PortfolioField.QUANTITY),
                    stockInfoMap.get(PortfolioField.LIMIT_HIGH),
                    stockInfoMap.get(PortfolioField.LIMIT_LOW),
                    stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY),
                    stockInfoMap.get(PortfolioField.SYMBOL_2));
            mPortfolioStocks.put(key, stock);
        }
        mDirtyPortfolioStockMap = false;

        return mPortfolioStocks;
    }

    public void setStocks(HashMap<String, PortfolioStock> stocks) {
        JSONObject json = new JSONObject();
        for (String symbol : stocks.keySet()) {
            PortfolioStock item = stocks.get(symbol);
            if (!item.isEmpty()) {
                try {
                    json.put(symbol, item.toJson());
                } catch (JSONException ignored) {
                }
            }
        }
        this.storage.putString(PORTFOLIO_JSON, json.toString());
        this.storage.apply();
        mDirtyPortfolioStockMap = true;
    }

    public HashMap<String, PortfolioStock> getStocksForWidget(List<String> symbols) {
        HashMap<String, PortfolioStock> stocksForWidget = new HashMap<>();
        HashMap<String, PortfolioStock> stocks = this.getStocks();

        for (String symbol : symbols) {
            PortfolioStock stock = stocks.get(symbol);
            if (stock != null && !stock.isEmpty()) {
                stocksForWidget.put(symbol, stock);
            }
        }

        return stocksForWidget;
    }

    public enum PortfolioField {
        PRICE, DATE, QUANTITY, LIMIT_HIGH, LIMIT_LOW, CUSTOM_DISPLAY, SYMBOL_2
    }
}
