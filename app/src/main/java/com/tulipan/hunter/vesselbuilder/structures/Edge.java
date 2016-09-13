package com.tulipan.hunter.vesselbuilder.structures;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Hunter on 3/18/2016.
 */
public class Edge {
    private final Context context;
    private float[] mSourceArray;
    private static final int TOP = 1;
    private static final int BOTTOM = 2;
    private static final int BYTES_PER_FLOAT = 4;
    public static final int POSITION_COMPONENTS_PER_VERTEX = 2;
    public static final int COLORBOOL_COMPONENTS_PER_VERTEX = 1;
    public static final int TOTAL_COMPONENTS_PER_VERTEX = POSITION_COMPONENTS_PER_VERTEX + COLORBOOL_COMPONENTS_PER_VERTEX;
    public static final int POSITION_STRIDE = POSITION_COMPONENTS_PER_VERTEX * BYTES_PER_FLOAT;
    public static final int COLOR_STRIDE = COLORBOOL_COMPONENTS_PER_VERTEX * BYTES_PER_FLOAT;
    public int numVertices;
    public float startX;
    public float startY;

    private float[] pivotPoint = new float[2];
    private float[] vertexData;
    private float[] vertexColorData;
    private float[] transformData;
    private float translateMin;
    private float rotateMin;
    public int topTrim;
    public int bottomTrim;
    public int topTrimLimit;
    public int bottomTrimLimit;

    public FloatBuffer vertexBuffer;
    public FloatBuffer colorBuffer;

    /**
     * To apply different color to the selected and trimmed parts of the edge, may need to
     * split edge into three parts and use two color uniforms.
     *
     * Alternatively, could import color as an attribute with a color associated with each line
     * and stored in the same data array. Then alter array as the slider is moved.
     *
     * In either case, the edge data would need to be altered each time a frame is drawn.
     *
     * Or perhaps it could be done in the glsl code somehow. Maybe a counter counting how many
     * vertices have been rendered and changing the color based on the number.
     */

    /**
     * Change centerPoint to pivotPoint. Have this change as the user selects a different range of points.
     * (Pivot point should be the point with the lowest y-coord, likely the last defined point).
     * Perhaps want to render this point to the user can clearly see where the pivot occurs.
     *
     * Also need to implement so kind of collision detection/prevention along the center line.
     * Also need to render a centerline.
     */

    private float[] testData = {
            0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 1.0f,
            0.25f, 0.5f, 1.0f,
            0.25f, 0.5f, 1.0f,
            0.5f, 0.0f, 1.0f};

    private float[] testDataOther = {
            0.2f, 1.0f, 1.0f,
            0.3f, 0.9f, 1.0f,
            0.3f, 0.9f, 1.0f,
            0.35f, 0.8f, 1.0f,
            0.35f, 0.8f, 1.0f,
            0.5f, 0.7f, 1.0f,
            0.5f, 0.7f, 1.0f,
            0.45f, 0.6f, 1.0f,
            0.45f, 0.6f, 1.0f,
            0.4f, 0.5f, 1.0f,
            0.4f, 0.5f, 1.0f,
            0.3f, 0.4f, 1.0f,
            0.3f, 0.4f, 1.0f,
            0.2f, 0.3f, 1.0f,
            0.2f, 0.3f, 1.0f,
            0.25f, 0.2f, 1.0f,
            0.25f, 0.2f, 1.0f,
            0.33f, 0.1f, 1.0f,
            0.33f, 0.1f, 1.0f,
            0.3f, 0.0f, 1.0f};

