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

package nitezh.ministock.activities.widget;

import android.view.View;
import android.widget.RemoteViews;

import nitezh.ministock.R;
import nitezh.ministock.Storage;
import nitezh.ministock.utils.ReflectionTools;

public class WidgetView {

    private final RemoteViews views;
    private final int arraySize;
    private final int widgetSize;

    public WidgetView(String packageName, Storage storage) {
        widgetSize = storage.getInt("widgetSize", 0);
        String background = storage.getString("background", "transparent");
        Integer imageViewSrcId;
        switch (background) {
            case "transparent":
                if (storage.getBoolean("large_font", false)) {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg_transparent68;
                }
                break;
            case "none":
                imageViewSrcId = R.drawable.blank;
                break;
            default:
                if (storage.getBoolean("large_font", false)) {
                    imageViewSrcId = R.drawable.ministock_bg_large;
                } else {
                    imageViewSrcId = R.drawable.ministock_bg;
                }
                break;
        }

        // Return the matching remote views instance
        RemoteViews views;
        if (widgetSize == 1) {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_1x4_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x4);
            }
        } else if (widgetSize == 2) {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_2x2_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x2);
            }
        } else if (widgetSize == 3) {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_2x4_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_2x4);
            }
        } else {
            if (storage.getBoolean("large_font", false)) {
                views = new RemoteViews(packageName, R.layout.widget_1x2_large);
            } else {
                views = new RemoteViews(packageName, R.layout.widget_1x2);
            }
        }
        views.setImageViewResource(R.id.widget_bg, imageViewSrcId);
        this.views = views;

        // Get the array widgetSize for widgets
        int arraySize = 0;
        if (widgetSize == 0 || widgetSize == 1) {
            arraySize = 4;
        } else if (widgetSize == 2 || widgetSize == 3) {
            arraySize = 10;
        }
        this.arraySize = arraySize;

        // Hide any rows for smaller widgets
        this.displayRows();
    }

    private static int getStockViewId(int line, int col) {
        return ReflectionTools.getField("text" + line + col);
    }

    public void clear() {
        if (widgetSize == 1 || widgetSize == 3) {
            for (int i = 1; i < this.getRowCount() + 1; i++) {
                for (int j = 1; j < 6; j++) {
                    views.setTextViewText(getStockViewId(i, j), "");
                }
            }
        } else {
            for (int i = 1; i < this.getRowCount() + 1; i++) {
                for (int j = 1; j < 4; j++) {
                    views.setTextViewText(getStockViewId(i, j), "");
                }
            }
        }
    }

    public int getRowCount() {
        return arraySize;
    }

    private void displayRows() {
        for (int i = 0; i < 11; i++) {
            int viewId = ReflectionTools.getField("line" + i);
            if (viewId > 0)
                views.setViewVisibility(ReflectionTools.getField("line" + i), View.GONE);
        }
        for (int i = 1; i < arraySize + 1; i++)
            views.setViewVisibility(ReflectionTools.getField("line" + i), View.VISIBLE);
    }

    public RemoteViews getViews() {
        return views;
    }
}
