package de.jamesbeans.quadrasolve;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Simon on 02.04.2017.
 * This activity provides the history, which is saved even when the app is closed.
 */

public class HistoryActivity extends AppCompatActivity {
    //todolater Nullstellen im Verlauf anzeigen
    private ListView history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar historytoolbar = (Toolbar) findViewById(R.id.historytoolbar);
        setSupportActionBar(historytoolbar);
        ActionBar ab = getSupportActionBar();
        assert null != ab;
        ab.setDisplayHomeAsUpEnabled(true);
        //set all the properties of the history list view, like the clicklistener for the elements
        history = (ListView) findViewById(R.id.historylist);
        history.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                SharedPreferences hist = getSharedPreferences("history", 0);
                int rescount = hist.getInt("rescount", 0);
                MainActivity.nexta = hist.getString("a" + (rescount - position - 1), "0");
                MainActivity.nextb = hist.getString("b" + (rescount - position - 1), "0");
                MainActivity.nextc = hist.getString("c" + (rescount - position - 1), "0");
                MainActivity.comefromhistory = true;
                ((Activity) arg1.getContext()).finish();
            }
        });
        history.setEmptyView(findViewById(R.id.histEmptyText));
        //Change all entries to have the right decimal seperator if the locale has changed
        if(!getSharedPreferences("history", 0).getString("Locale", "").equals(Locale.getDefault().toString())) {
            updateHistoryPref(getSharedPreferences("history", 0));
        }
        updateHistoryList();
    }

    //loads all entries in the history preference into the listview
    private void updateHistoryList() {
        SharedPreferences historypref = getSharedPreferences("history", 0);
        int rescount = historypref.getInt("rescount", 0);
        String[] formulae = new String[rescount];
        for(int i = rescount - 1; -1 < i; --i) {
            formulae[rescount - i - 1] = makeFormulaFromABC(historypref.getString("a" + i, "0"), historypref.getString("b" + i, "0"), historypref.getString("c" + i, "0"));
        }
        history.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_layout, formulae));
    }

    //changes all history preference entries to have the correct decimal seperator
    static void updateHistoryPref(SharedPreferences historypref) {
        int rescount = historypref.getInt("rescount", 0);
        SharedPreferences.Editor histedit = historypref.edit();
        char decsep = ((DecimalFormat) NumberFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
        for (int i = 0; i < rescount; ++i) {
            histedit.putString("a" + i, historypref.getString("a" + i, "0").replace(',', decsep).replace('.', decsep));
            histedit.putString("b" + i, historypref.getString("b" + i, "0").replace(',', decsep).replace('.', decsep));
            histedit.putString("c" + i, historypref.getString("c" + i, "0").replace(',', decsep).replace('.', decsep));
        }
        histedit.putString("Locale", Locale.getDefault().toString());
        histedit.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.historymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_clear:
                //clear all items
                history.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_layout, new String[0]));
                SharedPreferences hist = getSharedPreferences("history", 0);
                SharedPreferences.Editor histed = hist.edit();
                for(int i = 0; i < hist.getInt("rescount", 0); ++i) {
                    histed.remove("a" + i);
                    histed.remove("b" + i);
                    histed.remove("c" + i);
                }
                histed.putInt("rescount", 0);
                histed.apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String makeFormulaFromABC(String a, String b, String c) {
        String formula;
        Resources res = getResources();
        //prepare the different parts of the displayed formula regarding signs and integerness
        formula = res.getString(R.string.fofxequals) + a + res.getString(R.string.xsquared);
        if((double) 0 != Double.parseDouble(b.replace(',', '.'))) {
            formula = formula + signedString(b) + res.getString(R.string.x);
        }
        if((double) 0 != Double.parseDouble(c.replace(',', '.'))) {
            formula = formula + signedString(c);
        }
        return formula;
    }

    private String signedString(String s) {
        Resources res = getResources();
        if((int) '-' == (int) s.charAt(0)) {
            return res.getString(R.string.minus) + s.substring(1);
        } else{
            return res.getString(R.string.plus) + s;
        }
    }
}
