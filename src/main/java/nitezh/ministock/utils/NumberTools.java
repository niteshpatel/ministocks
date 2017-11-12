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


import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;


public class NumberTools {

    private static final int MAX_FRACTIONAL_DIGITS = 5;

    private NumberTools() {
    }

    public static String decimalPlaceFormat(String s) {
        try {
            return String.format("%." + 2 + "f", Double.parseDouble(s));
        } catch (Exception ignored) {
        }

        return s;
    }

    public static String getTrimmedDouble(double number, int digits) {
        return getTrimmedDouble(number, digits, null);
    }

    public static String getTrimmedDouble2(double number, int digits) {
        String numberAsString = Double.toString(number);

        // Find the position of the decimal point
        int decimalPos = numberAsString.indexOf(".");

        // If there is no decimal point, return immediately since there is
        // no work to do.
        if (decimalPos == -1) {
            return numberAsString;
        }
        // If there are more digits before the decimal place than the space
        // available then do the best we can and return with 0 dp.
        if (digits < decimalPos) {
            return String.format(Locale.getDefault(), "%.0f", number);
        }
        // If we have space to show the whole number, and the max precision
        // is null OR the number is greater than one then we always use 2 dp.
        if (Math.abs(number) >= 100 && (numberAsString.length() - 1 < digits)) {
            return String.format(Locale.getDefault(), "%.2f", number);
        }
        // If the number is greater than zero than the max precision is 2
        int precision = digits - decimalPos;
        if (Math.abs(number) >= 100) {
            precision = Math.min(precision, 2);
        }
        if (Math.abs(number) >= 10) {
            precision = Math.min(precision, 3);
        }
        // Trim precision as necessary (max precision 4)
        return String.format("%." + Math.min(precision, 4) + "f", number);
    }

    public static String getTrimmedDouble(double number, int digits, Integer maxPrecision) {
        String numberAsString = Double.toString(number);

        // Find the position of the decimal point
        int decimalPos = numberAsString.indexOf(".");

        // If there is no decimal point, return immediately since there is
        // no work to do.
        if (decimalPos == -1) {
            return numberAsString;
        }
        // If there are more digits before the decimal place than the space
        // available then do the best we can and return with 0 dp.
        if (digits < decimalPos) {
            return String.format(Locale.getDefault(), "%.0f", number);
        }
        // If we have space to show the whole number, and the max precision
        // is null OR the number is greater than one then we always use 2 dp.
        if ((Math.abs(number) >= 10 || maxPrecision == null) && (numberAsString.length() - 1 < digits)) {
            return String.format(Locale.getDefault(), "%.2f", number);
        }

        // If the number is greater than zero than the max precision is 2
        int precision = digits - decimalPos;
        if (Math.abs(number) >= 10 || maxPrecision == null) {
            precision = Math.min(precision, 2);
        }

        // Ignore maxPrecision = 0
        if (maxPrecision == null)
            maxPrecision = precision;
        // Trim precision as necessary (max precision 4)
        return String.format("%." + Math.min(precision, maxPrecision) + "f", number);
    }

    public static Double tryParseDouble(String value) {
        return tryParseDouble(value, null);
    }

    public static Double tryParseDouble(String value, Double defaultValue) {
        try {
            return parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getNormalisedVolume(String value) {
        Double volume;
        try {
            volume = tryParseDouble(value);

            if (volume > 999999999999D)
                value = String.format(Locale.getDefault(), "%.0fT", volume / 1000000000000D);
            else if (volume > 999999999D)
                value = String.format(Locale.getDefault(), "%.0fB", volume / 1000000000D);
            else if (volume > 999999D)
                value = String.format(Locale.getDefault(), "%.0fM", volume / 1000000D);
            else if (volume > 999D)
                value = String.format(Locale.getDefault(), "%.0fK", volume / 1000D);
            else
                value = String.format(Locale.getDefault(), "%.0f", volume);
        } catch (Exception ignored) {
        }
        return value;
    }

    public static String validatedDoubleString(String value) throws ParseException {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(MAX_FRACTIONAL_DIGITS);

        return format.format(parseDouble(value));
    }

    public static Double parseDouble(String value) throws ParseException {
        return parseDouble(value, Locale.getDefault());
    }

    public static Double parseDouble(String value, Locale locale) throws ParseException {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(MAX_FRACTIONAL_DIGITS);

        // Double.parse handles '+' but NumberFormat.parse does not, so do it here
        Number number = format.parse(value.replace("+", ""));

        return number.doubleValue();
    }

    public static String trim(String value, Locale locale) throws ParseException {
        char decimalFormatSymbol = new DecimalFormatSymbols(locale).getDecimalSeparator();
        int digitsAfterDecimal = value.length() - value.indexOf(decimalFormatSymbol) - 1;

        Double p = NumberTools.parseDouble(value, locale);
        int maxPrecision = Math.min(4, digitsAfterDecimal);

        return NumberTools.getTrimmedDouble(p, 6, maxPrecision);
    }
}