    public Edge(Context context, float[] source) {
        this.context = context;
        mSourceArray = source.clone();
        /* Need to flip and scale down the vertex data */
        flipAndScale();
        vertexData = new float[mSourceArray.length];
        vertexColorData = new float[mSourceArray.length / 2];
        int index = 0;
        int indexV = 0;
        int indexC = 0;
        while (index < mSourceArray.length) {
            vertexData[indexV] = mSourceArray[index];
            vertexData[indexV+1] = mSourceArray[index+1];
            vertexColorData[indexC] = 1.0f;
            index += 2;
            indexV += 2;
            indexC += 1;
        }
        numVertices = vertexData.length / POSITION_COMPONENTS_PER_VERTEX;
        topTrim = 0;
        bottomTrim = numVertices / 2 - 1;
        pivotPoint[0] = vertexData[vertexData.length - POSITION_COMPONENTS_PER_VERTEX];
        pivotPoint[1] = vertexData[vertexData.length - POSITION_COMPONENTS_PER_VERTEX + 1];
        transformData = new float[numVertices * 4];

        indexV = 0;
        int indexT = 0;
        float distance, xdiff, ydiff;
        float minX = vertexData[0];
        while (indexV < vertexData.length) {
            transformData[indexT] = vertexData[indexV];       // Transcribe X Coordinate
            transformData[indexT + 1] = vertexData[indexV + 1];       // Transcribe Y Coordinate
            xdiff = vertexData[indexV] - pivotPoint[0];
            ydiff = vertexData[indexV+1] - pivotPoint[1];
            distance = (float) Math.sqrt(xdiff * xdiff + ydiff * ydiff);    // Find distance from pivot via Pythagoras
            transformData[indexT + 2] = distance;        // Transcribe the distance in the third index
            if (distance > 0) {
                transformData[indexT+3] = ydiff > 0 ? (float) Math.asin(xdiff/distance) : (float) (Math.PI - Math.asin(xdiff/distance)); // Finds angle from vertical of the point from the pivot. Clockwise is positive.
            } else transformData[indexT+3] = (float) Math.PI;
            if (vertexData[indexV] < minX) minX = vertexData[indexV];
            indexV += 2;
            indexT += 4;
        }
        // transformData now contains (for each vertex) [xCoord, yCoord, distance from pivot, angle from vertical from pivot]
        // Calculate starting point: (startX is -1 * the lowest X value, so that it is touching the centerline, startY is -1 * the middle Y value so it is centered, assuming the edge is defined with vertices in order)
        startX = -minX;
        startY = -transformData[(numVertices/2)*4 + 1];
        pivotPoint[0] += startX;
        pivotPoint[1] += startY;

        // Apply starting translation
        indexT = 0;
        indexV = 0;
        while (indexT < transformData.length) {
            transformData[indexT] += startX;
            transformData[indexT + 1] += startY;
            vertexData[indexV] += startX;
            vertexData[indexV+1] += startY;

            indexT += 4;
            indexV += 2;
        }
        translateMin = 0.0f;
    }

    private void flipAndScale() {
        float maxX = 0f;
        float maxY = 0f;
        int xIndex = 0;
        int yIndex = 1;
        while (yIndex < mSourceArray.length) {
            if (mSourceArray[xIndex] > maxX) maxX = mSourceArray[xIndex];
            if (mSourceArray[yIndex] > maxY) maxY = mSourceArray[yIndex];
            xIndex += 2;
            yIndex += 2;
        }

        float scalingFactor = Math.max(maxX, maxY);
        xIndex = 0;
        yIndex = 1;
        while (yIndex < mSourceArray.length) {
            mSourceArray[xIndex] = mSourceArray[xIndex] / scalingFactor;
            mSourceArray[yIndex] = (maxY - mSourceArray[yIndex]) / scalingFactor;
            xIndex += 2;
            yIndex += 2;
        }
    }

    private void prepareTransformData() {
        int indexV = 0;
        int indexT = 0;
        float distance, xdiff, ydiff;
        float minDistance = vertexData[topTrim*4];
        while (indexV < vertexData.length) {
            transformData[indexT] = vertexData[indexV];       // Transcribe X Coordinate
            if (vertexData[indexV] < minDistance && indexV >= topTrim*4 && indexV < (bottomTrim+1)*4) minDistance = vertexData[indexV];
            transformData[indexT+1] = vertexData[indexV+1];       // Transcribe Y Coordinate
            xdiff = vertexData[indexV] - pivotPoint[0];
            ydiff = vertexData[indexV+1] - pivotPoint[1];
            distance = (float) Math.sqrt(xdiff * xdiff + ydiff * ydiff);    // Find distance from pivot via Pythagoras
            transformData[indexT+2] = distance;        // Transcribe the distance in the third index
            if (distance > 0) {
                transformData[indexT+3] = ydiff > 0 ? (float) Math.asin(xdiff/distance) : (float) (Math.PI - Math.asin(xdiff/distance)); // Finds angle from vertical of the point from the pivot. Clockwise is positive.
            } else transformData[indexT+3] = (float) Math.PI;
            indexV += 2;
            indexT += 4;
        }
        translateMin = 0 - minDistance;
    }

