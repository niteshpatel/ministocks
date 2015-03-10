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

package nitezh.ministock.activities;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import nitezh.ministock.R;


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
            ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{String.format("Symbol \'%s\' is not found. Press to add it anyway.", intent.getStringExtra(SearchManager.QUERY))});
            setListAdapter(adapter);
        } else if (Intent.ACTION_EDIT.equals(intent.getAction())) {
            startSearch(SearchManager.QUERY, false, null, false);
        }
    }
}
