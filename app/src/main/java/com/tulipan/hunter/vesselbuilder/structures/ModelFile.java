package com.tulipan.hunter.vesselbuilder.structures;

/**
 * Created by Hunter on 7/8/2016.
 */
public class ModelFile {
    private String mFilePath;
    private String mPreviewPath = null;
    private String mName;

    public ModelFile(String filePath) {
        mFilePath = filePath;
        String[] components = filePath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".obj", "");
    }

    public ModelFile(String filePath, String previewPath) {
        this(filePath);
        mPreviewPath = previewPath;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getPreviewPath() {
        return mPreviewPath;
    }

    public String getName() {
        return mName;
    }
}