    public void makeVertexBuffer() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
    }

    public void makeColorBuffer() {
        colorBuffer = ByteBuffer.allocateDirect(vertexColorData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexColorData);
    }

    /**
     * Could also do collision detection by keeping arrays of distances from from the pivot point, and point locations(for rotation calculations),
     * and the x-values of each point (for translation calculations).
     * Then perform simple calculations on each one, rather than full matrix multiplications on all points (and reversing them all if a collision occurs).
     *
     * Keep a distance and an angle for each point. These can be used to calculate the x coordinate to be tested.
     *
     * What happens when the pivot point changes (like when trimming the bottom)?
     */

    public float getMinAngle() {return rotateMin;}

    public float getMinDistance() {return translateMin;}

    public float getPivotX() {
        return pivotPoint[0];
    }

    public float getPivotY() {
        return pivotPoint[1];
    }

    public void updateColorData() {
        for (int i = 0; i < numVertices; i++) {
            vertexColorData[i] = (i < topTrim*2 || i > bottomTrim*2 + 1) ? 0.0f : 1.0f;
        }
        makeColorBuffer();
    }

    public void calcTrimBounds() {
        int topIndex = topTrim * 4;
        int bottomIndex = bottomTrim * 4 + 2;

        while (topIndex >= 0) {
            if (vertexData[topIndex] < 0) {
                topTrimLimit = (topIndex+4) / 4;
                break;
            }
            topIndex -= 4;
        }
        if (topIndex < 0) topTrimLimit = 0;

        while (bottomIndex < vertexData.length) {
            if (vertexData[bottomIndex] < 0) {
                bottomTrimLimit = (bottomIndex-6) / 4;
                break;
            }
            bottomIndex += 4;
        }
        if (bottomIndex >= vertexData.length) bottomTrimLimit = numVertices / 2 - 1;
    }

    /**
     * Perhaps we will need a maxAngle as well sometime in the future to accommodate wraparound or oddly shaped edges.
     * e.g. when a segment comes before the pivot point but extends to a lower y-coord value.
     */

    public void calcRotateBounds() {
        int index = topTrim * 8;
        float xDiff = -pivotPoint[0];
        float angle;
        float maxAngle = (float) -Math.PI;
        float rotAngle = 0f;

        if (xDiff == 0){
            float minAngle = (float) Math.PI;
            while (index < (bottomTrim+1)*8) {
                if (transformData[index+3] < minAngle) {
                    minAngle = transformData[index+3];
                }
                index += 4;
            }
            rotateMin = (float) -Math.toDegrees(minAngle);
        } else {
            while (index < (bottomTrim+1)*8) {
                // If the distance from the pivot point is greater than the x-coord of the pivot point, then calculate the angle at which that point will cross the y-axis, otherwise don't bother.
                if (transformData[index+2] > pivotPoint[0]) {
                    angle = (float) Math.asin(xDiff/transformData[index+2]);
                } else {
                    angle = (float) -Math.PI;
                }
                rotAngle = angle - transformData[index+3];
                if (rotAngle > maxAngle) maxAngle = rotAngle;
                index += 4;
            }
            maxAngle = (float) Math.toDegrees(maxAngle);
            rotateMin = maxAngle;       // maxAngle holds the angle at which the first point reaches x = 0. The amount we can rotate will be the difference between this and the current angle of that point.
        }
    }

    public void calcTranslateBounds(float startDistance) {
        int index = topTrim * 8;
        float minX = transformData[0];
        while (index < (bottomTrim+1)*8) {
            if (transformData[index] < minX) minX = transformData[index];
            index += 4;
        }
        translateMin = startDistance - minX;
    }

    public void trim() {
        pivotPoint[0] = vertexData[bottomTrim*4+2];
        pivotPoint[1] = vertexData[bottomTrim*4+3];
        prepareTransformData();
    }

    public void translate(float distance) {
        float transDist;
        if (distance >= translateMin) {
            transDist = distance;
        } else {
            transDist = translateMin;
        }
        // The rest of this function still needs work.
        int indexV = 0;
        int indexT = 0;
        while (indexV < vertexData.length) {
            transformData[indexT] += transDist;
            vertexData[indexV] = transformData[indexT];
            indexV += 2;
            indexT += 4;
        }
        pivotPoint[0] += transDist;
        prepareTransformData();
        makeVertexBuffer();
    }

    // Angle from vertical, clockwise is positive.
    public void rotate(float angle) {
        float rotAngle;
        if (angle >= rotateMin) {
            rotAngle = angle;
        } else {
            rotAngle = rotateMin;
        }
        int indexV = 0;
        int indexT = 0;
        while (indexV < vertexData.length) {
            transformData[indexT+3] += Math.toRadians(rotAngle);
            vertexData[indexV] = pivotPoint[0] + ((float) (Math.sin(transformData[indexT+3])) * transformData[indexT+2]);
            vertexData[indexV+1] = pivotPoint[1] + ((float) (Math.cos(transformData[indexT+3])) * transformData[indexT+2]);
            indexV += 2;
            indexT += 4;
        }
        prepareTransformData();
        makeVertexBuffer();
    }

    public float[] exportEdgeData() {
        /**
         * This function should take all transformations (trim, rotate, translate) into account and return
         * an array containing only the selected vertices after all given transformations.
         * This vertex data will be the input for an EdgeOBJ class.
         */
        float[] vertexArray;
        int indexD = topTrim*4;
        int indexA = 2;
        vertexArray = new float[(bottomTrim-topTrim+2)*2];

        vertexArray[0] = vertexData[indexD];
        vertexArray[1] = vertexData[indexD+1];
        indexD += 2;

        while (indexD < (bottomTrim+1)*4) {
            vertexArray[indexA] = vertexData[indexD];
            vertexArray[indexA+1] = vertexData[indexD+1];
            indexD += 4;
            indexA += 2;
        }

        return vertexArray;
    }
}

/**
 * 0.25, 0.5, 1, 0
 * 0.25, 0, 0.5, 0
 * 0.25, 0, 0.5, 0
 * 0, 0, 0.559017, -0.4636476
 * 0, 0, 0.559017, -0.4636476
 * 0.25, -0.5, 0.0, PI
 */
