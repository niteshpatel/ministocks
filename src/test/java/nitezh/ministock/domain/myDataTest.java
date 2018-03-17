package nitezh.ministock.domain;

import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import nitezh.ministock.activities.GlobalWidgetData;
import nitezh.ministock.activities.widget.WidgetRow;

import static nitezh.ministock.activities.GlobalWidgetData.myStockList;

/**
 * Created by GRao on 2/20/2018.
 */
@RunWith(RobolectricTestRunner.class)
public class myDataTest extends AndroidTestCase {

    private StockQuoteRepository stockRepository;
    private GlobalWidgetData testData;
    private Widget testWidget;
    private List<WidgetRow> myList;

    @Before
    public void setUp() {
        int WIDGET_ID = 1;
        int WIDGET_SIZE = 0;

        testWidget = new AndroidWidgetRepository(RuntimeEnvironment.application).addWidget(WIDGET_ID, WIDGET_SIZE);
        testData = new GlobalWidgetData();
        myList = new ArrayList();
        WidgetRow rowInfo = new WidgetRow(testWidget);
        rowInfo.setPrice("500");
        rowInfo.setSymbol("$");
        rowInfo.setVolume("90000");
        myStockList.add(rowInfo);
        testData.setGlobalList(myStockList); //set in global class
    }

    @Test
    public void testDataRetrieval() {

        myList = GlobalWidgetData.getList();          //retrieve from global class
        assertEquals("500", myList.get(0).getPrice());
        assertEquals("$", myList.get(0).getSymbol());
        assertEquals("90000", myList.get(0).getVolume());
    }
}
