package nitezh.ministock.activities.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import nitezh.ministock.R;
import nitezh.ministock.activities.MyData;
import nitezh.ministock.domain.Widget;

/**
 * Created by nicholasfong on 2018-02-09.
 */

public class Bonobo_widget_data_provider implements RemoteViewsService.RemoteViewsFactory {
    List mTickers = new ArrayList();
    List mPrices = new ArrayList();
    List mPercents = new ArrayList();

    List<WidgetRow> mylist = new ArrayList();
    Context mContext = null;

    public Bonobo_widget_data_provider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mTickers.size();
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
                                            R.layout.bonobo_row_item);
        mView.setTextViewText(R.id.ticker_col, (CharSequence) mTickers.get(position));
        mView.setTextViewText(R.id.price_col, (CharSequence) mPrices.get(position));
        mView.setTextViewText(R.id.percent_col, (CharSequence) mPercents.get(position) );
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
        mTickers.clear();
        mPrices.clear();
        mPercents.clear();
        for (int i=0; i<mylist.size() ; i++)
        {
            mTickers.add(mylist.get(i).getSymbol());
            mPrices.add(mylist.get(i).getPrice());
            mPercents.add(mylist.get(i).getStockInfo());
        }
    }

    @Override
    public void onDestroy() {

    }

}
