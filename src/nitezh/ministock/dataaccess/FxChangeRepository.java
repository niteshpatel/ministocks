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

package nitezh.ministock.dataaccess;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nitezh.ministock.Cache;
import nitezh.ministock.URLData;


public class FxChangeRepository {

    private static final String BASE_URL = "http://ministocks-app.appspot.com/getcurrencydata";

    private boolean hasFxSymbols(List<String> symbols) {
        for (String s : symbols) {
            if (s.contains("=")) {
                return true;
            }
        }
        return false;
    }

    public HashMap<String, String> getChanges(Cache cache, List<String> symbols) {
        HashMap<String, String> changes = new HashMap<>();
        if (!this.hasFxSymbols(symbols)) {
            return changes;
        }

        try {
            JSONObject jsonChanges = retrieveChangesAsJson(cache);
            String symbol;
            for (Iterator<String> iter = jsonChanges.keys(); iter.hasNext(); ) {
                symbol = iter.next();
                changes.put(symbol, jsonChanges.getString(iter.next()));
            }
        } catch (JSONException e) {
            return null;
        }

        return changes;
    }

    public JSONObject retrieveChangesAsJson(Cache cache) throws JSONException {
        return new JSONObject(URLData.getCachedUrlData(BASE_URL, cache, 86400));
    }
}
