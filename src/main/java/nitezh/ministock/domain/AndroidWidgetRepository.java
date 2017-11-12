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
import android.text.TextUtils;

import java.util.ArrayList;
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
    public Widget getWidget(int id) {
        return new AndroidWidget(this.context, id);
    }

    @Override
    public List<Integer> getIds() {
        List<Integer> ids = new ArrayList<>();
        for (String rawId : this.appStorage.getString("appWidgetIds", "").split(",")) {
            if (!rawId.equals("")) {
                ids.add(Integer.parseInt(rawId));
            }
        }

        return ids;
    }

    private void setIds(List<Integer> ids) {
        List<String> rawIds = new ArrayList<>();
        for (Integer id : ids) {
            rawIds.add(String.valueOf(id));
        }
        this.appStorage.putString("appWidgetIds", TextUtils.join(",", rawIds));
        this.appStorage.apply();
    }

    private void addWidgetId(int newId) {
        List<Integer> ids = this.getIds();
        if (ids.contains(newId)) {
            return;
        }
        ids.add(newId);
        this.setIds(ids);
    }

    @Override
    public Widget addWidget(int id, int size) {
        Widget widget = getWidget(id);
        if (this.getIds().contains(id)) {
            return widget;
        }

        this.addWidgetId(id);
        widget.setSize(size);
        widget.enableDailyChangeView();
        if (widget.isNarrow()) {
            widget.enablePercentChangeView();
        }

        widget.setStock1();
        widget.setStock1Summary();
        widget.save();

        return widget;
    }

    @Override
    public void delWidget(int oldId) {
        List<Integer> ids = this.getIds();
        if (!ids.contains(oldId)) {
            return;
        }
        ids.remove((Integer) oldId);  // Need to cast otherwise 'remove' uses location
        this.setIds(ids);
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
