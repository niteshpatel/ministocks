package nitezh.ministock.dataaccess;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.HttpURLConnection;
import java.net.URL;

import nitezh.ministock.activities.ChartActivity;
import nitezh.ministock.activities.GlobalWidgetData;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by GRao on 3/14/2018.
 */

@RunWith(RobolectricTestRunner.class)
public class ChartDataTest {

    String cht;             //Chart Type
    String chd;             //Chart Data  (i.e. t:Data1,Data2,Data2,etc)
    String chds;            //Text Format custom scaling
    String chxt;            //Visible Axes
    String chs;             //Chart Size
    String chtt;            //Chart Title\
    String urlString;
    URL url;
    GlobalWidgetData myData;
    ChartActivity chartActivity;
    Bundle bundle;

    @Before
    public void setUp() {
        cht = "cht=ls";                                             //Line Graph
        chd = "chd=t%3A";                                           //Data of line graph. Must begin with t:
        chds = "chds=a";                                            //Automatic text format scaling
        chxt = "chxt=x%2Cy";                                         //Specify X and Y Axes
        chs = "chs=500x500";                                        //chart size
        chtt = "chtt=Test%20Graph";                                 //Name of Graph
        chartActivity = mock(ChartActivity.class);
        bundle = mock(Bundle.class);
    }

    @Test
    public void generatePassingGraphTest() {
        int var[] = {10, 5, 20, 55};
        String chdVars = "";
        for (int i = 0; i < var.length; i++) {
            if (i == 0) { //first
                chdVars = chdVars + var[i];
            } else
                chdVars = chdVars + "%2C" + var[i];        //middle & end
        }
        urlString = "https://image-charts.com/chart?" + cht + "&" + chd + chdVars + "&" + chds + "&chof=.png&" + chs +
                "&chdls=000000&chco=F56991%2CFF9F80%2CFFC48C%2CD1F2A5%2CEFFAB4&" + chtt + "&" + chxt + "&chdlp=b&chf=bg%2Cs%2CFFFFFF&chbh=10&icwt=false";
        try {
            url = new URL(urlString);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            request.connect();
            assertEquals(request.HTTP_OK, request.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void generateFailingGraphTest() {
        urlString = "https://image-charts.com/chart?" + cht + "&" + chd;

        try {
            url = new URL(urlString);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestProperty("User-Agent", "Chrome/23.0.1271.95");
            request.connect();
            assertEquals(request.HTTP_BAD_REQUEST, request.getResponseCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
