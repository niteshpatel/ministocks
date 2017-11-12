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

package nitezh.ministock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.Spanned;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.Callable;


public class DialogTools {

    public static void showSimpleDialog(Context context, String title, String body) {
        alertWithCallback(context, title, body, "Close", null, null, null);
    }


    public static void alertWithCallback(
            Context context,
            String title,
            String body,
            String positiveButtonText,
            String negativeButtonText,
            final Callable<?> positiveCallback,
            final Callable<?> dismissCallback
    ) {
        // Create dialog
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);

        // Use HTML so we can stick bold in here
        Spanned html = Html.fromHtml(body);
        TextView text = new TextView(context);
        text.setPadding(10, 10, 10, 10);
        text.setTextSize(16);
        text.setText(html);

        // Scroll view to handle longer text
        ScrollView scroll = new ScrollView(context);
        scroll.setPadding(0, 0, 0, 0);
        scroll.addView(text);
        alertDialog.setView(scroll);

        // Set the close button text
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (positiveCallback != null) {
                            try {
                                positiveCallback.call();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
        // Optional negative button
        if (negativeButtonText != null) {
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        // Optional dismiss handler
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dismissCallback != null) {
                    try {
                        dismissCallback.call();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
        alertDialog.show();
    }

    public static void choiceWithCallback(
            Context context,
            String title,
            String negativeButtonText,
            final CharSequence[] choices,
            final InputAlertCallable callable
    ) {
        // Create dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title);

        // List click handler
        alertDialogBuilder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callable != null) {
                    try {
                        callable.setInputValue(choices[which].toString());
                        callable.call();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        // Optional negative button
        if (negativeButtonText != null) {
            alertDialogBuilder.setNegativeButton(negativeButtonText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }

        // Create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        alertDialog.show();
    }

    public static void inputWithCallback(Context context, String title, String body,
                                         String positiveButtonText, String negativeButtonText,
                                         String defaultInputText,
                                         final InputAlertCallable callable) {
        // Create dialog
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(body);

        // Set an EditText view to get user input
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(20, 0, 20, 0);
        LinearLayout layout = new LinearLayout(context);
        final EditText input = new EditText(context);
        if (defaultInputText != null) {
            input.setText(defaultInputText);
        }
        layout.addView(input);
        input.setLayoutParams(layoutParams);
        alertDialog.setView(layout);

        // Set the close button text
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callable != null) {
                            try {
                                callable.setInputValue(input.getText().toString());
                                callable.call();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
        // Optional negative button
        if (negativeButtonText != null) {
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        alertDialog.show();
    }

    public abstract static class InputAlertCallable implements Callable {
        private String inputValue;

        protected String getInputValue() {
            return this.inputValue;
        }

        void setInputValue(String inputValue) {
            this.inputValue = inputValue;
        }
    }
}
