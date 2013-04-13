package nitezh.ministock;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class Ministocks extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ministocks);

        Spanned html = Html
                .fromHtml("Note: This is a widget only app.<br /><br />"
                        + "<b>Getting started</b><br /><br />"
                        + "First long-press an empty space on your Home screen to bring up the <i>Add to Home screen</i> menu.<br /><br />"
                        + "Then select the Widgets option and finally, select the <i>Ministocks</i> item from the list of widgets.<br /><br />"
                        + "Multiple widgets can be added, as each widget stores its own data.<br /><br />"
                        + "<b>Press Home or Back to close.</b><br />");

        TextView text = (TextView) findViewById(R.id.ministocks_text);
        text.setText(html);

    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}
