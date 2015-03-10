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

package nitezh.ministock.configure;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import nitezh.ministock.PreferenceTools;
import nitezh.ministock.Storage;
import nitezh.ministock.UserData;
import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.WidgetRepository;
import nitezh.ministock.widget.WidgetBase;


abstract class ConfigureBase extends Activity {

    int mWidgetSize = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Setup the widget if the back button is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK)
            setupWidget(0);
        return super.onKeyDown(keyCode, event);
    }

    void setupWidget(int widgetSize) {
        // Update the widget when we end configuration
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            // Add the appWidgetId to our list of added appWidgetIds
            // and add default preferences (view and Stock1)
            Context context = getBaseContext();
            UserData.addAppWidgetSize(context, appWidgetId, widgetSize);
            Storage appStorage = PreferenceTools.getAppPreferences(context);
            WidgetRepository widgetRepository = new AndroidWidgetRepository(context, appStorage);

            // Get widget SharedPreferences
            Storage widgetStorage = widgetRepository.getWidgetStorage(appWidgetId);
            if (widgetSize == 0 || widgetSize == 2)
                widgetStorage.putBoolean("show_percent_change", true);
            widgetStorage.putString("Stock1", "^DJI");
            widgetStorage.putString("Stock1_summary", "Dow Jones Industrial Average");
            widgetStorage.apply();
            // Finally update
            WidgetBase.update(getApplicationContext(), appWidgetId, WidgetBase.VIEW_UPDATE);
        }
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the activity
        this.setVisible(false);
        // Create an alert box that informs the user how to access Setup
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Ministocks Widget Added");
        alertDialog.setMessage("Touch the left side of the widget to view setup options.");
        // Set the close button text, we have to specify an empty onClick
        // handler, even though onDismiss will handle the widget setup
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setupWidget(mWidgetSize);
            }
        });
        alertDialog.show();
    }
}
