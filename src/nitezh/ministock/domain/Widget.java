package nitezh.ministock.domain;

import org.json.JSONObject;

import java.util.List;

import nitezh.ministock.Storage;


public interface Widget {
    Storage getStorage();

    void setWidgetPreferencesFromJson(JSONObject jsonPrefs);

    JSONObject getWidgetPreferencesAsJson();

    @SuppressWarnings("UnusedDeclaration")
    void setPercentChange(boolean b);

    void setStock1(String s);

    void setStock1Summary(String s);

    void save();

    int getId();

    int getSize();

    void setSize(int size);

    String getStock(int i);

    int getPreviousView();

    void setView(int view);

    List<String> getSymbols();

    int getSymbolCount();
}
