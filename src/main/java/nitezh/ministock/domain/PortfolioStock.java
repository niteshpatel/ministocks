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

import org.json.JSONException;
import org.json.JSONObject;

import static nitezh.ministock.domain.PortfolioStockRepository.PortfolioField;

public class PortfolioStock {

    private final String price;
    private final String date;
    private final String quantity;
    private final String highLimit;
    private final String lowLimit;
    private final String customName;
    private final String symbol2;

    PortfolioStock(
            String price,
            String date,
            String quantity,
            String highLimit,
            String lowLimit,
            String customName,
            String symbol2
    ) {
        this.price = price;
        this.date = date;
        this.quantity = quantity;
        this.highLimit = highLimit;
        this.lowLimit = lowLimit;
        this.customName = customName;
        this.symbol2 = symbol2;
    }

    public String getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    String getQuantity() {
        return quantity;
    }

    String getHighLimit() {
        return highLimit;
    }

    String getLowLimit() {
        return lowLimit;
    }

    String getCustomName() {
        return customName;
    }

    private String getSymbol2() {
        return symbol2;
    }

    private void setJsonValue(JSONObject json, PortfolioField key, String value) {
        if (value == null || value.equals("")) {
            value = "empty";
        }
        try {
            json.put(key.name(), value);
        } catch (JSONException ignored) {
        }
    }

    JSONObject toJson() {
        JSONObject json = new JSONObject();
        this.setJsonValue(json, PortfolioField.PRICE, this.getPrice());
        this.setJsonValue(json, PortfolioField.DATE, this.getDate());
        this.setJsonValue(json, PortfolioField.QUANTITY, this.getQuantity());
        this.setJsonValue(json, PortfolioField.LIMIT_HIGH, this.getHighLimit());
        this.setJsonValue(json, PortfolioField.LIMIT_LOW, this.getLowLimit());
        this.setJsonValue(json, PortfolioField.CUSTOM_DISPLAY, this.getCustomName());
        this.setJsonValue(json, PortfolioField.SYMBOL_2, this.getSymbol2());
        return json;
    }

    boolean hasData() {
        return (this.getPrice() != null && !this.getPrice().equals("") ||
                (this.getCustomName() != null && !this.getCustomName().equals("")));
    }
}
