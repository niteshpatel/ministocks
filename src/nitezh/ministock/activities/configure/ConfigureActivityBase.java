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

package nitezh.ministock.activities.configure;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import java.util.concurrent.Callable;

import nitezh.ministock.DialogTools;
import nitezh.ministock.activities.widget.WidgetProviderBase;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.WidgetRepository;


abstract class ConfigureActivityBase extends Activity {

    int mWidgetSize = 0;

    @Override
    public boolean onKeyDown(int keyCode, @SuppressWarnings("NullableProblems") KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setupWidget(0);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setupWidget(int size) {
        // Update the widget when we end configuration
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);

            WidgetRepository widgetRepository = new AndroidWidgetRepository(getBaseContext());
            widgetRepository.addWidget(appWidgetId, size);
            WidgetProviderBase.updateWidgetAsync(getApplicationContext(), appWidgetId,
                    WidgetProviderBase.UpdateType.VIEW_UPDATE);
        }
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setVisible(false);
        DialogTools.alertWithCallback(this, "Ministocks Widget Added",
                "Touch the left side of the widget to view setup options.", "Close", null, null,
                new Callable() {
                    public Object call() throws Exception {
                        setupWidget(mWidgetSize);
                        return new Object();
                    }
                });
    }
}
