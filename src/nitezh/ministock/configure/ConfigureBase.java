package nitezh.ministock.configure;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import nitezh.ministock.Tools;
import nitezh.ministock.UserData;
import nitezh.ministock.widget.WidgetBase;

public abstract class ConfigureBase extends Activity {

    protected int mWidgetSize = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Setup the widget if the back button is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK)
            setupWidget(0);

        return super.onKeyDown(keyCode, event);
    }

    public void setupWidget(int widgetSize) {

        // Update the widget when we end configuration
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            int appWidgetId =
                    extras.getInt(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);

            Intent resultValue = new Intent();
            resultValue.putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetId);
            setResult(RESULT_OK, resultValue);

            // Add the appWidgetId to our list of added appWidgetIds
            // and add default preferences (view and Stock1)
            Context context = getBaseContext();
            UserData.addAppWidgetSize(context, appWidgetId, widgetSize);
            Editor editor = Tools.getWidgetPrefs(context, appWidgetId).edit();

            if (widgetSize == 0 || widgetSize == 2)
                editor.putBoolean("show_percent_change", true);

            editor.putString("Stock1", "^DJI");
            editor.putString("Stock1_summary", "Dow Jones Industrial Average");
            editor.commit();

            // Finally update
            WidgetBase.update(
                    getApplicationContext(),
                    appWidgetId,
                    WidgetBase.VIEW_UPDATE);
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
        alertDialog
                .setMessage("Touch the left side of the widget to view setup options.");

        // Set the close button text, we have to specify an empty onClick
        // handler, even though onDismiss will handle the widget setup
        alertDialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "Close",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        alertDialog
                .setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setupWidget(mWidgetSize);
                    }
                });

        alertDialog.show();
    }
}
