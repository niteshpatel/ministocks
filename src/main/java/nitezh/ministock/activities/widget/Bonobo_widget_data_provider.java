package nitezh.ministock.activities.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import nitezh.ministock.activities.MyData;
import nitezh.ministock.domain.Widget;

/**
 * Created by nicholasfong on 2018-02-09.
 */

public class Bonobo_widget_data_provider implements RemoteViewsService.RemoteViewsFactory {
    List mCollections = new ArrayList();
    List<WidgetRow> mylist = new ArrayList();
    Context mContext = null;

    public Bonobo_widget_data_provider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCollections.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                android.R.layout.simple_list_item_1);
        mView.setTextViewText(android.R.id.text1, (CharSequence) mCollections.get(position));
        mView.setTextColor(android.R.id.text1, Color.WHITE);
        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        mylist = MyData.getList();
        mCollections.clear();
        for (int i=0; i<mylist.size() ; i++)
        {
            stockRowAdder( mylist.get(i).getSymbol(),
                           mylist.get(i).getPrice(),
                           mylist.get(i).getStockInfo() );
        }
    }

    @Override
    public void onDestroy() {

    }
    public void stockRowAdder(String stName, String stPrice, String stPercent)
    {
        String stockRow = stName + "           " + stPrice + "           " + stPercent;
        mCollections.add(stockRow);
    }

}
