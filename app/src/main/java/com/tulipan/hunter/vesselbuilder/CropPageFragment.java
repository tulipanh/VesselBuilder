package com.tulipan.hunter.vesselbuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.tulipan.hunter.vesselbuilder.structures.ImageProject;

import java.util.ArrayList;

/**
 * Created by Hunter on 8/19/2016.
 */
public class CropPageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private static ImageProject mProject;
    private DrawingView mDrawView;
    private CropInterface mInterface;

    public static CropPageFragment newInstance(ImageProject project) {
        CropPageFragment fragment = new CropPageFragment();
        mProject = project;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.crop_image_fragment, container, false);

        FrameLayout layout = (FrameLayout) v.findViewById(R.id.crop_drawlayout);
        mDrawView = new DrawingView(mCurrentActivity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mDrawView.setLayoutParams(params);
        layout.addView(mDrawView);

        mInterface = new CropInterface(v);

        return v;
    }

    private class CropInterface implements View.OnClickListener {
        private View mView;
        private Button cropButton;
        private Button cleanButton;
        private Button acceptButton;
        private Button rejectButton;
        private Button resetButton;
        private Button contButton;

        public CropInterface (View v) {
            mView = v;

            cropButton = (Button) mView.findViewById(R.id.croppage_crop_button);
            cleanButton = (Button) mView.findViewById(R.id.croppage_clean_button);
            acceptButton = (Button) mView.findViewById(R.id.croppage_accept_button);
            rejectButton = (Button) mView.findViewById(R.id.croppage_reject_button);
            resetButton = (Button) mView.findViewById(R.id.croppage_reset_button);
            contButton = (Button) mView.findViewById(R.id.croppage_cont_button);

            cropButton.setOnClickListener(this);
            cleanButton.setOnClickListener(this);
            acceptButton.setOnClickListener(this);
            rejectButton.setOnClickListener(this);
            resetButton.setOnClickListener(this);
            contButton.setOnClickListener(this);

            acceptButton.setEnabled(false);
            rejectButton.setEnabled(false);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.croppage_crop_button:
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    if (mDrawView.getEditMode() == DrawingView.CROP) {
                        mDrawView.setEditMode(DrawingView.NONE);
                        cleanButton.setEnabled(true);
                        cleanButton.setAlpha(1.0f);
                    } else {
                        mDrawView.setEditMode(DrawingView.CROP);
                        cleanButton.setEnabled(false);
                        cleanButton.setAlpha(0.3f);
                    }
                    break;

                case R.id.croppage_clean_button:
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    if (mDrawView.getEditMode() == DrawingView.CLEAN) {
                        mDrawView.setEditMode(DrawingView.NONE);
                        cropButton.setEnabled(true);
                        cropButton.setAlpha(1.0f);
                    } else {
                        mDrawView.setEditMode(DrawingView.CLEAN);
                        cropButton.setEnabled(false);
                        cropButton.setAlpha(0.3f);
                    }
                    break;

                case R.id.croppage_accept_button:
                    mDrawView.accept();
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    break;

                case R.id.croppage_reject_button:
                    mDrawView.reject();
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    break;

                case R.id.croppage_reset_button:
                    mDrawView.reset();
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    break;

                case R.id.croppage_cont_button:
                    mDrawView.finalizeImage();
                    mCurrentActivity.replaceFragment(ExtractPageFragment.newInstance(mProject));
                    break;

                default:
                    break;
            }
        }

        public boolean acceptEnabled() {
            return acceptButton.isEnabled() && rejectButton.isEnabled();
        }

        public void enableAccept() {
            acceptButton.setEnabled(true);
            rejectButton.setEnabled(true);
        }

        public void disableAccept() {
            acceptButton.setEnabled(false);
            rejectButton.setEnabled(false);
        }
    }

    private class DrawingView extends View {
        private Context mContext;
        private int mWidth;
        private int mHeight;
        private int mEditMode;
        private static final int NONE = 0;
        private static final int CROP = 1;
        private static final int CLEAN = 2;

        /* Image properties */
        private float mScale;
        private float xTranslation;
        private float yTranslation;
        private int imageLeft;
        private int imageRight;
        private int imageTop;
        private int imageBottom;

        /* Drawing Objects */
        private Bitmap mWorkingImage;
        private Canvas mEditCanvas;
        private Canvas mTransferCanvas;
        private Bitmap mFilterImage;
        private Paint mBitmapPaint;
        private Paint boxPaint;
        private Paint circlePaint;
        private Paint blackPaint;

        private Path boxPath;
        private Path blackBoxPath;
        private Path blackPath;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            mContext = c;
            mEditMode = 0;

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            boxPaint = new Paint();
            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setColor(Color.WHITE);
            boxPaint.setStrokeWidth(2f);
            blackPaint = new Paint();
            blackPaint.setStyle(Paint.Style.FILL);
            blackPaint.setColor(Color.BLACK);
            circlePaint = new Paint();
            circlePaint.setAntiAlias(true);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);

            boxPath = new Path();
            blackPath = new Path();
            blackBoxPath = new Path();
            circlePath = new Path();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mWidth = w;
            mHeight = h;

            mWorkingImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            /* mWorkingImage is now a new, blank Bitmap the size of the View */
            mEditCanvas = new Canvas(mWorkingImage);
            /* mEditCanvas is now linked to mWorkingImage. Changes to the Canvas get saved to mWorkingImage */
            mFilterImage = mProject.getFilterImage().copy(Bitmap.Config.ARGB_8888, true);
            drawScaledBitmap(mFilterImage);
            /* A scaled down version of mFilterImage is now drawn to mEditCanvas and thus also to mWorkingImage */
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mWorkingImage, 0, 0, mBitmapPaint);
            /* A scaled version of mFilterImage, which is contained in mWorkingImage, is drawn to the View. */

            switch (mEditMode) {
                case CROP:
                    canvas.drawPath(boxPath, boxPaint);
                    canvas.drawPath(circlePath, circlePaint);
                    break;

                case CLEAN:
                    canvas.drawPath(blackBoxPath, blackPaint);
                    canvas.drawPath(blackPath, circlePaint);
                    break;

                default:
                    break;
            }
        }

        private void drawScaledBitmap(Bitmap image) {
            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();

            if (imageWidth >= imageHeight) {
                mScale = mWidth/imageWidth;
                xTranslation = 0f;
                yTranslation = (mHeight-imageHeight*mScale)/2f;
                imageLeft = 0;
                imageRight = mWidth;
                imageTop = (int) yTranslation;
                imageBottom = (int) (yTranslation+imageHeight*mScale);
            } else {
                mScale = mHeight/imageHeight;
                xTranslation = (mWidth-imageWidth*mScale)/2f;
                yTranslation = 0f;
                imageLeft = (int) xTranslation;
                imageRight = (int) (xTranslation+imageWidth*mScale);
                imageTop = 0;
                imageBottom = mHeight;
            }

            Matrix transformation = new Matrix();
            transformation.postTranslate(xTranslation, yTranslation);
            transformation.preScale(mScale, mScale);
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            mEditCanvas.drawBitmap(image, transformation, paint);   // Draws the scaled and translated version of mFilterImage to mWorkingImage
        }

        private int startX, startY, curX, curY, prevX, prevY;
        private static final int TOUCH_TOLERANCE = 4;
        private ArrayList<Point> boxPoints = new ArrayList<>();

        private void touchStart(int x, int y) {
            startX = curX = prevX = x;
            startY = curY = prevY = y;

            switch (mEditMode) {
                case CROP:
                    boxPath.reset();
                    break;

                case CLEAN:
                    blackBoxPath.addRect(curX-75, curY-75, curX+75, curY+75, Path.Direction.CW);
                    boxPoints.add(new Point(curX, curY));
                    if (!mInterface.acceptEnabled()) mInterface.enableAccept();
                    break;

                default:
                    break;
            }
        }

        private void touchMove(int x, int y) {
            curX = x;
            curY = y;
            int dx = Math.abs(curX - prevX);
            int dy = Math.abs(curY - prevY);

            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                switch (mEditMode) {
                    case CROP:
                        if (!mInterface.acceptEnabled()) mInterface.enableAccept();
                        circlePath.reset();
                        circlePath.addRect(startX, startY, curX, curY, Path.Direction.CW);
                        circlePath.addCircle(curX, curY, 30, Path.Direction.CW);
                        break;

                    case CLEAN:
                        blackPath.reset();
                        blackPath.addRect(curX-75, curY-75, curX+75, curY+75, Path.Direction.CW);
                        blackBoxPath.addRect(curX-75, curY-75, curX+75, curY+75, Path.Direction.CW);
                        boxPoints.add(new Point(curX, curY));
                        break;

                    default:
                        break;
                }
                prevX = curX;
                prevY = curY;
            }
        }

        private void touchUp() {
            switch (mEditMode) {
                case CROP:
                    circlePath.reset();
                    boxPath.addRect(startX, startY, curX, curY, Path.Direction.CW);
                    break;

                case CLEAN:
                    blackPath.reset();
                    break;

                default:
                    break;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(x, y);
                    invalidate();
                    break;

                case MotionEvent.ACTION_MOVE:
                    touchMove(x, y);
                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:
                    touchUp();
                    invalidate();
                    break;

                default:
                    break;
            }
            return true;
        }

        public void accept() {
            switch (mEditMode) {
                case CROP:
                    int cLeft, cRight, cTop, cBottom;
                    if (startX < curX) {
                        if (startY < curY) {
                            cLeft = Math.max(startX, imageLeft);
                            cRight = Math.min(curX, imageRight);
                            cTop = Math.max(startY, imageTop);
                            cBottom = Math.min(curY, imageBottom);
                        } else {
                            cLeft = Math.max(startX, imageLeft);
                            cRight = Math.min(curX, imageRight);
                            cTop = Math.max(curY, imageTop);
                            cBottom = Math.min(startY, imageBottom);
                        }
                    } else {
                        if (startY < curY) {
                            cLeft = Math.max(curX, imageLeft);
                            cRight = Math.min(startX, imageRight);
                            cTop = Math.max(startY, imageTop);
                            cBottom = Math.min(curY, imageBottom);
                        } else {
                            cLeft = Math.max(curX, imageLeft);
                            cRight = Math.min(startX, imageRight);
                            cTop = Math.max(curY, imageTop);
                            cBottom = Math.max(startY, imageBottom);
                        }
                    }

                    cLeft = (int) ((cLeft - xTranslation) / mScale) + 1;
                    cRight = (int) ((cRight - xTranslation) / mScale) - 1;
                    cTop = (int) ((cTop - (int) yTranslation) / mScale) + 1;
                    cBottom = (int) ((cBottom - (int) yTranslation) / mScale) - 1;

                    Bitmap newCrop = Bitmap.createBitmap(mFilterImage, cLeft, cTop, cRight - cLeft, cBottom - cTop);
                    mFilterImage = newCrop.copy(Bitmap.Config.ARGB_8888, true);
                    newCrop.recycle();
                    boxPath.reset();
                    break;

                case CLEAN:
                    Bitmap transferbmp = Bitmap.createBitmap(mFilterImage.getWidth(), mFilterImage.getHeight(), Bitmap.Config.ARGB_8888);
                    mTransferCanvas = new Canvas(transferbmp);
                    mTransferCanvas.drawBitmap(mFilterImage, 0, 0, mBitmapPaint);
                    blackBoxPath.reset();
                    int centerX, centerY;
                    int halfWidth = (int) (75/mScale);
                    for (Point p : boxPoints) {
                        centerX = (int) ((p.x - xTranslation)/mScale);
                        centerY = (int) ((p.y - yTranslation)/mScale);
                        blackBoxPath.addRect(centerX-halfWidth, centerY-halfWidth, centerX+halfWidth, centerY+halfWidth, Path.Direction.CW);
                    }

                    /* Canvas.drawRect() does not work! You must draw rectangles on a Path and then draw the Path! */
                    mTransferCanvas.drawPath(blackBoxPath, blackPaint);

                    boxPoints.clear();
                    blackBoxPath.reset();
                    mFilterImage = transferbmp.copy(Bitmap.Config.ARGB_8888, true);
                    transferbmp.recycle();
                    break;

                default:
                    break;
            }

            mWorkingImage.recycle();
            mWorkingImage = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mEditCanvas = new Canvas(mWorkingImage);
            drawScaledBitmap(mFilterImage);
            invalidate();
        }

        public void reject() {
            mWorkingImage.recycle();
            mWorkingImage = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mEditCanvas = new Canvas(mWorkingImage);
            drawScaledBitmap(mFilterImage);
            boxPath.reset();
            circlePath.reset();
            blackPath.reset();
            blackBoxPath.reset();
            boxPoints.clear();
            invalidate();
        }

        public void reset() {
            mWorkingImage.recycle();
            mFilterImage = mProject.getFilterImage().copy(Bitmap.Config.ARGB_8888, true);
            mTransferCanvas = new Canvas(mFilterImage);
            mWorkingImage = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mEditCanvas = new Canvas(mWorkingImage);
            drawScaledBitmap(mFilterImage);
            boxPath.reset();
            circlePath.reset();
            blackPath.reset();
            blackBoxPath.reset();
            boxPoints.clear();
            invalidate();
        }

        public int getEditMode() {return mEditMode;}

        public void setEditMode(int mode) {
            if (mode < 0 || mode > 2) throw new IllegalArgumentException();
            else {
                mEditMode = mode;
            }
        }

        public void finalizeImage() {
            mProject.setCleanImage(mFilterImage.copy(Bitmap.Config.ARGB_8888, true));
            mFilterImage.recycle();
            mWorkingImage.recycle();
        }
    }
}
