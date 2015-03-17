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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nitezh.ministock.Cache;
import nitezh.ministock.CustomAlarmManager;
import nitezh.ministock.PreferenceCache;
import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.UserData;
import nitezh.ministock.WidgetProvider;
import nitezh.ministock.activities.PreferencesActivity;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStock;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.StockQuoteRepository;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.DateTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.ReflectionTools;


public class WidgetProviderBase extends android.appwidget.AppWidgetProvider {

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

    private static HashMap<String, Object> getFormattedRow(String symbol, StockQuote quoteInfo,
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
            rowItems.put("COL2_VALUE", formatVolume(volume));
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

    // Format the volume to use K, M, B, or T suffix
    private static String formatVolume(String value) {
        Double volume;
        try {
            volume = NumberTools.parseDouble(value);
            if (volume > 999999999999D)
                value = String.format("%.0fT", volume / 1000000000000D);
            else if (volume > 999999999D)
                value = String.format("%.0fB", volume / 1000000000D);
            else if (volume > 999999D)
                value = String.format("%.0fM", volume / 1000000D);
            else if (volume > 999D)
                value = String.format("%.0fK", volume / 1000D);
            else
                value = String.format("%.0f", volume);
        } catch (Exception ignored) {
        }
        return value;
    }

    private static int getColourForChange(String value) {
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

    private static void setOnClickPendingIntents(Context context, int appWidgetId, RemoteViews views) {
        // Set an onClick handler on the 'widget_left' layout
        Intent left_intent = new Intent(context, WidgetProvider.class);
        left_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        left_intent.setAction("LEFT");
        views.setOnClickPendingIntent(R.id.widget_left, PendingIntent.getBroadcast(context, appWidgetId, left_intent, 0));

        // Set an onClick handler on the 'widget_right' layout
        Intent right_intent = new Intent(context, WidgetProvider.class);
        right_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        right_intent.setAction("RIGHT");
        views.setOnClickPendingIntent(R.id.widget_right, PendingIntent.getBroadcast(context, appWidgetId, right_intent, 0));
    }

    private static HashMap<ViewType, Boolean> getEnabledViews(Storage prefs, int widgetSize, boolean noPortfolio) {
        // Get widget view prefs
        boolean dailyChangePc = prefs.getBoolean("show_percent_change", false);
        boolean dailyChange = prefs.getBoolean("show_absolute_change", false);
        boolean totalChangePc = prefs.getBoolean("show_portfolio_change", false);
        boolean totalChange = prefs.getBoolean("show_portfolio_abs", false);
        boolean totalChangeAer = prefs.getBoolean("show_portfolio_aer", false);
        boolean plDailyChangePc = prefs.getBoolean("show_profit_daily_change", false);
        boolean plDailyChange = prefs.getBoolean("show_profit_daily_abs", false);
        boolean plTotalChangePc = prefs.getBoolean("show_profit_change", false);
        boolean plTotalChange = prefs.getBoolean("show_profit_abs", false);
        boolean plTotalChangeAer = prefs.getBoolean("show_profit_aer", false);

        HashMap<ViewType, Boolean> enabledViews = new HashMap<>();
        enabledViews.put(ViewType.VIEW_DAILY_PERCENT, dailyChangePc && (widgetSize == 0 || widgetSize == 2));
        enabledViews.put(ViewType.VIEW_DAILY_CHANGE, dailyChange);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT, totalChangePc && (widgetSize == 0 || widgetSize == 2) && !noPortfolio);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_CHANGE, totalChange && !noPortfolio);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT_AER, totalChangeAer && !noPortfolio);
        enabledViews.put(ViewType.VIEW_PL_DAILY_PERCENT, plDailyChangePc && (widgetSize == 0 || widgetSize == 2) && !noPortfolio);
        enabledViews.put(ViewType.VIEW_PL_DAILY_CHANGE, plDailyChange && !noPortfolio);
        enabledViews.put(ViewType.VIEW_PL_PERCENT, plTotalChangePc && (widgetSize == 0 || widgetSize == 2) && !noPortfolio);
        enabledViews.put(ViewType.VIEW_PL_CHANGE, plTotalChange);
        enabledViews.put(ViewType.VIEW_PL_PERCENT_AER, plTotalChangeAer && !noPortfolio);
        return enabledViews;
    }

    private static void updateWidget(Context context, int appWidgetId, UpdateType updateMode,
                                     HashMap<String, StockQuote> quotes) {
        Storage appStorage = PreferenceStorage.getInstance(context);
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);

        // Get widget SharedPreferences
        Storage widgetStorage = widgetRepository.getWidget(appWidgetId).getStorage();

        // Choose between two widget sizes
        int widgetSize = widgetStorage.getInt("widgetSize", 0);

        // Get relevant RemoteViews
        WidgetView widgetView = new WidgetView(context.getPackageName(), widgetStorage);
        RemoteViews views = widgetView.getViews();

        // Hide any rows for smaller widgets
        boolean found = false;
        List<String> symbols = new ArrayList<>();
        String sym;
        for (int i = 0; i < widgetView.getRowCount(); i++) {
            sym = widgetStorage.getString("Stock" + (i + 1), "");
            symbols.add(sym);
            if (!sym.equals("")) {
                found = true;
            }
        }
        // Ensure widget is not empty
        if (!found) {
            symbols.add("^DJI");
        }
        // Retrieve portfolio stocks
        Storage storage = PreferenceStorage.getInstance(context);
        Cache cache = new PreferenceCache(context);
        PortfolioStockRepository portfolioStockRepository =
                new PortfolioStockRepository(storage, cache, widgetRepository);
        HashMap<String, PortfolioStock> stocks =
                portfolioStockRepository.getStocksForWidget(symbols);

        // Check if we have an empty portfolio
        boolean noPortfolio = stocks.isEmpty();
        HashMap<ViewType, Boolean> enabledViews =
                getEnabledViews(widgetStorage, widgetSize, noPortfolio);

        // Check if we have any portfolio-less views
        boolean defaultViews = enabledViews.get(ViewType.VIEW_DAILY_PERCENT)
                && enabledViews.get(ViewType.VIEW_DAILY_CHANGE);

        // Setup setOnClickPendingIntents
        setOnClickPendingIntents(context, appWidgetId, views);

        // Return here if we are requesting a portfolio view and we have
        // no portfolio items to report on
        if (updateMode == UpdateType.VIEW_CHANGE && noPortfolio && !defaultViews) {
            return;
        }

        // Retrieve the last shown widgetView from the prefs
        int widgetDisplay = widgetStorage.getInt("widgetView", 0);
        if (updateMode == UpdateType.VIEW_CHANGE) {
            widgetDisplay += 1;
            widgetDisplay = widgetDisplay % 10;
        }

        // Skip views as relevant
        int count = 0;
        while (!enabledViews.get(ViewType.values()[widgetDisplay])) {
            count += 1;
            widgetDisplay += 1;
            widgetDisplay = widgetDisplay % 10;
            // Percent change as default view if none selected
            if (count > 10) {
                widgetDisplay = 0;
                break;
            }
        }

        // Only bother to save if the widget view changed
        if (widgetDisplay != widgetStorage.getInt("widgetView", 0)) {
            widgetStorage.putInt("widgetView", widgetDisplay);
            widgetStorage.apply();
        }
        StockQuoteRepository dataSource = new StockQuoteRepository(appStorage, new PreferenceCache(context), widgetRepository);
        String quotesTimeStamp = dataSource.getTimeStamp();

        // If we have stock data then update the widget view
        if (!quotes.isEmpty()) {
            widgetView.clear();
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
                        ViewType.values()[widgetDisplay], widgetStorage, widgetSize);
                // Values
                views.setTextViewText(getStockViewId(line_no, 1), makeBold(widgetStorage, (String) formattedRow.get("COL0_VALUE")));
                views.setTextViewText(getStockViewId(line_no, 2), makeBold(widgetStorage, (String) formattedRow.get("COL1_VALUE")));
                views.setTextViewText(getStockViewId(line_no, 3), makeBold(widgetStorage, (String) formattedRow.get("COL2_VALUE")));
                // Add the other values if we have a wide widget
                if (widgetSize == 1 || widgetSize == 3) {
                    views.setTextViewText(getStockViewId(line_no, 4), makeBold(widgetStorage, (String) formattedRow.get("COL3_VALUE")));
                    views.setTextViewText(getStockViewId(line_no, 5), makeBold(widgetStorage, (String) formattedRow.get("COL4_VALUE")));
                }
                // Colours
                views.setTextColor(getStockViewId(line_no, 1), (Integer) formattedRow.get("COL0_COLOUR"));
                if (!widgetStorage.getBoolean("colours_on_prices", false)) {
                    // Narrow widget
                    if (widgetSize == 0 || widgetSize == 2) {
                        views.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL1_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 3), (Integer) formattedRow.get("COL2_COLOUR"));
                    }
                    // Wide widget
                    else {
                        views.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL1_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 3), (Integer) formattedRow.get("COL2_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 4), (Integer) formattedRow.get("COL3_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 5), (Integer) formattedRow.get("COL4_COLOUR"));
                    }
                } else {
                    // Narrow widget
                    if (widgetSize == 0 || widgetSize == 2) {
                        views.setTextColor(getStockViewId(line_no, 3), (Integer) formattedRow.get("COL1_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL2_COLOUR"));
                    }
                    // Wide widget
                    else {
                        views.setTextColor(getStockViewId(line_no, 2), (Integer) formattedRow.get("COL4_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 4), (Integer) formattedRow.get("COL1_COLOUR"));
                        views.setTextColor(getStockViewId(line_no, 5), (Integer) formattedRow.get("COL1_COLOUR"));
                    }
                }
            }
            // Set footer display
            String updated_display = widgetStorage.getString("updated_display", "visible");
            switch (updated_display) {
                case "remove":
                    views.setViewVisibility(R.id.text_footer, View.GONE);
                    break;
                case "invisible":
                    views.setViewVisibility(R.id.text_footer, View.INVISIBLE);
                    break;
                default:
                    views.setViewVisibility(R.id.text_footer, View.VISIBLE);
                    // Set footer text and colour
                    String updated_colour = widgetStorage.getString("updated_colour", "light");
                    int footer_colour = Color.parseColor("#555555");
                    if (updated_colour.equals("light")) {
                        footer_colour = Color.GRAY;
                    } else if (updated_colour.equals("yellow")) {
                        footer_colour = Color.parseColor("#cccc77");
                    }
                    // Show short time if specified in prefs
                    if (!widgetStorage.getBoolean("short_time", false)) {
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
                    views.setTextViewText(R.id.text5, makeBold(widgetStorage, quotesTimeStamp));
                    views.setTextColor(R.id.text5, footer_colour);
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
                    views.setTextViewText(R.id.text6, makeBold(widgetStorage, currentViewText));
                    views.setTextColor(R.id.text6, footer_colour);
                    break;
            }
            // Finally update the widget with the RemoteViews
            AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
        }
    }

    public static void updateWidgetAsync(Context context, int appWidgetId, UpdateType updateType) {
        new GetDataTask().execute(context, appWidgetId, updateType);
    }

    public static void updateWidgets(Context context, UpdateType updateType) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        for (int appWidgetId : widgetRepository.getIds()) {
            WidgetProviderBase.updateWidgetAsync(context, appWidgetId, updateType);
        }

        CustomAlarmManager alarmManager = new CustomAlarmManager(context);
        alarmManager.setUpdateTimestamp();
        alarmManager.update();
    }

    private static void doScheduledUpdates(Context context) {
        boolean doUpdates = true;
        Storage prefs = PreferenceStorage.getInstance(context);

        // Only update after start time
        String startTime = prefs.getString("update_start", null);
        if (startTime != null && !startTime.equals("")) {
            if (DateTools.compareToNow(DateTools.parseSimpleDate(startTime)) == 1) {
                doUpdates = false;
            }
        }

        // Only update before end time
        String endTime = prefs.getString("update_end", null);
        if (endTime != null && !endTime.equals("")) {
            if (DateTools.compareToNow(DateTools.parseSimpleDate(endTime)) == -1) {
                doUpdates = false;
            }
        }

        // Do not update on weekends
        Boolean update_weekend = prefs.getBoolean("update_weekend", false);
        if (!update_weekend) {
            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1 || dayOfWeek == 7) {
                doUpdates = false;
            }
        }

        // Perform updates as appropriate
        if (doUpdates) {
            updateWidgets(context, UpdateType.VIEW_UPDATE);
        } else {
            updateWidgets(context, UpdateType.VIEW_NO_UPDATE);
        }
    }

    public void handleTouch(Context context, int appWidgetId, String action) {
        if (action.equals("LEFT")) {
            startPreferencesActivity(context, appWidgetId);
        } else if (action.equals("RIGHT")) {
            updateWidgetAsync(context, appWidgetId, UpdateType.VIEW_CHANGE);
        }
    }

    public void startPreferencesActivity(Context context, int appWidgetId) {
        PreferencesActivity.mAppWidgetId = appWidgetId;
        Intent activity = new Intent(context, PreferencesActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);
    }

    @Override
    public void onReceive(@SuppressWarnings("NullableProblems") Context context,
                          @SuppressWarnings("NullableProblems") Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case CustomAlarmManager.ALARM_UPDATE:
                doScheduledUpdates(context);

                break;
            case "LEFT":
            case "RIGHT":
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    int appWidgetId = extras.getInt(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                    handleTouch(context, appWidgetId, action);
                }

                break;
            default:
                super.onReceive(context, intent);
                break;
        }
    }

    public void updateWidgetsFromCache(Context context) {
        for (int id : new AndroidWidgetRepository(context).getIds()) {
            updateWidgetAsync(context, id, UpdateType.VIEW_NO_UPDATE);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        new CustomAlarmManager(context).update();
        updateWidgetsFromCache(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        new CustomAlarmManager(context).update();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        for (int appWidgetId : appWidgetIds) {
            widgetRepository.delWidget(appWidgetId);
        }
        if (widgetRepository.isEmpty()) {
            new CustomAlarmManager(context).cancel();
        }

        UserData.cleanupPreferenceFiles(context);
    }

    public static enum ViewType {
        VIEW_DAILY_PERCENT,
        VIEW_DAILY_CHANGE,
        VIEW_PORTFOLIO_PERCENT,
        VIEW_PORTFOLIO_CHANGE,
        VIEW_PORTFOLIO_PERCENT_AER,
        VIEW_PL_DAILY_PERCENT,
        VIEW_PL_DAILY_CHANGE,
        VIEW_PL_PERCENT,
        VIEW_PL_CHANGE,
        VIEW_PL_PERCENT_AER
    }

    public static enum UpdateType {
        VIEW_UPDATE,
        VIEW_NO_UPDATE,
        VIEW_CHANGE
    }

    private static class GetDataTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            Context context = (Context) params[0];
            int appWidgetId = (Integer) params[1];
            UpdateType updateMode = (UpdateType) params[2];

            // Get widget SharedPreferences
            WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
            Storage storage = widgetRepository.getWidget(appWidgetId).getStorage();
            // Choose between two widget sizes
            int widgetSize = storage.getInt("widgetSize", 0);
            // Get the array size for widgets
            int arraySize = 0;
            if (widgetSize == 0 || widgetSize == 1)
                arraySize = 4;
            else if (widgetSize == 2 || widgetSize == 3)
                arraySize = 10;
            boolean found = false;
            String[] symbols = new String[arraySize];
            for (int i = 0; i < arraySize; i++) {
                symbols[i] = storage.getString("Stock" + (i + 1), "");
                if (!symbols[i].equals("")) {
                    found = true;
                }
            }
            // Ensure widget is not empty
            if (!found) {
                symbols[0] = "^DJI";
            }

            Storage appStorage = PreferenceStorage.getInstance(context);
            StockQuoteRepository dataSource = new StockQuoteRepository(appStorage,
                    new PreferenceCache(context), widgetRepository);
            HashMap<String, StockQuote> quotes = dataSource.getQuotes(
                    Arrays.asList(symbols), updateMode == UpdateType.VIEW_UPDATE);
            updateWidget(context, appWidgetId, updateMode, quotes);

            return null;
        }
    }
}
