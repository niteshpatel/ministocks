package nitezh.ministock;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

//http://d.yimg.com/autoc.finance.yahoo.com/autoc?callback=YAHOO.Finance.SymbolSuggest.ssCallback&query=a

/**
 * The main activity for the dictionary. Also displays search results triggered
 * by the search dialog.
 */
public class SymbolSearchableActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symbol_search);

        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // from click on search results
            Intent resultValue = new Intent();
            resultValue.putExtra("symbol", intent.getDataString());
            setResult(RESULT_OK, resultValue);
            finish();

        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            ListAdapter adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            new String[]{String
                                    .format(
                                            "Symbol \'%s\' is not found. Press to add it anyway.",
                                            intent
                                                    .getStringExtra(SearchManager.QUERY))});
            setListAdapter(adapter);

        } else if (Intent.ACTION_EDIT.equals(intent.getAction())) {
            startSearch(SearchManager.QUERY, false, null, false);
        }
    }
}
