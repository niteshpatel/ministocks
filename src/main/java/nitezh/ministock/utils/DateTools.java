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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateTools {

    private DateTools() {
    }

    public static double elapsedDays(String dateString) {
        double daysElapsed = 0;
        if (dateString != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            try {
                double elapsed = (new Date().getTime() - formatter.parse(dateString).getTime()) / 1000;
                daysElapsed = elapsed / 86400;
            } catch (ParseException ignored) {
            }
        }
        return daysElapsed;
    }

    public static double elapsedTime(String dateString) {
        double daysElapsed = 0;
        if (dateString != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                daysElapsed = (new Date().getTime() - formatter.parse(dateString).getTime());
            } catch (ParseException ignored) {
            }
        }
        return daysElapsed;
    }

    public static Date parseSimpleDate(String dateString) {
        String[] items = dateString.split(":");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(items[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(items[1]));
        return cal.getTime();
    }

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

    public static String getNowAsString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public static String timeDigitPad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
