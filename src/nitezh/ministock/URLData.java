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
