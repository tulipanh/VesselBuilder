package com.tulipan.hunter.vesselbuilder;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.tulipan.hunter.vesselbuilder.structures.OBJModel;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import utils.MatrixHelper;
import utils.TextResourceReader;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

/**
 * Created by Hunter on 7/11/2016.
 */

public class ModelRenderer implements GLSurfaceView.Renderer{
    private final Context mContext;
    private final ModelRendererFragment mParentFragment;
    private OBJModel mObjectModel;

    private float[] rotationMatrix = new float[16];
    private float mSurfaceHeight;
    private float mSurfaceWidth;
    private float mMinDist;
    private float mMaxDist;
    private float mCameraDist;

    protected static final String U_MVPMATRIX = "u_MVPMatrix";
    protected static final String U_MMATRIX = "u_MMatrix";
    protected static final String U_COLOR = "u_Color";
    protected static final String A_POSITION = "a_Position";
    protected static final String A_NORMAL = "a_Normal";
    protected static final String U_VECTOR_TO_LIGHT = "u_VectorToLight";

    private int uMVPMatrixLocation;
    private int uMMatrixLocation;
    private int uColorLocation;
    private int aPositionLocation;
    private int aNormalLocation;
    private int uVectorToLightLocation;

    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    private int programObjectId;

    public ModelRenderer(Context context, ModelRendererFragment parent, OBJModel model) {
        mObjectModel = model;
        mObjectModel.makeVertexBuffer();
        this.mContext = context;
        mParentFragment = parent;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);

        // mObjectModel.makeVertexBuffer(); // This will be altered by the user's input, so maybe put this in onDrawFrame().

        String vertexShaderCode = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
        String fragmentShaderCode = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);
        final int vertexShaderObjectId = glCreateShader(GL_VERTEX_SHADER);
        final int fragmentShaderObjectId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vertexShaderObjectId, vertexShaderCode);
        glCompileShader(vertexShaderObjectId);
        glShaderSource(fragmentShaderObjectId, fragmentShaderCode);
        glCompileShader(fragmentShaderObjectId);

        programObjectId = glCreateProgram();
        glAttachShader(programObjectId, vertexShaderObjectId);
        glAttachShader(programObjectId, fragmentShaderObjectId);
        glLinkProgram(programObjectId);

        uMVPMatrixLocation = glGetUniformLocation(programObjectId, U_MVPMATRIX);
        uMMatrixLocation = glGetUniformLocation(programObjectId, U_MMATRIX);
        uColorLocation = glGetUniformLocation(programObjectId, U_COLOR);
        aPositionLocation = glGetAttribLocation(programObjectId, A_POSITION);
        aNormalLocation = glGetAttribLocation(programObjectId, A_NORMAL);
        uVectorToLightLocation = glGetUniformLocation(programObjectId, U_VECTOR_TO_LIGHT);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);

        mSurfaceHeight = height;
        mSurfaceWidth = width;

        mCameraDist = (float) (mObjectModel.getRadius() / Math.sin(Math.toRadians(50.0)));
        mMinDist = 1f;
        mMaxDist = (2*mCameraDist < 10f ? 10f : 2*mCameraDist);

        MatrixHelper.perspectiveM(projectionMatrix, 100, (float) width
                / (float) height, mMinDist, mMaxDist);

        setLookAtM(viewMatrix, 0, 0f, 0f, mMinDist + mCameraDist, 0f, 0f, 0f, 0f, 1f, 0f);

        setIdentityM(modelMatrix, 0);
        setIdentityM(rotationMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Rotations will be set to respond to touch controls

        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);

        glUseProgram(programObjectId);
        glUniformMatrix4fv(uMVPMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
        glUniformMatrix4fv(uMMatrixLocation, 1, false, modelMatrix, 0);
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glUniform3f(uVectorToLightLocation, 0.5773503f, 0.5773503f, 0.5773503f);

        mObjectModel.vertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, OBJModel.POSITION_COMPONENTS_PER_VERTEX, GL_FLOAT, false, OBJModel.STRIDE, mObjectModel.vertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        mObjectModel.vertexBuffer.position(0);

        mObjectModel.vertexBuffer.position(OBJModel.POSITION_COMPONENTS_PER_VERTEX);
        glVertexAttribPointer(aNormalLocation, OBJModel.NORMAL_COMPONENTS_PER_VERTEX, GL_FLOAT, false, OBJModel.STRIDE, mObjectModel.vertexBuffer);
        glEnableVertexAttribArray(aNormalLocation);
        mObjectModel.vertexBuffer.position(0);

        glDrawArrays(GL_TRIANGLES, 0, mObjectModel.numVertices);
    }

    public boolean rotateModel(float xComp, float yComp, float zComp, float angle) {
        if ((xComp + yComp + zComp) == 0f) return false;
        setIdentityM(rotationMatrix, 0);
        rotateM(rotationMatrix, 0, angle, xComp, yComp, zComp);
        multiplyMM(modelMatrix, 0, rotationMatrix, 0, modelMatrix, 0);
        mParentFragment.requestRender();
        return true;
    }

    public boolean zoomIn(float percent) {
        if (percent > 1f) return false;
        if ((mCameraDist * (1f - percent)) < (0.3*mObjectModel.getRadius())) return false;

        mCameraDist = mCameraDist * (1f - percent);
        setCamera();
        mParentFragment.requestRender();
        return true;
    }

    public boolean zoomOut(float percent) {
        if (percent < -1f) return false;
        mCameraDist = mCameraDist * (1f / (1f - percent));
        setCamera();
        mParentFragment.requestRender();
        return true;
    }

    public void reset() {
        setIdentityM(modelMatrix, 0);
        mCameraDist = (float) (mObjectModel.getRadius() / Math.sin(Math.toRadians(50.0)));
        setCamera();
        mParentFragment.requestRender();
    }

    private void setCamera() {
        mMinDist = 1f;
        mMaxDist = (2*mCameraDist < 10f ? 10f : 2*mCameraDist);

        MatrixHelper.perspectiveM(projectionMatrix, 100, mSurfaceWidth
                / mSurfaceHeight, mMinDist, mMaxDist);

        setLookAtM(viewMatrix, 0, 0f, 0f, mMinDist + mCameraDist, 0f, 0f, 0f, 0f, 1f, 0f);
    }
}
