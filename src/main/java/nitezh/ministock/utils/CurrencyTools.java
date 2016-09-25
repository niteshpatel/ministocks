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

package nitezh.ministock.utils;

import java.util.HashMap;
import java.util.Locale;


public class CurrencyTools {

    private static final HashMap<String, String> codeMap = new HashMap<>();
    private static final HashMap<String, String> charMap = new HashMap<>();

    private CurrencyTools() {
    }

    static {
        codeMap.put(".AS", "EUR");
        codeMap.put(".AX", "AUD");
        codeMap.put(".BA", "ARS");
        codeMap.put(".BC", "EUR");
        codeMap.put(".BE", "EUR");
        codeMap.put(".BI", "EUR");
        codeMap.put(".BM", "EUR");
        codeMap.put(".BO", "INR");
        codeMap.put(".CBT", "USD");
        codeMap.put(".CME", "USD");
        codeMap.put(".CMX", "USD");
        codeMap.put(".CO", "DKK");
        codeMap.put(".DE", "EUR");
        codeMap.put(".DU", "EUR");
        codeMap.put(".F", "EUR");
        codeMap.put(".HA", "EUR");
        codeMap.put(".HK", "HKD");
        codeMap.put(".HM", "EUR");
        codeMap.put(".JK", "IDR");
        codeMap.put(".KQ", "KRW");
        codeMap.put(".KS", "KRW");
        codeMap.put(".L", "GBP");
        codeMap.put(".MA", "EUR");
        codeMap.put(".MC", "EUR");
        codeMap.put(".MF", "EUR");
        codeMap.put(".MI", "EUR");
        codeMap.put(".MU", "EUR");
        codeMap.put(".MX", "MXN");
        codeMap.put(".NS", "INR");
        codeMap.put(".NX", "EUR");
        codeMap.put(".NYB", "USD");
        codeMap.put(".NYM", "USD");
        codeMap.put(".NZ", "NZD");
        codeMap.put(".OB", "USD");
        codeMap.put(".OL", "NOK");
        codeMap.put(".PA", "EUR");
        codeMap.put(".PK", "USD");
        codeMap.put(".SA", "BRL");
        codeMap.put(".SG", "EUR");
        codeMap.put(".SI", "SGD");
        codeMap.put(".SN", "CLP");
        codeMap.put(".SS", "CNY");
        codeMap.put(".ST", "SEK");
        codeMap.put(".SW", "CHF");
        codeMap.put(".SZ", "CNY");
        codeMap.put(".TA", "ILS");
        codeMap.put(".TO", "CAD");
        codeMap.put(".TW", "TWD");
        codeMap.put(".TWO", "TWD");
        codeMap.put(".V", "CAD");
        codeMap.put(".VI", "EUR");

        charMap.put("EUR", "€");
        charMap.put("AUD", "$");
        charMap.put("ARS", "$");
        charMap.put("INR", "R");
        charMap.put("USD", "$");
        charMap.put("DKK", "k");
        charMap.put("HKD", "$");
        charMap.put("IDR", "R");
        charMap.put("KRW", "₩");
        charMap.put("GBP", "£");
        charMap.put("MXN", "$");
        charMap.put("NZD", "$");
        charMap.put("NOK", "k");
        charMap.put("BRL", "$");
        charMap.put("SGD", "$");
        charMap.put("CLP", "$");
        charMap.put("CNY", "¥");
        charMap.put("SEK", "k");
        charMap.put("SW", "F");
        charMap.put("ILS", "₪");
        charMap.put("CAD", "$");
        charMap.put("TWD", "$");
    }

    private static String getCurrencyForSymbol(String symbol) {
        String currencyChar = null;
        int index = symbol.indexOf(".");
        if (index > -1) {
            currencyChar = charMap.get(codeMap.get(symbol.substring(index)));
        }

        if (currencyChar == null)
            currencyChar = "$";

        return currencyChar;
    }

    public static String addCurrencyToSymbol(String value, String symbol) {
        String currencySymbol = getCurrencyForSymbol(symbol);

        // £ needs division by 100
        if (currencySymbol.equals("£"))
            try {
                value = String.format(Locale.getDefault(), "%.0f", NumberTools.tryParseDouble(value) / 100);
            } catch (Exception ignored) {
            }

        // Move minus sign to front if present
        String prefix = "";
        if (value.contains("-")) {
            prefix = "-";
            value = value.substring(1);
        }
        return prefix + currencySymbol + value;
    }
}
