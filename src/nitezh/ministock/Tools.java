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

package nitezh.ministock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.text.Html;
import android.text.Spanned;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class Tools {

    public static final String BUILD = "59";
    private static final HashMap<String, String> mCurrencyCodeMap = new HashMap<String, String>();
    private static final HashMap<String, String> mCurrencyCharMap = new HashMap<String, String>();

    static {
        // Populate currency code map
        mCurrencyCodeMap.put(".AS", "EUR");
        mCurrencyCodeMap.put(".AX", "AUD");
        mCurrencyCodeMap.put(".BA", "ARS");
        mCurrencyCodeMap.put(".BC", "EUR");
        mCurrencyCodeMap.put(".BE", "EUR");
        mCurrencyCodeMap.put(".BI", "EUR");
        mCurrencyCodeMap.put(".BM", "EUR");
        mCurrencyCodeMap.put(".BO", "INR");
        mCurrencyCodeMap.put(".CBT", "USD");
        mCurrencyCodeMap.put(".CME", "USD");
        mCurrencyCodeMap.put(".CMX", "USD");
        mCurrencyCodeMap.put(".CO", "DKK");
        mCurrencyCodeMap.put(".DE", "EUR");
        mCurrencyCodeMap.put(".DU", "EUR");
        mCurrencyCodeMap.put(".F", "EUR");
        mCurrencyCodeMap.put(".HA", "EUR");
        mCurrencyCodeMap.put(".HK", "HKD");
        mCurrencyCodeMap.put(".HM", "EUR");
        mCurrencyCodeMap.put(".JK", "IDR");
        mCurrencyCodeMap.put(".KQ", "KRW");
        mCurrencyCodeMap.put(".KS", "KRW");
        mCurrencyCodeMap.put(".L", "GBP");
        mCurrencyCodeMap.put(".MA", "EUR");
        mCurrencyCodeMap.put(".MC", "EUR");
        mCurrencyCodeMap.put(".MF", "EUR");
        mCurrencyCodeMap.put(".MI", "EUR");
        mCurrencyCodeMap.put(".MU", "EUR");
        mCurrencyCodeMap.put(".MX", "MXN");
        mCurrencyCodeMap.put(".NS", "INR");
        mCurrencyCodeMap.put(".NX", "EUR");
        mCurrencyCodeMap.put(".NYB", "USD");
        mCurrencyCodeMap.put(".NYM", "USD");
        mCurrencyCodeMap.put(".NZ", "NZD");
        mCurrencyCodeMap.put(".OB", "USD");
        mCurrencyCodeMap.put(".OL", "NOK");
        mCurrencyCodeMap.put(".PA", "EUR");
        mCurrencyCodeMap.put(".PK", "USD");
        mCurrencyCodeMap.put(".SA", "BRL");
        mCurrencyCodeMap.put(".SG", "EUR");
        mCurrencyCodeMap.put(".SI", "SGD");
        mCurrencyCodeMap.put(".SN", "CLP");
        mCurrencyCodeMap.put(".SS", "CNY");
        mCurrencyCodeMap.put(".ST", "SEK");
        mCurrencyCodeMap.put(".SW", "CHF");
        mCurrencyCodeMap.put(".SZ", "CNY");
        mCurrencyCodeMap.put(".TA", "ILS");
        mCurrencyCodeMap.put(".TO", "CAD");
        mCurrencyCodeMap.put(".TW", "TWD");
        mCurrencyCodeMap.put(".TWO", "TWD");
        mCurrencyCodeMap.put(".V", "CAD");
        mCurrencyCodeMap.put(".VI", "EUR");

        // Populate currency char map
        mCurrencyCharMap.put("EUR", "€");
        mCurrencyCharMap.put("AUD", "$");
        mCurrencyCharMap.put("ARS", "$");
        mCurrencyCharMap.put("INR", "R");
        mCurrencyCharMap.put("USD", "$");
        mCurrencyCharMap.put("DKK", "k");
        mCurrencyCharMap.put("HKD", "$");
        mCurrencyCharMap.put("IDR", "R");
        mCurrencyCharMap.put("KRW", "₩");
        mCurrencyCharMap.put("GBP", "£");
        mCurrencyCharMap.put("MXN", "$");
        mCurrencyCharMap.put("NZD", "$");
        mCurrencyCharMap.put("NOK", "k");
        mCurrencyCharMap.put("BRL", "$");
        mCurrencyCharMap.put("SGD", "$");
        mCurrencyCharMap.put("CLP", "$");
        mCurrencyCharMap.put("CNY", "¥");
        mCurrencyCharMap.put("SEK", "k");
        mCurrencyCharMap.put("SW", "F");
        mCurrencyCharMap.put("ILS", "₪");
        mCurrencyCharMap.put("CAD", "$");
        mCurrencyCharMap.put("TWD", "$");
    }

    // Determine currency symbol from exchange
    private static String getCurrencySymbol(String symbol) {

        // Now add the currency symbol
        String currencyChar = null;

        // Extract the suffix and determine currency char
        int indexOfCode = symbol.indexOf(".");
        if (indexOfCode > -1) {
            String exchangeCode = symbol.substring(indexOfCode);
            String currencyCode = mCurrencyCodeMap.get(exchangeCode);
            currencyChar = mCurrencyCharMap.get(currencyCode);
        }

        // Default symbol is $
        if (currencyChar == null)
            currencyChar = "$";

        return currencyChar;
    }

    // Format as currency based on the stock exchange
    public static String addCurrencySymbol(String value, String symbol) {

        // Get currency symbol
        String currencySymbol = getCurrencySymbol(symbol);

        // Divide by 100 if needed
        if (currencySymbol.equals("£"))
            try {
                value = String.format("%.0f", Tools.parseDouble(value) / 100);

            } catch (Exception ignored) {
            }

        // Move minus sign to front if we have one
        String prefix = "";
        if (value.contains("-")) {
            prefix = "-";
            value = value.substring(1);
        }

        // Add minus sign to front if we have one
        return prefix + currencySymbol + value;
    }

    public static Double parseDouble(String value) {
        return parseDouble(value, null);
    }

    public static Double parseDouble(String value, Double defaultValue) {
        try {

            // First replace decimal points with the local separator
            char separator = new DecimalFormatSymbols().getDecimalSeparator();
            value = value.replace('.', separator);
            return NumberFormat.getInstance().parse(value).doubleValue();

        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.prefs_name),
                0);
    }

    public static SharedPreferences getWidgetPreferences(
            Context context,
            int appWidgetId) {
        SharedPreferences widgetPreferences = null;
        try {
            widgetPreferences =
                    context.getSharedPreferences(
                            context.getString(R.string.prefs_name)
                                    + appWidgetId,
                            0);

        } catch (NotFoundException ignored) {
        }
        return widgetPreferences;
    }

    public static String timeDigitPad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public static String decimalPlaceFormat(String s) {

        // Format to two decimal places
        try {
            return String.format("%." + 2 + "f", Double.parseDouble(s));

        } catch (Exception ignored) {
        }
        return s;
    }

    public static void showSimpleDialog(
            Context context,
            String title,
            String body) {

        alertWithCallback(context, title, body, "Close", null, null);
    }

    public static void alertWithCallback(
            Context context,
            String title,
            String body,
            String positiveButtonText,
            String negativeButtonText,
            final Callable<?> callable) {

        // Create dialog
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);

        // Use HTML so we can stick bold in here
        Spanned html = Html.fromHtml(body);

        TextView text = new TextView(context);
        text.setPadding(10, 10, 10, 10);
        text.setTextSize(16);
        text.setText(html);

        // Scroll view to handle longer text
        ScrollView scroll = new ScrollView(context);
        scroll.setPadding(0, 0, 0, 0);
        scroll.addView(text);
        alertDialog.setView(scroll);

        // Set the close button text
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                positiveButtonText,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callable != null) {
                            try {
                                callable.call();

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                });

        // Optional negative button
        if (negativeButtonText != null) {
            alertDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    negativeButtonText,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }

        alertDialog
                .setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });

        alertDialog.show();
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
            return String.format("%.0f", number);
        }

        // If we have space to show the whole number, and the max precision
        // is null OR the number is greater than one then we always use 2 dp.
        if (Math.abs(number) >= 100 && (numberAsString.length() - 1 < digits)) {
            return String.format("%.2f", number);
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

    public static String getTrimmedDouble(
            double number,
            int digits,
            Integer maxPrecision) {
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
            return String.format("%.0f", number);
        }

        // If we have space to show the whole number, and the max precision
        // is null OR the number is greater than one then we always use 2 dp.
        if ((Math.abs(number) >= 10 || maxPrecision == null)
                && (numberAsString.length() - 1 < digits)) {
            return String.format("%.2f", number);
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
        return String.format(
                "%." + Math.min(precision, maxPrecision) + "f",
                number);
    }

    /* Number of days elapsed from given date */
    public static double elapsedDays(String dateString) {

        double daysElapsed = 0;
        if (dateString != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

            try {
                double elapsed =
                        (new Date().getTime() - formatter
                                .parse(dateString)
                                .getTime()) / 1000;
                daysElapsed = elapsed / 86400;

            } catch (ParseException ignored) {
            }
        }
        return daysElapsed;
    }

    /* Number of milliseconds elapsed from given date */
    public static double elapsedTime(String dateString) {

        double daysElapsed = 0;
        if (dateString != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            try {
                daysElapsed = (new Date().getTime() - formatter
                        .parse(dateString)
                        .getTime());

            } catch (ParseException ignored) {
            }
        }
        return daysElapsed;
    }

    /* Parse simple hh:mm string into date */
    public static Date parseSimpleDate(String dateString) {
        String[] items = dateString.split(":");
        Date date = new Date();
        date.setHours(Integer.parseInt(items[0]));
        date.setMinutes(Integer.parseInt(items[1]));
        return date;
    }

    /* Compare a date to now */
    public static int compareToNow(Date date) {

        Long now = new Date().getTime();
        if (date.getTime() > now) {
            return 1;

        } else if (date.getTime() < now) {
            return -1;

        } else {
            return 0;
        }
    }

    public static int getField(String name) {
        try {
            return R.id.class.getField(name).getInt(R.class);

        } catch (Exception e) {
            return 0;
        }
    }
}
