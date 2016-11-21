package noise.phonocardiographygraph.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import noise.phonocardiographygraph.R;
import noise.phonocardiographygraph.signal.Signal;
import noise.phonocardiographygraph.signal.SignalPoint;
import noise.phonocardiographygraph.signal.SignalS;
import noise.phonocardiographygraph.signal.SignalXY;
import noise.phonocardiographygraph.signal.YSignal;

/**
 * Created by Tamas on 2015.03.04..
 */
public class Graph extends View {


    private int fontSize = 18;
    final float densityMultiplier;
    /* Constants */
    // Parameters of the graph in pixels
    public int[] GRAPH_BORDER_DP = {35, 15, 35, 50}; //top right bottom left in dp
    public int[] GRAPH_BORDER = {60, 40, 80, 120};
    public static final int MARK_LENGTH = 8;
    public static final int FONT_WIDTH = 9;
    private int zeroTextHeight = 11;
    private int zeroTextWidth = 11;

    public static final int X_MAX_TICKS = 8;
    public static final int Y_MAX_TICKS = 6;

    public static final int POINT_SIZE = 4;

    public static final double NO_WINDOW = -1.0;

    /* Default graph parameters */
    // x
    public  double DEFAULT_X_MIN = 0.0;
    public  double DEFAULT_X_MAX = 10.0;
    // y
    public  double DEFAULT_Y_MIN = 0.0;
    public  double DEFAULT_Y_MAX = 10.0;

    /* Graph parameters */
    // x
    private double xMin;
    private double xMax;
    private int scaledXMax;
    private int scaledXMin;
    private double xAxisScale;
    private double windowSize = NO_WINDOW;
    private boolean windowed;
    // x=A*x + B
    private double xScaleA;
    private double xScaleB;
    // y
    private double yMax;
    private double yMin;
    private int scaledYMax;
    private int scaledYMin;
    private double yAxisScale;
    // y=A*y + B
    private double yScaleA;
    private double yScaleB;

    private Signal<?> paramCurve = null;
    private ArrayList<Signal<?>> signals;

    private int refreshRate;
    private boolean refreshing;

    private Locale loc = Locale.US;
    private int viewWidth = 0;
    private int viewHeight = 0;

    // Paints
    private Paint axisBlackPaint;
    private Paint axisGrayPaint;
    private Paint gridPaint;
    private Paint curvePaint;
    private Paint axisTitlePaint;
    private Paint titlePaint;

    private boolean isDefault = false;

    public Graph(Context context, AttributeSet attrs) {
        super(context, attrs);
        paramCurve = new SignalS();
        signals = new ArrayList<>();
        densityMultiplier = getContext().getResources().getDisplayMetrics().density;
        for (int i = 0; i < GRAPH_BORDER_DP.length; i++)
            GRAPH_BORDER[i] = Math.round(GRAPH_BORDER_DP[i] * densityMultiplier);
        createPaints();
    }

    public void setDefaultScaling(double xmin, double xmax, double ymin, double ymax) {
        DEFAULT_X_MIN = xmin;
        DEFAULT_X_MAX = xmax;
        DEFAULT_Y_MIN = ymin;
        DEFAULT_Y_MAX = ymax;
    }

