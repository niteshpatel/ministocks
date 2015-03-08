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


class PreferenceCache extends Cache {

    public static final String JSON_CACHE = "JsonCache";
    private static String mCache = "";
    private SharedPreferences preferences = null;

    public PreferenceCache(Context context) {
        if (context != null) {
            preferences = PreferenceTools.getAppPreferences(context);
        }
    }

    @Override
    protected void persistCache(JSONObject cache) {
        mCache = cache.toString();
        if (preferences != null) {
            Editor editor = preferences.edit();
            editor.putString(JSON_CACHE, mCache);
            editor.apply();
        }
    }

    @Override
    protected JSONObject loadCache() {
        if (preferences != null && mCache.equals("")) {
            mCache = preferences.getString(JSON_CACHE, "");
        }
        JSONObject cache = new JSONObject();
        try {
            cache = new JSONObject(mCache);
        } catch (JSONException ignore) {
        }

        return cache;
    }
}
