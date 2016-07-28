package com.tulipan.hunter.vesselbuilder;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Hunter on 7/7/2016.
 */
public class ModelManager {
    private static final String TAG = "ModelManager";
    private VesselBuilderActivity mParentActivity;
    private Context mContext;
    private File mFileDirectory;
    private ArrayList<ModelFile> mModels = new ArrayList<>();

    public ModelManager(Context context) {
        mParentActivity = (VesselBuilderActivity) context;  //Not sure which form to record, Activity or Context
        mContext = context;
        loadModels();
    }

    private void loadModels() {
        if (!mParentActivity.checkPermissions()) {
            Toast.makeText(mParentActivity, "Permissions not granted. Cannot load files.", Toast.LENGTH_LONG).show();
            return;
        }
        setOBJFileDir();
        if (!mFileDirectory.canRead()) {
            Toast.makeText(mParentActivity, "Cannot read files.", Toast.LENGTH_LONG);
            return;
        }

        String[] fileNames = mFileDirectory.list();
        ArrayList<String> objFiles = new ArrayList();
        ArrayList<String> jpegFiles = new ArrayList();
        for (String s : fileNames) {
            if (s.length() > 4 && s.substring(s.length() - 4).contains(".obj")) {
                objFiles.add(s);
            } else if (s.length() > 5 && s.substring(s.length() - 5).contains(".jpeg")) {
                jpegFiles.add(s);
            }
        }

        for (String s : objFiles) {
            ModelFile model = null;
            String filePath = mFileDirectory.getPath() + "/" + s;
            String previewName = s.replace(".obj", ".jpeg");
            if (jpegFiles.contains(previewName)) {
                String previewPath = mFileDirectory.getPath() + "/" + previewName;
                model = new ModelFile(filePath, previewPath);
            } else {
                model = new ModelFile(filePath);
            }
            mModels.add(model);
        }
    }

    private void setOBJFileDir() {
        /**
         * Perhaps query how much free space available.
         */
        if (isExternalStorageWritable()) {
            mFileDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OBJFiles");
            if (!mFileDirectory.exists()) {
                if(!mFileDirectory.mkdir()) {
                    Log.e(TAG, "Failed to create directory.");
                }
            }
        } else {
            Toast.makeText(mContext, "External Storage Not Writable", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public ArrayList<ModelFile> getModels() {
        return mModels;
    }
}
