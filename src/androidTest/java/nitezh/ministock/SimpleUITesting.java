package nitezh.ministock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.inputmethodservice.Keyboard;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

/**
 * Created by GRao on 3/14/2018.
 */


@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class SimpleUITesting {

    String preferencesResourceId;

    @Before
    public void setup(){
        preferencesResourceId = "nitezh.ministock:id/prefs_but";
    }

    @Test
    public void clickListItemTest() throws UiObjectNotFoundException{
        String stockSymbol = "^DJI";
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiScrollable listView  = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(100);
        listView.scrollTextIntoView(stockSymbol);
        listView.waitForExists(5000);
        UiObject listViewItem = listView.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()),""+stockSymbol+"");
        listViewItem.click();
        Log.i("Base Widget View Click Test",stockSymbol+ " ListView item was clicked.");

        try { mDevice.wait(1000); }
        catch (Exception e) {e.printStackTrace();}

        mDevice.pressBack();
    }

    @Test
    public void clickPreferences() throws UiObjectNotFoundException{
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject button = mDevice.findObject(new UiSelector().resourceId(preferencesResourceId));
        button.clickAndWaitForNewWindow();
    }

    @Test
    public void ManipulateStocks() throws UiObjectNotFoundException{

        String searchFieldResourceId = "android:id/search_src_text";

        //click Preferences Button
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject button = mDevice.findObject(new UiSelector().resourceId(preferencesResourceId));
        button.clickAndWaitForNewWindow();

        //click Stocks setup
        String stockSetup = "Stocks setup";
        UiScrollable preferencesListView  = new UiScrollable(new UiSelector());
        preferencesListView.setMaxSearchSwipes(100);
        preferencesListView.scrollTextIntoView(stockSetup);
        preferencesListView.waitForExists(5000);
        UiObject preferencesListItem = preferencesListView.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()),""+stockSetup+"");
        preferencesListItem.click();

        //Add 2nd Stock
        int index = 1;
        UiScrollable stockListView  = new UiScrollable(new UiSelector());
        stockListView.getChild(new UiSelector().clickable(true).index(index)).click();

        String symbolToAdd = "K";
        UiObject searchField = mDevice.findObject(new UiSelector().resourceId(searchFieldResourceId));
        searchField.setText(symbolToAdd);

        searchField.clickAndWaitForNewWindow(2000);
        mDevice.pressDPadDown();
        mDevice.pressDPadUp();
        mDevice.pressEnter();

        //Change 1st Stock
        index = 0;
        stockListView.getChild(new UiSelector().clickable(true).index(index)).click();

        symbolToAdd = "MMD";
        searchField = mDevice.findObject(new UiSelector().resourceId(searchFieldResourceId));
        searchField.setText(symbolToAdd);

        searchField.clickAndWaitForNewWindow(2000);
        mDevice.pressDPadDown();
        mDevice.pressDPadUp();
        mDevice.pressEnter();
        
        //Remove 2nd Stock
        index = 1;
        stockListView.getChild(new UiSelector().clickable(true).index(index)).click();

        symbolToAdd = " ";
        searchField = mDevice.findObject(new UiSelector().resourceId(searchFieldResourceId));
        searchField.setText(symbolToAdd);

        searchField.clickAndWaitForNewWindow(2000);
        mDevice.pressDPadDown();
        mDevice.pressDPadDown();
        mDevice.pressDPadDown();
        mDevice.pressDPadDown();
        mDevice.pressEnter();

    }
}
