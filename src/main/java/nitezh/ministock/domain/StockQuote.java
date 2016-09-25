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

import java.text.ParseException;
import java.util.Locale;


public class StockQuote {

    private String symbol;
    private String price;
    private String change;
    private String percent;
    private final String exchange;
    private final String volume;
    private final String name;

    public StockQuote(
            String symbol,
            String price,
            String change,
            String percent,
            String exchange,
            String volume,
            String name,
            Locale locale) {

        this(
                symbol,
                price,
                change,
                percent,
                exchange,
                volume,
                name,
                null,
                locale
        );
    }

    public StockQuote(
            String symbol,
            String price,
            String change,
            String percent,
            String exchange,
            String volume,
            String name,
            String previousPrice,
            Locale locale) {

        this.symbol = symbol;
        this.exchange = exchange;
        this.volume = volume;
        this.name = name;

        // Get additional FX data if applicable
        Double p0 = null;
        boolean isFx = symbol.contains("=");
        if (isFx) {
            try {
                p0 = NumberTools.parseDouble(previousPrice, locale);
            } catch (Exception ignored) {
            }
        }

        // Set stock prices to 2 decimal places
        Double p = null;
        if (!price.equals("0.00")) {
            try {
                p = NumberTools.parseDouble(price, locale);
                if (isFx) {
                    this.price = NumberTools.getTrimmedDouble2(p, 6);
                } else {
                    this.price = NumberTools.trim(price, locale);
                }
            } catch (Exception e) {
                this.price = "0.00";
            }

            // Note that if the change or percent == "N/A" set to 0
            if (!this.isNonEmptyNumber(price) && p0 == null) {
                change = "0.00";
            }
            if (!this.isNonEmptyNumber(percent) && p0 == null) {
                percent = "0.00";
            }
        }

        // Changes are only set to 5 significant figures
        Double c = null;
        if (this.isNonEmptyNumber(change)) {
            try {
                c = NumberTools.parseDouble(change, locale);
            } catch (ParseException ignored) {
            }
        } else if (p0 != null && p != null) {
            c = p - p0;
        }
        if (c != null) {
            if (p != null && (p < 10 || isFx)) {
                this.change = NumberTools.getTrimmedDouble(c, 5, 3);
            } else {
                this.change = NumberTools.getTrimmedDouble(c, 5);
            }
        }

        // Percentage changes are only set to one decimal place
        Double pc = null;
        if (this.isNonEmptyNumber(percent)) {
            try {
                pc = NumberTools.parseDouble(percent.replace("%", ""), locale);
            } catch (ParseException ignored) {

            }
        } else {
            if (c != null && p != null) {
                pc = (c / p) * 100;
            }
        }
        if (pc != null) {
            this.percent = String.format(Locale.getDefault(), "%.1f", pc) + "%";
        }
    }

    private boolean isNonEmptyNumber(String value) {
        return !value.equals("N/A")
                && !value.equals("")
                && !value.equals("null");
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public String getChange() {
        return change;
    }

    public String getPercent() {
        return percent;
    }

    public String getExchange() {
        return exchange;
    }

    public String getVolume() {
        return volume;
    }

    public String getName() {
        return name;
    }
}
