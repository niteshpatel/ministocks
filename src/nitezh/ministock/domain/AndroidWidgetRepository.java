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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.Storage;


public class AndroidWidgetRepository implements WidgetRepository {

    private final Context context;
    private final Storage appStorage;

    public AndroidWidgetRepository(Context context) {
        this.context = context;
        this.appStorage = PreferenceStorage.getInstance(context);
    }

    @Override
    public void delWidgetId(int appWidgetId) {
        // Get the existing widgetIds from the storage
        ArrayList<String> newAppWidgetIds = new ArrayList<>();
        Collections.addAll(newAppWidgetIds, this.appStorage.getString("appWidgetIds", "").split(","));

        // Remove the one to remove
        newAppWidgetIds.remove(String.valueOf(appWidgetId));

        // Add the new appWidgetId
        StringBuilder appWidgetIds = new StringBuilder();
        for (String id : newAppWidgetIds) {
            appWidgetIds.append(id).append(",");
        }

        // Remove trailing comma
        if (appWidgetIds.length() > 0) {
            appWidgetIds.deleteCharAt(appWidgetIds.length() - 1);
        }

        // Update the storage too
        this.appStorage.putString("appWidgetIds", appWidgetIds.toString());
        this.appStorage.apply();
    }

    @Override
    public Widget getWidget(int id) {
        return new AndroidWidget(this.context, id);
    }

    @Override
    public Widget addWidget(int id, int size) {
        Widget widget = getWidget(id);
        widget.setSize(size);
        widget.setStock1("^DJI");
        widget.setStock1Summary("Dow Jones Industrial Average");
        widget.save();
        return widget;
    }

    @Override
    public void addWidgetId(int id) {
        if (this.getIds().contains(id)) {
            return;
        }

        StringBuilder rawIds = new StringBuilder();
        rawIds.append(this.appStorage.getString("appWidgetIds", ""));
        if (!rawIds.toString().equals("")) {
            rawIds.append(",");
        }
        rawIds.append(String.valueOf(id));
        this.appStorage.putString("appWidgetIds", rawIds.toString());
        this.appStorage.apply();
    }

    @Override
    public List<Integer> getIds() {
        StringBuilder rawAppWidgetIds = new StringBuilder();
        rawAppWidgetIds.append(this.appStorage.getString("appWidgetIds", ""));
        String[] appWidgetIds = rawAppWidgetIds.toString().split(",");
        int appWidgetIdsLength = 0;
        if (!rawAppWidgetIds.toString().equals("")) {
            appWidgetIdsLength = appWidgetIds.length;
        }

        Integer[] savedAppWidgetIds = new Integer[appWidgetIdsLength];
        for (int i = 0; i < appWidgetIds.length; i++) {
            if (!appWidgetIds[i].equals("")) {
                savedAppWidgetIds[i] = Integer.parseInt(appWidgetIds[i]);
            }
        }

        return Arrays.asList(savedAppWidgetIds);
    }

    @Override
    public boolean isEmpty() {
        return this.getIds().isEmpty();
    }

    @Override
    public Set<String> getWidgetsStockSymbols() {
        Storage widgetPreferences;
        Set<String> widgetStockSymbols = new HashSet<>();
        for (int appWidgetId : this.getIds()) {
            widgetPreferences = this.getWidget(appWidgetId).getStorage();
            if (widgetPreferences != null) {
                for (int i = 1; i < 11; i++) {
                    String stockSymbol = widgetPreferences.getString("Stock" + i, "");
                    if (!stockSymbol.equals("")) widgetStockSymbols.add(stockSymbol);
                }
            }
        }

        return widgetStockSymbols;
    }
}
