package de.jamesbeans.quadrasolve;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity {
    static int lastYesNoAction;
    //static boolean yesNoDismissed;
    //Holds the last inputs
    private SharedPreferences vals;
    //The coefficients of the function
    private EditText aval, bval, cval;
    private String astr, bstr, cstr;
    private double a, b, c;
    //The left (or only) root
    private static double x1;
    //the right root
    private static double x2;
    //the number of roots the function has
    private static int roots;
    public static String nexta, nextb, nextc;
    public static boolean comefromhistory;
    public static boolean offerLanguageChange;
    private NumpadKeyboardView keyboardView;

    public static final String RESCOUNT = "rescount";
    public static final String USEENGLISH = "useenglish";
    public static final String ATEXT = "atext";
    public static final String BTEXT = "btext";
    public static final String CTEXT = "ctext";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aval = (EditText) findViewById(R.id.aval);
        bval = (EditText) findViewById(R.id.bval);
        cval = (EditText) findViewById(R.id.cval);
        aval.setShowSoftInputOnFocus(false);
        bval.setShowSoftInputOnFocus(false);
        cval.setShowSoftInputOnFocus(false);

        keyboardView = (NumpadKeyboardView) findViewById(R.id.keyboardView);
        final Keyboard keyboard = new Keyboard(this, R.xml.numpad);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);

        final DecimalFormatSymbols sym = ((DecimalFormat) NumberFormat.getInstance()).getDecimalFormatSymbols();
        keyboardView.initialized = false;
        keyboardView.texts[13] = String.valueOf(sym.getDecimalSeparator());
        keyboardView.invalidate();

        final KeyboardView.OnKeyboardActionListener lkey = new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) { }

            @Override
            public void onRelease(int primaryCode) { }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                final View focusCurrent = getWindow().getCurrentFocus();
                if(!(focusCurrent instanceof EditText)) return;
                final EditText edittext = (EditText) focusCurrent;
                final Editable editable = edittext.getText();
                final int start = edittext.getSelectionStart();
                switch(primaryCode) {
                    case 67:
                        //delete
                        if(null != editable && 0 < start) editable.delete(start - 1, start);
                        break;
                    case 66:
                        //enter
                        if(Objects.equals(focusCurrent, aval)) {
                            //select aval
                            bval.setFocusableInTouchMode(true);
                            bval.requestFocus();
                        } else if (Objects.equals(focusCurrent, bval)) {
                            //select cval
                            cval.setFocusableInTouchMode(true);
                            cval.requestFocus();
                        } else if (Objects.equals(focusCurrent, cval)) {
                            //compute
                            calculate();
                        }
                        break;
                    case 46:
                        //comma or point
                        assert null != editable;
                        if(!editable.toString().contains(String.valueOf(sym.getDecimalSeparator()))) {
                            if('.' == sym.getDecimalSeparator()) {
                                editable.insert(start, ".");
                            } else if(1 < start || (1 == start && Character.isDigit(editable.toString().charAt(0)))){
                                editable.insert(start, ",");
                            }
                        }
                        break;
                    case 45:
                        //minus
                        if(0 == start && !editable.toString().contains("-")) editable.insert(0, "-");
                        break;
                    default:
                        editable.insert(start, Character.toString((char) primaryCode));
                        break;
                }
            }

            @Override
            public void onText(CharSequence text) { }

            @Override
            public void swipeLeft() { }

            @Override
            public void swipeRight() { }

            @Override
            public void swipeDown() { }

            @Override
            public void swipeUp() { }
        };
        keyboardView.setOnKeyboardActionListener(lkey);

        final View.OnFocusChangeListener lfocus = new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    showCustomKeyboard(v);
                } else hideCustomKeyboard();
            }
        };
        final View.OnClickListener lclick = new View.OnClickListener() {
            @Override public void onClick(View v) {
                showCustomKeyboard(v);
            }
        };
        aval.setOnFocusChangeListener(lfocus);
        aval.setOnClickListener(lclick);
        bval.setOnFocusChangeListener(lfocus);
        bval.setOnClickListener(lclick);
        cval.setOnFocusChangeListener(lfocus);
        cval.setOnClickListener(lclick);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        final Toolbar maintoolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(maintoolbar);

        vals = getSharedPreferences("vals", 0);
        if(comefromhistory) {
            aval.setText(nexta);
            bval.setText(nextb);
            cval.setText(nextc);
            comefromhistory = false;
        } else {
            aval.setText(vals.getString(ATEXT, "3"));
            bval.setText(vals.getString(BTEXT, "2"));
            cval.setText(vals.getString(CTEXT, "-6"));
        }

        if(offerLanguageChange) {
            offerLanguageChange = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Resources r = getResources();
                    displayYesNoDialog(r.getString(R.string.useenglish), r.getString(R.string.yes), r.getString(R.string.no), 1);
                }
            }).start();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override public void onBackPressed() {
        if( isCustomKeyboardVisible() ) hideCustomKeyboard(); else finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SharedPreferences.Editor ed = vals.edit();
        ed.putString(ATEXT, aval.getText().toString());
        ed.putString(BTEXT, bval.getText().toString());
        ed.putString(CTEXT, cval.getText().toString());
        ed.clear();
        ed.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!getSharedPreferences("history", 0).getString("Locale", "").equals(Locale.getDefault().toString())) {
            HistoryActivity.updateHistoryPref(getSharedPreferences("history", 0));
            final char decsep = ((DecimalFormat) NumberFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
            aval.setText(aval.getText().toString().replace(',', decsep).replace('.', decsep));
            bval.setText(bval.getText().toString().replace(',', decsep).replace('.', decsep));
            cval.setText(cval.getText().toString().replace(',', decsep).replace('.', decsep));
        }
        if(comefromhistory) {
            aval.setText(nexta);
            bval.setText(nextb);
            cval.setText(nextc);
            comefromhistory = false;
        }
    }

    private void calculate() {
        try { a = reallyIsNumber(aval.getText().toString(), "a");
        } catch (Error ignored) {
            return;
        }
        try { b = reallyIsNumber(bval.getText().toString(), "b");
        } catch (Error ignored) {
            return;
        }
        try { c = reallyIsNumber(cval.getText().toString(), "c");
        } catch (Error ignored) {
            return;
        }

        if(0 == a) {
            displayErrorDialog(getResources().getString(R.string.notzero));
            return;
        }

        astr = aval.getText().toString();
        bstr = bval.getText().toString();
        cstr = cval.getText().toString();

        //Calculation of p/2, because only that is needed in the calculations
        final double phalbe = (b / a) / 2.0;
        //calculation of q as preparation for the p-q-formula
        final double q = c / a;
        //indirect calculation of the number of roots
        if(0 < phalbe * phalbe - q) {
            //1st root is the left, second the right
            x1 = -phalbe - Math.sqrt(phalbe * phalbe - q);
            x2 = -phalbe + Math.sqrt(phalbe * phalbe - q);
            roots = 2;
            goToGraph();
        } else {
            //check, whether digit1 or 0 roots
            if(0 == phalbe * phalbe - q) {
                //p-q-formula simplifies
                x1 = -phalbe;
                roots = 1;
                goToGraph();
            } else {
                roots = 0;
                //Dialog sollte angezeigt werden: Soll Graph trotzdem gezeichnet werden?
                final Resources r = getResources();
                displayYesNoDialog(r.getString(R.string.norealroots), r.getString(R.string.plotgraph), r.getString(R.string.cancel), 0);
            }
        }
    }

    public void buttonOnClick(View v) {
        calculate();
    }

    private void displayYesNoDialog(String question, String yestext, String notext, int actionId) {
        final YesNoDialogFragment d = new YesNoDialogFragment();
        final Bundle b = new Bundle();
        b.putString("question", question);
        b.putString("positive_text", yestext);
        b.putString("negative_text", notext);
        b.putInt("actionId", actionId);
        d.setArguments(b);
        d.show(getSupportFragmentManager(), "YesNoDialogFragment");
    }

    public void evalYesNo() {
        switch(lastYesNoAction) {
            //Graph without roots
            case 0:
                goToGraph();
                break;
            case 1:
                final SharedPreferences.Editor ed = getDefaultSharedPreferences(getBaseContext()).edit();
                ed.putBoolean(USEENGLISH, true);
                ed.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                });
                break;
        }
    }

    //Displays an error dialog using the errordialogfragment class, has error message as parameter
    private void displayErrorDialog(String message) {
        final ErrorDialogFragment d = new ErrorDialogFragment();
        final Bundle b = new Bundle();
        b.putString("error_message", message);
        d.setArguments(b);
        d.show(getSupportFragmentManager(), "ErrorDialogFragment");
    }

    //passes values to graph activity and opens it
    private void goToGraph() {
        GraphActivity.roots = roots;
        GraphActivity.x1 = x1;
        GraphActivity.x2 = x2;
        GraphActivity.a = a;
        GraphActivity.b = b;
        GraphActivity.c = c;
        GraphActivity.astr = astr;
        GraphActivity.bstr = bstr;
        GraphActivity.cstr = cstr;

        final SharedPreferences hist = getSharedPreferences("history", 0);
        final int rescount = hist.getInt(RESCOUNT, 0);
        final String lasta;
        final String lastb;
        final String lastc;
        if(0 < rescount) {
            lasta = hist.getString("a" + (rescount - 1), "impossible");
            lastb = hist.getString("b" + (rescount - 1), "impossible");
            lastc = hist.getString("c" + (rescount - 1), "impossible");
        } else {
            lasta = "0"; lastb = "0"; lastc = "0";
        }
        if(!lasta.equals(astr) || !lastb.equals(bstr) || !lastc.equals(cstr)) {
            final SharedPreferences.Editor histed = hist.edit();
            histed.putString("a" + rescount, astr);
            histed.putString("b" + rescount, bstr);
            histed.putString("c" + rescount, cstr);
            histed.putInt(RESCOUNT, rescount + 1);
            histed.apply();
        }
        final Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        menu.findItem(R.id.useenglish).setChecked(getDefaultSharedPreferences(getBaseContext()).getBoolean(USEENGLISH, false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_history:
                final Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_help:
                final Intent intent2 = new Intent(this, AboutActivity.class);
                startActivity(intent2);
                return true;
            case R.id.useenglish:
                item.setChecked(!item.isChecked());
                final SharedPreferences.Editor ed = getDefaultSharedPreferences(getBaseContext()).edit();
                ed.putBoolean(USEENGLISH, item.isChecked());
                ed.apply();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideCustomKeyboard() {
        keyboardView.setVisibility(View.GONE);
        keyboardView.setEnabled(false);
    }

    private void showCustomKeyboard(View v) {
        keyboardView.setVisibility(View.VISIBLE);
        keyboardView.setEnabled(true);
        if(null != v) ((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private boolean isCustomKeyboardVisible() {
        return View.VISIBLE == keyboardView.getVisibility();
    }

    private double reallyIsNumber(String str, String name) throws Error {
        final Resources res = getResources();
        //All the possible errors
        if(str.isEmpty()) {
            displayErrorDialog(res.getString(R.string.inputrequested, name));
            throw new Error();
        }
        if(',' == str.charAt(0) || (1 < str.length() && str.substring(0, 2).equals("-,"))) {
            displayErrorDialog(name + res.getString(R.string.nan));
            throw new Error();
        }
        try {
            return Double.parseDouble(str.replace(",", "."));
        } catch(NumberFormatException ignored) {
            displayErrorDialog(name + res.getString(R.string.nan));
            throw new Error();
        }
    }
}