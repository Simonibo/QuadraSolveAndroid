package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;

import static java.lang.Math.sqrt;

public class GraphView extends View {
    public TextView rootTextView1;
    public TextView rootTextView2;
    public TextView apexTextView;
    public TextView cursor;
    public TextView curpoint;
    private final Paint whiteline = new Paint();
    private final Paint whitePoints = new Paint();
    private final Paint graphPoints = new Paint();
    private int canvasWidth;
    private int canvasHeight;
    private boolean drawPoint = false;
    private float touchX;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setDrawingCacheEnabled(true);
        //initialize the different paint
        whiteline.setColor(Color.WHITE);
        whiteline.setStrokeWidth(6); //should be 4
        whitePoints.setColor(Color.WHITE);
        whitePoints.setStrokeWidth(20);
        graphPoints.setColor(Color.argb(255, 48, 63, 159));
        graphPoints.setStrokeWidth(6); //should be 4
    }

    protected void onDraw(Canvas canvas) {
        //get the different values from the graph class for easier handling
        double x1 = Graph.x1;
        double x2 = Graph.x2;
        double a = Graph.a;
        double b = Graph.b;
        double c = Graph.c;
        double roots = Graph.roots;
        double scheitelx = Graph.scheitelx;
        double scheitely = Graph.scheitely;
        super.onDraw(canvas);
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        if(drawPoint) {
            //get the current state of the canvas
            Bitmap bi = getDrawingCache();
            canvas.drawBitmap(bi, 0, 0, whitePoints);
            //Get the touch points' coordinates in the graph's coordinate system
            double curx = lirp(touchX, 0, canvas.getWidth(), xmin, xmax);
            double cury = Graph.a * Math.pow(curx, 2) + Graph.b * curx + Graph.c;
            if(cury > ymin && cury < ymax) {
                canvas.drawCircle(touchX, (float) lirp(cury, ymin, ymax, canvas.getHeight(), 0), 10, whitePoints);
            }
            DecimalFormat df = new DecimalFormat("#.####");
            if(curpoint.getVisibility() == INVISIBLE) {
                curpoint.setVisibility(VISIBLE);
            }
            cursor.setText(df.format(curx) + getResources().getString(R.string.semicolon) + df.format(cury));
            drawPoint = false;
        } else {
            //Calculate xmin, xmax, ymin and ymax
            if (roots == 2) {
                xmin = 1.5 * x1 - 0.5 * x2;
                xmax = 1.5 * x2 - 0.5 * x1;
                if(a > 0) {
                    ymax = a * xmin * xmin + b * xmin + c;
                    ymin = -ymax / 2;
                } else {
                    ymin = a * xmin * xmin + b * xmin + c;
                    ymax = -ymin / 2;
                }
            } else {
                if (a > 0) {
                    ymin = scheitely - a;
                    ymax = scheitely + 3 * a;
                    xmin = -b / (2 * a) - Math.sqrt(Math.pow(b / (2 * a), 2) - (c - ymax) / a);
                    xmax = -b / (2 * a) + Math.sqrt(Math.pow(b / (2 * a), 2) - (c - ymax) / a);
                } else {
                    ymax = scheitely - a;
                    ymin = scheitely + 3 * a;
                    xmin = -b / (2 * a) - Math.sqrt(Math.pow(b / (2 * a), 2) - (c - ymin) / a);
                    xmax = -b / (2 * a) + Math.sqrt(Math.pow(b / (2 * a), 2) - (c - ymin) / a);
                }
            }

            //calculate the increment for the x-value per pixel
            double xincr = (xmax - xmin) / canvas.getWidth();
            double xval;
            double yval;
            //Draw the function
            for (int x = 0; x < canvas.getWidth(); x++) {
                xval = xmin + x * xincr;
                yval = a * Math.pow(xval, 2) + b * xval + c;
                canvas.drawPoint(x, Math.round(lirp(yval, ymin, ymax, canvas.getHeight(), 0)), graphPoints);
            }
            //calculate the positions of the axis
            long xaxis = Math.round(lirp(0, ymin, ymax, canvas.getHeight(), 0));
            long yaxis = Math.round(lirp(0, xmin, xmax, 0, canvas.getWidth()));
            if (xmin < 0 && xmax > 0) {
                canvas.drawLine(yaxis, 0, yaxis, canvas.getHeight(), whiteline);
                canvas.drawLine(yaxis - 35, 35, yaxis, 0, whiteline);
                canvas.drawLine(yaxis + 35, 35, yaxis, 0, whiteline);
            }
            if (ymin < 0 && ymax > 0) {
                canvas.drawLine(0, xaxis, canvas.getWidth(), xaxis, whiteline);
                canvas.drawLine(canvas.getWidth() - 35, xaxis - 35, canvas.getWidth(), xaxis, whiteline);
                canvas.drawLine(canvas.getWidth() - 35, xaxis + 35, canvas.getWidth(), xaxis, whiteline);
            }
            //Scheitelpunkt hervorheben
            //canvas.drawPoint(Math.round(lirp(scheitelx, xmin, xmax, 0, canvas.getWidth())), Math.round(lirp(scheitely, ymin, ymax, canvas.getHeight(), 0)), whitePoints);
            canvas.drawCircle(Math.round(lirp(scheitelx, xmin, xmax, 0, canvas.getWidth())), Math.round(lirp(scheitely, ymin, ymax, canvas.getHeight(), 0)), 15, whitePoints);
            //Nullstellen hervorheben
            if (roots > 0) {
                canvas.drawCircle(Math.round(lirp(x1, xmin, xmax, 0, canvas.getWidth())), Math.round(lirp(0, ymin, ymax, canvas.getHeight(), 0)), 15,  whitePoints);
            }
            if (roots == 2) {
                canvas.drawCircle(Math.round(lirp(x2, xmin, xmax, 0, canvas.getWidth())), Math.round(lirp(0, ymin, ymax, canvas.getHeight(), 0)), 15, whitePoints);
            }
            buildDrawingCache();
        }
    }

    //maps the value startVal, which ranges from smin to smax, to the range (emin, emax)
    private double lirp(double startVal, double smin, double smax, double emin, double emax) {
        return emin + (emax - emin) * ((startVal - smin) / (smax - smin));
    }

    //Handles all the touch events on the Graph, which include touching roots, calculated points and the apex
    @SuppressWarnings("UnusedAssignment")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean nearSomething;
        //Check, wether the touch was close enough to one of the roots and if so, put highlight on the corresponding textview (Not sure yet which highlight to pick)
        if(Graph.roots > 0 && sqrt(Math.pow(event.getX() - lirp(Graph.x1, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2)) < 50) {
            rootTextView1.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
            nearSomething = true;
        } else {
            rootTextView1.setTextColor(Color.WHITE); //setTypeface(null, Typeface.NORMAL);
            nearSomething = false;
        }
        if(Graph.roots == 2 && sqrt(Math.pow(event.getX() - lirp(Graph.x2, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2)) < 50) {
            rootTextView2.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
            nearSomething = true;
        } else {
            rootTextView2.setTextColor(Color.WHITE);
            nearSomething = false;
        }
        if(sqrt(Math.pow(event.getX() - lirp(Graph.scheitelx, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(Graph.scheitely, ymin, ymax, canvasHeight, 0), 2)) < 50) {
            apexTextView.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
            nearSomething = true;
        } else {
            apexTextView.setTextColor(Color.WHITE);
            nearSomething = false;
        }
        if(!nearSomething) {
            touchX = event.getX();
            drawPoint = true;
            invalidate();
        }
        return true;
    }
}
