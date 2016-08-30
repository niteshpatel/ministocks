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

import nitezh.ministock.utils.NumberTools;

import java.text.SimpleDateFormat;
import java.util.Date;


public class WidgetStock {

    String price;
    String displayName;
    String dailyChange;
    String dailyPercent;
    String volume;
    String totalChange;
    String totalPercent;
    String totalChangeAer;
    String totalPercentAer;
    String plHolding;
    String plDailyChange;
    String plTotalChange;
    String plTotalChangeAer;
    Boolean limitHighTriggered;
    Boolean limitLowTriggered;

    public WidgetStock(StockQuote quote, PortfolioStock portfolioStock) {

        price = quote.getPrice();

        displayName = quote.getName();
        if (portfolioStock != null && !portfolioStock.getCustomName().equals("")) {
            displayName = portfolioStock.getCustomName();
        }

        dailyChange = quote.getChange();
        dailyPercent = quote.getPercent();
        volume = NumberTools.getNormalisedVolume(quote.getVolume());

        Double elapsedYears = null;
        Double priceValue = NumberTools.parseDouble(quote.getPrice());
        Double dailyChangeValue = NumberTools.parseDouble(quote.getChange());

        Double buyPriceValue = null;
        Double quantityValue = null;
        Double limitHighValue = null;
        Double limitLowValue = null;

        if (portfolioStock != null) {
            buyPriceValue = NumberTools.parseDouble(portfolioStock.getPrice());
            quantityValue = NumberTools.parseDouble(portfolioStock.getQuantity());
            limitHighValue = NumberTools.parseDouble(portfolioStock.getHighLimit());
            limitLowValue = NumberTools.parseDouble(portfolioStock.getLowLimit());
        }

        Double priceChangeValue = null;
        try {
            priceChangeValue = priceValue - buyPriceValue;
        } catch (Exception ignored) {
        }

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(portfolioStock.getDate());
            double elapsed = (new Date().getTime() - date.getTime()) / 1000;
            elapsedYears = elapsed / 31536000;
        } catch (Exception ignored) {
        }

        if (priceChangeValue != null) {
            totalChange = NumberTools.getTrimmedDouble(priceChangeValue, 5);
        }

        if (priceChangeValue != null) {
            totalPercent = String.format("%.1f", 100 * (priceChangeValue / buyPriceValue)) + "%";
        }

        if (priceChangeValue != null && elapsedYears != null) {
            totalChangeAer = NumberTools.getTrimmedDouble(priceChangeValue / elapsedYears, 5);
        }

        if (priceChangeValue != null && elapsedYears != null) {
            totalPercentAer = String.format("%.1f", (100 * (priceChangeValue / buyPriceValue)) / elapsedYears) + "%";
        }

        if (priceValue != null && quantityValue != null) {
            plHolding = String.format("%.0f", priceValue * quantityValue);
        }

        if (dailyChangeValue != null && quantityValue != null) {
            plDailyChange = String.format("%.0f", dailyChangeValue * quantityValue);
        }

        if (priceChangeValue != null && quantityValue != null) {
            plTotalChange = String.format("%.0f", priceChangeValue * quantityValue);
        }

        if (priceChangeValue != null && quantityValue != null && elapsedYears != null) {
            plTotalChangeAer = String.format("%.0f", (priceChangeValue * quantityValue) / elapsedYears);
        }

        if (priceValue != null && limitHighValue != null) {
            limitHighTriggered = priceValue > limitHighValue;
        }

        if (priceValue != null && limitLowValue != null) {
            limitLowTriggered = priceValue < limitLowValue;
        }
    }

    public String getPrice() {
        return price;
    }

    public String getDisplayName() {
        return displayName;
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
