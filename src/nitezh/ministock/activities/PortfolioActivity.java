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

package nitezh.ministock.activities;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import nitezh.ministock.DialogTools;
import nitezh.ministock.PreferenceCache;
import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.UserData;
import nitezh.ministock.UserData.PortfolioField;
import nitezh.ministock.activities.widget.WidgetProviderBase;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;


public class PortfolioActivity extends Activity {

    private HashMap<String, StockQuote> mStockData = new HashMap<>();
    private HashMap<String, HashMap<PortfolioField, String>> mPortfolioStockMap = new HashMap<>();
    private Set<String> mWidgetsStockMap = new HashSet<>();
    private ListView mPortfolioList;
    private String mStockSymbol = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add any missing stocks from the widget stocks map to our local
        // portfolio stocks map
        Storage appStorage = PreferenceStorage.getInstance(this);
        WidgetRepository repository = new AndroidWidgetRepository(this);
        mPortfolioStockMap = UserData.getPortfolioStockMap(appStorage);
        mWidgetsStockMap = repository.getWidgetsStockSymbols();
        for (String symbol : mWidgetsStockMap) {
            if (!mPortfolioStockMap.containsKey(symbol)) {
                mPortfolioStockMap.put(symbol, new HashMap<PortfolioField, String>());
            }
        }

        // Get current prices
        Set<String> symbolSet = mPortfolioStockMap.keySet();
        mStockData = new StockQuoteRepository(appStorage, new PreferenceCache(this), repository).getQuotes(Arrays.asList(symbolSet.toArray(new String[symbolSet.size()])), false);

