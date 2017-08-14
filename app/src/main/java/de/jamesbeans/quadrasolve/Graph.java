package de.jamesbeans.quadrasolve;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;

import static android.view.View.VISIBLE;

public class Graph extends AppCompatActivity {
    static double a;
    static double b;
    static double c;
    static String astr;
    static String bstr;
    static String cstr;
    static double x1;
    static double x2;
    static int roots;
    static double scheitelx, scheitely;
    static boolean inited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        setTitle(getResources().getString(R.string.graph));
        //get the different views
        final GraphSurfaceView g = (GraphSurfaceView) findViewById(R.id.parabel);
        TextView function = (TextView) findViewById(R.id.function);
        TextView root1 = (TextView) findViewById(R.id.root1);
        TextView root2 = (TextView) findViewById(R.id.root2);
        TextView apex = (TextView) findViewById(R.id.apex);
        TextView curpoint = (TextView) findViewById(R.id.curpoint);
        RadioButton trace = (RadioButton) findViewById(R.id.trace);
        RadioButton pan = (RadioButton) findViewById(R.id.pan);

        trace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g.activity = "Tracing";
            }
        });
        pan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g.activity = "Panning";
            }
        });

        scheitely = c - (b * b) / (4 * a);
        scheitelx = -b / (2 * a);
        inited = true;

        //Pass the TextViews to the GraphView for easy access
        g.rootTextView1 = root1;
        g.rootTextView2 = root2;
        g.apexTextView = apex;
        g.curpoint = curpoint;

        //write the root and apex coordinates into the textviews
        DecimalFormat df = new DecimalFormat("#.####");
        Resources res = getResources();
        apex.setText(res.getString(R.string.fourconcat, apex.getText(), df.format(scheitelx), res.getString(R.string.semicolon), df.format(scheitely)));
        if(roots == 0) {
            root1.setVisibility(VISIBLE);
            if(b != 0) {
                root1.setText(res.getString(R.string.fiveconcat, res.getString(R.string.complexroots), df.format(-b / (2 * a)), res.getString(R.string.spaceplusminus), df.format(Math.sqrt(c / a - Math.pow(b / (2 * a), 2))), res.getString(R.string.i)));
            } else {
                root1.setText(res.getString(R.string.fourconcat, res.getString(R.string.complexroots), res.getString(R.string.plusminus), df.format(Math.sqrt(c / a)), res.getString(R.string.i)));
            }
        }
        if(roots > 0) {
            root1.setVisibility(VISIBLE);
            root1.setText(getResources().getString(R.string.twoconcat, root1.getText(), df.format(x1)));
        }
        if(roots == 2) {
            root2.setVisibility(VISIBLE);
            root2.setText(getResources().getString(R.string.twoconcat, root2.getText(), df.format(x2)));
        }

        CharSequence formula;
        if(a == 1) {
            formula = res.getString(R.string.fofxequals) + res.getString(R.string.xsquared);
        } else if(a == -1) {
            formula = "f(x) = -x²";
        } else {
            formula = res.getString(R.string.fofxequals) + astr + res.getString(R.string.xsquared);
        }
        //prepare the different parts of the displayed formula regarding signs and integerness
        if(b != 0) {
            if(b == 1) {
                formula = formula + " + x";
            } else if(b == -1) {
                formula = formula + " - x";
            } else if(b > 0) {
                formula = formula + " + " + bstr + res.getString(R.string.x);
            } else {
                formula = formula + " - " + bstr.substring(1) + res.getString(R.string.x);
            }
        }
        if(c != 0) {
            if(c > 0) {
                formula = formula + " + " + cstr;
            } else {
                formula = formula + " - " + cstr.substring(1);
            }
        }
        function.setText(formula);
    }
}
