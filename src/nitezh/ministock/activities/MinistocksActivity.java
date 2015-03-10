/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import nitezh.ministock.R;


public class MinistocksActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ministocks);
        Spanned html = Html.fromHtml("Note: This is a widget only app.<br /><br />" + "<b>Getting started</b><br /><br />" + "First long-press an empty space on your Home screen to bring up the <i>Add to Home screen</i> menu.<br /><br />" + "Then select the Widgets option and finally, select the <i>MinistocksActivity</i> item from the list of widgets.<br /><br />" + "Multiple widgets can be added, as each widget stores its own data.<br /><br />" + "<b>Press Home or Back to close.</b><br />");
        TextView text = (TextView) findViewById(R.id.ministocks_text);
        text.setText(html);
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }
}