        // Update the list view with the portfolio stock map info
        refreshView();
    }

    void updatePortfolioStock() {
        updatePortfolioStock("", "", "", "", "", "");
    }

    void updatePortfolioStock(String price, String date, String quantity, String limitHigh, String limitLow, String customDisplay) {
        // Set the last updated stock details in our local portfolio stock map
        HashMap<PortfolioField, String> stockInfoMap = new HashMap<>();
        stockInfoMap.put(PortfolioField.PRICE, price);
        stockInfoMap.put(PortfolioField.DATE, date);
        stockInfoMap.put(PortfolioField.QUANTITY, quantity);
        stockInfoMap.put(PortfolioField.LIMIT_HIGH, limitHigh);
        stockInfoMap.put(PortfolioField.LIMIT_LOW, limitLow);
        stockInfoMap.put(PortfolioField.CUSTOM_DISPLAY, customDisplay);
        mPortfolioStockMap.put(mStockSymbol, stockInfoMap);
    }

    void refreshView() {
        setContentView(R.layout.portfolio);

        // Get number format instance to parse numbers from different locales
        NumberFormat numberFormat = NumberFormat.getInstance();

        // Sort dictionary by putting values into a list
        ArrayList<String> sortedSymbols = new ArrayList<>();
        for (String key : mPortfolioStockMap.keySet())
            sortedSymbols.add(key);

        // Ensure symbols beginning with ^ appear first
        try {
            Collections.sort(sortedSymbols, new RuleBasedCollator("< '^' < a"));
        } catch (ParseException ignored) {
        }
        // Put data into a custom adapter
        List<Map<String, String>> listViewData = new ArrayList<>();
        for (String symbol : sortedSymbols) {
            // Get quote and portfolio info
            StockQuote data = mStockData.get(symbol);
            HashMap<PortfolioField, String> stockInfoMap = mPortfolioStockMap.get(symbol);

            // Add name if we have one
            Map<String, String> group = new HashMap<>();
            String name = "No description";
            if (data != null) {
                if (stockInfoMap.containsKey(PortfolioField.CUSTOM_DISPLAY)) {
                    name = stockInfoMap.get(PortfolioField.CUSTOM_DISPLAY);
                    group.put("customName", name);
                }
                if (name.equals(""))
                    name = data.getName();
            }
            group.put("name", name);

            // Get the current price if we have the data
            String currentPrice = "";
            if (data != null)
                currentPrice = data.getPrice();
            group.put("currentPrice", currentPrice);

            // Default labels
            group.put("limitHigh_label", "High alert:");
            group.put("limitLow_label", "Low alert:");

            // Add stock info the the list view
            if (stockInfoMap.containsKey(PortfolioField.PRICE) && !stockInfoMap.get(PortfolioField.PRICE).equals("")) {
                // Buy price and label
                String buyPrice = stockInfoMap.get(PortfolioField.PRICE);
                group.put("buyPrice", buyPrice);

                // Buy date and label
                String date = stockInfoMap.get(PortfolioField.DATE);
                group.put("date", date);

                // High alert and label
                String limitHigh = NumberTools.decimalPlaceFormat(stockInfoMap.get(PortfolioField.LIMIT_HIGH));
                if (limitHigh != null && !limitHigh.equals(""))
                    group.put("limitHigh_label", "High alert:");
                group.put("limitHigh", limitHigh);

                // Low alert and label
                String limitLow = NumberTools.decimalPlaceFormat(stockInfoMap.get(PortfolioField.LIMIT_LOW));
                if (limitLow != null && !limitLow.equals(""))
                    group.put("limitLow_label", "Low alert:");
                group.put("limitLow", limitLow);

                // Quantity and label
                String quantity = stockInfoMap.get(PortfolioField.QUANTITY);
                group.put("quantity", quantity);

                // Calculate last change, including percentage
                String lastChange = "";
                try {
                    if (data != null) {
                        lastChange = data.getPercent();
                        try {
                            Double change = numberFormat.parse(data.getChange()).doubleValue();
                            Double totalChange = NumberTools.parseDouble(stockInfoMap.get(PortfolioField.QUANTITY)) * change;
                            lastChange += " / " + CurrencyTools.addCurrencyToSymbol(String.format("%.0f", (totalChange)), symbol);
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }
                group.put("lastChange", lastChange);

                // Calculate total change, including percentage
                String totalChange = "";
                try {
                    Double price = numberFormat.parse(currentPrice).doubleValue();
                    Double buy = Double.parseDouble(buyPrice);
                    Double totalPercentChange = price - buy;
                    totalChange = String.format("%.0f", 100 * totalPercentChange / buy) + "%";

                    // Calculate change
                    try {
                        Double quanta = NumberTools.parseDouble(stockInfoMap.get(PortfolioField.QUANTITY));
                        totalChange += " / " + CurrencyTools.addCurrencyToSymbol(String.format("%.0f", quanta * totalPercentChange), symbol);
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
                group.put("totalChange", totalChange);

                // Calculate the holding value
                String holdingValue = "";
                try {
                    Double holdingQuanta = NumberTools.parseDouble(stockInfoMap.get(PortfolioField.QUANTITY));
                    Double holdingPrice = numberFormat.parse(currentPrice).doubleValue();
                    holdingValue = CurrencyTools.addCurrencyToSymbol(String.format("%.0f", (holdingQuanta * holdingPrice)), symbol);
                } catch (Exception ignored) {
                }
                group.put("holdingValue", holdingValue);
            }
            group.put("symbol", symbol);
            listViewData.add(group);
        }
        // Assign the list to the ListView
        SimpleAdapter adapter = new SimpleAdapter(this, listViewData, R.layout.portfolio_list_item, new String[]{"symbol", "name", "buyPrice", "date", "limitHigh", "limitHigh_label", "limitLow", "limitLow_label", "quantity", "currentPrice", "lastChange", "totalChange", "holdingValue",}, new int[]{R.id.portfolio_list_item_symbol, R.id.portfolio_list_item_name, R.id.portfolio_list_item_buy_price, R.id.portfolio_list_item_date, R.id.portfolio_list_item_limit_high, R.id.portfolio_list_item_limit_high_label, R.id.portfolio_list_item_limit_low, R.id.portfolio_list_item_limit_low_label, R.id.portfolio_list_item_quantity, R.id.portfolio_list_item_current_price, R.id.portfolio_list_item_last_change, R.id.portfolio_list_item_total_change, R.id.portfolio_list_item_holding_value});

        // Assign the SimpleAdapter to the ListView
        ListView portfolio_list = (ListView) findViewById(R.id.portfolio_list);
        mPortfolioList = portfolio_list;

        // Set an onClick listener for the ListView
        portfolio_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                showPortfolioItemEdit(a, position);
            }
        });
        portfolio_list.setAdapter(adapter);

        // Register a context menu for this ListView
        registerForContextMenu(portfolio_list);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        String symbol = (String) (((android.widget.TextView) (((AdapterView.AdapterContextMenuInfo) menuInfo).targetView).findViewById(R.id.portfolio_list_item_symbol))).getText();
        mStockSymbol = symbol;
        menu.setHeaderTitle(symbol);
        menu.add(0, 0, 0, "Edit details");
        menu.add(0, 1, 0, "Clear details");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Callable callable = new Callable() {
                @Override
                public Object call() throws Exception {
                    updatePortfolioStock();
                    refreshView();
                    return new Object();
                }
            };
            DialogTools.alertWithCallback(this, "Confirm Delete",
                    "Clear portfolio info for " + mStockSymbol + "?", "Delete", "Cancel",
                    callable, null);
        } else if (item.getItemId() == 0) {
            AdapterContextMenuInfo menuInfo = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo());
            showPortfolioItemEdit(mPortfolioList, menuInfo.position);
        }
        return super.onContextItemSelected(item);
    }

    void showPortfolioItemEdit(AdapterView<?> a, int position) {
        // Create the portfolio item edit dialog
        final Dialog portfolioItemEdit = new Dialog(this);
        portfolioItemEdit.setContentView(R.layout.portfolio_item);

        // Get current details
        @SuppressWarnings("unchecked") HashMap<String, String> stockMap = (HashMap<String, String>) a.getItemAtPosition(position);
        mStockSymbol = stockMap.get("symbol");

        // Get current data for this stock
        StockQuote data = mStockData.get(mStockSymbol);
        String currentPrice = "";
        if (data != null)
            currentPrice = data.getPrice();

        // Get portfolio details for this stock
        String price = stockMap.get("buyPrice") != null ? stockMap.get("buyPrice") : "";
        String date = stockMap.get("date") != null ? stockMap.get("date") : "";
        String quantity = stockMap.get("quantity") != null ? stockMap.get("quantity") : "";
        String limitHigh = stockMap.get("limitHigh") != null ? stockMap.get("limitHigh") : "";
        String limitLow = stockMap.get("limitLow") != null ? stockMap.get("limitLow") : "";
        String customDisplay = stockMap.get("customName") != null ? stockMap.get("customName") : "";

        // If there is no price data, use today's price and date
        if (price.equals("")) {
            price = currentPrice;
            date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
        // Initialise the price
        EditText priceEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_price);
        priceEditText.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceEditText.setText(price);

        // Initialise the date if the price has been set
        // to avoid getting the <none held> text
        EditText dateEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_date);
        dateEditText.setInputType(InputType.TYPE_CLASS_DATETIME + InputType.TYPE_DATETIME_VARIATION_DATE);
        if (!date.equals("") && !price.equals(""))
            dateEditText.setText(date);

        // Initialise the quantity if the price has been set
        EditText quantityEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_quantity);
        quantityEditText.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!quantity.equals("") && !price.equals(""))
            quantityEditText.setText(quantity);

        // Initialise the limit high if the price has been set
        EditText limitHighEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_limit_high);
        limitHighEditText.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!limitHigh.equals("") && !price.equals(""))
            limitHighEditText.setText(limitHigh);

        // Initialise the limit low if the price has been set
        EditText limitLowEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_limit_low);
        limitLowEditText.setInputType(InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!limitLow.equals("") && !price.equals(""))
            limitLowEditText.setText(limitLow);

        // Initialise the custom display if the price has been set
        EditText customDisplayText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_symbol);
        customDisplayText.setInputType(InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if (!customDisplay.equals("") && !customDisplay.equals("No description"))
            customDisplayText.setText(customDisplay);

        // Setup OK button
        Button okButton = (Button) portfolioItemEdit.findViewById(R.id.portfolio_item_save);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve updated price
                EditText priceEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_price);
                String price = priceEditText.getText().toString();

                // Retrieve updated date
                EditText dateEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_date);
                String date = dateEditText.getText().toString();

                // Retrieve updated quantity
                EditText quantityEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_quantity);
                String quantity = quantityEditText.getText().toString();

                // Retrieve updated quantity
                EditText limitHighEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_limit_high);
                String limitHigh = limitHighEditText.getText().toString();

                // Retrieve updated quantity
                EditText limitLowEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_limit_low);
                String limitLow = limitLowEditText.getText().toString();

                // Retrieve custom display text
                EditText customDisplayText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_symbol);
                String customDisplay = customDisplayText.getText().toString();

                // If the price is empty then clear all other values
                if (price.equals("")) {
                    date = "";
                    quantity = "";
                    limitHigh = "";
                    limitLow = "";
                }
                // Otherwise validate the fields
                else {
                    try {
                        // First validate and parse the data, if this fails then
                        // dismiss the dialog without making any changes
                        price = Double.toString(Double.parseDouble(price));

                        // Allow a blank date
                        if (!date.equals("")) {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            date = formatter.format(formatter.parse(date.replaceAll("[./]", "-"))).toUpperCase();
                        }
                        // Allow a blank quantity
                        if (!quantity.equals("")) {
                            quantity = Double.toString(Double.parseDouble(quantity));
                        }
                        // Allow a blank limitHigh
                        if (!limitHigh.equals("")) {
                            limitHigh = Double.toString(Double.parseDouble(limitHigh));
                        }
                        // Allow a blank limitLow
                        if (!limitLow.equals("")) {
                            limitLow = Double.toString(Double.parseDouble(limitLow));
                        }
                    } catch (Exception e) {
                        // On error just ignore the input and close the dialog
                        portfolioItemEdit.dismiss();
                        return;
                    }
                }
                // If the string only has one digit after the decimal
                // point add another one
                if (price.indexOf(".") == price.length() - 2) {
                    price = price + "0";
                }
                // Update the actual values
                updatePortfolioStock(price, date, quantity, limitHigh, limitLow, customDisplay);
                refreshView();
                portfolioItemEdit.dismiss();
            }
        });
        // Setup Cancel button
        Button cancelButton = (Button) portfolioItemEdit.findViewById(R.id.portfolio_item_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                portfolioItemEdit.dismiss();
            }
        });

        // Display the dialog
        portfolioItemEdit.setTitle(mStockSymbol + " purchase details");
        portfolioItemEdit.show();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove any items that are empty and not in any widgets and then
        // update the portfolio stocks in the preferences;
        for (String symbol : mPortfolioStockMap.keySet()) {
            String price = mPortfolioStockMap.get(symbol).get(PortfolioField.PRICE);
            if ((price == null || price.equals("")) && !mWidgetsStockMap.contains(symbol)) {
                mPortfolioStockMap.remove(symbol);
            }
        }
        UserData.setPortfolioStockMap(this, mPortfolioStockMap);

        // Update all widget views and exit
        WidgetProviderBase.updateWidgets(this, WidgetProviderBase.VIEW_NO_UPDATE);
        finish();
    }
}
