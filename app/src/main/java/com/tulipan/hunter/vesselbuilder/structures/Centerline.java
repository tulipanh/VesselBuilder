package com.tulipan.hunter.vesselbuilder.structures;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Hunter on 3/25/2016.
 */
public class Centerline {
    private final Context context;
    private static final int BYTES_PER_FLOAT = 4;
    public static final int POSITION_COMPONENTS_PER_VERTEX = 2;
    public static final int COLORBOOL_COMPONENTS_PER_VERTEX = 1;
    public static final int TOTAL_COMPONENTS_PER_VERTEX = POSITION_COMPONENTS_PER_VERTEX + COLORBOOL_COMPONENTS_PER_VERTEX;
    public static final int STRIDE = TOTAL_COMPONENTS_PER_VERTEX * BYTES_PER_FLOAT;

    public int numVertices;
    private float[] vertexData;
    public FloatBuffer vertexBuffer;

    private float[] centerlineData = {
            0.0f, 100f, 1.0f,
            0.0f, -100f, 1.0f
    };

    public Centerline(Context context) {
        this.context = context;
        vertexData = centerlineData.clone();
        numVertices = vertexData.length / TOTAL_COMPONENTS_PER_VERTEX;
    }

    public void makeVertexBuffer() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
    }
}
