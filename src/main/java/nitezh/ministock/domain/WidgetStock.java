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
import java.util.Date;
import java.util.Locale;

import nitezh.ministock.utils.NumberTools;


public class WidgetStock {
    private final String symbol;
    private final String price;
    private final String name;
    private final String dailyChange;
    private final String dailyPercent;
    private final String volume;

    private String customName;
    private String totalChange;
    private String totalPercent;
    private String totalChangeAer;
    private String totalPercentAer;
    private String plHolding;
    private String plDailyChange;
    private String plTotalChange;
    private String plTotalChangeAer;
    private Boolean limitHighTriggered;
    private Boolean limitLowTriggered;

    public WidgetStock(StockQuote quote, PortfolioStock portfolioStock) {

        price = quote.getPrice();

        symbol = quote.getSymbol();
        name = quote.getName();
        if (portfolioStock != null && !portfolioStock.getCustomName().equals("")) {
            customName = portfolioStock.getCustomName();
        }

        dailyChange = quote.getChange();
        dailyPercent = quote.getPercent();
        volume = NumberTools.getNormalisedVolume(quote.getVolume());

        Double elapsedYears = null;
        Double priceValue = NumberTools.tryParseDouble(quote.getPrice());
        Double dailyChangeValue = NumberTools.tryParseDouble(quote.getChange());

        Double buyPriceValue = null;
        Double quantityValue = null;
        Double limitHighValue = null;
        Double limitLowValue = null;

        if (portfolioStock != null) {
            buyPriceValue = NumberTools.tryParseDouble(portfolioStock.getPrice());
            quantityValue = NumberTools.tryParseDouble(portfolioStock.getQuantity());
            limitHighValue = NumberTools.tryParseDouble(portfolioStock.getHighLimit());
            limitLowValue = NumberTools.tryParseDouble(portfolioStock.getLowLimit());
        }

        Double priceChangeValue = null;
        try {
            priceChangeValue = priceValue - buyPriceValue;
        } catch (Exception ignored) {
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(portfolioStock.getDate());
            double elapsed = (new Date().getTime() - date.getTime()) / 1000;
            elapsedYears = elapsed / 31536000;
        } catch (Exception ignored) {
        }

        if (priceChangeValue != null) {
            totalChange = NumberTools.getTrimmedDouble(priceChangeValue, 5);
        }

        if (priceChangeValue != null) {
            totalPercent = String.format(Locale.getDefault(), "%.1f", 100 * (priceChangeValue / buyPriceValue)) + "%";
        }

        if (priceChangeValue != null && elapsedYears != null) {
            totalChangeAer = NumberTools.getTrimmedDouble(priceChangeValue / elapsedYears, 5);
        }

        if (priceChangeValue != null && elapsedYears != null) {
            totalPercentAer = String.format(Locale.getDefault(), "%.1f", (100 * (priceChangeValue / buyPriceValue)) / elapsedYears) + "%";
        }

        if (quantityValue != null) {
            plHolding = String.format(Locale.getDefault(), "%.0f", priceValue * quantityValue);
        }

        if (dailyChangeValue != null && quantityValue != null) {
            plDailyChange = String.format(Locale.getDefault(), "%.0f", dailyChangeValue * quantityValue);
        }

        if (priceChangeValue != null && quantityValue != null) {
            plTotalChange = String.format(Locale.getDefault(), "%.0f", priceChangeValue * quantityValue);
        }

        if (priceChangeValue != null && quantityValue != null && elapsedYears != null) {
            plTotalChangeAer = String.format(Locale.getDefault(), "%.0f", (priceChangeValue * quantityValue) / elapsedYears);
        }

        if (limitHighValue != null) {
            limitHighTriggered = priceValue > limitHighValue;
        }

        if (limitLowValue != null) {
            limitLowTriggered = priceValue < limitLowValue;
        }
    }

    public String getPrice() {
        return price;
    }

    public String getShortName() {
        if (customName != null) {
            return customName;
        }

        return symbol;
    }

    public String getLongName() {
        if (customName != null) {
            return customName;
        }

        return name;
    }

    public String getDailyChange() {
        return dailyChange;
    }

    public String getDailyPercent() {
        return dailyPercent;
    }

    public String getVolume() {
        return volume;
    }

    public String getTotalChange() {
        return totalChange;
    }

    public String getTotalPercent() {
        return totalPercent;
    }

    public String getTotalChangeAer() {
        return totalChangeAer;
    }

    public String getTotalPercentAer() {
        return totalPercentAer;
    }

    public String getPlHolding() {
        return plHolding;
    }

    public String getPlDailyChange() {
        return plDailyChange;
    }

    public String getPlTotalChange() {
        return plTotalChange;
    }

    public String getPlTotalChangeAer() {
        return plTotalChangeAer;
    }

    public Boolean getLimitHighTriggered() {
        return limitHighTriggered != null ? limitHighTriggered : false;
    }

    public Boolean getLimitLowTriggered() {
        return limitLowTriggered != null ? limitLowTriggered : false;
    }
}
