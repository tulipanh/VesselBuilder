package com.tulipan.hunter.vesselbuilder.structures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tulipan.hunter.vesselbuilder.VesselBuilderActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import utils.FileUtils;
import utils.PictureUtils;

/**
 * Created by Hunter on 1/27/2016.
 */
public class ImageProject {
    private Context mContext;
    private VesselBuilderActivity mActivity;
    private UUID mId;
    private File mFilesDir;
    private File mPhotoFile;
    private File mFilterFile;

    private boolean mNewFilter;
    private float mLowerThreshold;
    private float mUpperThreshold;
    private float mUpperPercent;
    private float mLowerPercent;

    private Bitmap mPhotoBitmap;
    private Bitmap mFilterBitmap;
    private Bitmap mCleanBitmap;

    private int[] mVertexArray;

    public ImageProject(VesselBuilderActivity activity){
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mId = UUID.randomUUID();
        mFilesDir = getPublicPicturesDir();
        // mFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(mFilesDir != null) {
            mPhotoFile = new File(mFilesDir, "IMG_" + mId.toString() + ".bmp");
            mFilterFile = new File(mFilesDir, "IMG_" + mId.toString() + "FILTERED.bmp");
        }
        else {
            mPhotoFile = null;
            mFilterFile = null;
        }

        mLowerThreshold = 80;
        mUpperThreshold = 100;
        mUpperPercent = 1.0f;
        mLowerPercent = 1.0f;
        mNewFilter = true;

