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
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tulipan.hunter.vesselbuilder.structures.ImageProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Hunter on 8/30/2016.
 */
public class ExtractPageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private static ImageProject mProject;
    private DrawingView mDrawView;
    private View mShroud;
    private ExtractInterface mInterface;

    public static ExtractPageFragment newInstance(ImageProject project) {
        ExtractPageFragment fragment = new ExtractPageFragment();
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
        View v = inflater.inflate(R.layout.extract_edge_fragment, container, false);

        FrameLayout layout = (FrameLayout) v.findViewById(R.id.extract_drawlayout);
        mDrawView = new DrawingView(mCurrentActivity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mDrawView.setLayoutParams(params);
        layout.addView(mDrawView);

        mShroud = v.findViewById(R.id.extractpage_shroud);

        mInterface = new ExtractInterface(v);

        return v;
    }

    private void  setShroud(boolean visible) {
        if (visible) {
            mShroud.setVisibility(View.VISIBLE);
        } else {
            mShroud.setVisibility(View.GONE);
        }
    }

    private class ExtractInterface implements View.OnClickListener {
        private View mView;
        private Button acceptButton;
        private Button rejectButton;
        private Button contButton;

        public ExtractInterface(View v) {
            mView = v;

            acceptButton = (Button) mView.findViewById(R.id.extractpage_accept_button);
            rejectButton = (Button) mView.findViewById(R.id.extractpage_reject_button);
            contButton = (Button) mView.findViewById(R.id.extractpage_cont_button);

            acceptButton.setOnClickListener(this);
            rejectButton.setOnClickListener(this);
            contButton.setOnClickListener(this);

            acceptButton.setEnabled(false);
            rejectButton.setEnabled(false);
            contButton.setEnabled(false);
            acceptButton.setAlpha(0.3f);
            rejectButton.setAlpha(0.3f);
            contButton.setAlpha(0.3f);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.extractpage_accept_button:
                    mDrawView.accept();
                    contButton.setEnabled(true);
                    contButton.setAlpha(1.0f);
                    break;

                case R.id.extractpage_reject_button:
                    mDrawView.reject();
                    disableButtons();
                    break;

                case R.id.extractpage_cont_button:
                    mDrawView.cont();
                    break;

                default:
                    break;

            }
        }

        public boolean buttonsEnabled() {
            return acceptButton.isEnabled() && rejectButton.isEnabled();
        }

        public void enableButtons() {
            acceptButton.setEnabled(true);
            acceptButton.setAlpha(1.0f);
            rejectButton.setEnabled(true);
            rejectButton.setAlpha(1.0f);
        }

        public void disableButtons() {
            acceptButton.setEnabled(false);
            acceptButton.setAlpha(0.3f);
            rejectButton.setEnabled(false);
            rejectButton.setAlpha(0.3f);
            contButton.setEnabled(false);
            contButton.setAlpha(0.3f);
        }

        public void disableAccept() {
            acceptButton.setEnabled(false);
            acceptButton.setAlpha(0.3f);
        }

        public void enableAccept() {
            acceptButton.setEnabled(true);
            acceptButton.setAlpha(1.0f);
        }

        public void enableCont() {
            contButton.setEnabled(true);
            contButton.setAlpha(1.0f);
        }
    }

    public class DrawingView extends View {
        private Context mContext;
        private int mWidth;
        private int mHeight;
        private int mImageWidth;
        private int mImageHeight;
        private float mScale, xTranslation, yTranslation;
        private int imageRight, imageLeft, imageTop, imageBottom;
        private boolean mProcessing;
        private boolean mDrawingEnabled;
        int[] mVertexArray;
        int[] mThinnedArray;
        int[] mRangeArray;

        private Bitmap mWorkingImage;
        private Bitmap mCleanImage;
        private Canvas mEditCanvas;

        private Paint mBitmapPaint;
        private Paint tracePaint;
        private Paint selectPaint;
        private Paint interPaint;

        private Path tracePath;
        private Path selectPath;
        private Path interPath;

        public DrawingView(Context c) {
            super(c);
            mContext = c;
            mProcessing = false;
            mDrawingEnabled = true;
            mRangeArray = null;

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            tracePaint = new Paint();
            selectPaint = new Paint();
            interPaint = new Paint();
            tracePaint.setStyle(Paint.Style.STROKE);
            tracePaint.setColor(ContextCompat.getColor(mContext, R.color.colorHighlight));
            tracePaint.setStrokeWidth(50f);
            tracePaint.setStrokeJoin(Paint.Join.ROUND);
            selectPaint.setColor(ContextCompat.getColor(mContext, R.color.colorSelected));
            selectPaint.setStyle(Paint.Style.STROKE);
            selectPaint.setStrokeJoin(Paint.Join.ROUND);
            selectPaint.setStrokeWidth(2f);
            interPaint.setColor(ContextCompat.getColor(mContext, R.color.colorInterpolated));
            interPaint.setStyle(Paint.Style.STROKE);
            interPaint.setStrokeJoin(Paint.Join.ROUND);
            interPaint.setStrokeWidth(2f);

            tracePath = new Path();
            selectPath = new Path();
            interPath = new Path();
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
            mCleanImage = mProject.getCleanImage().copy(Bitmap.Config.ARGB_8888, true);
            mImageWidth = mCleanImage.getWidth();
            mImageHeight = mCleanImage.getHeight();
            drawScaledBitmap(mCleanImage);
            /* A scaled down version of mFilterImage is now drawn to mEditCanvas and thus also to mWorkingImage */
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

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mWorkingImage, 0, 0, mBitmapPaint);
            /* A scaled version of mFilterImage, which is contained in mWorkingImage, is drawn to the View. */

            canvas.drawPath(tracePath, tracePaint);
            canvas.drawPath(selectPath, selectPaint);
            canvas.drawPath(interPath, interPaint);
        }

        private int startX, startY, curX, curY, prevX, prevY;
        private static final int TOUCH_TOLERANCE = 4;
        private ArrayList<Point> tracePoints = new ArrayList<>();

        private void touchStart(int x, int y) {
            startX = curX = prevX = x;
            startY = curY = prevY = y;

            tracePath.reset();
            tracePath.moveTo(curX, curY);
            tracePoints.add(new Point(curX, curY));
        }

        private void touchMove(int x, int y) {
            curX = x;
            curY = y;
            int dx = Math.abs(curX - prevX);
            int dy = Math.abs(curY - prevY);

            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                tracePath.lineTo(curX, curY);
                tracePoints.add(new Point(curX, curY));

                prevX = curX;
                prevY = curY;
            }
        }

        private void touchUp() {
            if (!mInterface.buttonsEnabled()) mInterface.enableButtons();
            mDrawingEnabled = false;
            processTrace2();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (mProcessing) return true;
            if (!mDrawingEnabled) return true;

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

        private void processTrace2() {
            int imageX, imageY, sumX, sumY;
            ArrayList<Point> whitePix;
            Point avgPoint;
            Path foundPath = new Path();
            ArrayList<Point> missedTrace = new ArrayList<>();

            setShroud(true);
            mProcessing = true;
            mVertexArray = new int[tracePoints.size()*2];
            int vIdx = 0;
            int radius = (int)(25/mScale);

            for (Point p : tracePoints) {
                imageX = xImageConvert(p.x);
                imageY = yImageConvert(p.y);
                if (imageX >= 0 && imageX < mImageWidth && imageY >= 0 && imageY < mImageHeight) {
                    whitePix = findWhiteWithinRange2(imageX, imageY, radius);
                    if (whitePix.size() == 0) {
                        if (vIdx >= 2) missedTrace.add(new Point(imageX, imageY));
                    } else if (missedTrace.size() > 0) {
                        Point forwardVector = new Point(imageX - mVertexArray[vIdx - 2], imageY - mVertexArray[vIdx - 1]);
                        Point missedVector = new Point(missedTrace.get(0).x - mVertexArray[vIdx - 2], missedTrace.get(0).y - mVertexArray[vIdx - 1]);
                        int dotProduct = missedVector.x * forwardVector.x + missedVector.y * forwardVector.y;
                        float length = (float) (dotProduct / (Math.sqrt(forwardVector.x * forwardVector.x + forwardVector.y * forwardVector.y)));
                        float proportion = (float) (length / (Math.sqrt(forwardVector.x * forwardVector.x + forwardVector.y * forwardVector.y)));
                        Point newPoint = new Point((int) (mVertexArray[vIdx - 2] + proportion * forwardVector.x), (int) (mVertexArray[vIdx - 1] + proportion * forwardVector.y));
                        Point correctionVector = new Point(newPoint.x - missedTrace.get(0).x, newPoint.y - missedTrace.get(0).y);
                        for (Point m : missedTrace) {
                            mVertexArray[vIdx] = m.x + correctionVector.x;
                            mVertexArray[vIdx + 1] = m.y + correctionVector.y;
                            vIdx += 2;
                            if (foundPath.isEmpty()) foundPath.moveTo(xViewConvert(mVertexArray[vIdx - 2]), yViewConvert(mVertexArray[vIdx - 1]));
                            else foundPath.lineTo(xViewConvert(mVertexArray[vIdx - 2]), yViewConvert(mVertexArray[vIdx - 1]));
                        }
                        missedTrace.clear();
                    } else {
                        sumX = 0;
                        sumY = 0;
                        for (Point w : whitePix) {
                            sumX += w.x;
                            sumY += w.y;
                        }
                        avgPoint = new Point(sumX / whitePix.size(), sumY / whitePix.size());
                        mVertexArray[vIdx] = avgPoint.x;
                        mVertexArray[vIdx + 1] = avgPoint.y;
                        vIdx += 2;
                        if (foundPath.isEmpty()) foundPath.moveTo(xViewConvert(avgPoint.x), yViewConvert(avgPoint.y));
                        else foundPath.lineTo(xViewConvert(avgPoint.x), yViewConvert(avgPoint.y));
                    }
                }
            }

            selectPath = foundPath;
            tracePath.reset();
            mProcessing = false;
            mDrawingEnabled = true;
            setShroud(false);
        }

        private ArrayList<Point> findWhiteWithinRange(int x, int y, int range) {
            ArrayList<Point> whitePoints = new ArrayList<>();

            /* Find the range of pixels to search */
            if (mRangeArray == null || mRangeArray.length != 2*range+1) {
                mRangeArray = new int[2 * range + 1];
                int maxDist = range * range;
                int lastYDist = 0;
                for (int i = -range; i < 0; i++) {
                    for (int j = lastYDist; j <= range; j++) {
                        if (i * i + j * j > maxDist) {
                            mRangeArray[i + range] = j - 1;
                            lastYDist = j - 1;
                            break;
                        }
                    }
                }
                int idx = 0;
                for (int i = 2 * range; i > range; i--) {
                    mRangeArray[i] = mRangeArray[idx++];
                }
                mRangeArray[range] = range;
            }
            /* Search the pixels and store the coordinates of the white ones. */
            for (int i = -range; i <= range; i++) {
                for (int j = mRangeArray[i+range]; j >= -mRangeArray[i+range]; j--) {
                    if (x+i < mImageWidth && x+i >= 0 && y+j < mImageHeight && y+j >= 0 && mCleanImage.getPixel(x+i, y+j) > -8000000) {
                        whitePoints.add(new Point(x+i, y+j));
                    }
                }
            }

            return whitePoints;
        }

        /**
            This only searches in the same row as the trace point, dramatically decreasing the time needed to find a good point.
            However, this diminishes its efficacy, so that it will only work well with roughly vertical traces.
            Any trace that is nearly horizontal would likely give bad results, though no errors. This could be remedied by
            keeping track of direction in addition to location, and searching perpendicular to the direction of motion.
            This would also unfortunately increase the computational complexity and lead to slower results.

            Solution implemented that searches in the same column and the same row, i.e. in a big + sign.
            Still needs to be tested to ensure accuracy/efficacy and efficiency.
         */

        private ArrayList<Point> findWhiteWithinRange2(int x, int y, int range) {
            ArrayList<Point> whitePoints = new ArrayList<>();

            for (int i = -range; i <= range; i++) {
                if (x+i < mImageWidth && x+i >= 0 && y < mImageHeight && y >= 0 && mCleanImage.getPixel(x+i, y) > -8000000) {
                    whitePoints.add(new Point(x+i, y));
                }
            }

            for (int i = -range; i <= range; i++) {
                if (y+i < mImageHeight && y+i >= 0 && x < mImageWidth && x >= 0 && mCleanImage.getPixel(x, y+i) > -8000000) {
                    whitePoints.add(new Point(x, y+i));
                }
            }

            return whitePoints;
        }

        private int xImageConvert(int x) {
            return (int) ((x - xTranslation) / mScale);
        }

        private int yImageConvert(int y) {
            return (int) ((y - yTranslation) / mScale);
        }

        private float xViewConvert(int x) {
            return x*mScale + xTranslation;
        }

        private float yViewConvert(int y) {
            return y*mScale + yTranslation;
        }

        private float distance(Point last, int x, int y) {
            return (float) Math.sqrt(Math.pow(x-last.x, 2) + Math.pow(y-last.y, 2));
        }

        public class PointYComparator implements Comparator<Point> {
            public int compare(Point p1, Point p2) {
                int y1 = p1.y;
                int y2 = p2.y;

                return y1-y2;
            }
        }

        public void accept() {
            /**
             * This will thin the vertex array and add it to the ImageProject.
             * Its goal is to thin the vertex array down to 100 vertices, but if the change in position reaches a threshold between samples, it should add points in that region.
             */

            selectPath.reset();
            interPath.reset();

            ArrayList<Integer> thinnedArray = new ArrayList<>();
            /* Using a thinFactor of length/100 gives us 50 vertices */
            int thinFactor = mVertexArray.length / 60;
            if (thinFactor % 2 != 0) thinFactor += 1;
            int xIndex = 0;
            int yIndex = 1;
            selectPath.moveTo(xViewConvert(mVertexArray[0]), yViewConvert(mVertexArray[1]));

            while (yIndex < mVertexArray.length) {
                thinnedArray.add(mVertexArray[xIndex]);
                thinnedArray.add(mVertexArray[yIndex]);
                thinnedArray.add(mVertexArray[xIndex]);
                thinnedArray.add(mVertexArray[yIndex]);
                selectPath.lineTo(xViewConvert(mVertexArray[xIndex]), yViewConvert(mVertexArray[yIndex]));
                xIndex += thinFactor;
                yIndex += thinFactor;
            }

            /* Remove the first instance of the first vertex and the last instance of the last vertex */
            thinnedArray.remove(0);
            thinnedArray.remove(0);
            thinnedArray.remove(thinnedArray.size()-1);
            thinnedArray.remove(thinnedArray.size()-1);

            mThinnedArray = new int[thinnedArray.size()];

            for (int i = 0; i < thinnedArray.size(); i++) {
                mThinnedArray[i] = thinnedArray.get(i).intValue();
            }

            mInterface.disableAccept();
            mInterface.enableCont();
            mDrawingEnabled = false;
            invalidate();
        }

        public void reject() {
            /**
             * This will reset the Paths, ArrayList, and Vertex array. It will also disable buttons.
             */
            tracePath.reset();
            selectPath.reset();
            interPath.reset();
            tracePoints.clear();
            mVertexArray = null;
            mInterface.disableButtons();
            mDrawingEnabled = true;
            invalidate();
        }

        public void cont() {
            /**
             * This will initialize an AlterPageFragment with the imageProject and put in on the Fragment Stack
             */
            selectPath.reset();
            mProject.setEdgeVertexArray(mThinnedArray);
            mCurrentActivity.replaceFragment(AlterPageFragment.newInstance(mProject));
        }
    }
}
