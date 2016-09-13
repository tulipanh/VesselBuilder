package com.tulipan.hunter.vesselbuilder.structures;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.tulipan.hunter.vesselbuilder.AlterPageFragment;
import com.tulipan.hunter.vesselbuilder.VesselBuilderActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;

/**
 * Created by Hunter on 4/24/2016.
 */

public class EdgeOBJ {
    private Context mContext;
    private VesselBuilderActivity mParentActivity;
    private String TAG = "EdgeOBJ";
    private File mFileDirectory;
    private File mObjectFile;
    private float[] baseVertexArray;
    private float[] outputVertexData;
    private float[] outputNormalData;

    private final int OUTER_TOP_EDGE = 0;
    private final int OUTER_GENERAL = 1;
    private final int OUTER_BOTTOM_EDGE = 2;
    private final int OUTER_BOTTOM = 3;
    private final int INNER_TOP_EDGE = 4;
    private final int INNER_GENERAL = 5;
    private final int INNER_BOTTOM_EDGE = 6;
    private final int INNER_BOTTOM = 7;

    /**
     * Should add some description of how this class organizes the definition data.
     * Order of vertices and vertex normals, for instance.
     */

    public EdgeOBJ(Context context, float[] edgeVertexData) {

        baseVertexArray = edgeVertexData.clone();
        mContext = context;
        mParentActivity = (VesselBuilderActivity) mContext;
        outputVertexData = new float[baseVertexArray.length*108+6];
        outputNormalData = new float[baseVertexArray.length*108+6];
        createVertices();
        checkPermissions();
        setOBJFileDir();
        if (mFileDirectory.exists() && mFileDirectory.canWrite()) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date now = new Date();
            String filename = "test" + formatter.format(now) + ".obj";
            mObjectFile = new File(mFileDirectory.getAbsolutePath() + "/" + filename);
            try {
                mObjectFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mObjectFile.exists() && mObjectFile.canWrite()) {
                FileOutputStream outputStream = null;

                try {
                    outputStream = new FileOutputStream(mObjectFile);
                    writeMesh(outputStream);
                    MediaScannerConnection.scanFile(mContext, new String[] {mObjectFile.getAbsolutePath()}, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.flush();
                            outputStream.close();
                            Toast.makeText(mContext, "File Written.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void createVertices() {
        float[] rotMatrix = new float[16];
        float[] vertexVector = new float[4];
        float[] rotatedVector = new float[4];

        setIdentityM(rotMatrix, 0);
        rotateM(rotMatrix, 0, 10, 0.0f, 1.0f, 0.0f);

        /**
         * Make a circle of vertices for each vertex in the edge, by moving each through
         * 10-degree rotations around the y-axis 36 times.
         */
        float minX = baseVertexArray[0];
        int index = 0;
        int indexO = 0;
        while (index < baseVertexArray.length) {
            vertexVector[0] = baseVertexArray[index];
            vertexVector[1] = baseVertexArray[index+1];
            vertexVector[2] = 0f;
            vertexVector[3] = 0f;
            outputVertexData[indexO] = vertexVector[0];
            outputVertexData[indexO+1] = vertexVector[1];
            outputVertexData[indexO+2] = vertexVector[2];
            indexO += 3;
            if (vertexVector[0] < minX) minX = vertexVector[0]; // Need the min X value to determine how thick the walls should be.
            for (int i = 0; i < 35; i++) {  // Apply a 10-degree rotation 35 times.
                multiplyMV(rotatedVector, 0, rotMatrix, 0, vertexVector, 0);
                outputVertexData[indexO] = rotatedVector[0];
                outputVertexData[indexO+1] = rotatedVector[1];
                outputVertexData[indexO+2] = rotatedVector[2];
                vertexVector = rotatedVector.clone();
                indexO += 3;
            }
            index += 2;
        }
        /**
         * Add a central bottom vertex.
         */
        outputVertexData[indexO] = 0.0f;
        outputVertexData[indexO+1] = baseVertexArray[index-1];
        outputVertexData[indexO+2] = 0.0f;
        indexO += 3;

        /**
         * Determine wall and floor thickness.
         * Wall thickness is 5% of the vessel width.
         * Floor thickness is the same as wall thickness, but is obtained by compressing all
         * vertices in the y direction with the top vertices remaining the same.
         * So the lower the vertices are, the more they will be translated up, until the lowest
         * vertices will be translated the full thickness.
         */
        float thickness = minX/7f;
        float transY = thickness/(baseVertexArray.length/2);
        int numY = 0;

        /**
         * Add the interior wall vertices.
         */
        index = 0;
        while (index < baseVertexArray.length) {
            vertexVector[0] = baseVertexArray[index] - thickness;
            vertexVector[1] = baseVertexArray[index+1] + numY * transY;
            vertexVector[2] = 0f;
            vertexVector[3] = 0f;
            outputVertexData[indexO] = vertexVector[0];
            outputVertexData[indexO+1] = vertexVector[1];
            outputVertexData[indexO+2] = vertexVector[3];
            indexO += 3;
            for (int i = 0; i < 35; i++) {
                multiplyMV(rotatedVector, 0, rotMatrix, 0, vertexVector, 0);
                outputVertexData[indexO] = rotatedVector[0];
                outputVertexData[indexO+1] = rotatedVector[1];
                outputVertexData[indexO+2] = rotatedVector[2];
                vertexVector = rotatedVector.clone();
                indexO += 3;
            }
            index += 2;
            numY += 1;
        }
        /**
         * Add the interior central vertex.
         */
        outputVertexData[indexO] = 0.0f;
        outputVertexData[indexO+1] = baseVertexArray[index-1] + thickness;
        outputVertexData[indexO+2] = 0.0f;
        indexO += 3;

        /**
         * All vertices have been created, now we must calculate all the vertex normals.
         * These do not necessarily need to be in the same order as long as the face definitions
         * reflect that, but I would prefer to keep the normals organized.
         * Normals defined below as the cross product of two vectors, with each vector going from one
         * vertex to another.
         */
        int n = baseVertexArray.length/2;
        int numVertices = 72*n + 2;
        int indexA1, indexA2, indexB1, indexB2, p;
        float[] vecA = new float[3];
        float[] vecB = new float[3];
        float[] vecN;
        int vType;

        indexA1 = indexA2 = indexB1 = indexB2 = 0;

        for (int i = 0; i < numVertices; i++) {
            p = i+1;
            if (p < 37) vType = OUTER_TOP_EDGE;
            else if (p < 36*n-35) vType = OUTER_GENERAL;
            else if (p < 36*n+1) vType = OUTER_BOTTOM_EDGE;
            else if (p < 36*n+2) vType = OUTER_BOTTOM;
            else {
                p -= 36*n+1;
                if (p < 37) vType = INNER_TOP_EDGE;
                else if (p < 36*n-35) vType = INNER_GENERAL;
                else if (p < 36*n+1) vType = INNER_BOTTOM_EDGE;
                else vType = INNER_BOTTOM;
            }

            switch (vType) {
                case OUTER_TOP_EDGE:
                    indexA2 = i+36;
                    if (p != 1 && p != 36) {
                        indexA1 = i+36*n+1;
                        indexB1 = i-1;
                        indexB2 = i+1;
                    } else if (p == 36) {
                        indexA1 = (36*n+36);
                        indexB1 = i-1;
                        indexB2 = i-35;
                    } else {
                        indexA1 = 36*n+1;
                        indexB1 = i+35;
                        indexB2 = i+1;
                    }
                    break;

                case OUTER_GENERAL:
                    indexA1 = i-36;
                    indexA2 = i+36;
                    if (p%36 > 1) {
                        indexB1 = i-1;
                        indexB2 = i+1;
                    } else if (p%36 == 0) {
                        indexB1 = i-1;
                        indexB2 = i-35;
                    } else {
                        indexB1 = i+35;
                        indexB2 = i+1;
                    }
                    break;

                case OUTER_BOTTOM_EDGE:
                    indexA1 = i-36;
                    indexA2 = 36*n;
                    if (p%36 > 1) {
                        indexB1 = i-1;
                        indexB2 = i+1;
                    } else if (p%36 == 0) {
                        indexB1 = i-1;
                        indexB2 = i-35;
                    } else {
                        indexB1 = i+35;
                        indexB2 = i+1;
                    }
                    break;

                case OUTER_BOTTOM:
                    indexA1 = 36*n-34;
                    indexA2 = 36*n;
                    indexB1 = 36*n-1;
                    indexB2 = 36*n;
                    break;

                case INNER_TOP_EDGE:
                    indexA2 = i+36;
                    if (p != 36 && p != 0) {
                        indexA1 = p-1;
                        indexB1 = i+1;
                        indexB2 = i-1;
                    } else if (p == 36) {
                        indexA1 = 35;
                        indexB1 = i-35;
                        indexB2 = i-1;
                    } else {
                        indexA1 = 0;
                        indexB1 = i+1;
                        indexB2 = i+35;
                    }
                    break;

                case INNER_GENERAL:
                    indexA1 = i-36;
                    indexA2 = i+36;
                    if (p%36 > 1) {
                        indexB1 = i+1;
                        indexB2 = i-1;
                    } else if (p%36 == 0) {
                        indexB1 = i-35;
                        indexB2 = i-1;
                    } else {
                        indexB1 = i+1;
                        indexB2 = i+35;
                    }
                    break;

                case INNER_BOTTOM_EDGE:
                    indexA1 = i-36;
                    indexA2 = 72*n+1;
                    if (p%36 > 1) {
                        indexB1 = i+1;
                        indexB2 = i-1;
                    } else if (p%36 == 0) {
                        indexB1 = i-35;
                        indexB2 = i-1;
                    } else {
                        indexB1 = i+1;
                        indexB2 = i+35;
                    }
                    break;

                case INNER_BOTTOM:
                    indexA1 = 72*n;
                    indexA2 = 72*n+1;
                    indexB1 = 72*n-35;
                    indexB2 = 72*n+1;
                    break;
            }
            indexA1 *= 3;
            indexA2 *= 3;
            indexB1 *= 3;
            indexB2 *= 3;

            vecA[0] = outputVertexData[indexA2] - outputVertexData[indexA1];
            vecA[1] = outputVertexData[indexA2+1] - outputVertexData[indexA1+1];
            vecA[2] = outputVertexData[indexA2+2] - outputVertexData[indexA1+2];
            vecB[0] = outputVertexData[indexB2] - outputVertexData[indexB1];
            vecB[1] = outputVertexData[indexB2+1] - outputVertexData[indexB1+1];
            vecB[2] = outputVertexData[indexB2+2] - outputVertexData[indexB1+2];

            vecN = normCrossProduct(vecA, vecB);

            outputNormalData[i*3] = vecN[0];
            outputNormalData[i*3+1] = vecN[1];
            outputNormalData[i*3+2] = vecN[2];
        }
    }

    private void writeFaces(FileOutputStream ostream) {
        int n = baseVertexArray.length/2;
        int numVertices = 72*n + 2;
        FaceLineWriter writer = new FaceLineWriter(ostream);

        int vType, p;

        for (int i = 1; i <= numVertices; i++) {
            p = i;
            if (p < 37) vType = OUTER_TOP_EDGE;
            else if (p < 36*n-35) vType = OUTER_GENERAL;
            else if (p < 36*n+1) vType = OUTER_BOTTOM_EDGE;
            else if (p < 36*n+2) vType = OUTER_BOTTOM;
            else {
                p -= 36*n+1;
                if (p < 37) vType = INNER_TOP_EDGE;
                else if (p < 36*n-35) vType = INNER_GENERAL;
                else if (p < 36*n+1) vType = INNER_BOTTOM_EDGE;
                else vType = INNER_BOTTOM;
            }

            switch (vType) {
                case OUTER_TOP_EDGE:
                    if (p != 36) {
                        writer.writeLine(i, i+37, i+1);
                        writer.writeLine(i, i+36, i+37);
                        writer.writeLine(i, i+1, 36*n+2+i);
                        writer.writeLine(i, 36*n+2+i, 36*n+1+i);
                    } else {
                        writer.writeLine(i, i+1, i-35);
                        writer.writeLine(i, i+36, i+1);
                        writer.writeLine(i, i-35, 36*n+2);
                        writer.writeLine(i, 36*n+2, 36*n+37);
                    }
                    break;

                case OUTER_GENERAL:
                    if (p%36 != 0) {
                        writer.writeLine(i, i+37, i+1);
                        writer.writeLine(i, i+36, i+37);
                    } else {
                        writer.writeLine(i, i+1, i-35);
                        writer.writeLine(i, i+36, i+1);
                    }
                    break;

                case OUTER_BOTTOM_EDGE:
                    if (p%36 != 0) writer.writeLine(i, 36*n+1, i+1);
                    else writer.writeLine(i, 36*n+1, i-35);
                    break;

                case OUTER_BOTTOM:
                    // Intentionally Blank
                    break;

                case INNER_TOP_EDGE:
                    if (p != 1) {
                        writer.writeLine(i, i+35, i-1);
                        writer.writeLine(i, i+36, i+35);
                    } else {
                        writer.writeLine(i, i+71, i+35);
                        writer.writeLine(i, i+36, i+71);
                    }
                    break;

                case INNER_GENERAL:
                    if (p%36 != 1) {
                        writer.writeLine(i, i+35, i-1);
                        writer.writeLine(i, i+36, i+35);
                    } else {
                        writer.writeLine(i, i+71, i+35);
                        writer.writeLine(i, i+36, i+71);
                    }
                    break;

                case INNER_BOTTOM_EDGE:
                    if (p%36 != 1) writer.writeLine(i, 72*n+2, i-1);
                    else writer.writeLine(i, 72*n+2, i+35);
                    break;

                case INNER_BOTTOM:
                    // Intentionally Blank
                    break;
            }
        }
    }

    private float[] normCrossProduct(float[] vecA, float[] vecB) {
        float[] cross = new float[3];
        float mag;
        if (vecA.length != 3 || vecB.length != 3)  return null;
        cross[0] = vecA[1]*vecB[2]-vecA[2]*vecB[1];
        cross[1] = vecA[2]*vecB[0]-vecA[0]*vecB[2];
        cross[2] = vecA[0]*vecB[1]-vecA[1]*vecB[0];
        mag = (float) Math.sqrt(cross[0]*cross[0] + cross[1]*cross[1] + cross[2]*cross[2]);
        cross[0] /= mag;
        cross[1] /= mag;
        cross[2] /= mag;

        return cross;
    }

    private void checkPermissions() {
        if (!(ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(mParentActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, mParentActivity.REQUEST_WRITE_STORAGE);
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

    private void writeMesh(FileOutputStream ostream) {
        String lineString;

        try {
            int index = 0;
            while (index < outputVertexData.length) {
                lineString = "v " + Float.toString(outputVertexData[index]) + " " + Float.toString(outputVertexData[index+1]) + " " + Float.toString(outputVertexData[index+2]) + "\n";
                ostream.write(lineString.getBytes());
                index += 3;
            }
            index = 0;
            while (index < outputNormalData.length) {
                lineString = "vn " + Float.toString(outputNormalData[index]) + " " + Float.toString(outputNormalData[index+1]) + " " + Float.toString(outputNormalData[index+2]) + "\n";
                ostream.write(lineString.getBytes());
                index += 3;
            }
            // Writing faces goes here
            writeFaces(ostream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FaceLineWriter {
        private FileOutputStream mOstream;

        public FaceLineWriter(FileOutputStream ostream) {
            mOstream = ostream;
        }

        public void writeLine(int v1, int v2, int v3) {
            String lineString;

            lineString = "f " + Integer.toString(v1) + "//" + Integer.toString(v1) + " " + Integer.toString(v2) + "//" + Integer.toString(v2) + " " + Integer.toString(v3) + "//" + Integer.toString(v3) + "\n";
            try {
                mOstream.write(lineString.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Vertex Normal Notes Below:
 */
/**
 * See Notebook for full and more general calculations.
 *
 *
 * Edge Case 1 (vertices on the outer edge of the rim):
 * Normal 1: Vector1 = (36n + 2) -> 37, Vector2 = 36 -> 2
 * Normal 2: Vector1 = (36n + 3) -> 38, Vector2 = 1 -> 3
 * .
 * .
 * .
 * Normal 35: Vector1 = (36n + 36) -> 71, Vector2 = 34 -> 36
 * Normal 36: Vector1 = (36n + 37) -> 72, Vector2 = 35 -> 1;
 *
 * Edge Case 2 (vertices on the outer base edge):
 * Normal 1: Vector1 = 36(n-1) - 35 -> 36n + 1, Vector2 = 36n -> 36n - 34
 * Normal 2: Vector1 = 36(n-1) - 34 -> 36n + 1, Vector2 = 36n - 35 -> 36n - 33
 * .
 * .
 * .
 * Normal 35: Vector1 = 36(n-1) - 1 -> 36n + 1, Vector2 = 36n - 2 -> 36n
 * Normal 36: Vector1 = 36(n-1) -> 36n + 1, Vector2 = 36n - 1 -> 36n - 35
 *
 * Special Case 1 (the outer bottom vertex):
 * Normal 1: Vector1 = 36n -> 36n + 1, Vector2 = 36n - 1 -> 36n + 1
 *
 * Edge Case 3 (vertices on the inner edge of the rim):
 * Normal 1: Vector1 = 1 -> 36(n+1) + 2, Vector2 = 36n + 3 -> 36(n+1) + 1
 * Normal 2: Vector1 = 2 -> 36(n+1) + 3, Vector2 = 36n + 4 -> 36n + 2
 * .
 * .
 * .
 * Normal 35: Vector1 = 35 -> 36(n+2), Vector2 = 36(n+1) + 1 -> 36n + 35
 * Normal 36: Vector1 = 36 -> 36(n+2) + 1, Vector2 = 36n + 2 -> 36(n+1)
 *
 * Edge Case 4 (vertices on the inner base edge):
 * Normal 1: Vector1 = 72n - 70 -> 72n + 2, Vector2 = 72n - 33 -> 72n + 1
 * Normal 2: Vector1 = 72n - 69 -> 72n + 2, Vector2 = 72n - 32 -> 72n - 34
 * .
 * .
 * .
 * Normal 35: Vector1 = 72n - 36 -> 72n + 2, Vector2 = 72n + 1 -> 72n - 1
 * Normal 36: Vector1 = 72n - 35 -> 72n + 2, Vector2 = 72n - 34 -> 72n
 *
 * Special Case 2 (the inner bottom vertex):
 * Normal 1: Vector1 = 72n -> 72n + 2, Vector2 = 72n + 1 -> 72n + 2
 *
 */
/**
 * Face Definition Notes Below:
 */
/**
 * Under this scheme we would have 14400 faces for an edge with 100 vertices.
 * Is this too many?
 */

/**
 * For the Outer Wall
 */

/**
 * Face 1:  1// 38// 2//
 * Face 2:  2// 39// 3//
 * .
 * .
 * .
 * Face 35: 35// 72// 36//
 * Face 36: 36// 37// 1//
 */

/**
 * Face 37: 1// 37// 38//
 * Face 38: 2// 38// 39//
 * .
 * .
 * .
 * Face 71: 35// 71// 72//
 * Face 72: 36// 72// 37//
 */

/**
 * Repeat the pattern but add 36 to all numbers.
 * Last face to be drawn will be:
 * (n-1)*36// n*36// (36*n-35)//
 * Where n is the number of vertices in the original edge.
 */

/**
 * For the bottom.
 */

/**
 * Face 1: (36*n-35)// (36*n+1)// (36*n-34)//
 * Face 2: (36*n-34)// (36*n+1)// (36*n-33)//
 * .
 * .
 * .
 * Face 37: (36*n-1)// (36*n+1)// (36*n-0)//
 * Face 36: (36*n-0)// (36*n+1)// (36*n-35)//
 */