        mPhotoBitmap = null;
        mFilterBitmap = null;
    }

    public File getPhotoFile() {
        return mPhotoFile;
    }

    public File getFilterFile() {
        return mFilterFile;
    }

    public UUID getID() {
        return mId;
    }

    public boolean changeLowerThreshold(int diff) {
        if (mLowerPercent + diff < -1) {
            mLowerPercent = -1f;
            return false;
        }
        else if (mLowerPercent + diff > 3) {
            mLowerPercent = 3f;
            return false;
        }
        else {
            mLowerPercent += diff;
            return true;
        }
    }

    public boolean changeUpperThreshold(float diff) {
        if (mUpperPercent + diff < -1) {
            mUpperPercent = -1f;
            return false;
        }
        else if (mUpperPercent + diff > 3) {
            mUpperPercent = 3f;
            return false;
        }
        else {
            mUpperPercent += diff;
            return true;
        }
    }

    public void setUpperThreshold(float value) {
        mUpperThreshold = value;
    }

    public void setLowerThreshold(float value) {
        mLowerThreshold = value;
    }

    public float getLowerThreshold() {
        return mLowerThreshold;
    }

    public float getUpperThreshold() {
        return mUpperThreshold;
    }

    public boolean filterChanged() {
        return mNewFilter;
    }

    public Bitmap getPhotoImage() {
        if (mPhotoBitmap == null && mPhotoFile != null && mPhotoFile.exists()) {
            mPhotoBitmap = BitmapFactory.decodeFile(getPhotoFile().getPath());
        }
        return mPhotoBitmap;
    }

    public Bitmap getFilterImage() {
        if (mFilterBitmap == null && mFilterFile != null && mFilterFile.exists()) {
            mFilterBitmap = BitmapFactory.decodeFile(getFilterFile().getPath());
        }
        return mFilterBitmap;
    }

    public Bitmap getCleanImage() {
        return mCleanBitmap; }

    public void setCleanImage(Bitmap image) {
        mCleanBitmap = image;
    }

    public void setAutoThreshold() {
        if (mPhotoBitmap == null && mPhotoFile != null && mPhotoFile.exists()) {
            mPhotoBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mActivity);
        } else if (mPhotoBitmap == null) return;

        Bitmap inter = mPhotoBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat rgba = new Mat();
        Utils.bitmapToMat(inter, rgba);
        Mat gray = new Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC1, new Scalar(0));
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGRA2GRAY, 4);
        testMats(rgba);
        testMats(gray);
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(gray, mean, stddev);
        mLowerThreshold = (float)(mean.get(0,0)[0] - stddev.get(0,0)[0]);
        mUpperThreshold = (float)(mean.get(0,0)[0] + stddev.get(0,0)[0]);
        rgba.release();
        gray.release();
        inter.recycle();
    }

    public void applyFilter() {
        if (mPhotoBitmap == null && mPhotoFile != null && mPhotoFile.exists()) {
            mPhotoBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mActivity);
        } else if (mPhotoBitmap == null) return;

        if (mFilterBitmap == null) {
            if (mFilterFile.exists()) {
                mFilterBitmap = PictureUtils.getScaledBitmap(mFilterFile.getPath(), mActivity);
            } else {
                mFilterBitmap = Bitmap.createBitmap(mPhotoBitmap.getWidth(), mPhotoBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            }
        }

        Bitmap inter = mPhotoBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat rgba = new Mat();
        Utils.bitmapToMat(inter, rgba);
        Mat gray = new Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC1, new Scalar(0));
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGRA2GRAY, 4);
        testMats(rgba);
        testMats(gray);
        Imgproc.Canny(gray, gray, mLowerThreshold, mUpperThreshold);
        Imgproc.cvtColor(gray, rgba, Imgproc.COLOR_GRAY2RGBA, 4);
        Utils.matToBitmap(rgba, mFilterBitmap);
        rgba.release();
        gray.release();
        inter.recycle();

        /*
        Mat rgba = new Mat(mPhotoBitmap.getHeight(), mPhotoBitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(mPhotoBitmap, rgba);

        Mat inter = new Mat();
        Imgproc.Canny(rgba, inter, mLowerThreshold, mUpperThreshold);
        Imgproc.cvtColor(inter, rgba, Imgproc.COLOR_GRAY2BGRA, 4);
        inter.release();
        Utils.matToBitmap(rgba, mFilterBitmap);
        rgba.release();
        */
        mNewFilter = false;
    }

    private void  testMats(Mat mat) {
        Bitmap test = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, test);
        test.recycle();
    }

    public void rotateImage() {
        if (mPhotoBitmap == null && mPhotoFile != null && mPhotoFile.exists()) {
            mPhotoBitmap = BitmapFactory.decodeFile(getPhotoFile().getPath());
        }
        Matrix rotate = new Matrix();
        rotate.preRotate(90);
        mPhotoBitmap = Bitmap.createBitmap(mPhotoBitmap, 0, 0, mPhotoBitmap.getWidth(), mPhotoBitmap.getHeight(), rotate, true);
        writePhotoToFile();
    }

    public void reflectImage() {
        if (mPhotoBitmap == null && mPhotoFile != null && mPhotoFile.exists()) {
            mPhotoBitmap = BitmapFactory.decodeFile(getPhotoFile().getPath());
        }
        Matrix reflect = new Matrix();
        reflect.preScale(-1.0f, 1.0f);
        mPhotoBitmap = Bitmap.createBitmap(mPhotoBitmap, 0, 0, mPhotoBitmap.getWidth(), mPhotoBitmap.getHeight(), reflect, true);
        writePhotoToFile();
    }

    public void writePhotoToFile() {
        if (mPhotoBitmap == null) return;

        try {
            OutputStream fOut = new FileOutputStream(mPhotoFile);
            mPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            MediaScannerConnection.scanFile(mContext, new String[]{mPhotoFile.getAbsolutePath()}, null, null);
        } catch(FileNotFoundException e) {
            Toast.makeText(mContext, "File Not Found", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            Toast.makeText(mContext, "Output Stream Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeFilterToFile() {
        if (mFilterBitmap == null) return;

        try {
            OutputStream fOut = new FileOutputStream(mFilterFile);
            mFilterBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            MediaScannerConnection.scanFile(mContext, new String[]{mFilterFile.getAbsolutePath()}, null, null);
        } catch(FileNotFoundException e) {
            Toast.makeText(mContext, "File Not Found", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            Toast.makeText(mContext, "Output Stream Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void setEdgeVertexArray(int[] array) {
        mVertexArray = array.clone();
    }

    public float[] getEdgeVertexArray() {
        float[] array = new float[mVertexArray.length];
        for (int i = 0; i < mVertexArray.length; i++) {
            array[i] = mVertexArray[i];
        }
        return array;
    }

    private File getPublicPicturesDir() {
        File publicDir;

        FileUtils.checkPublicWritePermissions(mActivity, mContext);
        if (FileUtils.publicWritePermissionsGranted(mContext)) {
            publicDir = setPicFileDir();
            return publicDir;
        } else {
            Toast.makeText(mContext, "Directory not reachable.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private File setPicFileDir() {
        /**
         * Perhaps query how much free space available.
         */
        File fileDirectory = null;

        if (FileUtils.isExternalStorageWritable()) {
            fileDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EDGE_PICS");
            if (!fileDirectory.exists()) {
                if(!fileDirectory.mkdir()) {
                    Log.e(mActivity.TAG, "Failed to create directory.");
                }
            }
        } else {
            Toast.makeText(mContext, "External Storage Not Writable", Toast.LENGTH_SHORT).show();
        }
        return fileDirectory;
    }

    public void deallocate() {
        /**
         * TODO: This function should deallocate all member images and delete the files associated with them.
         */
    }
}
