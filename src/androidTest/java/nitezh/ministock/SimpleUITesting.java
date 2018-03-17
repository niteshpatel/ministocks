package nitezh.ministock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

/**
 * Created by Gurkomal Rao, Jefferson Casimir, Nicholas Fong on 3/14/2018.
 */

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class SimpleUITesting {

    UiDevice mDevice;

    @Before
    public void setup(){
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @After
    public void finish(){
        mDevice.pressHome();    //After every test go back to home screen
    }

    @Test
    public void clickListItemTest() throws UiObjectNotFoundException
    {
        int index = 0;
        UiScrollable listView = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(100);
        listView.waitForExists(5000);
        listView.getChild(new UiSelector().clickable(true).index(index)).click();
    }

    @Test
    public void clickPreferencesTest() throws UiObjectNotFoundException{
        selectPreferences();
    }

    @Test
    public void clickStockSetupTest() throws UiObjectNotFoundException{
        selectPreferences();                        //click Preferences Button
        selectStockSetup();                        //click Stocks setup
        setStock(1, "K");       //Add 2nd Stock
        setStock(0, "MMD");     //Change 1st Stock
        removeStock(1);                     //Remove 2nd Stock
    }

    private void selectPreferences()throws UiObjectNotFoundException{
        String preferencesResourceId = "nitezh.ministock:id/prefs_but";
        UiObject button = mDevice.findObject(new UiSelector().resourceId(preferencesResourceId));
        button.clickAndWaitForNewWindow();
    }

    private void selectStockSetup() throws UiObjectNotFoundException{
        String stockSetup = "Stocks setup";
        UiScrollable preferencesListView  = new UiScrollable(new UiSelector());
        preferencesListView.setMaxSearchSwipes(100);
        preferencesListView.scrollTextIntoView(stockSetup);
        preferencesListView.waitForExists(5000);
        UiObject preferencesListItem = preferencesListView.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()),""+stockSetup+"");
        preferencesListItem.click();
    }

    private UiObject selectStockView(int index)throws UiObjectNotFoundException{
        String searchFieldResourceId = "android:id/search_src_text";
        UiScrollable stockListView  = new UiScrollable(new UiSelector());
        stockListView.getChild(new UiSelector().clickable(true).index(index)).click();
        UiObject searchField = mDevice.findObject(new UiSelector().resourceId(searchFieldResourceId));
        return searchField;
    }

    private void setStock(int index, String symbolToAdd) throws UiObjectNotFoundException{
        UiObject searchField = selectStockView(index);
        searchField.setText(symbolToAdd);
        searchField.clickAndWaitForNewWindow(2000);
        mDevice.pressDPadDown();
        mDevice.pressDPadUp();
        mDevice.pressEnter();
    }

    private void removeStock(int index)throws UiObjectNotFoundException{
        UiObject searchField = selectStockView(index);
        searchField.clearTextField();
        searchField.clickAndWaitForNewWindow(2000);
        mDevice.click(540,200);
    }

}
