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

package nitezh.ministock;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Provides search suggestions for a list of words and their definitions.
 */
public class SymbolProvider extends ContentProvider {

    private static final String AUTHORITY = "nitezh.ministocks.stocksymbols";
    private static final int SEARCH_SUGGEST = 0;
    private static final int SHORTCUT_REFRESH = 1;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    /**
     * The columns we'll include in our search suggestions. There are others
     * that could be used to further customise the suggestions, see the docs in
     * {@link SearchManager} for the details on additional columns that are
     * supported.
     */
    private static final String[] COLUMNS = {
            "_id", // must include this column
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    /**
     * Sets up a uri matcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, SHORTCUT_REFRESH);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SHORTCUT_REFRESH);
        return matcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!TextUtils.isEmpty(selection)) {
            throw new IllegalArgumentException("selection not allowed for " + uri);
        }
        if (selectionArgs != null && selectionArgs.length != 0) {
            throw new IllegalArgumentException("selectionArgs not allowed for " + uri);
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            throw new IllegalArgumentException("sortOrder not allowed for " + uri);
        }
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                String query = null;
                if (uri.getPathSegments().size() > 1) {
                    query = uri.getLastPathSegment().toLowerCase();
                }
                return getSuggestions(query);
            case SHORTCUT_REFRESH:
                return null;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    private Cursor getSuggestions(String query) {
        query = query == null ? "" : query.toLowerCase().trim();
        List<Map<String, String>> suggestions = StockSuggestions.getSuggestions(query);

        // Check whether an exact match is found in the symbol
        if (!query.equals("")) {
            boolean symbolFound = false;
            for (Map<String, String> suggestion1 : suggestions) {
                if (suggestion1.get("symbol").equals(query.toUpperCase())) {
                    symbolFound = true;
                    break;
                }
            }
            // If we didn't find the symbol add it as a manual match
            if (!symbolFound) {
                Map<String, String> suggestion = new HashMap<>();
                suggestion.put("symbol", "Use " + query.toUpperCase());
                suggestion.put("name", "");
                suggestions.add(0, suggestion);
            }
        }
        // Add an entry to remove the symbol
        Map<String, String> cancelSuggestion = new HashMap<>();
        cancelSuggestion.put("symbol", "Remove symbol and close");
        cancelSuggestion.put("name", "");
        suggestions.add(cancelSuggestion);

        // Now populate the cursor
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for (int i = 0; i < suggestions.size(); i++) {
            Map<String, String> item = suggestions.get(i);
            String symbol = item.get("symbol");
            cursor.addRow(new Object[]{i, symbol, item.get("name"), item.get("name"), symbol});
        }
        return cursor;
    }

    /**
     * All queries for this provider are for the search suggestion and shortcut
     * refresh mime type.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SHORTCUT_REFRESH:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        return true;
    }
}
