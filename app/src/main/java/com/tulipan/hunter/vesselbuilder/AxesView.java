package com.tulipan.hunter.vesselbuilder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Hunter on 7/25/2016.
 */
public class AxesView extends View {
    Paint xPaint = new Paint();
    Paint yPaint = new Paint();
    Paint zPaint = new Paint();

    public AxesView(Context context) {
        super(context);
        init(context);
    }

    public AxesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AxesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        xPaint.setColor(getResources().getColor(R.color.colorPrimary));
        yPaint.setColor(getResources().getColor(R.color.colorTriadGreen));
        zPaint.setColor(getResources().getColor(R.color.colorTriadRed));
        xPaint.setStrokeWidth(4.0f);
        yPaint.setStrokeWidth(4.0f);
        zPaint.setStrokeWidth(4.0f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine(getWidth()*(1/3f), getHeight()*(2/3f), getWidth()*(1/3f), 10, yPaint);
        canvas.drawLine(getWidth()*(1/3f), getHeight()*(2/3f), getWidth()-10, getHeight()*(2/3f), xPaint);
        canvas.drawLine(getWidth()*(1/3f), getHeight()*(2/3f), 10, getHeight()-10, zPaint);
    }
}
