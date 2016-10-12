package com.tulipan.hunter.vesselbuilder.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tulipan.hunter.vesselbuilder.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Hunter on 10/2/2016.
 */
public class DoubleSeekBarView extends View {
    private Context mContext;
    private boolean mIsTouchable;
    private Integer mMinValue = 0;
    private Integer mMaxValue = 0;
    private Float mStepSize = 0f;
    private float mSizeOfOne;
    private float mUpperValue;
    private float mLowerValue;
    private float mUpperPosition;
    private float mLowerPosition;
    private int mSelected;
    private int mLastSelected;
    private boolean mUpperSet;
    private boolean mLowerSet;

    private static final int NONE = 0;
    private static final int LOWER = 1;
    private static final int UPPER = 2;

    private Paint upperPaint = new Paint();
    private Paint lowerPaint = new Paint();
    private Paint barPaint = new Paint();
    private Paint highlightPaint = new Paint();
    private int circleRadius;
    private float sideBuffer;

    private ActionExecutor mActionExecutor = new ActionExecutor();

    public DoubleSeekBarView(Context context) {
        super(context);
        init(context);
    }

    public DoubleSeekBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DoubleSeekBarView, 0, 0);
        try {
            mMinValue = a.getInteger(R.styleable.DoubleSeekBarView_minimumValue, 0);
            mMaxValue = a.getInteger(R.styleable.DoubleSeekBarView_maximumValue, 0);
            mStepSize = a.getFloat(R.styleable.DoubleSeekBarView_stepSize, 0);
        } finally {
            a.recycle();
        }
        init(context);
    }

    public DoubleSeekBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        /**
         * Normal constructor stuff goes here.
         */
        mIsTouchable = true;
        if (mMaxValue < mMinValue) throw new IllegalArgumentException();
        if (mStepSize > mMaxValue - mMinValue) throw new IllegalArgumentException();
        mUpperValue = mMaxValue.floatValue();
        mLowerValue = mMinValue.floatValue();
        mUpperSet = false;
        mLowerSet = false;

        upperPaint.setColor(ContextCompat.getColor(context, R.color.doubleSeekbarUpper));
        lowerPaint.setColor(ContextCompat.getColor(context, R.color.doubleSeekbarLower));
        barPaint.setColor(ContextCompat.getColor(context, R.color.doubleSeekbarBar));
        highlightPaint.setColor(ContextCompat.getColor(context, R.color.doubleSeekbarHighlight));
        upperPaint.setAntiAlias(true);
        lowerPaint.setAntiAlias(true);
        barPaint.setAntiAlias(true);
        highlightPaint.setAntiAlias(true);
        upperPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        lowerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        barPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        barPaint.setStrokeWidth(10f);
        lowerPaint.setStrokeWidth(10f);
        upperPaint.setStrokeWidth(10f);
        highlightPaint.setStrokeWidth(10f);
        circleRadius = 20;
        sideBuffer = 30f;

        mSelected = NONE;
        mLastSelected = NONE;
    }

    @Override
    public void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        mSizeOfOne = (getWidth()-2*sideBuffer)/(float)(mMaxValue - mMinValue);

        mLowerPosition = mLowerValue*mSizeOfOne+sideBuffer;
        mUpperPosition = mUpperValue*mSizeOfOne+sideBuffer;
    }

    @Override
    public void onDraw(Canvas canvas) {
        /**
         * Draw the view based on the state of the member variables.
         */
        int width = getWidth();
        int height = getHeight();

        canvas.drawLine(sideBuffer, height/2, mLowerPosition, height/2, barPaint);
        canvas.drawLine(mLowerPosition, height / 2, mUpperPosition, height / 2, lowerPaint);
        canvas.drawLine(mUpperPosition, height/2, width-sideBuffer, height/2, barPaint);
        if (mSelected == LOWER) canvas.drawCircle(mLowerPosition, height/2, 2*circleRadius, highlightPaint);
        else if (mSelected == UPPER) canvas.drawCircle(mUpperPosition, height/2, 2*circleRadius, highlightPaint);
        canvas.drawCircle(mLowerPosition, height/2, circleRadius, lowerPaint);
        canvas.drawCircle(mUpperPosition, height/2, circleRadius, upperPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        if (!mIsTouchable) return true;

        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /**
                 * Perform actions that change how the View is drawn.
                 */
                if (x < mLowerPosition+2*circleRadius && x > mLowerPosition-2*circleRadius) {
                    if (x < mUpperPosition+2*circleRadius && x > mUpperPosition-2*circleRadius) {
                        mSelected = mLastSelected;
                    } else {
                        mSelected = LOWER;
                    }
                } else if (x < mUpperPosition+2*circleRadius && x > mUpperPosition-2*circleRadius) {
                    mSelected = UPPER;
                } else {
                    mSelected = NONE;
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                /**
                 * Perform actions that change how the View is drawn.
                 */
                switch (mSelected) {
                    case LOWER:
                        if (x < mUpperPosition) {
                            if (x > sideBuffer) mLowerPosition = x;
                            else mLowerPosition = sideBuffer;
                            /* Calculate mLowerValue based on position */
                            float progression = (mLowerPosition-sideBuffer)/mSizeOfOne;
                            mLowerValue = Math.round(progression/mStepSize)*mStepSize;
                        }
                        break;

                    case UPPER:
                        if (x > mLowerPosition) {
                            if (x < getWidth()-sideBuffer) mUpperPosition = x;
                            else mUpperPosition = getWidth()-sideBuffer;
                            /* Calculate mUpperValue based on position */
                            float progression = (mUpperPosition-sideBuffer)/mSizeOfOne;
                            mUpperValue = Math.round(progression / mStepSize)*mStepSize;
                        }
                        break;

                    case NONE:
                        break;
                }
                invalidate();
                mActionExecutor.touchMove();
                break;

            case MotionEvent.ACTION_UP:
                /**
                 * Perform actions that change how the View is drawn.
                 */
                if (mSelected != NONE) {
                    mLastSelected = mSelected;
                }
                mSelected = NONE;
                invalidate();
                mActionExecutor.touchUp();
                break;
        }

        return true;
    }

    private float getRoundedValue(float val) {
        BigDecimal bd = new BigDecimal(val);
        BigDecimal step = new BigDecimal(mStepSize);
        bd = bd.divide(step, RoundingMode.HALF_EVEN).setScale(0, RoundingMode.HALF_EVEN);
        bd = bd.multiply(step);
        return bd.floatValue();
    }

    public float getUpperValue() {
        return getRoundedValue(mUpperValue);
    }


    public float getLowerValue() {
        return getRoundedValue(mLowerValue);
    }

    public void setUpperValue(float value) {
        /* Yes this is sloppy since comparing floating point values is more complicated due to floating point error. */
        float valToSet = getRoundedValue(value);
        if (lessThan(valToSet, mLowerValue)) valToSet = mLowerValue;
        mUpperValue = valToSet;
        mUpperPosition = mUpperValue*mSizeOfOne+sideBuffer;
        invalidate();
    }

    public void setLowerValue(float value) {
        /* This is sloppy for the same reasons as setUpperValue. */
        /* It could be made better with a comparison function that uses a delta value of mStepSize. */
        float valToSet = getRoundedValue(value);
        if (greaterThan(valToSet, mUpperValue)) valToSet = mUpperValue;
        mLowerValue = valToSet;
        mLowerPosition = mLowerValue*mSizeOfOne+sideBuffer;
        invalidate();
    }

    public void setTouchable(boolean value) {
        mIsTouchable = value;
    }

    private boolean lessThan(float a, float b) {
        return (a-b < -mStepSize);
    }

    private boolean greaterThan(float a, float b) {
        return (a-b > mStepSize);
    }

    private boolean equalTo(float a, float b) {
        return (Math.abs(a-b) < mStepSize);
    }

    public void setActionExecutor(ActionExecutor e) { mActionExecutor = e;}

    public static class ActionExecutor {

        public void touchMove() {}

        public void touchUp() {}
    }
}
