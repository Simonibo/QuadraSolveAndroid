package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.TextView;

import java.text.DecimalFormat;

import static java.lang.Math.sqrt;

/**
 * Created by Simon on 14.08.2017.
 * The Surfaceview which draws the graph and supports tracing, panning & zooming
 */

public class GraphSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    public TextView rootTextView1, rootTextView2, apexTextView, curpoint;
    private final Paint whiteline = new Paint(), whitePoints = new Paint(), graphPoints = new Paint(), gridLines = new Paint(), black = new Paint();
    private int canvasWidth, canvasHeight;
    private boolean drawPoint = false;
    private float touchX;
    private double xmin, xmax, ymin, ymax;
    private double a, b, c, x1, x2, roots, scheitelx, scheitely;
    private boolean inited;
    private double gridIntervX, gridIntervY;
    String activity;
    private double lastx, lasty;
    private Bitmap bm, bmlastdraw;
    private Canvas canvas;
    private boolean isFirstDrawPoint;

    public GraphSurfaceView(Context context) {
        super(context);
        init();
    }

    public GraphSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GraphSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);
        activity = "Tracing";
        //configure the different paints
        whiteline.setColor(Color.WHITE);
        whiteline.setStrokeWidth(6); //should be 4
        whitePoints.setColor(Color.WHITE);
        whitePoints.setStrokeWidth(20);
        graphPoints.setColor(Color.argb(255, 48, 63, 159));
        graphPoints.setStrokeWidth(6);
        graphPoints.setStyle(Paint.Style.STROKE);  //should be 4
        gridLines.setColor(Color.DKGRAY);
        gridLines.setStrokeWidth(2);
        black.setColor(Color.BLACK);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { draw(); }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }

    private void draw() {
        if(!inited) {
            inited = true;
            Canvas tmp = holder.lockCanvas();
            canvasWidth = tmp.getWidth();
            canvasHeight = tmp.getHeight();
            holder.unlockCanvasAndPost(tmp);
            bm = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bm);
            //get the different values from the graph class for easier handling
            x1 = Graph.x1;
            x2 = Graph.x2;
            a = Graph.a;
            b = Graph.b;
            c = Graph.c;
            roots = Graph.roots;
            scheitelx = Graph.scheitelx;
            scheitely = Graph.scheitely;
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
            calculateGridlinePositions();
        }
        if(drawPoint) {
            if(isFirstDrawPoint) {
                bmlastdraw = bm.copy(Bitmap.Config.ARGB_8888, false);
                isFirstDrawPoint = false;
            }
            //get the current state of the canvas
            canvas.drawBitmap(bmlastdraw, 0, 0, whitePoints);
            //Get the touch points' coordinates in the graph's coordinate system
            final double curx = lirp(touchX, 0, canvasWidth, xmin, xmax);
            final double cury = Graph.a * Math.pow(curx, 2) + Graph.b * curx + Graph.c;
            if(cury > ymin && cury < ymax) {
                canvas.drawCircle(touchX, (float) lirp(cury, ymin, ymax, canvasHeight, 0), 10, whitePoints);
            }
            DecimalFormat df = new DecimalFormat("#.####");
            if(curpoint.getVisibility() == INVISIBLE) {
                curpoint.setVisibility(VISIBLE);
            }
            curpoint.setText(getResources().getString(R.string.curpoint) + df.format(curx) + getResources().getString(R.string.semicolon) + df.format(cury));
            Canvas screenCanvas = holder.lockCanvas();
            screenCanvas.drawBitmap(bm, 0, 0, whiteline);
            holder.unlockCanvasAndPost(screenCanvas);
        } else {
            //Pre-drawing done in worker thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    canvas.drawRect(0, 0, canvasWidth, canvasHeight, black);
                    isFirstDrawPoint = true;
                    if(activity.equals("Zooming")) {
                        calculateGridlinePositions();
                    }
                    drawGridLines(canvas);
                    //draw the actual function
                    Path p = new Path();
                    float fofx1 = (float) (a * Math.pow(xmin, 2) + b * xmin + c);
                    p.moveTo(0, (float) lirp(fofx1, ymin, ymax, canvasHeight, 0));
                    p.quadTo(canvasWidth / 2, (float) lirp((fofx1 + (2 * xmin * a + b) * (xmax - xmin) / 2), ymin, ymax, canvasHeight, 0), canvasWidth, (float) lirp((a * Math.pow(xmax, 2) + b * xmax + c), ymin, ymax, canvasHeight, 0));
                    canvas.drawPath(p, graphPoints);
                    //calculate the positions of the axis
                    long xaxis = (int) lirp(0, ymin, ymax, canvasHeight, 0);
                    long yaxis = (int) lirp(0, xmin, xmax, 0, canvasWidth);
                    if (xmin < 0 && xmax > 0) {
                        canvas.drawLine(yaxis, 0, yaxis, canvasHeight, whiteline);
                        canvas.drawLine(yaxis - 35, 35, yaxis, 0, whiteline);
                        canvas.drawLine(yaxis + 35, 35, yaxis, 0, whiteline);
                    }
                    if (ymin < 0 && ymax > 0) {
                        canvas.drawLine(0, xaxis, canvasWidth, xaxis, whiteline);
                        canvas.drawLine(canvasWidth - 35, xaxis - 35, canvasWidth, xaxis, whiteline);
                        canvas.drawLine(canvasWidth - 35, xaxis + 35, canvasWidth, xaxis, whiteline);
                    }
                    //Scheitelpunkt hervorheben
                    canvas.drawCircle(Math.round(lirp(scheitelx, xmin, xmax, 0, canvasWidth)), Math.round(lirp(scheitely, ymin, ymax, canvasHeight, 0)), 15, whitePoints);
                    //Nullstellen hervorheben
                    if (roots > 0) {
                        canvas.drawCircle(Math.round(lirp(x1, xmin, xmax, 0, canvasWidth)), Math.round(lirp(0, ymin, ymax, canvasHeight, 0)), 15,  whitePoints);
                    }
                    if (roots == 2) {
                        canvas.drawCircle(Math.round(lirp(x2, xmin, xmax, 0, canvasWidth)), Math.round(lirp(0, ymin, ymax, canvasHeight, 0)), 15, whitePoints);
                    }
                    post(new Runnable() {
                        @Override
                        public void run() {
                            Canvas screenCanvas = holder.lockCanvas();
                            screenCanvas.drawBitmap(bm, 0, 0, whiteline);
                            holder.unlockCanvasAndPost(screenCanvas);
                        }
                    });
                }
            }).start();
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
        if(activity.equals("Tracing")) {
            boolean nearSomething;
            //Check, wether the touch was close enough to one of the roots and if so, put highlight on the corresponding textview (Not sure yet which highlight to pick)
            if (Graph.roots > 0 && sqrt(Math.pow(event.getX() - lirp(Graph.x1, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2)) < 50) {
                rootTextView1.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                rootTextView1.setTextColor(Color.WHITE); //setTypeface(null, Typeface.NORMAL);
                nearSomething = false;
            }
            if (Graph.roots == 2 && sqrt(Math.pow(event.getX() - lirp(Graph.x2, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2)) < 50) {
                rootTextView2.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                rootTextView2.setTextColor(Color.WHITE);
                nearSomething = false;
            }
            if (sqrt(Math.pow(event.getX() - lirp(Graph.scheitelx, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(Graph.scheitely, ymin, ymax, canvasHeight, 0), 2)) < 50) {
                apexTextView.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                apexTextView.setTextColor(Color.WHITE);
                nearSomething = false;
            }
            if (!nearSomething) {
                touchX = event.getX();
                drawPoint = true;
                draw();
            }
        } else {
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastx = event.getX();
                lasty = event.getY();
            } else if(event.getActionMasked() == MotionEvent.ACTION_MOVE || event.getActionMasked() == MotionEvent.ACTION_UP) {
                double xchange = lirp(event.getX() - lastx, 0, canvasWidth, 0, xmax - xmin);
                xmin -= xchange;
                xmax -= xchange;
                double ychange = lirp(event.getY() - lasty, 0, canvasHeight, 0, ymax - ymin);
                ymin += ychange;
                ymax += ychange;
                lastx = event.getX();
                lasty = event.getY();
                activity = "Panning";
                drawPoint = false;
                draw();
            }
        }
        return true;
    }

    private void calculateGridlinePositions() {
        double xspan = xmax - xmin;
        double yspan = ymax - ymin;
        int magordx = (int) Math.floor(Math.log10(xspan));
        double powx = Math.pow(10, magordx);
        int magordy = (int) Math.floor(Math.log10(yspan));
        double powy = Math.pow(10, magordy);
        int spandurchpowx = (int) Math.floor(xspan / powx);
        int spandurchpowy = (int) Math.floor(yspan / powy);
        if(spandurchpowx == 1) {
            gridIntervX = powx / 5;
        } else if (spandurchpowx < 5){
            gridIntervX = powx / 2;
        } else {
            gridIntervX = powx;
        }
        if(spandurchpowy == 1) {
            gridIntervY = powy / 5;
        } else if (spandurchpowy < 5){
            gridIntervY = powy / 2;
        } else {
            gridIntervY = powy;
        }
    }

    private void drawGridLines(Canvas canvas) {
        for(double d = gridIntervX * Math.ceil(xmin / gridIntervX); d <= xmax; d += gridIntervX) {
            long lirped = (int) lirp(d, xmin, xmax, 0, canvasWidth);
            canvas.drawLine(lirped, 0, lirped, canvasHeight, gridLines);
        }
        for(double d = gridIntervY * Math.ceil(ymin / gridIntervY); d <= ymax; d += gridIntervY) {
            long lirped = (int) lirp(d, ymin, ymax, canvasHeight, 0);
            canvas.drawLine(0, lirped, canvasWidth, lirped, gridLines);
        }
    }

    //todo drawAxisLabels schreiben
    /*
    private void drawAxisLabels(Canvas canvas) {
        double lisx, lisy;
        for(double d = lisx * Math.ceil(xmin / lisx); d <= xmax; d += lisx) {

        }
    }
    */
}