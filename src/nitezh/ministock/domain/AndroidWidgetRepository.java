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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import java.util.HashSet;
import java.util.Set;

import nitezh.ministock.LocalStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;


public class AndroidWidgetRepository implements WidgetRepository {

    private final Context context;
    private final Storage appStorage;

    public AndroidWidgetRepository(Context context, Storage appStorage) {
        this.context = context;
        this.appStorage = appStorage;
    }

    @Override
    public Storage getWidgetStorage(int appWidgetId) {
        SharedPreferences widgetPreferences = null;
        try {
            widgetPreferences = this.context.getApplicationContext().getSharedPreferences(context.getString(R.string.prefs_name) + appWidgetId, 0);
        } catch (Resources.NotFoundException ignored) {
        }

        return new LocalStorage(widgetPreferences);
    }

    @Override
    public Set<String> getWidgetsStockSymbols() {
        Set<String> widgetStockSymbols = new HashSet<>();
        Storage widgetPreferences;

        // Add the stock symbols from the widget preferences
        for (int appWidgetId : this.getIds()) {
            widgetPreferences = this.getWidgetStorage(appWidgetId);
            if (widgetPreferences == null)
                continue;

            // If widget preferences were found, extract the stock symbols
            for (int i = 1; i < 11; i++) {
                String stockSymbol = widgetPreferences.getString("Stock" + i, "");
                if (!stockSymbol.equals(""))
                    widgetStockSymbols.add(stockSymbol);
            }
        }
        return widgetStockSymbols;
    }

    @Override
    public int[] getIds() {
        // Get the widgetIds from the preferences
        StringBuilder rawAppWidgetIds = new StringBuilder();
        rawAppWidgetIds.append(this.appStorage.getString("appWidgetIds", ""));

        // Create an array of appWidgetIds
        String[] appWidgetIds = rawAppWidgetIds.toString().split(",");
        int appWidgetIdsLength = 0;
        if (!rawAppWidgetIds.toString().equals(""))
            appWidgetIdsLength = appWidgetIds.length;
        int[] savedAppWidgetIds = new int[appWidgetIdsLength];
        for (int i = 0; i < appWidgetIds.length; i++)
            if (!appWidgetIds[i].equals(""))
                savedAppWidgetIds[i] = Integer.parseInt(appWidgetIds[i]);
        return savedAppWidgetIds;
    }
}
