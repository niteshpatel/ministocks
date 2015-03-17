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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;

public class AndroidWidget implements Widget {

    private final Storage storage;
    private final Context context;
    private final int id;

    public AndroidWidget(Context context, int id) {
        this.context = context;
        this.id = id;
        this.storage = this.getStorage();
    }

    @Override
    public Storage getStorage() {
        SharedPreferences widgetPreferences = null;
        try {
            widgetPreferences = context.getSharedPreferences(context.getString(R.string.prefs_name) + this.id, 0);
        } catch (Resources.NotFoundException ignored) {
        }

        return new PreferenceStorage(widgetPreferences);
    }

    @Override
    public void setWidgetPreferencesFromJson(JSONObject jsonPrefs) {
        String key;
        for (Iterator iter = jsonPrefs.keys(); iter.hasNext(); ) {
            key = (String) iter.next();
            try {
                Object value = jsonPrefs.get(key);
                if (value instanceof String) {
                    this.storage.putString(key, (String) value);
                } else if (value instanceof Boolean) {
                    this.storage.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    this.storage.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    this.storage.putFloat(key, (Float) value);
                } else if (value instanceof Long) {
                    this.storage.putLong(key, (Long) value);
                }
            } catch (JSONException ignored) {
            }
        }
        this.storage.apply();
    }

    @Override
    public JSONObject getWidgetPreferencesAsJson() {
        JSONObject jsonPrefs = new JSONObject();
        for (Map.Entry<String, ?> entry : this.storage.getAll().entrySet()) {
            try {
                jsonPrefs.put(entry.getKey(), entry.getValue());
            } catch (JSONException ignored) {
            }
        }

        return jsonPrefs;
    }

    @Override
    public void setPercentChange(boolean b) {
        this.storage.putBoolean("show_percent_change", true);
    }

    @Override
    public void setStock1(String s) {
        this.storage.putString("Stock1", "^DJI");
    }

    @Override
    public void setStock1Summary(String s) {
        this.storage.putString("Stock1_summary", "Dow Jones Industrial Average");
    }

    @Override
    public void save() {
        this.storage.apply();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        return this.storage.getInt("widgetSize", 0);
    }

    @Override
    public void setSize(int size) {
        this.storage.putInt("widgetSize", size);
        if (size == 0 || size == 2) {
            this.setPercentChange(true);
        }
    }

    @Override
    public String getStock(int i) {
        return this.storage.getString("Stock" + (i + 1), "");
    }

    @Override
    public int getPreviousView() {
        return this.storage.getInt("widgetView", 0);
    }

    @Override
    public void setView(int view) {
        if (view != this.getPreviousView()) {
            this.storage.putInt("widgetView", view);
            this.save();
        }
    }

    @Override
    public List<String> getSymbols() {
        boolean found = false;
        List<String> symbols = new ArrayList<>();
        String s;
        for (int i = 0; i < this.getSymbolCount(); i++) {
            s = this.getStock(i);
            symbols.add(s);
            if (!s.equals("")) {
                found = true;
            }
        }

        if (!found) {
            symbols.add("^DJI");
        }
        return symbols;
    }

    @Override
    public int getSymbolCount() {
        int size = this.getSize();
        int count = 0;
        if (size == 0 || size == 1) {
            count = 4;
        } else if (size == 2 || size == 3) {
            count = 10;
        }
        return count;
    }
}