    void createPaints() {
        axisBlackPaint = new Paint();
        axisBlackPaint.setColor(Color.BLACK);
        //axisBlackPaint.setTypeface(Typeface.MONOSPACE);
        axisBlackPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.graphFontSize));
        Rect aSize = new Rect();
        axisBlackPaint.getTextBounds("0", 0, "0".length(), aSize);
        zeroTextHeight = aSize.height();
        zeroTextWidth = aSize.width();
        titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.titleFontSize));
        axisTitlePaint = new Paint();
        axisTitlePaint.setColor(Color.BLACK);
        axisTitlePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.axisTitleFontSize));
        // axisBlackPaint.setFontFeatureSettings( Font.BOLD, fontSize););
        axisGrayPaint = new Paint();
        axisGrayPaint.setColor(Color.GRAY);
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);

        curvePaint = new Paint();
        curvePaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateGraphParameters(paramCurve);
        drawGrid(canvas);
        drawAxis(canvas);
        for (Signal<?> s : signals) {
            //System.out.println(s);
            if (!s.isEmpty())
                drawSignal(canvas, s);
        }

    }

    private double calcScale(double range, int maxTicks) {
        double scale = 0.0001f;
        double scaleTemp = scale;
        maxTicks--;
        while ((int) Math.round(range / scale) > maxTicks) {
            scaleTemp = scale;
            if ((int) Math.round(range / scaleTemp) > maxTicks)
                scaleTemp = scale * 2.0f;
            if ((int) Math.round(range / scaleTemp) > maxTicks)
                scaleTemp = scale * 2.5f;
            if ((int) Math.round(range / scaleTemp) > maxTicks)
                scaleTemp = scale * 5.0f;
            if ((int) Math.round(range / scaleTemp) > maxTicks)
                scale *= 10.0f;
            else
                scale = scaleTemp;
        }
        return scale;
    }

    /**
     * Calculates mins, maxes and scales.
     *
     * @param curve
     */
    public void calculateGraphParameters(Signal<?> curve) {
        synchronized (curve) {
            if (curve == null)
                setGraphParametersToDefault();
            else if (curve.isEmpty())
                setGraphParametersToDefault();
            else {
               // System.out.println("size: " + getWidth() + ", " + getHeight());
                isDefault = false;
                yMax = curve.max().doubleValue();
                yMin = curve.min().doubleValue();

                yAxisScale = calcScale(yMax - yMin, Y_MAX_TICKS);
                yMax = yAxisScale * (Math.ceil(yMax / yAxisScale));
                yMin = yAxisScale * (Math.floor(yMin / yAxisScale));
                yScaleA = (getHeight() - GRAPH_BORDER[0] - GRAPH_BORDER[2]) / (yMin - yMax);
                yScaleB = -1.0f * yScaleA * yMax + GRAPH_BORDER[2];
                scaledYMin = scaleAndRoundY(yMin);
                scaledYMax = scaleAndRoundY(yMax);

                if (curve instanceof SignalXY) {
                    xMin = ((SignalXY) curve).getFirst().getX();
                    xMax = ((SignalXY) curve).getLast().getX();
                } else {
                    xMin = ((YSignal<?>) curve).getStartTime();
                    xMax = xMin + ((YSignal<?>) curve).getLength();
                }
                if (windowSize != NO_WINDOW && xMax - xMin > windowSize) {
                    xMin = xMax - windowSize;
                    windowed = true;
                } else {
                    windowed = false;
                }

                xAxisScale = calcScale(xMax - xMin, X_MAX_TICKS);
                if (!refreshing) {
                    xMax = xAxisScale * (Math.ceil(xMax / xAxisScale));
                    xMin = xAxisScale * (Math.floor(xMin / xAxisScale));
                }
                xScaleA = (getWidth() - GRAPH_BORDER[1] - GRAPH_BORDER[3]) / (xMax - xMin);
                xScaleB = -1.0f * xScaleA * xMin + GRAPH_BORDER[3];
                scaledXMax = scaleAndRoundX(xMax);
                scaledXMin = scaleAndRoundX(xMin);
                //System.out.println("YA: " + yScaleA + ", YB: " +yScaleB + ", XA: " + xScaleA + ", XB: " +xScaleB);
                //System.out.println("symin: " + scaledYMin + ", symax: " +scaledYMax + ", sxmin: " + scaledXMin + ", sxmax: " +scaledXMax);
            }
        }
    }


    /**
     * Default params.
     */
    public void setGraphParametersToDefault() {
        if (isDefault == false) {
            xMin = DEFAULT_X_MIN;
            xMax = DEFAULT_X_MAX;
            xAxisScale = calcScale(xMax - xMin, X_MAX_TICKS);
            yMin = DEFAULT_Y_MIN;
            yMax = DEFAULT_Y_MAX;
            yAxisScale = calcScale(yMax - yMin, Y_MAX_TICKS);
            isDefault = true;
        }
        yScaleA = (getHeight() - GRAPH_BORDER[0] - GRAPH_BORDER[2]) / (yMin - yMax);
        yScaleB = -1.0f * yScaleA * yMax + GRAPH_BORDER[2];
        scaledYMin = scaleAndRoundY(yMin);
        scaledYMax = scaleAndRoundY(yMax);
        xScaleA = (getWidth() -  GRAPH_BORDER[1] - GRAPH_BORDER[3]) / (xMax - xMin);
        xScaleB = -1.0f * xScaleA * xMin + GRAPH_BORDER[3];
        scaledXMax = scaleAndRoundX(xMax);
        scaledXMin = scaleAndRoundX(xMin);
        windowed = false;
    }

    int calcYAxisCountOffsetX(String number) {
        Rect textBounds = new Rect();
        int textWidth, lastCharWidth;
        axisBlackPaint.getTextBounds(number,  number.length() - 1, number.length(), textBounds);
        lastCharWidth = textBounds.width();
        axisBlackPaint.getTextBounds(number,  0, number.length(), textBounds);
        textWidth = textBounds.width();

        return textWidth + ((zeroTextWidth - lastCharWidth) / 2);
    }

    int calcXAxisCountOffsetX(String number) {
        Rect textBounds = new Rect();
        axisBlackPaint.getTextBounds(number,  0, number.length(), textBounds);
        return textBounds.width() / 2;
    }

    int calcDigitsAfterDecPoint(double scale) {
        int digitsAfterDecPoint = (int) Math.floor(0.05 + Math.log10(Math.abs(scale)));
        if (digitsAfterDecPoint >= 1)
            return 0;
        double norm = scale / Math.pow(10, digitsAfterDecPoint);
        int plusDigits = 0;
        if (Math.abs(norm - 2.5) <= 0.05)
            plusDigits = 1;
        if (digitsAfterDecPoint >= 0)
            digitsAfterDecPoint = 0;
        else
            digitsAfterDecPoint *= -1;
        return digitsAfterDecPoint + plusDigits;
    }

    /**
     * Draws the axes.
     *
     * @param canvas
     */
    public void drawAxis(Canvas canvas) {

        synchronized (paramCurve) {
            double xCount1, xCount2;
            double yCount;
            int xDigitsAfterDecPoint = calcDigitsAfterDecPoint(xAxisScale);
            int yDigitsAfterDecPoint = calcDigitsAfterDecPoint(yAxisScale);

            int x;
            int y;
            String number;

            // Draw the horizontal lines + numbers
            String yAxisNumberFormat = "%." + yDigitsAfterDecPoint + "f";
            for (yCount = yMin; yCount <= yMax; yCount += yAxisScale) {
                y = scaleAndRoundY(yCount);
                canvas.drawLine(scaledXMin - MARK_LENGTH / 2, y, scaledXMin + MARK_LENGTH / 2, y, axisBlackPaint);
                number = String.format(loc, yAxisNumberFormat, yCount);
                canvas.drawText(number, scaledXMin - (4 + calcYAxisCountOffsetX(number) + MARK_LENGTH), y + (zeroTextHeight / 2), axisBlackPaint);
            }
            canvas.drawLine(scaledXMin - MARK_LENGTH / 2, scaledYMax, scaledXMax + MARK_LENGTH / 2, scaledYMax, axisGrayPaint);
            canvas.drawLine(scaledXMin - MARK_LENGTH / 2, scaledYMin, scaledXMax + MARK_LENGTH / 2, scaledYMin, axisGrayPaint);
            canvas.drawLine(scaledXMin, scaledYMin + MARK_LENGTH / 2, scaledXMin, scaledYMax - MARK_LENGTH / 2, axisGrayPaint);

            // Draw the vertical line + numbers
            boolean writeNumber;
            String xAxisNumberFormat = "%." + xDigitsAfterDecPoint + "f";
            for (xCount1 = Math.rint(xMin / xAxisScale) * xAxisScale; xCount1 <= xMax; xCount1 += xAxisScale) {
                if (!windowed) {
                    if (xCount1 > xMin + xAxisScale * 0.1) {
                        xCount2 = xCount1;
                        x = scaleAndRoundX(xCount2);
                        writeNumber = true;
                    } else if (xCount1 < xMin - xAxisScale * 0.1) {
                        xCount2 = xMin;
                        x = scaledXMin;
                        writeNumber = false;
                    } else {
                        xCount2 = xCount1;
                        x = scaledXMin;
                        writeNumber = true;
                    }
                } else {
                    if (xCount1 > xMin + xAxisScale * 0.001) {
                        xCount2 = xCount1;
                        x = scaleAndRoundX(xCount2);
                        writeNumber = true;
                    } else if (xCount1 < xMin - xAxisScale * 0.001) {
                        xCount2 = xMin;
                        x = scaledXMin;
                        writeNumber = false;
                    } else {
                        xCount2 = xCount1;
                        x = scaledXMin;
                        writeNumber = true;
                    }
                }
                canvas.drawLine(x, scaledYMin + MARK_LENGTH / 2, x, scaledYMin - MARK_LENGTH / 2, axisBlackPaint);
                number = String.format(loc, xAxisNumberFormat, xCount2);
                if (writeNumber)
                   canvas.drawText(number, x - calcXAxisCountOffsetX(number),
                                scaledYMin + 4 + (MARK_LENGTH / 2) + zeroTextHeight, axisBlackPaint);
            }
            canvas.drawLine(scaledXMax, scaledYMin + MARK_LENGTH / 2, scaledXMax, scaledYMax - MARK_LENGTH / 2, axisBlackPaint);

            // Draw titles
            if (paramCurve != null)
                if (paramCurve.getTitle() != null) {
                    Rect rect = new Rect();
                    titlePaint.getTextBounds(paramCurve.getTitle(), 0, paramCurve.getTitle().length(), rect);
                    canvas.drawText(paramCurve.getTitle(), scaledXMin + ((scaledXMax - scaledXMin) / 2), scaledYMax - GRAPH_BORDER[0]
                                    / 2 + rect.height(), titlePaint);
                }
            if (paramCurve.getxAxisTitle() != null) {
                canvas.drawText(paramCurve.getxAxisTitle(),  scaledXMin + ((scaledXMax - scaledXMin) / 2) -
                        (FONT_WIDTH * paramCurve.getxAxisTitle().length() / 2), scaledYMin + (GRAPH_BORDER[2] / 2) + (2 * FONT_WIDTH), axisBlackPaint);
            }
            if (paramCurve.getyAxisTitle() != null) {
                canvas.save();
                float h = getHeight();
                canvas.rotate(-90.0f, 0, h);
                canvas.translate(0.0f, h);
                Rect textSize = new Rect();
                axisBlackPaint.getTextBounds(paramCurve.getyAxisTitle(), 0, paramCurve.getyAxisTitle().length(), textSize);
                //System.out.println("Y min: " + scaledYMin + ",Y max: " + scaledYMax + ",X min: " + scaledXMin + ",X max: " + scaledXMax);
                //canvas.drawCircle(GRAPH_BORDER[2] + (scaledYMin - scaledYMax) / 2, 0, 100, axisBlackPaint);
                canvas.drawText(paramCurve.getyAxisTitle(), GRAPH_BORDER[2] + ((scaledYMin - scaledYMax) / 2) - (textSize.width() / 2), (GRAPH_BORDER[3]) - (8 * zeroTextWidth), axisBlackPaint);
                canvas.restore();
            }

        }
    }


    /**
     * Draws the grid.
     *
     * @param canvas
     */
    public void drawGrid(Canvas canvas) {
        double yCount;
        int y;
        // Draw the horizontal lines
        for (yCount = yMin; yCount < yMax; yCount += yAxisScale) {
            y = scaleAndRoundY(yCount);
            canvas.drawLine(scaledXMin - MARK_LENGTH / 2, y, scaledXMax + MARK_LENGTH / 2, y, gridPaint);
        }
    }

    /**
     * Draws a signal. Uses the private functions to draw different kinds of signals.
     *
     * @param canvas
     * @param signal
     */
    public void drawSignal(Canvas canvas, Signal<?> signal) {
        curvePaint.setColor(signal.getColor());
        switch (signal.getGraphType()) {
            case LINE:
                if (signal instanceof SignalXY)
                    drawCurve(canvas, (SignalXY) signal);
                else if (signal instanceof YSignal<?>)
                    drawCurve(canvas, (YSignal<?>) signal);
                break;
            case POINTS:
                if (signal instanceof SignalXY)
                    drawPoints(canvas, (SignalXY) signal);
                else if (signal instanceof YSignal<?>)
                    drawPoints(canvas, (YSignal<?>) signal);
                break;
            case LINE_AND_POINTS:
                if (signal instanceof SignalXY) {
                    drawCurve(canvas, (SignalXY) signal);
                    drawPoints(canvas, (SignalXY) signal);
                } else if (signal instanceof YSignal<?>) {
                    drawCurve(canvas, (YSignal<?>) signal);
                    drawPoints(canvas, (YSignal<?>) signal);
                }
                break;
            case SIGNS:
                if (signal instanceof SignalXY)
                    drawSigns(canvas, (SignalXY) signal);
                else if (signal instanceof YSignal<?>)
                    drawSigns(canvas, (YSignal<?>) signal);
                break;
        }
    }


	/*
     * Private functions to draw signals.
	 */

    private void drawCurve(Canvas canvas, SignalXY curve) {
        synchronized (curve) {
            //System.out.println(curve.size());
            //System.out.println("xygraph");
            Iterator<SignalPoint> curveIterator = curve.iterator();
            SignalPoint point = curveIterator.next();
            int x1, y1, x2, y2;
            double x = point.getX();
            while (curveIterator.hasNext() && x < xMin) {
                point = curveIterator.next();
                x = point.getX();
            }

            x2 = scaleAndRoundX(point.getX());
            y2 = scaleAndRoundY(point.getY());
            while (curveIterator.hasNext()) {
                x1 = x2;
                y1 = y2;
                point = curveIterator.next();
                x2 = scaleAndRoundX(point.getX());
                y2 = scaleAndRoundY(point.getY());
                //System.out.println("x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2);
                if (scaledPointIsOnGraph(x1, y1) && scaledPointIsOnGraph(x2, y2)) {
                    //System.out.println("x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2);
                    canvas.drawLine(x1, y1, x2, y2, curvePaint);
                }
            }
        }
    }

    private void drawCurve(Canvas canvas, YSignal<?> curve) {
        synchronized (curve) {
            //canvas.setColor(curve.getColor());

            int x1, y1, x2, y2;
            int startIndex = (int) Math.floor((xMin - curve.getStartTime()) / curve.getDt());
            if (startIndex < 0)
                startIndex = 0;
            x2 = scaleAndRoundX(curve.getX(startIndex));
            y2 = scaleAndRoundY(curve.get(startIndex).doubleValue());
            for (int i = startIndex + 1; i < curve.size(); i++) {
                x1 = x2;
                y1 = y2;
                x2 = scaleAndRoundX(curve.getX(i));
                y2 = scaleAndRoundY(curve.get(i).doubleValue());
                if (scaledPointIsOnGraph(x1, y1) && scaledPointIsOnGraph(x2, y2))
                    canvas.drawLine(x1, y1, x2, y2, curvePaint);
            }
        }
    }

    private void drawPoints(Canvas canvas, SignalXY curve) {
        synchronized (curve) {
            Iterator<SignalPoint> curveIterator = curve.iterator();
            SignalPoint point1 = curveIterator.next();
            double x = point1.getX();
            while (curveIterator.hasNext() && x < xMin) {
                point1 = curveIterator.next();
                x = point1.getX();
            }
            int x1 = scaleAndRoundX(point1.getX());
            int y1 = scaleAndRoundY(point1.getY());
            //canvas.setColor(curve.getColor());
            if (scaledPointIsOnGraph(x1, y1))
                canvas.drawRect(x1 - POINT_SIZE / 2, y1 - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE, curvePaint);
            while (curveIterator.hasNext()) {
                point1 = curveIterator.next();
                x1 = scaleAndRoundX(point1.getX());
                y1 = scaleAndRoundY(point1.getY());
                if (scaledPointIsOnGraph(x1, y1))
                    canvas.drawRect(x1 - POINT_SIZE / 2, y1 - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE, curvePaint);
            }
        }
    }

    private void drawPoints(Canvas canvas, YSignal<?> curve) {
        synchronized (curve) {
            int startIndex = (int) Math.floor((xMin - curve.getStartTime()) / curve.getDt());
            if (startIndex < 0)
                startIndex = 0;
            int x1, y1;
            //canvas.setColor(curve.getColor());
            for (int i = startIndex; i < curve.size(); i++) {
                x1 = scaleAndRoundX(curve.getX(i));
                y1 = scaleAndRoundY(curve.get(i).doubleValue());
                if (scaledPointIsOnGraph(x1, y1))
                    canvas.drawRect(x1 - POINT_SIZE / 2, y1 - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE, curvePaint);
            }
        }
    }

    /*
     *
     */
    private void drawSigns(Canvas canvas, SignalXY points) {
        synchronized (points) {
            Iterator<SignalPoint> curveIterator = points.iterator();
            SignalPoint point1 = curveIterator.next();
            double x = point1.getX();
            while (curveIterator.hasNext() && x < xMin) {
                point1 = curveIterator.next();
                x = point1.getX();
            }
            int x1 = scaleAndRoundX(point1.getX());
            int y1 = scaleAndRoundY(point1.getY());
            //canvas.setColor(points.getColor());
            if (scaledPointIsOnGraph(x1, y1))
                canvas.drawLine(x1, scaledYMin, x1, scaledYMax, curvePaint);
            while (curveIterator.hasNext()) {
                point1 = curveIterator.next();
                x1 = scaleAndRoundX(point1.getX());
                y1 = scaleAndRoundY(point1.getY());
                if (scaledPointIsOnGraph(x1, y1))
                    canvas.drawLine(x1, scaledYMin, x1, scaledYMax, curvePaint);
            }
        }
    }

    private void drawSigns(Canvas canvas, YSignal<?> points) {
        synchronized (points) {
            int startIndex = (int) Math.floor((xMin - points.getStartTime()) / points.getDt());
            if (startIndex < 0)
                startIndex = 0;
            int x1;
            //canvas.setColor(points.getColor());
            for (int i = startIndex; i < points.size(); i++) {
                x1 = scaleAndRoundX(points.getX(i));
                if (scaledXValueIsOnGraph(x1))
                    canvas.drawLine(x1, scaledYMin, x1, scaledYMax, curvePaint);
            }
        }
    }


    private float pxToDip(int px) {
        return px * densityMultiplier;
    }

    private int dipToPx(float dip) {
        return (int) Math.rint(dip / densityMultiplier);
    }


    @SuppressWarnings("unused")
    private boolean pointIsOnGraph(double x, double y) {
        return (x >= xMin && x <= xMax && y >= yMin && y <= yMax);
    }


    private boolean scaledPointIsOnGraph(int x, int y) {
        return (x >= scaledXMin && x <= scaledXMax && y <= scaledYMin && y >= scaledYMax);
    }

    @SuppressWarnings("unused")
    private boolean yValueIsOnGraph(double y) {
        return y >= yMin && y <= yMax;
    }

    @SuppressWarnings("unused")
    private boolean scaledYValueIsOnGraph(int y) {

        return y <= scaledYMin && y >= scaledYMax;
    }

    @SuppressWarnings("unused")
    private boolean xValueIsOnGraph(double x) {

        return x >= xMin && x <= xMax;
    }

    private boolean scaledXValueIsOnGraph(int x) {

        return x >= scaledXMin && x <= scaledXMax;
    }

    /*
     *
     */
    private int scaleAndRoundY(double y) {
        return (int) Math.round(yScaleA * y + yScaleB);
    }

    /*
     *
     */
    private int scaleAndRoundX(double x) {
        return (int) Math.round(xScaleA * x + xScaleB);
    }

    /**
     * Resets the graph.
     */
    public void reset() {
        paramCurve = null;
        signals.clear();
    }

    /**
     * @param paramCurve the curve used to calculate the graph scaling.
     */
    public void setParamCurve(Signal<?> paramCurve) {
        this.paramCurve = paramCurve;
    }

    /**
     * @param signal signal to draw.
     */
    public void addSignal(Signal<?> signal) {
        signals.add(signal);
    }

    /**
     * @return the refresh rate.
     */
    public int getRefreshRate() {
        return refreshRate;
    }

    /**
     * @param refreshRate the nwe refresh rate.
     */
    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    /**
     * @return is refreshing.
     */
    public boolean isRefreshing() {
        return refreshing;
    }

    /**
     * @param refreshing refreshing.
     */
    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    /**
     * @return the max width of the graph.
     */
    public double getWindowSize() {
        return windowSize;
    }

    /**
     * @param windowSize the new max width of the graph.
     */
    public void setWindowSize(double windowSize) {
        this.windowSize = windowSize;
    }

    /**
     * @return font size.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * @param fontSize the new font size.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

}
