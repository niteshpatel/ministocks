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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nitezh.ministock.utils.DateTools;

public class CustomAlarmManager {

    public static final String ALARM_UPDATE = "nitezh.ministock.ALARM_UPDATE";
    private final PreferenceStorage appStorage;
    private final AlarmManager alarmManager;
    private final PendingIntent pendingIntent;

    public CustomAlarmManager(Context context) {
        this.appStorage = PreferenceStorage.getInstance(context);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0,
                new Intent(ALARM_UPDATE), 0);
    }

    private Long getUpdateInterval() {
        return Long.parseLong((this.appStorage.getString("update_interval",
                Long.toString(AlarmManager.INTERVAL_HALF_HOUR))));
    }

    private int getTimeToNextUpdate(Long updateInterval) {
        Double timeToNextUpdate = updateInterval.doubleValue();
        Double elapsedTime = DateTools.elapsedTime(this.appStorage.getString("last_update1", null));
        if (elapsedTime > 0) {
            timeToNextUpdate = Math.max(updateInterval - elapsedTime, 0);
        }

        return timeToNextUpdate.intValue();
    }

    public void setUpdateTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.appStorage.putString("last_update1", formatter.format(new Date()));
        this.appStorage.apply();
    }

    public void reinitialize() {
        this.cancel();

        Long updateInterval = getUpdateInterval();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MILLISECOND, this.getTimeToNextUpdate(updateInterval));
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                updateInterval, pendingIntent);
    }

    public void cancel() {
        this.alarmManager.cancel(this.pendingIntent);
    }
}
