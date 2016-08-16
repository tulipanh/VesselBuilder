package com.tulipan.hunter.vesselbuilder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Hunter on 8/5/2016.
 */

/**
 * In order to use the "progressLevel" attribute, the XML where this view is declared must have
 * an xml namespace for "http://schemas.android.com/apk/res/com.tulipan.hunter.vesselbuilder or the
 * name of whatever package this class is included in.
 */
public class ProgressView extends View {
    private Context mContext;
    private Integer mProgressLevel;
    private Paint comPaint = new Paint();
    private Paint uncPaint = new Paint();
    private Paint accPaint = new Paint();

    public ProgressView(Context context) {
        super(context);
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0);
        try {
            mProgressLevel = a.getInteger(R.styleable.ProgressView_progressLevel, 0);
        } finally {
            a.recycle();
        }
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        comPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        uncPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        accPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

    @Override
    public void onDraw(Canvas canvas) {
        /**
         * This should draw four solid circles connected by three thin rectangles (or thick strokes).
         * The rectangles should have arrows on them if possible pointing to the right.
         * They should be light colored and then darkened as progress is made.
         * The current step should be highlighted in some way (perhaps a white circle or something.
         * (0-4 with 0 indicating no progress and 4 indicating on the last step)
         * They should be spaced and sized using the getWidth() and getHeight() functions with some
         * reasonable minimum and maximum (or just maximum).
         */

        Point[] circleCenters = new Point[4];

        circleCenters[0] = new Point(getWidth()/8, getHeight()/3 + 5);
        circleCenters[1] = new Point(3*getWidth()/8, getHeight()/3 + 5);
        circleCenters[2] = new Point(5*getWidth()/8, getHeight()/3 + 5);
        circleCenters[3] = new Point(7*getWidth()/8, getHeight()/3 + 5);

        /* Draw the lines between the steps, darker if already completed */
        comPaint.setStrokeWidth(20.0f);
        comPaint.setStrokeJoin(Paint.Join.ROUND);
        accPaint.setStrokeWidth(20.0f);
        accPaint.setStrokeJoin(Paint.Join.ROUND);
        int colorSplit;
        colorSplit = (mProgressLevel < 1) ? 0 : mProgressLevel-1;
        colorSplit = (mProgressLevel > 4) ? 3 : colorSplit;
        canvas.drawLine(circleCenters[0].x, circleCenters[0].y, circleCenters[colorSplit].x, circleCenters[colorSplit].y, comPaint);
        canvas.drawLine(circleCenters[colorSplit].x, circleCenters[colorSplit].y, circleCenters[3].x, circleCenters[3].y, accPaint);

        /* Draw the circles indicating the steps, darker if already completed */
        comPaint.setStrokeWidth(5.0f);
        comPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        accPaint.setStrokeWidth(5.0f);
        accPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (int i = 0; i <= colorSplit; i++) {
            canvas.drawCircle(circleCenters[i].x, circleCenters[i].y, 40f, comPaint);
        }
        for (int i = colorSplit+1; i < 4; i++) {
            canvas.drawCircle(circleCenters[i].x, circleCenters[i].y, 40f, accPaint);
        }

        /* Draw the highlight indicating which step you are on. */
        accPaint.setStrokeWidth(5.0f);
        accPaint.setStrokeJoin(Paint.Join.ROUND);
        accPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(circleCenters[colorSplit].x, circleCenters[colorSplit].y, 30f, accPaint);
        accPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(circleCenters[colorSplit].x, circleCenters[colorSplit].y, 20f, accPaint);

        /* Draw the labels for the steps. */
        String[] stepStrings = {"GET", "CROP", "EXTRACT", "ALTER"};
        float[] stepOffsets = {35f, 50f, 75f, 55f};

        comPaint.setTextSize(40);
        accPaint.setTextSize(40);
        comPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        accPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        for (int i = 0; i < 4; i++) {
            if (i == colorSplit) canvas.drawText(stepStrings[i], circleCenters[i].x - stepOffsets[i], circleCenters[i].y + 85, comPaint);
            else canvas.drawText(stepStrings[i], circleCenters[i].x - stepOffsets[i], circleCenters[i].y + 85, accPaint);
        }
    }
}
