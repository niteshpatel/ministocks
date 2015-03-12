package nitezh.ministock.domain;

import org.json.JSONObject;

import nitezh.ministock.Storage;


public interface Widget {
    Storage getStorage();

    void setWidgetPreferencesFromJson(JSONObject jsonPrefs);

    JSONObject getWidgetPreferencesAsJson();

    void setSize(int size);

    @SuppressWarnings("UnusedDeclaration")
    void setPercentChange(boolean b);

    void setStock1(String s);

    void setStock1Summary(String s);

    void save();
}
