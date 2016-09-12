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

import nitezh.ministock.Storage;


public class StorageCache extends Cache {

    private static final String JSON_CACHE = "JsonCache";
    private static String mCache = "";
    private Storage storage = null;

    public StorageCache(Storage storage) {
        if (storage != null) {
            this.storage = storage;
        }
    }

    @Override
    protected void persistCache(JSONObject cache) {
        mCache = cache.toString();
        if (this.storage != null) {
            this.storage.putString(JSON_CACHE, mCache);
            this.storage.apply();
        }
    }

    @Override
    protected JSONObject loadCache() {
        if (storage != null && mCache.equals("")) {
            mCache = storage.getString(JSON_CACHE, "");
        }
        JSONObject cache = new JSONObject();
        try {
            cache = new JSONObject(mCache);
        } catch (JSONException ignore) {
        }

        return cache;
    }
}
