package nitezh.ministock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooSymbolSuggestions {

    private static final String BASE_URL =
            "http://d.yimg.com/autoc.finance.yahoo.com/autoc?callback=YAHOO.Finance.SymbolSuggest.ssCallback&query=";
    public static final Pattern PATTERN_RESPONSE =
            Pattern
                    .compile("YAHOO\\.Finance\\.SymbolSuggest\\.ssCallback\\((\\{.*?\\})\\)");

    /**
     * Get Symbol Suggestions *
     */
    public static List<Map<String, String>> getSuggestions(String query) {
        List<Map<String, String>> suggestions =
                new ArrayList<Map<String, String>>();

        String response;
        try {
            String url = BASE_URL + URLEncoder.encode(query, "UTF-8");
            response = URLData.getURLData(null, url, 86400);

        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            response = null;
        }

        // Return if empty response
        if (response.equals("") || response == null) {
            return suggestions;
        }

        Matcher m = PATTERN_RESPONSE.matcher(response);
        if (m.find()) {
            response = m.group(1);
            try {
                JSONArray jsonA =
                        new JSONObject(response)
                                .getJSONObject("ResultSet")
                                .getJSONArray("Result");

                for (int i = 0; i < jsonA.length(); i++) {

                    Map<String, String> suggestion =
                            new HashMap<String, String>();

                    JSONObject jsonO = jsonA.getJSONObject(i);
                    suggestion.put("symbol", jsonO.getString("symbol"));
                    suggestion.put("name", jsonO.getString("name"));
                    suggestions.add(suggestion);
                }
                return suggestions;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return suggestions;
    }
}
