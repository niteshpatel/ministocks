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

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.WidgetRepository;


public class PreferenceTools {

    public static LocalStorage getAppPreferences(Context context) {
        return new LocalStorage(context.getSharedPreferences(context.getString(R.string.prefs_name), 0));
    }

    public static JSONObject getWidgetPreferencesAsJson(Context context, int appWidgetId) {
        Storage appStorage = PreferenceTools.getAppPreferences(context);
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context, appStorage);
        JSONObject jsonPrefs = new JSONObject();
        Storage storage = widgetRepository.getWidgetStorage(appWidgetId);
        for (Map.Entry<String, ?> entry : storage.getAll().entrySet()) {
            try {
                jsonPrefs.put(entry.getKey(), entry.getValue());
            } catch (JSONException ignored) {
            }
        }

        return jsonPrefs;
    }

    public static void setWidgetPreferencesFromJson(Context context, int appWidgetId, JSONObject jsonPrefs) {
        Storage appStorage = PreferenceTools.getAppPreferences(context);
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context, appStorage);
        Storage storage = widgetRepository.getWidgetStorage(appWidgetId);
        String key;
        for (Iterator iter = jsonPrefs.keys(); iter.hasNext(); ) {
            key = (String) iter.next();
            try {
                Object value = jsonPrefs.get(key);
                if (value instanceof String) {
                    storage.putString(key, (String) value);
                } else if (value instanceof Boolean) {
                    storage.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    storage.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    storage.putFloat(key, (Float) value);
                } else if (value instanceof Long) {
                    storage.putLong(key, (Long) value);
                }
            } catch (JSONException ignored) {
            }
        }
        storage.apply();
    }
}
