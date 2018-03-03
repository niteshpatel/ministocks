package nitezh.ministock.activities;

import android.app.Application;
import nitezh.ministock.activities.widget.WidgetRow;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Cristi Arde on 2/11/2018.
 */

public class GlobalWidgetData extends Application{
    // Use in population of listview 
    public static  List<WidgetRow> myStockList = new ArrayList<WidgetRow>();

    public static List<WidgetRow> getList()
    {
        return myStockList;
    }

    public void setGlobalList(List<WidgetRow> list)
    {
        myStockList = list;
    }
}
