/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel https://github.com/niteshpatel/ministocks

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class URLData {

    /* URL data retrieval without caching */
    public static String getURLData2(String url) {

        // TODO (nasty hack to avoid broken data from Yahoo)
        if (url.indexOf("INDU") == -1) {
            url = url.replace("&s=", "&s=INDU+");
        }

        // Grab the data from the source
        String response = null;
        try {
            // Set connection timeout and socket timeout
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            InputStream stream = connection.getInputStream();

            // Read information out of input stream
            BufferedReader r =
                    new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                builder.append(line + "\n");
            }
            response = builder.toString();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

    /* URL data retrieval that supports caching */
    public static String getURLData(Context context, String url, Integer ttl) {

        String data = null;
        PreferenceCache cache = new PreferenceCache(context);

        // Return cached data if we have it
        if (ttl != null && cache != null) {
            data = cache.get(url);
            if (data != null)
                return data;
        }

        // Get fresh data from the URL
        data = getURLData2(url);

        // Update cache if we have one
        if (cache != null && data != null)
            cache.put(url, data, ttl);

        // Only return strings
        if (data == null)
            data = "";

        return data;
    }
}
