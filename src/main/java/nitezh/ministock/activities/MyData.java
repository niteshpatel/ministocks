package nitezh.ministock.activities;

import android.app.Application;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.RemoteViews;

import nitezh.ministock.PreferenceStorage;
import nitezh.ministock.R;
import nitezh.ministock.WidgetProvider;
import nitezh.ministock.activities.widget.WidgetRow;
import nitezh.ministock.domain.*;
import nitezh.ministock.utils.CurrencyTools;
import nitezh.ministock.utils.NumberTools;
import nitezh.ministock.utils.ReflectionTools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static nitezh.ministock.activities.widget.WidgetProviderBase.UpdateType;
import static nitezh.ministock.activities.widget.WidgetProviderBase.ViewType;

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
