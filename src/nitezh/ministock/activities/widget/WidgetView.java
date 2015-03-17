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

package nitezh.ministock.activities.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.WidgetProvider;
import nitezh.ministock.domain.PortfolioStock;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.ReflectionTools;

import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType;
import static nitezh.ministock.activities.widget.WidgetProviderBase.ViewType;

public class WidgetView {

    private final RemoteViews remoteViews;
    private final int arraySize;
    private final int widgetSize;
    private final Widget widget;
    private final boolean hasPortfolioData;
    private HashMap<ViewType, Boolean> enabledViews;
    private Storage storage;

    public WidgetView(Context context, Widget widget, boolean hasPortfolioData) {
        String packageName = context.getPackageName();
        this.widget = widget;
        this.hasPortfolioData = hasPortfolioData;
        this.storage = widget.getStorage();
        this.widgetSize = storage.getInt("widgetSize", 0);
        String background = storage.getString("background", "transparent");
        Integer imageViewSrcId;
        switch (background) {
            case "transparent":
                if (storage.getBoolean("large_font", false)) {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68;
                }
                break;
            case "none":
                imageViewSrcId = R.drawable.blank;
                break;
            default:
                if (storage.getBoolean("large_font", false)) {
                    imageViewSrcId = R.drawable.ministock_bg_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg;
                }
                break;
        }

        // Return the matching remote views instance
        RemoteViews views;
        if (widgetSize == 1) {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_1x4_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x4);
            }
        } else if (widgetSize == 2) {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_2x2_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x2);
            }
        } else if (widgetSize == 3) {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_2x4_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x4);
            }
        } else {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_1x2_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x2);
            }
        }
        views.setImageViewResource(R.id.widget_bg, imageViewSrcId);
        this.remoteViews = views;

        // Get the array widgetSize for widgets
        int arraySize = 0;
        if (widgetSize == 0 || widgetSize == 1) {
            arraySize = 4;
        } else if (widgetSize == 2 || widgetSize == 3) {
            arraySize = 10;
        }
        this.arraySize = arraySize;

        // Hide any rows for smaller widgets
        this.displayRows();
        this.setEnabledViews();
        this.setOnClickPendingIntents(context, widget.getId());
    }

    private static int getStockViewId(int line, int col) {
        return ReflectionTools.getField("text" + line + col);
    }

    // Global formatter so we can perform global text formatting in one place
    private static SpannableString makeBold(Storage prefs, String s) {
        SpannableString span = new SpannableString(s);
        if (prefs.getString("text_style", "normal").equals("bold"))
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        else
            span.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
        return span;
    }

    private void setOnClickPendingIntents(Context context, int appWidgetId) {
        // Set an onClick handler on the 'widget_left' layout
        Intent left_intent = new Intent(context, WidgetProvider.class);
        left_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        left_intent.setAction("LEFT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_left, PendingIntent.getBroadcast(context, appWidgetId, left_intent, 0));

        // Set an onClick handler on the 'widget_right' layout
        Intent right_intent = new Intent(context, WidgetProvider.class);
        right_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        right_intent.setAction("RIGHT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_right, PendingIntent.getBroadcast(context, appWidgetId, right_intent, 0));
    }

    public void setEnabledViews() {
        Storage storage = this.widget.getStorage();
        int size = this.widget.getSize();

        // Get widget view prefs
        boolean dailyChangePc = storage.getBoolean("show_percent_change", false);
        boolean dailyChange = storage.getBoolean("show_absolute_change", false);
        boolean totalChangePc = storage.getBoolean("show_portfolio_change", false);
        boolean totalChange = storage.getBoolean("show_portfolio_abs", false);
        boolean totalChangeAer = storage.getBoolean("show_portfolio_aer", false);
        boolean plDailyChangePc = storage.getBoolean("show_profit_daily_change", false);
        boolean plDailyChange = storage.getBoolean("show_profit_daily_abs", false);
        boolean plTotalChangePc = storage.getBoolean("show_profit_change", false);
        boolean plTotalChange = storage.getBoolean("show_profit_abs", false);
        boolean plTotalChangeAer = storage.getBoolean("show_profit_aer", false);

        HashMap<WidgetProviderBase.ViewType, Boolean> enabledViews = new HashMap<>();
        enabledViews.put(ViewType.VIEW_DAILY_PERCENT, dailyChangePc && (size == 0 || size == 2));
        enabledViews.put(ViewType.VIEW_DAILY_CHANGE, dailyChange);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT, totalChangePc && (size == 0 || size == 2) && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_CHANGE, totalChange && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT_AER, totalChangeAer && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_DAILY_PERCENT, plDailyChangePc && (size == 0 || size == 2) && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_DAILY_CHANGE, plDailyChange && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_PERCENT, plTotalChangePc && (size == 0 || size == 2) && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_CHANGE, plTotalChange);
        enabledViews.put(ViewType.VIEW_PL_PERCENT_AER, plTotalChangeAer && this.hasPortfolioData);
        this.enabledViews = enabledViews;
    }

    public HashMap<WidgetProviderBase.ViewType, Boolean> getEnabledViews() {
        return this.enabledViews;
    }

    private HashMap<String, Object> getFormattedRow(String symbol, StockQuote quoteInfo,
                                                    HashMap<String, PortfolioStock> portfolioStockMap, ViewType widgetView,
                                                    Storage preferences, int widgetSize) {
        // Create the HashMap for our return values
        HashMap<String, Object> rowItems = new HashMap<>();

        // Initialise columns
        rowItems.put("COL0_VALUE", symbol);
        rowItems.put("COL0_COLOUR", Color.WHITE);
        rowItems.put("COL1_VALUE", "");
        rowItems.put("COL1_COLOUR", Color.WHITE);
        rowItems.put("COL2_VALUE", "");
        rowItems.put("COL2_COLOUR", Color.WHITE);
        rowItems.put("COL3_VALUE", "");
        rowItems.put("COL3_COLOUR", Color.WHITE);
        rowItems.put("COL4_VALUE", "");
        rowItems.put("COL4_COLOUR", Color.WHITE);

        // Set the stock symbol, and strip off exchange suffix
        // if requested in the preferences
        if (preferences.getBoolean("hide_suffix", false)) {
            int dotIndex = symbol.indexOf(".");
            if (dotIndex > -1) {
                rowItems.put("COL0_VALUE", symbol.substring(0, dotIndex));
            }
        }
        // If there is no quote info return immediately
        if (quoteInfo == null || quoteInfo.getPrice() == null || quoteInfo.getPercent() == null) {
            if (widgetSize == 0 || widgetSize == 2) {
                rowItems.put("COL1_VALUE", "no");
                rowItems.put("COL1_COLOUR", Color.GRAY);
                rowItems.put("COL2_VALUE", "data");
                rowItems.put("COL2_COLOUR", Color.GRAY);
            } else {
                rowItems.put("COL3_VALUE", "no");
                rowItems.put("COL3_COLOUR", Color.GRAY);
                rowItems.put("COL4_VALUE", "data");
                rowItems.put("COL4_COLOUR", Color.GRAY);
            }
            return rowItems;
        }
        // Retrieve quote info
        String name = quoteInfo.getName();
        String price = quoteInfo.getPrice();
        String change = quoteInfo.getChange();
        String percent = quoteInfo.getPercent();
        String volume = quoteInfo.getVolume();

        // Get the buy info for this stock from the portfolio
        PortfolioStock stockInfo = portfolioStockMap.get(symbol);

        // Set default values
        if (widgetSize == 1 || widgetSize == 3) {
            rowItems.put("COL0_VALUE", name);
            rowItems.put("COL2_VALUE", NumberTools.getNormalisedVolume(volume));
            rowItems.put("COL2_COLOUR", WidgetColors.VOLUME);
            rowItems.put("COL3_VALUE", change);
            rowItems.put("COL3_COLOUR", WidgetColors.NA);
            rowItems.put("COL4_VALUE", percent);
            rowItems.put("COL4_COLOUR", WidgetColors.NA);
        } else {
            rowItems.put("COL2_VALUE", percent);
            rowItems.put("COL2_COLOUR", WidgetColors.NA);
        }
        // Override the name if a custom value has been provided
        if (stockInfo != null && !stockInfo.getCustomName().equals("")) {
            rowItems.put("COL0_VALUE", stockInfo.getCustomName());
        }
        // Set the price
        rowItems.put("COL1_VALUE", price);
        // Initialise variables for values
        String daily_change = quoteInfo.getChange();
        String daily_percent = quoteInfo.getPercent();
        String total_change = null;
        String total_percent = null;
        String aer_change = null;
        String aer_percent = null;
        String pl_holding = null;
        String pl_daily_change = null;
        String pl_total_change = null;
        String pl_aer_change = null;
        Double years_elapsed = null;
        Boolean limitHigh_triggered = false;
        Boolean limitLow_triggered = false;
        Double d_price = NumberTools.parseDouble(price);
        Double d_dailyChange = NumberTools.parseDouble(change);
        Double d_buyPrice = null;
        Double d_quantity = null;
        Double d_limitHigh = null;
        Double d_limitLow = null;
        try {
            d_buyPrice = NumberTools.parseDouble(stockInfo.getPrice());
        } catch (Exception ignored) {
        }
        try {
            d_quantity = NumberTools.parseDouble(stockInfo.getQuantity());
        } catch (Exception ignored) {
        }
        try {
            d_limitHigh = NumberTools.parseDouble(stockInfo.getHighLimit());
        } catch (Exception ignored) {
        }
        try {
            d_limitLow = NumberTools.parseDouble(stockInfo.getLowLimit());
        } catch (Exception ignored) {
        }
        Double d_priceChange = null;
        try {
            d_priceChange = d_price - d_buyPrice;
        } catch (Exception ignored) {
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(stockInfo.getDate());
            double elapsed = (new Date().getTime() - date.getTime()) / 1000;
            years_elapsed = elapsed / 31536000;
        } catch (Exception ignored) {
        }
        // total_change
        if (d_priceChange != null)
            total_change = NumberTools.getTrimmedDouble(d_priceChange, 5);
        // total_percent
        if (d_priceChange != null)
            total_percent = String.format("%.1f", 100 * (d_priceChange / d_buyPrice)) + "%";
        // aer_change
        if (d_priceChange != null && years_elapsed != null)
            aer_change = NumberTools.getTrimmedDouble(d_priceChange / years_elapsed, 5);
        // aer_percent
        if (d_priceChange != null && years_elapsed != null)
            aer_percent = String.format("%.1f", (100 * (d_priceChange / d_buyPrice)) / years_elapsed) + "%";
        // pl_holding
        if (d_price != null && d_quantity != null)
            pl_holding = String.format("%.0f", d_price * d_quantity);
        // pl_daily_change
        if (d_dailyChange != null && d_quantity != null)
            pl_daily_change = String.format("%.0f", d_dailyChange * d_quantity);
        // pl_total_change
        if (d_priceChange != null && d_quantity != null)
            pl_total_change = String.format("%.0f", d_priceChange * d_quantity);
        // pl_aer_change
        if (d_priceChange != null && d_quantity != null && years_elapsed != null)
            pl_aer_change = String.format("%.0f", (d_priceChange * d_quantity) / years_elapsed);
        // limitHigh_triggered
        if (d_price != null && d_limitHigh != null)
            limitHigh_triggered = d_price > d_limitHigh;
        // limitLow_triggered
        if (d_price != null && d_limitLow != null)
            limitLow_triggered = d_price < d_limitLow;
        Boolean pl_view = false;
        Boolean pl_change = false;
        String column1 = null;
        String column2 = null;
        String column3 = null;
        String column4 = null;
        if (widgetSize == 0 || widgetSize == 2) {
            switch (widgetView) {
                case VIEW_DAILY_PERCENT:
                    column2 = daily_percent;

                    break;
                case VIEW_DAILY_CHANGE:
                    column2 = daily_change;

                    break;
                case VIEW_PORTFOLIO_PERCENT:
                    column2 = total_percent;

                    break;
                case VIEW_PORTFOLIO_CHANGE:
                    column2 = total_change;

                    break;
                case VIEW_PORTFOLIO_PERCENT_AER:
                    column2 = aer_percent;

                    break;
                case VIEW_PL_DAILY_PERCENT:
                    pl_view = true;
                    column1 = pl_holding;
                    column2 = daily_percent;

                    break;
                case VIEW_PL_DAILY_CHANGE:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column2 = pl_daily_change;

                    break;
                case VIEW_PL_PERCENT:
                    pl_view = true;
                    column1 = pl_holding;
                    column2 = total_percent;

                    break;
                case VIEW_PL_CHANGE:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column2 = pl_total_change;

                    break;
                case VIEW_PL_PERCENT_AER:
                    pl_view = true;
                    column1 = pl_holding;
                    column2 = aer_percent;

                    break;
            }
        } else {
            switch (widgetView) {
                case VIEW_DAILY_PERCENT:
                    column3 = daily_change;
                    column4 = daily_percent;

                    break;
                case VIEW_DAILY_CHANGE:
                    column3 = daily_change;
                    column4 = daily_percent;

                    break;
                case VIEW_PORTFOLIO_PERCENT:
                    column3 = total_change;
                    column4 = total_percent;

                    break;
                case VIEW_PORTFOLIO_CHANGE:
                    column3 = total_change;
                    column4 = total_percent;

                    break;
                case VIEW_PORTFOLIO_PERCENT_AER:
                    column3 = aer_change;
                    column4 = aer_percent;

                    break;
                case VIEW_PL_DAILY_PERCENT:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column3 = pl_daily_change;
                    column4 = daily_percent;

                    break;
                case VIEW_PL_DAILY_CHANGE:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column3 = pl_daily_change;
                    column4 = daily_percent;

                    break;
                case VIEW_PL_PERCENT:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column3 = pl_total_change;
                    column4 = total_percent;

                    break;
                case VIEW_PL_CHANGE:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column3 = pl_total_change;
                    column4 = total_percent;

                    break;
                case VIEW_PL_PERCENT_AER:
                    pl_view = true;
                    pl_change = true;
                    column1 = pl_holding;
                    column3 = pl_aer_change;
                    column4 = aer_percent;

                    break;
            }
        }
        // Set the price column colour if we have hit an alert
        // (this is only relevant for non-profit and loss views)
        if (limitHigh_triggered && !pl_view) {
            rowItems.put("COL1_COLOUR", WidgetColors.HIGH_ALERT);
        }
        if (limitLow_triggered && !pl_view) {
            rowItems.put("COL1_COLOUR", WidgetColors.LOW_ALERT);
        }
        // Set the price column to the holding value and colour
        // the column blue if we have no holdings
        if (pl_view && column1 == null) {
            rowItems.put("COL1_COLOUR", WidgetColors.NA);
        }
        // Add currency symbol if we have a holding
        if (column1 != null) {
            rowItems.put("COL1_VALUE", CurrencyTools.addCurrencyToSymbol(column1, symbol));
        }
        // Set the value and colour for the change values
        if (widgetSize == 1 || widgetSize == 3) {
            if (column3 != null) {
                if (pl_change) {
                    rowItems.put("COL3_VALUE", CurrencyTools.addCurrencyToSymbol(column3, symbol));
                } else {
                    rowItems.put("COL3_VALUE", column3);
                }
                rowItems.put("COL3_COLOUR", getColourForChange(column3));
            }
            if (column4 != null) {
                rowItems.put("COL4_VALUE", column4);
                rowItems.put("COL4_COLOUR", getColourForChange(column4));
            }
        } else {
            if (column2 != null) {
                if (pl_change) {
                    rowItems.put("COL2_VALUE", CurrencyTools.addCurrencyToSymbol(column2, symbol));
                } else {
                    rowItems.put("COL2_VALUE", column2);
                }
                rowItems.put("COL2_COLOUR", getColourForChange(column2));
            }
        }
        return rowItems;
    }

    private int getColourForChange(String value) {
        double parsedValue = NumberTools.parseDouble(value, 0d);
        int colour;
        if (parsedValue < 0) {
            colour = WidgetColors.LOSS;
        } else if (parsedValue == 0) {
            colour = WidgetColors.SAME;
        } else {
            colour = WidgetColors.GAIN;
        }
        return colour;
    }

    public void clear() {
        if (widgetSize == 1 || widgetSize == 3) {
            for (int i = 1; i < this.getRowCount() + 1; i++) {
                for (int j = 1; j < 6; j++) {
                    remoteViews.setTextViewText(getStockViewId(i, j), "");
                }
            }
        } else {
            for (int i = 1; i < this.getRowCount() + 1; i++) {
                for (int j = 1; j < 4; j++) {
                    remoteViews.setTextViewText(getStockViewId(i, j), "");
                }
            }
        }
    }

    public int getRowCount() {
        return arraySize;
    }

    private void displayRows() {
        for (int i = 0; i < 11; i++) {
            int viewId = ReflectionTools.getField("line" + i);
            if (viewId > 0)
                remoteViews.setViewVisibility(ReflectionTools.getField("line" + i), View.GONE);
        }
        for (int i = 1; i < arraySize + 1; i++)
            remoteViews.setViewVisibility(ReflectionTools.getField("line" + i), View.VISIBLE);
    }

    public RemoteViews getRemoteViews() {
        return remoteViews;
    }

    public int getNextView(UpdateType updateMode) {
        int currentView = this.widget.getPreviousView();
        if (updateMode == UpdateType.VIEW_CHANGE) {
            currentView += 1;
            currentView = currentView % 10;
        }

        // Skip views as relevant
        int count = 0;
        while (!this.getEnabledViews().get(ViewType.values()[currentView])) {
            count += 1;
            currentView += 1;
            currentView = currentView % 10;
            // Percent change as default view if none selected
            if (count > 10) {
                currentView = 0;
                break;
            }
        }
        widget.setView(currentView);
        return currentView;
    }

    public void update(List<String> symbols, HashMap<String, StockQuote> quotes,
                       HashMap<String, PortfolioStock> stocks, UpdateType updateMode, String quotesTimeStamp) {
        int widgetDisplay = this.getNextView(updateMode);
        this.clear();
        int line_no = 0;
        for (String symbol : symbols) {
            // Skip blank symbols
            if (symbol.equals("")) {
                continue;
            }
            // Get the info for this quote
            StockQuote quoteInfo;
            quoteInfo = quotes.get(symbol);
            line_no++;
            HashMap<String, Object> formattedRow = getFormattedRow(symbol, quoteInfo, stocks,
                    ViewType.values()[widgetDisplay], storage, widgetSize);
            // Values
            remoteViews.setTextViewText(getStockViewId(line_no, 1), makeBold(storage, (String) formattedRow.get("COL0_VALUE")));
            remoteViews.setTextViewText(getStockViewId(line_no, 2), makeBold(storage, (String) formattedRow.get("COL1_VALUE")));
            remoteViews.setTextViewText(getStockViewId(line_no, 3), makeBold(storage, (String) formattedRow.get("COL2_VALUE")));
            // Add the other values if we have a wide widget
            if (widgetSize == 1 || widgetSize == 3) {
                remoteViews.setTextViewText(getStockViewId(line_no, 4), makeBold(storage, (String) formattedRow.get("COL3_VALUE")));
                remoteViews.setTextViewText(getStockViewId(line_no, 5), makeBold(storage, (String) formattedRow.get("COL4_VALUE")));
            }
            // Colours
            remoteViews.setTextColor(getStockViewId(line_no, 1), (Integer) formattedRow.get("COL0_COLOUR"));
            if (!storage.getBoolean("colours_on_prices", false)) {
                // Narrow widget
                if (widgetSize == 0 || widgetSize == 2) {
                    remoteViews.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 3), (Integer) formattedRow.get("COL2_COLOUR"));
                }
                // Wide widget
                else {
                    remoteViews.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 3), (Integer) formattedRow.get("COL2_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 4), (Integer) formattedRow.get("COL3_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 5), (Integer) formattedRow.get("COL4_COLOUR"));
                }
            } else {
                // Narrow widget
                if (widgetSize == 0 || widgetSize == 2) {
                    remoteViews.setTextColor(getStockViewId(line_no, 3), (Integer) formattedRow.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL2_COLOUR"));
                }
                // Wide widget
                else {
                    remoteViews.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL4_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 4), (Integer) formattedRow.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(line_no, 5), (Integer) formattedRow.get("COL1_COLOUR"));
                }
            }
        }
        // Set footer display
        String updated_display = storage.getString("updated_display", "visible");
        switch (updated_display) {
            case "remove":
                remoteViews.setViewVisibility(R.id.text_footer, View.GONE);
                break;
            case "invisible":
                remoteViews.setViewVisibility(R.id.text_footer, View.INVISIBLE);
                break;
            default:
                remoteViews.setViewVisibility(R.id.text_footer, View.VISIBLE);
                // Set footer text and colour
                String updated_colour = storage.getString("updated_colour", "light");
                int footer_colour = Color.parseColor("#555555");
                if (updated_colour.equals("light")) {
                    footer_colour = Color.GRAY;
                } else if (updated_colour.equals("yellow")) {
                    footer_colour = Color.parseColor("#cccc77");
                }
                // Show short time if specified in prefs
                if (!storage.getBoolean("short_time", false)) {
                    // Get current day and month
                    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
                    String current_date = formatter.format(new Date()).toUpperCase();
                    // Set default time as today
                    if (quotesTimeStamp.equals("")) {
                        quotesTimeStamp = "NO DATE SET";
                    }
                    // Check if we should use yesterdays date or today's time
                    String[] split_time = quotesTimeStamp.split(" ");
                    if ((split_time[0] + " " + split_time[1]).equals(current_date)) {
                        quotesTimeStamp = split_time[2];
                    } else {
                        quotesTimeStamp = (split_time[0] + " " + split_time[1]);
                    }
                }
                // Set time stamp
                remoteViews.setTextViewText(R.id.text5, makeBold(storage, quotesTimeStamp));
                remoteViews.setTextColor(R.id.text5, footer_colour);
                // Set the widget view text in the footer
                String currentViewText = "";
                if (widgetSize == 0 || widgetSize == 2) {
                    switch (ViewType.values()[widgetDisplay]) {
                        case VIEW_DAILY_PERCENT:
                            currentViewText = "";
                            break;
                        case VIEW_DAILY_CHANGE:
                            currentViewText = "DA";
                            break;
                        case VIEW_PORTFOLIO_PERCENT:
                            currentViewText = "PF T%";
                            break;
                        case VIEW_PORTFOLIO_CHANGE:
                            currentViewText = "PF TA";
                            break;
                        case VIEW_PORTFOLIO_PERCENT_AER:
                            currentViewText = "PF AER";
                            break;
                        case VIEW_PL_DAILY_PERCENT:
                            currentViewText = "P/L D%";
                            break;
                        case VIEW_PL_DAILY_CHANGE:
                            currentViewText = "P/L DA";
                            break;
                        case VIEW_PL_PERCENT:
                            currentViewText = "P/L T%";
                            break;
                        case VIEW_PL_CHANGE:
                            currentViewText = "P/L TA";
                            break;
                        case VIEW_PL_PERCENT_AER:
                            currentViewText = "P/L AER";
                            break;
                    }
                } else {
                    switch (ViewType.values()[widgetDisplay]) {
                        case VIEW_DAILY_PERCENT:
                            currentViewText = "";
                            break;
                        case VIEW_DAILY_CHANGE:
                            currentViewText = "";
                            break;
                        case VIEW_PORTFOLIO_PERCENT:
                            currentViewText = "PF T";
                            break;
                        case VIEW_PORTFOLIO_CHANGE:
                            currentViewText = "PF T";
                            break;
                        case VIEW_PORTFOLIO_PERCENT_AER:
                            currentViewText = "PF AER";
                            break;
                        case VIEW_PL_DAILY_PERCENT:
                            currentViewText = "P/L D";
                            break;
                        case VIEW_PL_DAILY_CHANGE:
                            currentViewText = "P/L D";
                            break;
                        case VIEW_PL_PERCENT:
                            currentViewText = "P/L T";
                            break;
                        case VIEW_PL_CHANGE:
                            currentViewText = "P/L T";
                            break;
                        case VIEW_PL_PERCENT_AER:
                            currentViewText = "P/L AER";
                            break;
                    }
                }
                // Update the view name and view name separator
                remoteViews.setTextViewText(R.id.text6, makeBold(storage, currentViewText));
                remoteViews.setTextColor(R.id.text6, footer_colour);
                break;
        }
    }

    public boolean canChangeView(UpdateType updateMode) {
        HashMap<ViewType, Boolean> enabledViews = this.getEnabledViews();
        boolean hasMultipleDefaultViews = enabledViews.get(ViewType.VIEW_DAILY_PERCENT)
                && enabledViews.get(ViewType.VIEW_DAILY_CHANGE);

        return !(updateMode == UpdateType.VIEW_CHANGE
                && !this.hasPortfolioData
                && !hasMultipleDefaultViews);

    }
}
