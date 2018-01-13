package de.jamesbeans.quadrasolve;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MASK;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static de.jamesbeans.quadrasolve.GraphActivity.PANNING;
import static de.jamesbeans.quadrasolve.GraphActivity.TRACING;
import static de.jamesbeans.quadrasolve.GraphActivity.ZOOMING;

/**
 * Displays the graph
 * Created by Simon on 18.08.2017.
 */

public class GraphView extends View {
    public TextView rootTextView1, rootTextView2, apexTextView, curpoint;
    private final Paint whiteline = new Paint(), whitePoints = new Paint(Paint.ANTI_ALIAS_FLAG), graphPoints = new Paint(), gridLines = new Paint(), black = new Paint();
    private final TextPaint labelText = new TextPaint(TextPaint.ANTI_ALIAS_FLAG), superscript = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
    private int canvasWidth, canvasHeight;
    boolean drawPoint;
    private float touchX;
    private double xmin, xmax, ymin, ymax;
    private double a, b, c, x1, x2, scheitelx, scheitely;
    private int roots;
    boolean inited;
    private double gridIntervX, gridIntervY, labelIntervX, labelIntervY, powx, powy;
    private String suprx, supry;
    private float suplengthx, suplengthy, baseheight;
    private HashMap<Double, Float> baselenghts;
    private boolean doScientificX, doScientificY;
    private static final int initialHashMapSize = 50;
    private final DecimalFormat df = new DecimalFormat("#.####");
    String activity;
    private float lastx, lasty;
    private Bitmap bm, bmlastdraw;
    private Canvas canvas;
    private boolean isFirstDrawPoint;
    private ScaleGestureDetector sd;
    private int apid;
    boolean zoomIndependent;
    private final Path p = new Path();

    public GraphView(Context context) {
        super(context);
        init(context);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context ct) {
        sd = new ScaleGestureDetector(ct, new ScaleListener());
        activity = TRACING;
        //configure the different paints
        whiteline.setColor(Color.WHITE);
        whiteline.setStrokeWidth(6); //should be 4
        whitePoints.setColor(Color.WHITE);
        final int highlightPointsStrokeWidth = 20;
        whitePoints.setStrokeWidth(highlightPointsStrokeWidth);
        graphPoints.setColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
        graphPoints.setStrokeWidth(6);
        graphPoints.setStyle(Paint.Style.STROKE); //should be 4
        gridLines.setColor(Color.DKGRAY);
        gridLines.setStrokeWidth(2);
        black.setColor(Color.BLACK);
        labelText.setColor(Color.WHITE);
        final int baseTextSize = 50;
        labelText.setTextSize(baseTextSize);
        baseheight = labelText.getFontSpacing();
        superscript.setColor(Color.WHITE);
        final int superscriptSize = 35;
        superscript.setTextSize(superscriptSize);

        baselenghts = new HashMap<>(initialHashMapSize);
    }

