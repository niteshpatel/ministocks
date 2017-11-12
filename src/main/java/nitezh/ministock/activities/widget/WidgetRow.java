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

import android.graphics.Color;

import nitezh.ministock.domain.Widget;

class WidgetRow {
    private final Widget widget;

    private String symbol;
    private final int symbolDisplayColor;
    private String price;
    private int priceColor;
    private String volume;
    private int volumeColor;
    private String stockInfo;
    private int stockInfoColor;
    private String stockInfoExtra;
    private int stockInfoExtraColor;

    WidgetRow(Widget widget) {
        this.widget = widget;

        this.symbol = "";
        this.symbolDisplayColor = Color.WHITE;
        this.price = "";
        this.priceColor = Color.WHITE;
        this.volume = "";
        this.volumeColor = Color.WHITE;
        this.stockInfo = "";
        this.stockInfoColor = Color.WHITE;
        this.stockInfoExtra = "";
        this.stockInfoExtraColor = Color.WHITE;

    }

    int getSymbolDisplayColor() {
        return symbolDisplayColor;
    }

    int getPriceColor() {
        return priceColor;
    }

    void setPriceColor(int priceColor) {
        this.priceColor = priceColor;
    }

    int getVolumeColor() {
        return volumeColor;
    }

    void setVolumeColor(int volumeColor) {
        this.volumeColor = volumeColor;
    }

    int getStockInfoColor() {
        return stockInfoColor;
    }

    void setStockInfoColor(int stockInfoColor) {
        this.stockInfoColor = stockInfoColor;
    }

    int getStockInfoExtraColor() {
        return stockInfoExtraColor;
    }

    void setStockInfoExtraColor(int stockInfoExtraColor) {
        this.stockInfoExtraColor = stockInfoExtraColor;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        if (this.widget.getHideSuffix()) {
            int dotIndex = symbol.indexOf(".");
            if (dotIndex > -1) {
                symbol = symbol.substring(0, dotIndex);
            }
        }
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    String getStockInfo() {
        return stockInfo;
    }

    void setStockInfo(String stockInfo) {
        this.stockInfo = stockInfo;
    }

    String getStockInfoExtra() {
        return stockInfoExtra;
    }

    void setStockInfoExtra(String stockInfoExtra) {
        this.stockInfoExtra = stockInfoExtra;
    }
}
