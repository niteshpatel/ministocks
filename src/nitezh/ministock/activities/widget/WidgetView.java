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

import nitezh.ministock.PreferenceCache;
import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.WidgetProvider;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStock;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.StockQuote;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.domain.WidgetStock;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.ReflectionTools;

import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType;
import static nitezh.ministock.activities.widget.WidgetProviderBase.ViewType;

public class WidgetView {

    private final RemoteViews remoteViews;
    private final Widget widget;
    private final boolean hasPortfolioData;
    private final List<String> symbols;
    private final HashMap<String, PortfolioStock> portfolioStocks;
    private final HashMap<String, StockQuote> quotes;
    private final UpdateType updateMode;
    private final String quotesTimeStamp;
    private final Context context;
    private HashMap<ViewType, Boolean> enabledViews;

    public WidgetView(Context context, int appWidgetId, UpdateType updateMode,
                      HashMap<String, StockQuote> quotes, String quotesTimeStamp) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);

        this.context = context;
        this.widget = widgetRepository.getWidget(appWidgetId);
        this.quotes = quotes;
        this.quotesTimeStamp = quotesTimeStamp;
        this.updateMode = updateMode;
        this.symbols = widget.getSymbols();

        this.portfolioStocks = new PortfolioStockRepository(PreferenceStorage.getInstance(context),
                new PreferenceCache(context), widgetRepository).getStocksForSymbols(symbols);
        this.hasPortfolioData = portfolioStocks.isEmpty();

        this.remoteViews = this.getBlankRemoteViews(this.widget, context.getPackageName());
        this.enabledViews = this.calculateEnabledViews(this.widget);
    }

    private static int getStockViewId(int line, int col) {
        return ReflectionTools.getField("text" + line + col);
    }

    private RemoteViews getBlankRemoteViews(Widget widget, String packageName) {
        String backgroundStyle = widget.getBackgroundStyle();
        boolean useLargeFont = widget.useLargeFont();

        RemoteViews views;
        if (widget.getSize() == 1) {
            if (useLargeFont) {
                views = new RemoteViews(packageName, R.layout.widget_1x4_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x4);
            }
        } else if (widget.getSize() == 2) {
            if (useLargeFont) {
                views = new RemoteViews(packageName, R.layout.widget_2x2_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x2);
            }
        } else if (widget.getSize() == 3) {
            if (useLargeFont) {
                views = new RemoteViews(packageName, R.layout.widget_2x4_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x4);
            }
        } else {
            if (useLargeFont) {
                views = new RemoteViews(packageName, R.layout.widget_1x2_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x2);
            }
        }
        views.setImageViewResource(R.id.widget_bg,
                getImageViewSrcId(backgroundStyle, useLargeFont));
        this.hideUnusedRows(views, widget.getSymbolCount());
        return views;
    }

    private int getImageViewSrcId(String backgroundStyle, Boolean useLargeFont) {
        Integer imageViewSrcId;
        switch (backgroundStyle) {
            case "transparent":
                if (useLargeFont) {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68;
                }
                break;
            case "none":
                imageViewSrcId = R.drawable.blank;
                break;
            default:
                if (useLargeFont) {
                    imageViewSrcId = R.drawable.ministock_bg_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg;
                }
                break;
        }
        return imageViewSrcId;
    }

    // Global formatter so we can perform global text formatting in one place
    private SpannableString makeBold(String s) {
        SpannableString span = new SpannableString(s);
        if (this.widget.getTextStyle()) {
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        } else {
            span.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
        }
        return span;
    }

    public void setOnClickPendingIntents() {
        Intent leftTouchIntent = new Intent(this.context, WidgetProvider.class);
        leftTouchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widget.getId());
        leftTouchIntent.setAction("LEFT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_left,
                PendingIntent.getBroadcast(this.context, this.widget.getId(), leftTouchIntent, 0));

        Intent rightTouchIntent = new Intent(this.context, WidgetProvider.class);
        rightTouchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, this.widget.getId());
        rightTouchIntent.setAction("RIGHT");
        this.remoteViews.setOnClickPendingIntent(R.id.widget_right,
                PendingIntent.getBroadcast(this.context, this.widget.getId(), rightTouchIntent, 0));
    }

    public HashMap<WidgetProviderBase.ViewType, Boolean> getEnabledViews() {
        return this.enabledViews;
    }

    public HashMap<ViewType, Boolean> calculateEnabledViews(Widget widget) {
        HashMap<WidgetProviderBase.ViewType, Boolean> enabledViews = new HashMap<>();
        enabledViews.put(ViewType.VIEW_DAILY_PERCENT, widget.hasDailyPercentView());
        enabledViews.put(ViewType.VIEW_DAILY_CHANGE, widget.hasDailyChangeView());
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT, widget.hasTotalPercentView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_CHANGE, widget.hasTotalChangeView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PORTFOLIO_PERCENT_AER, widget.hasTotalChangeAerView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_DAILY_PERCENT, widget.hasDailyPlPercentView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_DAILY_CHANGE, widget.hasDailyPlChangeView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_PERCENT, widget.hasTotalPlPercentView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_CHANGE, widget.hasTotalPlChangeView() && this.hasPortfolioData);
        enabledViews.put(ViewType.VIEW_PL_PERCENT_AER, widget.hasTotalPlPercentAerView() && this.hasPortfolioData);
        return enabledViews;
    }

    private HashMap<String, Object> getRowInfo(String symbol, ViewType widgetView) {
        HashMap<String, Object> rowInfo = new HashMap<>();

        // Initialise columns
        rowInfo.put("COL0_VALUE", symbol);
        rowInfo.put("COL0_COLOUR", Color.WHITE);
        rowInfo.put("COL1_VALUE", "");
        rowInfo.put("COL1_COLOUR", Color.WHITE);
        rowInfo.put("COL2_VALUE", "");
        rowInfo.put("COL2_COLOUR", Color.WHITE);
        rowInfo.put("COL3_VALUE", "");
        rowInfo.put("COL3_COLOUR", Color.WHITE);
        rowInfo.put("COL4_VALUE", "");
        rowInfo.put("COL4_COLOUR", Color.WHITE);

        // Set the stock symbol, and strip off exchange suffix
        // if requested in the preferences
        if (this.widget.getHideSuffix()) {
            int dotIndex = symbol.indexOf(".");
            if (dotIndex > -1) {
                rowInfo.put("COL0_VALUE", symbol.substring(0, dotIndex));
            }
        }

        // If there is no quote info return immediately
        StockQuote quote = this.quotes.get(symbol);
        if (quote == null || quote.getPrice() == null || quote.getPercent() == null) {
            if (this.widget.isNarrow()) {
                rowInfo.put("COL1_VALUE", "no");
                rowInfo.put("COL1_COLOUR", Color.GRAY);
                rowInfo.put("COL2_VALUE", "data");
                rowInfo.put("COL2_COLOUR", Color.GRAY);
            } else {
                rowInfo.put("COL3_VALUE", "no");
                rowInfo.put("COL3_COLOUR", Color.GRAY);
                rowInfo.put("COL4_VALUE", "data");
                rowInfo.put("COL4_COLOUR", Color.GRAY);
            }
            return rowInfo;
        }

        // Set default values
        PortfolioStock portfolioStock = this.portfolioStocks.get(symbol);
        WidgetStock widgetStock = new WidgetStock(quote, portfolioStock);
        rowInfo.put("COL1_VALUE", widgetStock.getPrice());
        if (!widget.isNarrow()) {
            rowInfo.put("COL0_VALUE", widgetStock.getDisplayName());
            rowInfo.put("COL2_VALUE", widgetStock.getVolume());
            rowInfo.put("COL2_COLOUR", WidgetColors.VOLUME);
            rowInfo.put("COL3_VALUE", widgetStock.getDailyChange());
            rowInfo.put("COL3_COLOUR", WidgetColors.NA);
            rowInfo.put("COL4_VALUE", widgetStock.getDailyPercent());
            rowInfo.put("COL4_COLOUR", WidgetColors.NA);
        } else {
            rowInfo.put("COL2_VALUE", widgetStock.getDailyPercent());
            rowInfo.put("COL2_COLOUR", WidgetColors.NA);
        }

        Boolean plView = false;
        Boolean plChange = false;
        String column1 = null;
        String column2 = null;
        String column3 = null;
        String column4 = null;

        if (widget.isNarrow()) {
            switch (widgetView) {
                case VIEW_DAILY_PERCENT:
                    column2 = widgetStock.getDailyPercent();
                    break;

                case VIEW_DAILY_CHANGE:
                    column2 = widgetStock.getDailyChange();
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    column2 = widgetStock.getTotalPercent();
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    column2 = widgetStock.getTotalChange();
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    column2 = widgetStock.getTotalChangeAer();
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    plView = true;
                    column1 = widgetStock.getPlHolding();
                    column2 = widgetStock.getDailyPercent();
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column2 = widgetStock.getPlDailyChange();
                    break;

                case VIEW_PL_PERCENT:
                    plView = true;
                    column1 = widgetStock.getPlHolding();
                    column2 = widgetStock.getTotalPercent();
                    break;

                case VIEW_PL_CHANGE:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column2 = widgetStock.getTotalChange();
                    break;

                case VIEW_PL_PERCENT_AER:
                    plView = true;
                    column1 = widgetStock.getPlHolding();
                    column2 = widgetStock.getTotalPercentAer();
                    break;
            }
        } else {
            switch (widgetView) {
                case VIEW_DAILY_PERCENT:
                    column3 = widgetStock.getDailyChange();
                    column4 = widgetStock.getDailyPercent();
                    break;

                case VIEW_DAILY_CHANGE:
                    column3 = widgetStock.getDailyChange();
                    column4 = widgetStock.getDailyPercent();
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    column3 = widgetStock.getTotalChange();
                    column4 = widgetStock.getTotalPercent();
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    column3 = widgetStock.getTotalChange();
                    column4 = widgetStock.getTotalPercent();
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    column3 = widgetStock.getTotalChangeAer();
                    column4 = widgetStock.getTotalPercentAer();
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column3 = widgetStock.getPlDailyChange();
                    column4 = widgetStock.getDailyPercent();
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column3 = widgetStock.getPlDailyChange();
                    column4 = widgetStock.getDailyPercent();
                    break;

                case VIEW_PL_PERCENT:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column3 = widgetStock.getPlTotalChange();
                    column4 = widgetStock.getTotalPercent();
                    break;

                case VIEW_PL_CHANGE:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column3 = widgetStock.getPlTotalChange();
                    column4 = widgetStock.getTotalPercent();
                    break;

                case VIEW_PL_PERCENT_AER:
                    plView = true;
                    plChange = true;
                    column1 = widgetStock.getPlHolding();
                    column3 = widgetStock.getPlTotalChangeAer();
                    column4 = widgetStock.getTotalPercentAer();
                    break;
            }
        }

        // Set the price column colour if we have hit an alert
        // (this is only relevant for non-profit and loss views)
        if (widgetStock.getLimitHighTriggered() && !plView) {
            rowInfo.put("COL1_COLOUR", WidgetColors.HIGH_ALERT);
        }
        if (widgetStock.getLimitLowTriggered() && !plView) {
            rowInfo.put("COL1_COLOUR", WidgetColors.LOW_ALERT);
        }

        // Set the price column to the holding value and colour
        // the column blue if we have no holdings
        if (plView && column1 == null) {
            rowInfo.put("COL1_COLOUR", WidgetColors.NA);
        }

        // Add currency symbol if we have a holding
        if (column1 != null) {
            rowInfo.put("COL1_VALUE", CurrencyTools.addCurrencyToSymbol(column1, symbol));
        }

        // Set the value and colour for the change values
        if (!widget.isNarrow()) {
            if (column3 != null) {
                if (plChange) {
                    rowInfo.put("COL3_VALUE", CurrencyTools.addCurrencyToSymbol(column3, symbol));
                } else {
                    rowInfo.put("COL3_VALUE", column3);
                }
                rowInfo.put("COL3_COLOUR", getColourForChange(column3));
            }
            if (column4 != null) {
                rowInfo.put("COL4_VALUE", column4);
                rowInfo.put("COL4_COLOUR", getColourForChange(column4));
            }
        } else {
            if (column2 != null) {
                if (plChange) {
                    rowInfo.put("COL2_VALUE", CurrencyTools.addCurrencyToSymbol(column2, symbol));
                } else {
                    rowInfo.put("COL2_VALUE", column2);
                }
                rowInfo.put("COL2_COLOUR", getColourForChange(column2));
            }
        }
        return rowInfo;
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
        int columnCount = (!widget.isNarrow()) ? 6 : 4;
        for (int i = 1; i < this.widget.getSymbolCount() + 1; i++) {
            for (int j = 1; j < columnCount; j++) {
                this.remoteViews.setTextViewText(getStockViewId(i, j), "");
            }
        }
    }

    private void hideUnusedRows(RemoteViews views, int count) {
        for (int i = 0; i < 11; i++) {
            int viewId = ReflectionTools.getField("line" + i);
            if (viewId > 0) {
                views.setViewVisibility(ReflectionTools.getField("line" + i), View.GONE);
            }
        }
        for (int i = 1; i < count + 1; i++) {
            views.setViewVisibility(ReflectionTools.getField("line" + i), View.VISIBLE);
        }
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

    public void applyPendingChanges() {
        int widgetDisplay = this.getNextView(this.updateMode);
        this.clear();

        int lineNo = 0;
        for (String symbol : this.symbols) {
            if (symbol.equals("")) {
                continue;
            }

            // Get the info for this quote
            lineNo++;
            HashMap<String, Object> rowInfo = getRowInfo(symbol, ViewType.values()[widgetDisplay]);

            // Values
            remoteViews.setTextViewText(getStockViewId(lineNo, 1), makeBold((String) rowInfo.get("COL0_VALUE")));
            remoteViews.setTextViewText(getStockViewId(lineNo, 2), makeBold((String) rowInfo.get("COL1_VALUE")));
            remoteViews.setTextViewText(getStockViewId(lineNo, 3), makeBold((String) rowInfo.get("COL2_VALUE")));

            // Add the other values if we have a wide widget
            if (!widget.isNarrow()) {
                remoteViews.setTextViewText(getStockViewId(lineNo, 4), makeBold((String) rowInfo.get("COL3_VALUE")));
                remoteViews.setTextViewText(getStockViewId(lineNo, 5), makeBold((String) rowInfo.get("COL4_VALUE")));
            }

            // Colours
            remoteViews.setTextColor(getStockViewId(lineNo, 1), (Integer) rowInfo.get("COL0_COLOUR"));
            if (!this.widget.getColorsOnPrices()) {
                if (widget.isNarrow()) {
                    remoteViews.setTextColor(getStockViewId(lineNo, 2), (Integer) rowInfo.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 3), (Integer) rowInfo.get("COL2_COLOUR"));
                } else {
                    remoteViews.setTextColor(getStockViewId(lineNo, 2), (Integer) rowInfo.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 3), (Integer) rowInfo.get("COL2_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 4), (Integer) rowInfo.get("COL3_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 5), (Integer) rowInfo.get("COL4_COLOUR"));
                }
            } else {

                // Narrow widget
                if (widget.isNarrow()) {
                    remoteViews.setTextColor(getStockViewId(lineNo, 3), (Integer) rowInfo.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 2), (Integer) rowInfo.get("COL2_COLOUR"));
                }

                // Wide widget
                else {
                    remoteViews.setTextColor(getStockViewId(lineNo, 2), (Integer) rowInfo.get("COL4_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 4), (Integer) rowInfo.get("COL1_COLOUR"));
                    remoteViews.setTextColor(getStockViewId(lineNo, 5), (Integer) rowInfo.get("COL1_COLOUR"));
                }
            }
        }

        // Set footer display
        switch (this.widget.getFooterVisibility()) {
            case "remove":
                remoteViews.setViewVisibility(R.id.text_footer, View.GONE);
                break;

            case "invisible":
                remoteViews.setViewVisibility(R.id.text_footer, View.INVISIBLE);
                break;

            default:
                remoteViews.setViewVisibility(R.id.text_footer, View.VISIBLE);

                // Set time stamp
                int footerColor = this.getFooterColor();
                remoteViews.setTextViewText(R.id.text5, makeBold(this.getTimeStamp()));
                remoteViews.setTextColor(R.id.text5, footerColor);

                // Set the view label
                remoteViews.setTextViewText(R.id.text6, makeBold(this.getLabel(widgetDisplay)));
                remoteViews.setTextColor(R.id.text6, footerColor);
                break;
        }
    }

    public int getFooterColor() {
        String colorType = this.widget.getFooterColor();
        int color = Color.parseColor("#555555");
        if (colorType.equals("light")) {
            color = Color.GRAY;
        } else if (colorType.equals("yellow")) {
            color = Color.parseColor("#cccc77");
        }

        return color;
    }

    public String getLabel(int widgetDisplay) {
        // Set the widget view text in the footer
        String label = "";
        if (widget.isNarrow()) {
            switch (ViewType.values()[widgetDisplay]) {
                case VIEW_DAILY_PERCENT:
                    label = "";
                    break;

                case VIEW_DAILY_CHANGE:
                    label = "DA";
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    label = "PF T%";
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    label = "PF TA";
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    label = "PF AER";
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    label = "P/L D%";
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    label = "P/L DA";
                    break;

                case VIEW_PL_PERCENT:
                    label = "P/L T%";
                    break;

                case VIEW_PL_CHANGE:
                    label = "P/L TA";
                    break;

                case VIEW_PL_PERCENT_AER:
                    label = "P/L AER";
                    break;
            }
        } else {
            switch (ViewType.values()[widgetDisplay]) {
                case VIEW_DAILY_PERCENT:
                    label = "";
                    break;

                case VIEW_DAILY_CHANGE:
                    label = "";
                    break;

                case VIEW_PORTFOLIO_PERCENT:
                    label = "PF T";
                    break;

                case VIEW_PORTFOLIO_CHANGE:
                    label = "PF T";
                    break;

                case VIEW_PORTFOLIO_PERCENT_AER:
                    label = "PF AER";
                    break;

                case VIEW_PL_DAILY_PERCENT:
                    label = "P/L D";
                    break;

                case VIEW_PL_DAILY_CHANGE:
                    label = "P/L D";
                    break;

                case VIEW_PL_PERCENT:
                    label = "P/L T";
                    break;

                case VIEW_PL_CHANGE:
                    label = "P/L T";
                    break;

                case VIEW_PL_PERCENT_AER:
                    label = "P/L AER";
                    break;
            }
        }

        return label;
    }

    public String getTimeStamp() {
        String timeStamp = this.quotesTimeStamp;
        if (!this.widget.showShortTime()) {
            String date = new SimpleDateFormat("dd MMM").format(new Date()).toUpperCase();

            // Check if we should use yesterdays date or today's time
            String[] parts = timeStamp.split(" ");
            String fullDate = parts[0] + " " + parts[1];
            if (fullDate.equals(date)) {
                timeStamp = parts[2];
            } else {
                timeStamp = fullDate;
            }
        }

        return timeStamp;
    }

    public boolean canChangeView() {
        HashMap<ViewType, Boolean> enabledViews = this.getEnabledViews();
        boolean hasMultipleDefaultViews = enabledViews.get(ViewType.VIEW_DAILY_PERCENT)
                && enabledViews.get(ViewType.VIEW_DAILY_CHANGE);

        return !(this.updateMode == UpdateType.VIEW_CHANGE
                && !this.hasPortfolioData
                && !hasMultipleDefaultViews);
    }

    public boolean hasPendingChanges() {
        return (!this.quotes.isEmpty() || this.canChangeView());
    }
}
