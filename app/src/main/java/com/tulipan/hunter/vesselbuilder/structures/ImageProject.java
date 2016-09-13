package com.tulipan.hunter.vesselbuilder.structures;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tulipan.hunter.vesselbuilder.VesselBuilderActivity;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
    private File mGreyFile;
    private File mFilterFile;

    private boolean mNewFilter;
    private int mLowerThreshold;
    private int mUpperThreshold;

    private Bitmap mPhotoBitmap;
    private Bitmap mFilterBitmap;
    private Bitmap mCleanBitmap;
    private Mat rgba;

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
            mGreyFile = null;
            mFilterFile = null;
        }

        mLowerThreshold = 150;
        mUpperThreshold = 180;
        mNewFilter = true;

        mPhotoBitmap = null;
        mFilterBitmap = null;
        rgba = null;
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

    public void setLowerThreshold(int value) {
        if (value < mUpperThreshold) {
            mLowerThreshold = value;
        } else {
            mLowerThreshold = mUpperThreshold;
        }
        mNewFilter = true;
    }

    public void setUpperThreshold(int value) {
        if (value > mLowerThreshold) {
            mUpperThreshold = value;
        } else {
            mUpperThreshold = mLowerThreshold;
        }
        mNewFilter = true;
    }

    public int getLowerThreshold() {
        return mLowerThreshold;
    }

    public int getUpperThreshold() {
        return mUpperThreshold;
    }

    public boolean filterChanged() {
        return mNewFilter;
    }

    public Bitmap getFilterImage() {
        return mFilterBitmap;
    }

    public Bitmap getCleanImage() { return mCleanBitmap; }

    public void setCleanImage(Bitmap image) {
        mCleanBitmap = image;
    }

    public void applyFilter() {
        /* TODO: This should be split so that the image is not written to file every time the filter is applied. */
        /* It should only need to write to file when the filter values are accepted and final */
        if (mPhotoBitmap == null) {
            mPhotoBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mActivity);
        }
        if (mFilterBitmap == null) {
            if (mFilterFile.exists()) {
                mFilterBitmap = PictureUtils.getScaledBitmap(mFilterFile.getPath(), mActivity);
            } else {
                mFilterBitmap = Bitmap.createBitmap(mPhotoBitmap);
            }
        }

        if (rgba == null) {
            rgba = new Mat(mPhotoBitmap.getHeight(), mPhotoBitmap.getWidth(), CvType.CV_8UC1);
            Utils.bitmapToMat(mPhotoBitmap, rgba);
        }

        Mat inter = new Mat();
        Imgproc.Canny(rgba, inter, mLowerThreshold, mUpperThreshold);
        Imgproc.cvtColor(inter, rgba, Imgproc.COLOR_GRAY2BGRA, 4);
        inter.release();
        Utils.matToBitmap(rgba, mFilterBitmap);
        mNewFilter = false;
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
}
