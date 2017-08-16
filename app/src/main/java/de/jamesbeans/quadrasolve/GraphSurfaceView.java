package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.TextView;

import java.text.DecimalFormat;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MASK;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Math.sqrt;

/**
 * Created by Simon on 14.08.2017.
 * The Surfaceview which draws the graph and supports tracing, panning & zooming
 */

public class GraphSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder holder;
    public TextView rootTextView1, rootTextView2, apexTextView, curpoint;
    private final Paint whiteline = new Paint(), whitePoints = new Paint(), graphPoints = new Paint(), gridLines = new Paint(), black = new Paint(), labelText = new Paint(), superscript = new Paint();
    private int canvasWidth, canvasHeight;
    boolean drawPoint;
    private float touchX;
    private double xmin, xmax, ymin, ymax;
    private double a, b, c, x1, x2, roots, scheitelx, scheitely;
    boolean inited;
    private double gridIntervX, gridIntervY, labelIntervX, labelIntervY, powx, powy;
    private int magordx, magordy;
    final DecimalFormat df = new DecimalFormat("#.####");;
    String activity;
    private float lastx, lasty;
    private Bitmap bm, bmlastdraw;
    private Canvas canvas;
    private boolean isFirstDrawPoint;
    private ScaleGestureDetector sd;
    private int apid;
    boolean zoomIndependent;
    final int ipid = -1;
    final int arrowSize = 35;
    final int touchTolerance = 50;
    final int highlightCircleRadius = 15;
    final int axislabeldist = 15;

    public GraphSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public GraphSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraphSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context ct) {
        holder = getHolder();
        holder.addCallback(this);
        sd = new ScaleGestureDetector(ct, new ScaleListener());
        activity = "Tracing";
        //configure the different paints
        whiteline.setColor(Color.WHITE);
        whiteline.setStrokeWidth(6.0F); //should be 4
        whitePoints.setColor(Color.WHITE);
        whitePoints.setStrokeWidth(20.0F);
        graphPoints.setColor(Color.argb(255, 48, 63, 159));
        graphPoints.setStrokeWidth(6.0F);
        graphPoints.setStyle(Paint.Style.STROKE);  //should be 4
        gridLines.setColor(Color.DKGRAY);
        gridLines.setStrokeWidth(2.0F);
        black.setColor(Color.BLACK);
        labelText.setColor(Color.WHITE);
        labelText.setTextSize(40);
        superscript.setColor(Color.WHITE);
        superscript.setTextSize(25);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) { draw(); }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { }

    void draw() {
        if(!inited) {
            inited = true;
            final Canvas tmp = holder.lockCanvas();
            canvasWidth = tmp.getWidth();
            canvasHeight = tmp.getHeight();
            holder.unlockCanvasAndPost(tmp);
            bm = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bm);
            //get the different values from the graph class for easier access
            x1 = GraphActivity.x1;
            x2 = GraphActivity.x2;
            a = GraphActivity.a;
            b = GraphActivity.b;
            c = GraphActivity.c;
            roots = GraphActivity.roots;
            scheitelx = GraphActivity.scheitelx;
            scheitely = GraphActivity.scheitely;
            //Calculate xmin, xmax, ymin and ymax
            if (2.0 == roots) {
                xmin = 1.5 * x1 - 0.5 * x2;
                xmax = 1.5 * x2 - 0.5 * x1;
                if(0 < a) {
                    ymax = a * xmin * xmin + b * xmin + c;
                    ymin = -ymax / 2.0;
                } else {
                    ymin = a * xmin * xmin + b * xmin + c;
                    ymax = -ymin / 2.0;
                }
            } else {
                if (0 < a) {
                    ymin = scheitely - a;
                    ymax = scheitely + 3.0 * a;
                    xmin = -b / (2.0 * a) - Math.sqrt(Math.pow(b / (2.0 * a), 2.0) - (c - ymax) / a);
                    xmax = -b / (2.0 * a) + Math.sqrt(Math.pow(b / (2.0 * a), 2.0) - (c - ymax) / a);
                } else {
                    ymax = scheitely - a;
                    ymin = scheitely + 3.0 * a;
                    xmin = -b / (2.0 * a) - Math.sqrt(Math.pow(b / (2.0 * a), 2.0) - (c - ymin) / a);
                    xmax = -b / (2.0 * a) + Math.sqrt(Math.pow(b / (2.0 * a), 2.0) - (c - ymin) / a);
                }
            }
            calculateGridAndLabelPositions();
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
            final double cury = GraphActivity.a * Math.pow(curx, 2.0) + GraphActivity.b * curx + GraphActivity.c;
            if(cury > ymin && cury < ymax) {
                canvas.drawCircle(touchX, (float) lirp(cury, ymin, ymax, canvasHeight, 0), 10.0F, whitePoints);
            }
            if(INVISIBLE == curpoint.getVisibility()) {
                curpoint.setVisibility(VISIBLE);
            }
            curpoint.setText(getResources().getString(R.string.curpoint) + df.format(curx) + getResources().getString(R.string.semicolon) + df.format(cury));
            final Canvas screenCanvas = holder.lockCanvas();
            screenCanvas.drawBitmap(bm, 0, 0, whiteline);
            holder.unlockCanvasAndPost(screenCanvas);
        } else {
            //Pre-drawing done in worker thread
            //new Thread(new Runnable() {
                //@Override
                //public void run() {
                    canvas.drawRect(0, 0, canvasWidth, canvasHeight, black);
                    isFirstDrawPoint = true;
                    if(activity.equals("Zooming")) {
                        calculateGridAndLabelPositions();
                    }
                    drawGridLines(canvas);
                    //draw the actual function
                    final Path p = new Path();
                    final float fofx1 = (float) (a * Math.pow(xmin, 2.0) + b * xmin + c);
                    p.moveTo(0, (float) lirp(fofx1, ymin, ymax, canvasHeight, 0));
                    p.quadTo((canvasWidth / 2), (float) lirp((fofx1 + (2.0 * xmin * a + b) * (xmax - xmin) / 2.0), ymin, ymax, canvasHeight, 0), canvasWidth, (float) lirp((a * Math.pow(xmax, 2.0) + b * xmax + c), ymin, ymax, canvasHeight, 0));
                    canvas.drawPath(p, graphPoints);
                    //calculate the positions of the axis
                    final long xaxis = (int) lirp(0, ymin, ymax, canvasHeight, 0);
                    final long yaxis = (int) lirp(0, xmin, xmax, 0, canvasWidth);
                    if (0 > xmin && 0 < xmax) {
                        canvas.drawLine(yaxis, 0, yaxis, canvasHeight, whiteline);
                        canvas.drawLine((yaxis - arrowSize), arrowSize, yaxis, 0, whiteline);
                        canvas.drawLine((yaxis + arrowSize), arrowSize, yaxis, 0, whiteline);
                    }
                    if (0 > ymin && 0 < ymax) {
                        canvas.drawLine(0, xaxis, canvasWidth, xaxis, whiteline);
                        canvas.drawLine((canvasWidth - arrowSize), (xaxis - arrowSize), canvasWidth, xaxis, whiteline);
                        canvas.drawLine((canvasWidth - arrowSize), (xaxis + arrowSize), canvasWidth, xaxis, whiteline);
                    }
                    //Scheitelpunkt hervorheben
                    canvas.drawCircle(Math.round(lirp(scheitelx, xmin, xmax, 0, canvasWidth)), Math.round(lirp(scheitely, ymin, ymax, canvasHeight, 0)), highlightCircleRadius, whitePoints);
                    //Nullstellen hervorheben
                    if (0 < roots) {
                        canvas.drawCircle(Math.round(lirp(x1, xmin, xmax, 0, canvasWidth)), Math.round(lirp(0, ymin, ymax, canvasHeight, 0)), highlightCircleRadius,  whitePoints);
                    }
                    if (2.0 == roots) {
                        canvas.drawCircle(Math.round(lirp(x2, xmin, xmax, 0, canvasWidth)), Math.round(lirp(0, ymin, ymax, canvasHeight, 0)), highlightCircleRadius, whitePoints);
                    }
                    drawAxisLabels(canvas);
                    //post(new Runnable() {
                        //@Override
                        //public void run() {
                            final Canvas screenCanvas = holder.lockCanvas();
                            screenCanvas.drawBitmap(bm, 0, 0, whiteline);
                            holder.unlockCanvasAndPost(screenCanvas);
                    //    }
                    //});
                //}
            //}).start();
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
            if (0 < GraphActivity.roots && touchTolerance > sqrt(Math.pow(event.getX() - lirp(GraphActivity.x1, xmin, xmax, 0, canvasWidth), 2.0) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2.0))) {
                rootTextView1.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                rootTextView1.setTextColor(Color.WHITE); //setTypeface(null, Typeface.NORMAL);
                nearSomething = false;
            }
            if (2 == GraphActivity.roots && touchTolerance > sqrt(Math.pow(event.getX() - lirp(GraphActivity.x2, xmin, xmax, 0, canvasWidth), 2.0) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2.0))) {
                rootTextView2.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                rootTextView2.setTextColor(Color.WHITE);
                nearSomething = false;
            }
            if (touchTolerance > sqrt(Math.pow(event.getX() - lirp(GraphActivity.scheitelx, xmin, xmax, 0, canvasWidth), 2.0) + Math.pow(event.getY() - lirp(GraphActivity.scheitely, ymin, ymax, canvasHeight, 0), 2.0))) {
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
            //let the scaleinspector inspect all touch events
            sd.onTouchEvent(event);
            final int action = event.getAction();
            switch(action & ACTION_MASK) {
                case ACTION_DOWN:
                    lastx = event.getX();
                    lasty = event.getY();
                    apid = event.getPointerId(0);
                    break;
                case ACTION_MOVE:
                    final int pointerIndex = event.findPointerIndex(apid);
                    final float x = event.getX(pointerIndex);
                    final float y = event.getY(pointerIndex);
                    // Only move if the ScaleGestureDetector isn't processing a gesture.
                    if (!sd.isInProgress()) {
                        final double xchange = lirp(x - lastx, 0, canvasWidth, 0, xmax - xmin);
                        xmin -= xchange;
                        xmax -= xchange;
                        final double ychange = lirp(y - lasty, 0, canvasHeight, 0, ymax - ymin);
                        ymin += ychange;
                        ymax += ychange;
                        activity = "Panning";
                        drawPoint = false;
                        draw();
                    }
                    lastx = event.getX();
                    lasty = event.getY();
                    break;
                case ACTION_UP:
                    apid = ipid;
                    break;
                case ACTION_CANCEL:
                    apid = ipid;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    final int pid = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = event.getPointerId(pid);
                    if (pointerId == apid) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pid == 0 ? 1 : 0;
                        lastx = event.getX(newPointerIndex);
                        lasty = event.getY(newPointerIndex);
                        apid = event.getPointerId(newPointerIndex);
                    }
                    break;
            }
        }
        return true;
    }

    private void calculateGridAndLabelPositions() {
        final double xspan = xmax - xmin;
        final double yspan = ymax - ymin;
        magordx = (int) Math.floor(Math.log10(xspan));
        powx = Math.pow(10.0, magordx);
        magordy = (int) Math.floor(Math.log10(yspan));
        powy = Math.pow(10.0, magordy);
        final int spandurchpowx = (int) Math.floor(xspan / powx);
        final int spandurchpowy = (int) Math.floor(yspan / powy);
        if(1 == spandurchpowx) {
            gridIntervX = powx / 5.0;
        } else if (5 > spandurchpowx){
            gridIntervX = powx / 2.0;
        } else {
            gridIntervX = powx;
        }
        if(1 == spandurchpowy) {
            gridIntervY = powy / 5.0;
        } else if (5 > spandurchpowy){
            gridIntervY = powy / 2.0;
        } else {
            gridIntervY = powy;
        }
        labelIntervX = gridIntervX * 2;
        labelIntervY = gridIntervY * 2;
    }

    private void drawGridLines(Canvas canvas) {
        for(double d = gridIntervX * Math.ceil(xmin / gridIntervX); d <= xmax; d += gridIntervX) {
            final long lirped = (int) lirp(d, xmin, xmax, 0, canvasWidth);
            canvas.drawLine(lirped, 0, lirped, canvasHeight, gridLines);
        }
        for(double d = gridIntervY * Math.ceil(ymin / gridIntervY); d <= ymax; d += gridIntervY) {
            final long lirped = (int) lirp(d, ymin, ymax, canvasHeight, 0);
            canvas.drawLine(0, lirped, canvasWidth, lirped, gridLines);
        }
    }

    private void drawAxisLabels(Canvas canvas) {
        final double startx = labelIntervX * Math.ceil(xmin / labelIntervX);
        boolean doScientificX = Math.abs(magordx) >= 3;
        final double starty = labelIntervY * Math.ceil(ymin / labelIntervY);
        boolean doScientificY = Math.abs(magordy) >= 3;
        //calculate the positions of the axis
        final long xaxis = (int) lirp(0, ymin, ymax, canvasHeight, 0);
        final long yaxis = (int) lirp(0, xmin, xmax, 0, canvasWidth);
        final String suprx = Integer.toString(magordx);
        final String supry = Integer.toString(magordy);
        final float baseheight = labelText.getFontSpacing();
        final float xAxisYBase = xaxis + axislabeldist + baseheight * 0.7f;
        for(double d = startx; d <= xmax; d += labelIntervX) {
            if(d != 0) {
                final float lirped = (float) lirp(d, xmin, xmax, 0, canvasWidth);
                canvas.drawLine(lirped, xaxis, lirped, xaxis + 10, whiteline);
                if (doScientificX) {
                    final String base = df.format(d / powx) + "x10";
                    final float baseLength = labelText.measureText(base);
                    float offset = (baseLength + superscript.measureText(suprx)) / 2;
                    canvas.drawText(base, lirped - offset, xAxisYBase, labelText);
                    canvas.drawText(suprx, lirped - offset + baseLength, xAxisYBase - 0.8f * baseheight, superscript);

                } else {
                    final String n = df.format(d);
                    canvas.drawText(n, lirped - labelText.measureText(n) / 2, xAxisYBase, labelText);
                }
            }
        }
        for(double d = starty; d <= ymax; d += labelIntervY) {
            if(d != 0) {
                final float lirped = (float) lirp(d, ymin, ymax, canvasHeight, 0);
                canvas.drawLine(yaxis, lirped, yaxis + 10, lirped, whiteline);
                if (doScientificY) {
                    final String base = df.format(d / powy) + "x10";
                    final float baseLength = labelText.measureText(base);
                    canvas.drawText(base, yaxis + axislabeldist, lirped + 0.25f * baseheight, labelText);
                    canvas.drawText(supry, yaxis + axislabeldist + baseLength, lirped - 0.1f * baseheight, superscript);
                } else {
                    canvas.drawText(df.format(d), yaxis + axislabeldist, lirped + 0.25f * baseheight, labelText);
                }
            }
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            final double fx = lirp(detector.getFocusX(), 0, canvasWidth, xmin, xmax);
            final double fy = lirp(detector.getFocusY(), 0, canvasHeight, ymin, ymax);
            if(zoomIndependent) {
                final float bsfx = detector.getPreviousSpanX() / detector.getCurrentSpanX() - 1;
                final float bsfy = detector.getPreviousSpanY() / detector.getCurrentSpanY() - 1;
                xmax += (xmax - fx) * bsfx;
                xmin -= (fx - xmin) * bsfx;
                ymax += (ymax - fy) * bsfy;
                ymin -= (fy - ymin) * bsfy;
            } else {
                final float bsf = 1 / detector.getScaleFactor() - 1;
                xmax += (xmax - fx) * bsf;
                xmin -= (fx - xmin) * bsf;
                ymax += (ymax - fy) * bsf;
                ymin -= (fy - ymin) * bsf;
            }
            activity = "Zooming";
            draw();
            return true;
        }
    }
}
