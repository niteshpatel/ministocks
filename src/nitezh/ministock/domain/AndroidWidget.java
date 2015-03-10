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

import java.util.Iterator;
import java.util.Map;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.Storage;

public class AndroidWidget implements Widget {

    private final Storage storage;
    private final int appWidgetId;
    private final Context context;

    public AndroidWidget(Context context, int appWidgetId) {
        this.context = context;
        this.storage = getStorage();
        this.appWidgetId = appWidgetId;
    }

    @Override
    public Storage getStorage() {
        SharedPreferences widgetPreferences = null;
        try {
            widgetPreferences = context.getSharedPreferences(context.getString(R.string.prefs_name) + this.appWidgetId, 0);
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
}