    @Override
    public void onDraw(Canvas actualcanvas) {
        if(!inited) {
            inited = true;
            canvasWidth = actualcanvas.getWidth();
            canvasHeight = actualcanvas.getHeight();
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
            if (2 == roots) {
                xmin = 3 * x1 / 2 - x2 / 2;
                xmax = 3 * x2 / 2 - x1 / 2;
                if(0 < a) {
                    ymax = a * xmin * xmin + b * xmin + c;
                    ymin = -ymax / 2;
                } else {
                    ymin = a * xmin * xmin + b * xmin + c;
                    ymax = -ymin / 2;
                }
            } else {
                final double povertwo = b / (2 * a);
                if (0 < a) {
                    ymin = scheitely - a;
                    ymax = scheitely + 3 * a;
                    xmin = -povertwo - Math.sqrt(Math.pow(povertwo, 2) - (c - ymax) / a);
                    xmax = -povertwo + Math.sqrt(Math.pow(povertwo, 2) - (c - ymax) / a);
                } else {
                    ymax = scheitely - a;
                    ymin = scheitely + 3 * a;
                    xmin = -povertwo - Math.sqrt(Math.pow(povertwo, 2) - (c - ymin) / a);
                    xmax = -povertwo + Math.sqrt(Math.pow(povertwo, 2) - (c - ymin) / a);
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
            actualcanvas.drawBitmap(bmlastdraw, 0, 0, whitePoints);
            //Get the touch points' coordinates in the graph's coordinate system
            final double curx = lirp(touchX, 0, canvasWidth, xmin, xmax);
            final double cury = GraphActivity.a * Math.pow(curx, 2.0) + GraphActivity.b * curx + GraphActivity.c;
            if(cury > ymin && cury < ymax) {
                actualcanvas.drawCircle(touchX, (float) lirp(cury, ymin, ymax, canvasHeight, 0), 10, whitePoints);
            }
            if(INVISIBLE == curpoint.getVisibility()) {
                curpoint.setVisibility(VISIBLE);
            }
            curpoint.setText(getResources().getString(R.string.curpoint) + df.format(curx) + getResources().getString(R.string.semicolon) + df.format(cury));
        } else {
            canvas.drawRect(0, 0, canvasWidth, canvasHeight, black);
            isFirstDrawPoint = true;
            if(Objects.equals(activity, ZOOMING)) {
                calculateGridAndLabelPositions();
            }
            drawGridLines(canvas);
            //draw the actual function
            final float fofx1 = (float) (a * Math.pow(xmin, 2.0) + b * xmin + c);
            p.moveTo(0, (float) lirp(fofx1, ymin, ymax, canvasHeight, 0));
            p.quadTo(canvasWidth >> 1, (float) lirp((fofx1 + (2 * xmin * a + b) * (xmax - xmin) / 2), ymin, ymax, canvasHeight, 0), canvasWidth, (float) lirp((a * Math.pow(xmax, 2) + b * xmax + c), ymin, ymax, canvasHeight, 0));
            canvas.drawPath(p, graphPoints);
            p.reset();
            //calculate the positions of the axis
            final long xaxis = (int) lirp(0, ymin, ymax, canvasHeight, 0);
            final long yaxis = (int) lirp(0, xmin, xmax, 0, canvasWidth);
            final int arrowSize = 35;
            if (0 > xmin && 0 < xmax) {
                canvas.drawLine(yaxis, 0, yaxis, canvasHeight, whiteline);
                if(xaxis >= arrowSize) {
                    canvas.drawLine((yaxis - arrowSize), arrowSize, yaxis, 0, whiteline);
                    canvas.drawLine((yaxis + arrowSize), arrowSize, yaxis, 0, whiteline);
                }
            }
            if (0 > ymin && 0 < ymax) {
                canvas.drawLine(0, xaxis, canvasWidth, xaxis, whiteline);
                if(yaxis <= canvasWidth - arrowSize) {
                    canvas.drawLine((canvasWidth - arrowSize), (xaxis - arrowSize), canvasWidth, xaxis, whiteline);
                    canvas.drawLine((canvasWidth - arrowSize), (xaxis + arrowSize), canvasWidth, xaxis, whiteline);
                }
            }
            //Scheitelpunkt hervorheben
            final int highlightCircleRadius = 15;
            canvas.drawCircle(Math.round(lirp(scheitelx, xmin, xmax, 0, canvasWidth)), Math.round(lirp(scheitely, ymin, ymax, canvasHeight, 0)), highlightCircleRadius, whitePoints);
            //Nullstellen hervorheben
            if (0 < roots) {
                canvas.drawCircle(Math.round(lirp(x1, xmin, xmax, 0, canvasWidth)), Math.round(lirp(0, ymin, ymax, canvasHeight, 0)), highlightCircleRadius,  whitePoints);
            }
            if (2 == roots) {
                canvas.drawCircle(Math.round(lirp(x2, xmin, xmax, 0, canvasWidth)), Math.round(lirp(0, ymin, ymax, canvasHeight, 0)), highlightCircleRadius, whitePoints);
            }
            drawAxisLabels(canvas);
            actualcanvas.drawBitmap(bm, 0, 0, whiteline);
        }
    }

    //maps the value startVal, which ranges from smin to smax, to the range (emin, emax)
    private static double lirp(double startVal, double smin, double smax, double emin, double emax) {
        return emin + (emax - emin) * ((startVal - smin) / (smax - smin));
    }

    //Handles all the touch events on the Graph, which include touching roots, calculated points and the apex
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(Objects.equals(activity, TRACING)) {
            boolean nearSomething = false;
            //Check, wether the touch was close enough to one of the roots and if so, put highlight on the corresponding textview (Not sure yet which highlight to pick)
            final int touchTolerance = 50;
            if (0 < GraphActivity.roots && touchTolerance > Math.sqrt(Math.pow(event.getX() - lirp(GraphActivity.x1, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2))) {
                rootTextView1.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                rootTextView1.setTextColor(Color.WHITE); //setTypeface(null, Typeface.NORMAL);
            }
            if (2 == GraphActivity.roots && touchTolerance > Math.sqrt(Math.pow(event.getX() - lirp(GraphActivity.x2, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(0, ymin, ymax, canvasHeight, 0), 2))) {
                rootTextView2.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                rootTextView2.setTextColor(Color.WHITE);
            }
            if (touchTolerance > Math.sqrt(Math.pow(event.getX() - lirp(GraphActivity.scheitelx, xmin, xmax, 0, canvasWidth), 2) + Math.pow(event.getY() - lirp(GraphActivity.scheitely, ymin, ymax, canvasHeight, 0), 2))) {
                apexTextView.setTextColor(Color.RED); //setTypeface(null, Typeface.BOLD);
                nearSomething = true;
            } else {
                apexTextView.setTextColor(Color.WHITE);
            }
            if (!nearSomething) {
                touchX = event.getX();
                drawPoint = true;
                invalidate();
            }
        } else {
            //let the scaleinspector inspect all touch events
            sd.onTouchEvent(event);
            final int action = event.getAction();
            final int ipid = -1;
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
                        activity = PANNING;
                        drawPoint = false;
                        invalidate();
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
        final int magordx = (int) Math.floor(Math.log10(xspan));
        powx = Math.pow(10, magordx);
        final int magordy = (int) Math.floor(Math.log10(yspan));
        powy = Math.pow(10, magordy);
        final int spandurchpowx = (int) Math.floor(xspan / powx);
        final int spandurchpowy = (int) Math.floor(yspan / powy);
        if(1 == spandurchpowx) {
            gridIntervX = powx / 5;
        } else if (5 > spandurchpowx){
            gridIntervX = powx / 2;
        } else {
            gridIntervX = powx;
        }
        if(1 == spandurchpowy) {
            gridIntervY = powy / 5;
        } else if (5 > spandurchpowy){
            gridIntervY = powy / 2;
        } else {
            gridIntervY = powy;
        }
        labelIntervX = gridIntervX * 2;
        labelIntervY = gridIntervY * 2;
        doScientificX = Math.abs(magordx) >= 3;
        doScientificY = Math.abs(magordy) >= 3;
        suprx = Integer.toString(magordx);
        supry = Integer.toString(magordy);
        suplengthx = superscript.measureText(suprx);
        suplengthy = superscript.measureText(supry);
        baselenghts = new HashMap<>(initialHashMapSize);
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
        final double starty = labelIntervY * Math.ceil(ymin / labelIntervY);
        //calculate the positions of the axis
        final int xaxis = (int) lirp(0, ymin, ymax, canvasHeight, 0);
        final int yaxis = (int) lirp(0, xmin, xmax, 0, canvasWidth);
        final int axislabeldist = 15;
        float yAxisXBase;
        if(yaxis < 0) {
            yAxisXBase = 0;
        } else if(yaxis < canvasWidth) {
            yAxisXBase = yaxis + axislabeldist;
        } else {
            //just to prevent error, is changed in loop
            yAxisXBase = -1;
        }
        final float xAxisYBase;
        if(xaxis < 0) {
            xAxisYBase = baseheight * 0.7f;
        } else if (xaxis > canvasHeight) {
            xAxisYBase = canvasHeight;
        } else {
            xAxisYBase = xaxis + axislabeldist + baseheight * 0.7f;
        }
        final boolean xaxisonscreen = xaxis > 0 && xaxis < canvasHeight;
        final boolean yaxissonscreen = yaxis > 0 && yaxis < canvasWidth;
        final int markerlength = 10;
        for (double d = startx; d <= xmax; d += labelIntervX) {
            if (d != 0) {
                final float lirped = (float) lirp(d, xmin, xmax, 0, canvasWidth);
                if(xaxisonscreen) {
                    canvas.drawLine(lirped, xaxis, lirped, xaxis + markerlength, whiteline);
                }
                if (doScientificX) {
                    final String base = df.format(d / powx) + "x10";
                    final float baseLength;
                    if(baselenghts.containsKey(d)) {
                        baseLength = baselenghts.get(d);
                    } else {
                        baseLength = labelText.measureText(base);
                        baselenghts.put(d, baseLength);
                    }
                    final float offset = (baseLength + suplengthx) / 2;
                    canvas.drawText(base, lirped - offset, xAxisYBase, labelText);
                    final float xAxisSuperYFactor = 0.3f;
                    canvas.drawText(suprx, lirped - offset + baseLength, xAxisYBase - xAxisSuperYFactor * baseheight, superscript);
                } else {
                    final String n = df.format(d);
                    canvas.drawText(n, lirped - labelText.measureText(n) / 2, xAxisYBase, labelText);
                }
            }
        }
        for(double d = starty; d <= ymax; d += labelIntervY) {
            if(d != 0) {
                final float lirped = (float) lirp(d, ymin, ymax, canvasHeight, 0);
                if(yaxissonscreen) {
                    canvas.drawLine(yaxis, lirped, yaxis + markerlength, lirped, whiteline);
                }
                final float yAxisYBaseFactor = 0.25f;
                if (doScientificY) {
                    final String base = df.format(d / powy) + "x10";
                    final float baseLength = labelText.measureText(base);
                    if(yaxis >= canvasWidth) {
                        yAxisXBase = canvasWidth - baseLength - suplengthy;
                    }
                    canvas.drawText(base, yAxisXBase, lirped + yAxisYBaseFactor * baseheight, labelText);
                    final float yAxisSuperYBaseFactor = 0.1f;
                    canvas.drawText(supry, yAxisXBase + baseLength, lirped - yAxisSuperYBaseFactor * baseheight, superscript);
                } else {
                    final String dstr = df.format(d);
                    if(yaxis >= canvasWidth) {
                        yAxisXBase = canvasWidth - labelText.measureText(dstr);
                    }
                    canvas.drawText(dstr, yAxisXBase, lirped + yAxisYBaseFactor * baseheight, labelText);
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
            activity = ZOOMING;
            invalidate();
            return true;
        }
    }
}
