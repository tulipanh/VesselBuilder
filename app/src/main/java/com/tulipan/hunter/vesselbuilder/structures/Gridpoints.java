package com.tulipan.hunter.vesselbuilder.structures;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Hunter on 3/28/2016.
 */
public class Gridpoints {
    private final Context context;
    private static final int BYTES_PER_FLOAT = 4;
    public static final int POSITION_COMPONENTS_PER_VERTEX = 2;
    public static final int COLORBOOL_COMPONENTS_PER_VERTEX = 1;
    public static final int TOTAL_COMPONENTS_PER_VERTEX = POSITION_COMPONENTS_PER_VERTEX + COLORBOOL_COMPONENTS_PER_VERTEX;
    public static final int STRIDE = TOTAL_COMPONENTS_PER_VERTEX * BYTES_PER_FLOAT;

    public int numVertices;
    private float[] gridData = new float[363];
    private float[] vertexData;
    public FloatBuffer vertexBuffer;

    public Gridpoints(Context context) {
        this.context = context;
        float xVal = -2.5f;
        float yVal = -2.5f;
        int rowCount = 0;
        int colCount = 0;
        int index = 0;
        while (index < 363) {
            gridData[index] = xVal;
            gridData[index + 1] = yVal;
            gridData[index + 2] = 1.0f;
            if (xVal == 0.0f && yVal == 0.0f) gridData[index + 2] = 0.0f;

            colCount += 1;
            xVal += 0.5f;
            if (colCount == 11) {
                colCount = 0;
                xVal = -2.5f;
                yVal += 0.5f;
                rowCount += 1;
            }

            index += 3;
        }
        vertexData = gridData.clone();
        numVertices = vertexData.length / TOTAL_COMPONENTS_PER_VERTEX;
    }

    public void makeVertexBuffer() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
    }
}
