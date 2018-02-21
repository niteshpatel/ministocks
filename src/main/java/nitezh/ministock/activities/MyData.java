package nitezh.ministock.activities;

import android.app.Application;
import nitezh.ministock.activities.widget.WidgetRow;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Cristi Arde on 2/11/2018.
 */

public class MyData extends Application{
    public static  List<WidgetRow> myDataList = new ArrayList<WidgetRow>();

    public static List<WidgetRow> getList()
    {
        return myDataList;
    }

    public void setGlobalList(List<WidgetRow> list)
    {
        myDataList = list;
    }
}
