package nitezh.ministock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

/**
 * Created by GRao on 3/14/2018.
 */


@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class SimpleUITesting {

    @Before
    public void setup(){

    }

    @Test
    public void clickListItemTest() throws UiObjectNotFoundException{
        String name = "^DJI";
        UiScrollable listView  = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(100);
        listView.scrollTextIntoView(name);
        listView.waitForExists(5000);
        UiObject listViewItem = listView.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()),""+name+"");
        listViewItem.click();
        System.out.println("\""+name+"\" ListView item was clicked.");
    }
}
