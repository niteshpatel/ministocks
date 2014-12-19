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
import android.content.SharedPreferences.Editor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

class PreferenceCache {
    private static String mCache = "";
    private SharedPreferences preferences = null;

    public PreferenceCache(Context context) {
        if (context != null)
            preferences = Tools.getAppPreferences(context);
    }

    public void put(String key, String data, Integer ttl) {
        // Get cache
        JSONObject cache = getCache();
        // Set expiration based on ttl
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, ttl);
        Long expiry = calendar.getTimeInMillis();
        // Update cache
        JSONObject item = new JSONObject();
        try {
            item.put("value", data);
            item.put("expiry", expiry);
            cache.put(key, item);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** TODO: Clean up expired items **/
        // Save cache
        mCache = cache.toString();
        if (preferences != null) {
            Editor editor = preferences.edit();
            editor.putString(key, mCache);
            editor.commit();
        }
    }

    public String get(String key) {
        // Get cache
        JSONObject cache = getCache();
        // Get cached value
        try {
            JSONObject item = cache.getJSONObject(key);
            // Return null if we are expired
            Calendar calendar = Calendar.getInstance();
            if (item.getLong("expiry") < calendar.getTimeInMillis())
                return null;
            return item.getString("value");
        } catch (JSONException e) {
            return null;
        }
    }

    JSONObject getCache() {
        // Get cache
        if (preferences != null && mCache.equals(""))
            mCache = preferences.getString("JSONcache", "");
        JSONObject cache = new JSONObject();
        try {
            cache = new JSONObject(mCache);
        } catch (JSONException ignore) {
        }
        return cache;
    }
}
