package nitezh.ministock.activities.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import nitezh.ministock.R;
import nitezh.ministock.activities.GlobalWidgetData;

/**
 * Created by nicholasfong on 2018-02-09.
 */

public class Bonobo_widget_data_provider implements RemoteViewsService.RemoteViewsFactory {
    List sTickers = new ArrayList();
    List sPrices = new ArrayList();
    List sPercents = new ArrayList();

    List<WidgetRow> stockList = new ArrayList();
    Context mContext = null;
    private int mAppWidgetId;


    public Bonobo_widget_data_provider(Context context, Intent intent) {
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mContext = context;
    }

    @Override
    public int getCount() {
        return sTickers.size();
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
        mView.setTextViewText(R.id.ticker_col, (CharSequence) sTickers.get(position));
        mView.setTextViewText(R.id.price_col, (CharSequence) sPrices.get(position));
        mView.setTextViewText(R.id.percent_col, (CharSequence) sPercents.get(position));

        Bundle extras = new Bundle();
        extras.putInt(WidgetProviderBase.ROW_POSITION, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        mView.setOnClickFillInIntent(R.id.bonobo_item, fillInIntent);

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
        stockList = GlobalWidgetData.getList();
        sTickers.clear();
        sPrices.clear();
        sPercents.clear();

        for (int i = 0; i < stockList.size(); i++) {
            sTickers.add(stockList.get(i).getSymbol());
            sPrices.add(stockList.get(i).getPrice());
            sPercents.add(stockList.get(i).getStockInfo());
        }
    }

    @Override
    public void onDestroy() {

    }
}
