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
import android.content.SharedPreferences;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;


public class PreferenceTools {

    public static SharedPreferences getAppPreferences(Context context) {
        return context.getSharedPreferences(context.getString(R.string.prefs_name), 0);
    }

    public static SharedPreferences getWidgetPreferences(Context context, int appWidgetId) {
        SharedPreferences widgetPreferences = null;
        try {
            widgetPreferences = context.getApplicationContext().getSharedPreferences(context.getString(R.string.prefs_name) + appWidgetId, 0);
        } catch (Resources.NotFoundException ignored) {
        }
        return widgetPreferences;
    }

    public static JSONObject getWidgetPreferencesAsJson(Context context, int appWidgetId) {
        JSONObject jsonPrefs = new JSONObject();
        SharedPreferences prefs = getWidgetPreferences(context, appWidgetId);
        String key;
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            key = entry.getKey();
            try {
                jsonPrefs.put(key, entry.getValue());
            } catch (JSONException ignored) {
            }
        }
        return jsonPrefs;
    }

    public static void setWidgetPreferencesFromJson(Context context, int appWidgetId, JSONObject jsonPrefs) {
        SharedPreferences.Editor editor = getWidgetPreferences(context, appWidgetId).edit();
        String key;
        for (Iterator iter = jsonPrefs.keys(); iter.hasNext(); ) {
            key = (String) iter.next();
            try {
                Object value = jsonPrefs.get(key);
                if (value instanceof String) {
                    editor.putString(key, (String) value);
                } else if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    editor.putFloat(key, (Float) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                }
            } catch (JSONException ignored) {
            }
        }
        editor.apply();
    }
}
