package nitezh.ministock.domain;

import android.test.AndroidTestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import nitezh.ministock.activities.MyData;
import nitezh.ministock.activities.widget.WidgetRow;
import nitezh.ministock.mocks.MockCache;
import nitezh.ministock.mocks.MockStorage;
import nitezh.ministock.mocks.MockWidgetRepository;

import static nitezh.ministock.activities.MyData.myDataList;
import static org.junit.Assert.assertEquals;

/**
 * Created by GRao on 2/20/2018.
 */
@RunWith(RobolectricTestRunner.class)
public class myDataTest extends AndroidTestCase{

    private StockQuoteRepository stockRepository;
    private MyData testData;
    private Widget testWidget;
    private List<WidgetRow> myList;

    @Before
    public void setUp() {
        int WIDGET_ID = 1;
        int WIDGET_SIZE = 0;

        testWidget = new AndroidWidgetRepository(RuntimeEnvironment.application).addWidget(WIDGET_ID, WIDGET_SIZE);
        testData = new MyData();
        myList = new ArrayList();
        WidgetRow rowInfo = new WidgetRow(testWidget);
        rowInfo.setPrice("500");
        rowInfo.setSymbol("$");
        rowInfo.setVolume("90000");
        myDataList.add(rowInfo);
        testData.setGlobalList(myDataList); //set in global class
    }

    @Test
    public void testDataRetrieval(){

        myList = MyData.getList();          //retrieve from global class
        assertEquals("500", myList.get(0).getPrice());
        assertEquals("$", myList.get(0).getSymbol());
        assertEquals("90000", myList.get(0).getVolume());
    }
}
