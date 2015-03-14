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

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.WidgetRepository;


public class UserData {

    // Object for intrinsic lock
    public static final Object sFileBackupLock = new Object();

    public static void cleanupPreferenceFiles(Context context) {
        // Remove old preferences if we are upgrading
        ArrayList<String> l = new ArrayList<>();

        // Shared preferences is never deleted
        l.add(context.getString(R.string.prefs_name) + ".xml");
        WidgetRepository repository = new AndroidWidgetRepository(context);
        for (int id : repository.getIds())
            l.add(context.getString(R.string.prefs_name) + id + ".xml");

        // Remove files we do not have an active widget for
        String appDir = context.getFilesDir().getParentFile().getPath();
        File f_shared_preferences = new File(appDir + "/shared_prefs");

        // Check if shared_preferences exists
        // TODO: Work out why this is ever null and an alternative strategy
        if (f_shared_preferences.exists())
            for (File f : f_shared_preferences.listFiles())
                if (!l.contains(f.getName()))
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
    }

    public static void backupWidget(Context context, int appWidgetId, String backupName) {
        try {
            // Get existing data collection from storage if present
            JSONObject backupContainer = new JSONObject();
            String rawJson = readInternalStorage(context, PortfolioStockRepository.WIDGET_JSON);
            if (rawJson != null) {
                backupContainer = new JSONObject(rawJson);
            }
            // Now get data for current widget, append to existing data and write to storage
            WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
            JSONObject backupJson = widgetRepository.getWidget(appWidgetId).getWidgetPreferencesAsJson();
            backupContainer.put(backupName, backupJson);
            writeInternalStorage(context, backupContainer.toString(), PortfolioStockRepository.WIDGET_JSON);
        } catch (JSONException ignored) {
        }
    }

    public static void restoreWidget(Context context, int appWidgetId, String backupName) {
        try {
            // Get existing data collection from storage
            JSONObject backupContainer = new JSONObject(readInternalStorage(context, PortfolioStockRepository.WIDGET_JSON));

            // Update widget with preferences from backup
            WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
            widgetRepository.getWidget(appWidgetId).setWidgetPreferencesFromJson(
                    backupContainer.getJSONObject(backupName));

            // Show confirmation to user
            DialogTools.showSimpleDialog(context, "AppWidgetProvider restored",
                    "The current widget preferences have been restored from your selected backup.");

            // restart activity to force reload of preferences
            Activity activity = ((Activity) context);
            Intent intent = activity.getIntent();
            activity.finish();
            activity.startActivity(intent);
        } catch (JSONException ignored) {
        }
    }

    public static CharSequence[] getWidgetBackupNames(Context context) {
        // Get existing data collection from storage
        try {
            String rawJson = readInternalStorage(context, PortfolioStockRepository.WIDGET_JSON);
            if (rawJson == null) {
                return null;
            }
            JSONObject backupContainer = new JSONObject(readInternalStorage(context, PortfolioStockRepository.WIDGET_JSON));
            Iterator<String> iterator = backupContainer.keys();
            ArrayList<String> backupList = new ArrayList<>();
            while (iterator.hasNext()) {
                backupList.add(iterator.next());
            }
            CharSequence[] backupNames = new String[backupList.size()];
            return backupList.toArray(backupNames);
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static void writeInternalStorage(Context context, String stringData, String filename) {
        try {
            synchronized (UserData.sFileBackupLock) {
                FileOutputStream fos;
                fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(stringData.getBytes());
                fos.close();
            }
            BackupManager backupManager = new BackupManager(context);
            backupManager.dataChanged();
        } catch (IOException ignored) {
        }
    }

    public static String readInternalStorage(Context context, String filename) {
        try {
            StringBuffer fileContent = new StringBuffer();
            synchronized (UserData.sFileBackupLock) {
                FileInputStream fis;
                fis = context.openFileInput(filename);
                byte[] buffer = new byte[1024];
                while (fis.read(buffer) != -1) {
                    fileContent.append(new String(buffer));
                }
            }

            return new String(fileContent);
        } catch (IOException ignored) {
        }

        return null;
    }
}
