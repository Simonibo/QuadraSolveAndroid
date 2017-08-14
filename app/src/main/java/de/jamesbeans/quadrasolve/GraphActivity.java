package de.jamesbeans.quadrasolve;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;

import static android.view.View.VISIBLE;

public class GraphActivity extends AppCompatActivity {
    static double a, b, c;
    static String astr, bstr, cstr;
    static double x1, x2;
    static int roots;
    static double scheitelx, scheitely;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        assert null != ab;
        ab.setDisplayHomeAsUpEnabled(true);

        setTitle(getResources().getString(R.string.graph));
        //get the different views
        final GraphSurfaceView g = (GraphSurfaceView) findViewById(R.id.parabel);
        final TextView function = (TextView) findViewById(R.id.function);
        final TextView root1 = (TextView) findViewById(R.id.root1);
        final TextView root2 = (TextView) findViewById(R.id.root2);
        final TextView apex = (TextView) findViewById(R.id.apex);
        final TextView curpoint = (TextView) findViewById(R.id.curpoint);
        RadioButton trace = (RadioButton) findViewById(R.id.trace);
        RadioButton pan = (RadioButton) findViewById(R.id.pan);
        Button reset = (Button) findViewById(R.id.reset);

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

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g.inited = false;
                g.drawPoint = false;
                g.draw();
                curpoint.setText("");
            }
        });

        scheitely = c - (b * b) / (4.0 * a);
        scheitelx = -b / (2.0 * a);

        //Pass the TextViews to the GraphView for easy access
        g.rootTextView1 = root1;
        g.rootTextView2 = root2;
        g.apexTextView = apex;
        g.curpoint = curpoint;

        //write the root and apex coordinates into the textviews
        DecimalFormat df = new DecimalFormat("#.####");
        Resources res = getResources();
        apex.setText(res.getString(R.string.fourconcat, apex.getText(), df.format(scheitelx), res.getString(R.string.semicolon), df.format(scheitely)));
        if(0 == roots) {
            root1.setVisibility(VISIBLE);
            if((double) 0 != b) {
                root1.setText(res.getString(R.string.fiveconcat, res.getString(R.string.complexroots), df.format(-b / (2.0 * a)), res.getString(R.string.spaceplusminus), df.format(Math.sqrt(c / a - Math.pow(b / (2.0 * a), 2.0))), res.getString(R.string.i)));
            } else {
                root1.setText(res.getString(R.string.fourconcat, res.getString(R.string.complexroots), res.getString(R.string.plusminus), df.format(Math.sqrt(c / a)), res.getString(R.string.i)));
            }
        }
        if(0 < roots) {
            root1.setVisibility(VISIBLE);
            root1.setText(getResources().getString(R.string.twoconcat, root1.getText(), df.format(x1)));
        }
        if(2 == roots) {
            root2.setVisibility(VISIBLE);
            root2.setText(getResources().getString(R.string.twoconcat, root2.getText(), df.format(x2)));
        }

        CharSequence formula;
        if(1.0 == a) {
            formula = res.getString(R.string.fofxequals) + res.getString(R.string.xsquared);
        } else if(-1.0 == a) {
            formula = "f(x) = -x²";
        } else {
            formula = res.getString(R.string.fofxequals) + astr + res.getString(R.string.xsquared);
        }
        //prepare the different parts of the displayed formula regarding signs and integerness
        if((double) 0 != b) {
            if(1.0 == b) {
                formula = formula + " + x";
            } else if(-1.0 == b) {
                formula = formula + " - x";
            } else if((double) 0 < b) {
                formula = formula + " + " + bstr + res.getString(R.string.x);
            } else {
                formula = formula + " - " + bstr.substring(1) + res.getString(R.string.x);
            }
        }
        if((double) 0 != c) {
            if((double) 0 < c) {
                formula = formula + " + " + cstr;
            } else {
                formula = formula + " - " + cstr.substring(1);
            }
        }
        function.setText(formula);
    }
}
