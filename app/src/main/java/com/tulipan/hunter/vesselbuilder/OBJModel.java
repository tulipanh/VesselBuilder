package com.tulipan.hunter.vesselbuilder;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * Created by Hunter on 11/30/2015.
 */

public class OBJModel {
    private final Context context;
    private static final int BYTES_PER_FLOAT = 4;
    public static final int POSITION_COMPONENTS_PER_VERTEX = 3;
    public static final int NORMAL_COMPONENTS_PER_VERTEX = 3;
    public static final int TOTAL_COMPONENTS_PER_VERTEX = POSITION_COMPONENTS_PER_VERTEX + NORMAL_COMPONENTS_PER_VERTEX;
    public static final int STRIDE = TOTAL_COMPONENTS_PER_VERTEX * BYTES_PER_FLOAT;

    public int numVertices;

    private float mRadius = 0f;
    private Float maxDim = 0f;
    private Float minDim = 0f;

    private List<Float> vList = new ArrayList<>();
    private List<Float> nList = new ArrayList<>();
    private List<Integer> fCList = new ArrayList<>();
    private List<Integer> fNList = new ArrayList<>();

    private float[] vertexData;
    public FloatBuffer vertexBuffer;

    public OBJModel(Context context, File objectFile) {
        this.context = context;
        numVertices = 0;

        try {
            FileInputStream objStream = new FileInputStream(objectFile);
            InputStreamReader isr = new InputStreamReader(objStream);
            BufferedReader buffer = new BufferedReader(isr);
            String line;
            Float tempFlo;
            String tempVertString;
            StringTokenizer parts;
            StringTokenizer faceParts;
            String indexString;

            while ((line = buffer.readLine()) != null) {
                parts = new StringTokenizer(line, " ");
                int numTokens = parts.countTokens();
                if (numTokens == 0) continue;
                String part = parts.nextToken();
                if (part.equals("v")) {
                    for (int i = 0; i < 3; i++) {
                        tempFlo = Float.parseFloat(parts.nextToken());
                        if (tempFlo > maxDim) maxDim = tempFlo;
                        if (tempFlo < minDim) minDim = tempFlo;
                        vList.add(tempFlo);
                    }
                }
                else if (part.equals("vn")) {
                    for (int i = 0; i < 3; i++) {
                        nList.add(Float.parseFloat(parts.nextToken()));
                    }
                }
                else if (part.equals("f")) {
                    for (int i = 0; i < 3; i++) {
                        tempVertString = parts.nextToken();
                        faceParts = new StringTokenizer(tempVertString, "/");
                        indexString = faceParts.nextToken();
                        fCList.add(Integer.parseInt(indexString));
                        if (tempVertString.indexOf("//") == -1) indexString = faceParts.nextToken();
                        indexString = faceParts.nextToken();
                        fNList.add(Integer.parseInt(indexString));
                    }
                }
            }
        } catch(IOException e) {
            // Do something if exception is caught.
        }
    }

    public void makeVertexBuffer() {
        if (fCList.size() == fNList.size() && fCList.size() > 0 && fCList.size() % 3 == 0 && vList.size() > 0 && nList.size() > 0 && vList.size() % 3 == 0 && nList.size() % 3 == 0) {
            ListIterator<Integer> fCIter = fCList.listIterator();
            ListIterator<Integer> fNIter = fNList.listIterator();
            List<Float> temp = new ArrayList<>();
            int vIndex;
            int nIndex;
            int vListIndex;
            int nListIndex;

            Float trueMaxDim;
            Float div = 1f;
            if (maxDim > (-1 * minDim)) trueMaxDim = maxDim;
            else trueMaxDim = (-1 * minDim);
            // div = trueMaxDim / 10f;
            mRadius = trueMaxDim;

            /**
             * This seems to be shrinking the model as it makes the vertex buffer.
             * I'm not sure why I did this here. It seems that if I need to fit a model
             * onto the screen, I should just change the camera position rather than
             * the model data. Then again, it's not the actual data that is being changed,
             * just the buffer that the renderer reads from.
             */

            while(fCIter.hasNext()) {
                vIndex = fCIter.next().intValue();
                nIndex = fNIter.next().intValue();
                vListIndex = (vIndex - 1) * 3;
                nListIndex = (nIndex - 1) * 3;
                temp.add(vList.get(vListIndex) / div);
                temp.add(vList.get(vListIndex + 1) / div);
                temp.add(vList.get(vListIndex + 2) / div);
                temp.add(nList.get(nListIndex));
                temp.add(nList.get(nListIndex + 1));
                temp.add(nList.get(nListIndex + 2));
            }

            vertexData = new float[temp.size()];
            int j = 0;
            for (Float f : temp) {
                vertexData[j++] = f.floatValue();
            }
            temp = null;

            numVertices = vertexData.length / TOTAL_COMPONENTS_PER_VERTEX;

            vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);

            return;
        }
        else {
            return;
        }
    }

    public float getRadius() {
        return mRadius;
    }
}
