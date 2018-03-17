package nitezh.ministock.activities.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;


/**
 * Created by nicholasfong on 2018-02-09.
 */

public class Bonobo_widget_service extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("Test", "widget service ");
        Bonobo_widget_data_provider dataProvider = new Bonobo_widget_data_provider(
                getApplicationContext(), intent);
        return dataProvider;
    }

}
