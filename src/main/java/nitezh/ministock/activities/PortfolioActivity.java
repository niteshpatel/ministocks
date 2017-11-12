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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import nitezh.ministock.DialogTools;
import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.activities.widget.WidgetProviderBase;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.utils.NumberTools;


public class PortfolioActivity extends Activity {

    private PortfolioStockRepository portfolioRepository;
    private ListView mPortfolioListView;
    private String mStockSymbol = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Storage storage = PreferenceStorage.getInstance(this);
        AndroidWidgetRepository widgetRepository = new AndroidWidgetRepository(this);

        this.portfolioRepository = new PortfolioStockRepository(
                storage,
                widgetRepository
        );

        this.portfolioRepository.updateStocksQuotes();

        this.refreshView();
    }

    private void refreshView() {
        setContentView(R.layout.portfolio);

        List<Map<String, String>> listViewData = this.portfolioRepository.getDisplayInfo();
        SimpleAdapter adapter = new SimpleAdapter(this, listViewData, R.layout.portfolio_list_item,
                new String[]{
                        "symbol",
                        "name",
                        "buyPrice",
                        "date",
                        "limitHigh",
                        "limitLow",
                        "quantity",
                        "currentPrice",
                        "lastChange",
                        "totalChange",
                        "holdingValue"
                },
                new int[]{
                        R.id.portfolio_list_item_symbol,
                        R.id.portfolio_list_item_name,
                        R.id.portfolio_list_item_buy_price,
                        R.id.portfolio_list_item_date,
                        R.id.portfolio_list_item_limit_high,
                        R.id.portfolio_list_item_limit_low,
                        R.id.portfolio_list_item_quantity,
                        R.id.portfolio_list_item_current_price,
                        R.id.portfolio_list_item_last_change,
                        R.id.portfolio_list_item_total_change,
                        R.id.portfolio_list_item_holding_value
                });

        ListView portfolioListView = (ListView) findViewById(R.id.portfolio_list);
        portfolioListView.setAdapter(adapter);
        portfolioListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                showPortfolioItemEdit(a, position);
            }
        });
        registerForContextMenu(portfolioListView);
        mPortfolioListView = portfolioListView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        View adapterView = ((AdapterContextMenuInfo) menuInfo).targetView;
        View view = adapterView.findViewById(R.id.portfolio_list_item_symbol);
        mStockSymbol = ((android.widget.TextView) view).getText().toString();
        menu.setHeaderTitle(mStockSymbol);
        menu.add(0, 0, 0, "Edit details");
        menu.add(0, 1, 0, "Clear details");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Callable callable = new Callable() {
                @Override
                public Object call() throws Exception {
                    portfolioRepository.updateStock(mStockSymbol);
                    refreshView();
                    return new Object();
                }
            };
            DialogTools.alertWithCallback(this, "Confirm Delete",
                    "Clear portfolio info for " + mStockSymbol + "?", "Delete", "Cancel",
                    callable, null);
        } else if (item.getItemId() == 0) {
            AdapterContextMenuInfo menuInfo = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo());
            showPortfolioItemEdit(mPortfolioListView, menuInfo.position);
        }
        return super.onContextItemSelected(item);
    }

    private void showPortfolioItemEdit(AdapterView<?> a, int position) {
        // Create the portfolio item edit dialog
        final Dialog portfolioItemEdit = new Dialog(this);
        portfolioItemEdit.setContentView(R.layout.portfolio_item);

        // Get current details
        @SuppressWarnings("unchecked") HashMap<String, String> stockMap = (HashMap<String, String>) a.getItemAtPosition(position);
        mStockSymbol = stockMap.get("symbol");

        // Get current data for this stock
        StockQuote data = this.portfolioRepository.stocksQuotes.get(mStockSymbol);
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
        priceEditText.setText(price);

        // Initialise the date if the price has been set
        // to avoid getting the <none held> text
        EditText dateEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_date);
        if (!date.equals("") && !price.equals(""))
            dateEditText.setText(date);

        // Initialise the quantity if the price has been set
        EditText quantityEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_quantity);
        if (!quantity.equals("") && !price.equals(""))
            quantityEditText.setText(quantity);

        // Initialise the limit high if the price has been set
        EditText limitHighEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_limit_high);
        if (!limitHigh.equals("") && !price.equals(""))
            limitHighEditText.setText(limitHigh);

        // Initialise the limit low if the price has been set
        EditText limitLowEditText = (EditText) portfolioItemEdit.findViewById(R.id.portfolio_item_limit_low);
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

                        price = NumberTools.validatedDoubleString(price);

                        // Allow a blank date
                        if (!date.equals("")) {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            date = formatter.format(formatter.parse(date.replaceAll("[./]", "-"))).toUpperCase();
                        }
                        // Allow a blank quantity
                        if (!quantity.equals("")) {
                            quantity = NumberTools.validatedDoubleString(quantity);
                        }
                        // Allow a blank limitHigh
                        if (!limitHigh.equals("")) {
                            limitHigh = NumberTools.validatedDoubleString(limitHigh);
                        }
                        // Allow a blank limitLow
                        if (!limitLow.equals("")) {
                            limitLow = NumberTools.validatedDoubleString(limitLow);
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
                portfolioRepository.updateStock(mStockSymbol, price, date, quantity,
                        limitHigh, limitLow, customDisplay);
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

        this.portfolioRepository.saveChanges();
        WidgetProviderBase.updateWidgets(this, WidgetProviderBase.UpdateType.VIEW_NO_UPDATE);
        finish();
    }
}
