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


public class StockQuote {

    private String symbol;
    private String price;
    private String change;
    private String percent;
    private String exchange;
    private String volume;
    private String name;

    public StockQuote(String symbol, String price, String change, String percent, String exchange,
                      String volume, String name) {
        this(symbol, price, change, percent, exchange, volume, name, null);
    }

    public StockQuote(String symbol, String price, String change, String percent, String exchange,
                      String volume, String name, String previousPrice) {
        this.symbol = symbol;
        this.exchange = exchange;
        this.volume = volume;
        this.name = name;

        // Get additional FX data if applicable
        Double p0 = null;
        boolean isFx = symbol.contains("=");
        if (isFx) {
            try {
                p0 = Double.parseDouble(previousPrice);
            } catch (Exception ignored) {
            }
        }

        // Set stock prices to 2 decimal places
        Double p = null;
        if (!price.equals("0.00")) {
            try {
                p = Double.parseDouble(price);
                if (isFx) {
                    this.price = NumberTools.getTrimmedDouble2(p, 6);
                } else {
                    this.price = NumberTools.getTrimmedDouble(p, 6, 4);
                }
            } catch (Exception e) {
                this.price = "0.00";
            }

            // Note that if the change or percent == "N/A" set to 0
            if ((price.equals("N/A") || price.equals("")) && p0 == null) {
                change = "0.00";
            }
            if ((percent.equals("N/A") || percent.equals("")) && p0 == null) {
                percent = "0.00";
            }
        }

        // Changes are only set to 5 significant figures
        Double c = null;
        if (!change.equals("N/A") && !change.equals("")) {
            c = Double.parseDouble(change);
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
        if (!percent.equals("N/A") && !percent.equals("")) {
            pc = Double.parseDouble(percent.replace("%", ""));
        } else {
            if (c != null && p != null) {
                pc = (c / p) * 100;
            }
        }
        if (pc != null) {
            this.percent = String.format("%.1f", pc) + "%";
        }
    }

    public static StockQuote deserialize(String serialized) {
        String[] parts = serialized.split(";");
        return new StockQuote(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
    }

    public String getSymbol() {
        return symbol;
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

    public String serialize() {
        return String.format("%s;%s;%s;%s;%s;%s;%s", symbol, price, change, percent, exchange, volume, name);
    }
}
