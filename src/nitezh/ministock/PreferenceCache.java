package nitezh.ministock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class PreferenceCache {

    private static String mCache = "";
    private SharedPreferences prefs = null;

    public PreferenceCache(Context context) {
        if (context != null)
            prefs = Tools.getAppPrefs(context);
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
        if (prefs != null) {
            Editor editor = prefs.edit();
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

    public JSONObject getCache() {

        // Get cache
        if (prefs != null && mCache.equals(""))
            mCache = prefs.getString("JSONcache", "");

        JSONObject cache = new JSONObject();
        try {
            cache = new JSONObject(mCache);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cache;
    }
}
