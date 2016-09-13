package com.tulipan.hunter.vesselbuilder.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.tulipan.hunter.vesselbuilder.R;

/**
 * Created by Hunter on 7/25/2016.
 */
public class AxesView extends View {
    private Context mContext;
    private Paint xPaint = new Paint();
    private Paint yPaint = new Paint();
    private Paint zPaint = new Paint();

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
        mContext = context;
        xPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        yPaint.setColor(ContextCompat.getColor(context, R.color.colorTriadGreenDark));
        zPaint.setColor(ContextCompat.getColor(context, R.color.colorTriadRedDark));
        xPaint.setStrokeWidth(4.0f);
        yPaint.setStrokeWidth(4.0f);
        zPaint.setStrokeWidth(4.0f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawY(canvas, getWidth() * (1 / 3f), getHeight() * (2 / 3f), getWidth() * (1 / 3f), 10, yPaint);
        drawX(canvas, getWidth() * (1 / 3f), getHeight() * (2 / 3f), getWidth() - 10, getHeight() * (2 / 3f), xPaint);
        drawZ(canvas, getWidth() * (1 / 3f), getHeight() * (2 / 3f), 10, getHeight() - 10, zPaint);
    }

    private void drawY(Canvas canvas, float startX, float startY, float endX, float endY, Paint paint) {
        canvas.drawLine(startX, startY, endX, endY + 4f, paint);
        Path triPath = new Path();
        triPath.setFillType(Path.FillType.EVEN_ODD);
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Paint.Style.FILL);
        triPath.moveTo(endX, endY);
        float xDiff = (float) Math.abs(12f * Math.tan(Math.PI / 6f));
        triPath.lineTo(endX+xDiff, endY+12f);
        triPath.lineTo(endX-xDiff, endY+12f);
        triPath.lineTo(endX, endY);
        triPath.close();
        canvas.drawPath(triPath, paint);
    }

    private void drawX(Canvas canvas, float startX, float startY, float endX, float endY, Paint paint) {
        canvas.drawLine(startX, startY, endX - 4f, endY, paint);
        Path triPath = new Path();
        triPath.setFillType(Path.FillType.EVEN_ODD);
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Paint.Style.FILL);
        triPath.moveTo(endX, endY);
        float yDiff = (float) Math.abs(12f * Math.tan(Math.PI / 6f));
        triPath.lineTo(endX-12f, endY+yDiff);
        triPath.lineTo(endX-12f, endY-yDiff);
        triPath.lineTo(endX, endY);
        triPath.close();
        canvas.drawPath(triPath, paint);
    }

    private void drawZ(Canvas canvas, float startX, float startY, float endX, float endY, Paint paint) {
        canvas.drawLine(startX, startY, endX + 2f, endY - 2f, paint);
        Path triPath = new Path();
        triPath.setFillType(Path.FillType.EVEN_ODD);
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Paint.Style.FILL);
        triPath.moveTo(endX, endY);
        float longDiff = (float) (12d*(Math.cos(Math.PI/12d)/Math.cos(Math.PI/6f)));
        float shortDiff = (float) (12d*(Math.sin(Math.PI/12d)/Math.cos(Math.PI/6d)));
        triPath.lineTo(endX+shortDiff, endY-longDiff);
        triPath.lineTo(endX+longDiff, endY-shortDiff);
        triPath.lineTo(endX, endY);
        triPath.close();
        canvas.drawPath(triPath, paint);
    }
}
