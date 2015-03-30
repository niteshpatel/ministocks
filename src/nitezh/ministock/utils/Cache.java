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


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public abstract class Cache {

    public void put(String key, String data, Integer ttl) {
        JSONObject item = new JSONObject();
        try {
            item.put("value", data);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, ttl);
            item.put("expiry", calendar.getTimeInMillis());

            JSONObject cache = loadCache();
            cache.put(key, item);
            persistCache(cache);
        } catch (JSONException ignored) {
        }
    }

    public String get(String key) {
        try {
            JSONObject item = loadCache().getJSONObject(key);
            if (item.getLong("expiry") > Calendar.getInstance().getTimeInMillis()) {
                return item.getString("value");
            }
        } catch (JSONException ignored) {
        }
        return null;
    }

    protected abstract JSONObject loadCache();

    protected abstract void persistCache(JSONObject cache);
}
