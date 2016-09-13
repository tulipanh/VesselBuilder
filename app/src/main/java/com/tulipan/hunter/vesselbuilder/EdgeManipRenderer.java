package com.tulipan.hunter.vesselbuilder;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.View;

import com.tulipan.hunter.vesselbuilder.structures.Centerline;
import com.tulipan.hunter.vesselbuilder.structures.Edge;
import com.tulipan.hunter.vesselbuilder.structures.Gridpoints;
import com.tulipan.hunter.vesselbuilder.structures.ImageProject;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import utils.TextResourceReader;

import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.*;
import static android.opengl.Matrix.*;


/**
 * Created by Hunter on 3/18/2016.
 */

/**
 * Things to remember:
 *  - Matrices are in column-major order.
 *  - Switch cases need breaks at the end.
 *  - Angles in the Edge are measured from North, Clockwise is positive.
 *  - Angles in the Renderer are measured from East, Counter-Clockwise is positive.
 *  - Matrix transformations in general must be coded in reverse order.
 */

/**
 * Actions that need to be rendered as the user interacts via touch and output if accepted:
 *      - Trimming of the edge from either end
 *      - Rotating of the edge around its center-point
 *      - Translating of the edge in the x-axis
 *
 * A centerline must also be rendered as a reference point for the x-axis translation.
 * A horizontal line should connect the bottom point to the centerline, also as a reference.
 */

/**
 * Need to determine what needs to get saved when the app gets exited and how to restore the state.
 * e.g. when the tabs button is hit purposefully or accidentally.
 */

public class EdgeManipRenderer implements GLSurfaceView.Renderer{
    private final VesselBuilderActivity mActivity;
    private final Context context;
    private final AlterPageFragment mParentFragment;
    private ImageProject mProject;
    private float dAngle;
    private float mPreviousAngle;
    private float dDistance;
    private float mPreviousDistance;
    private int mPreviousTopTrim;
    private int mPreviousBottomTrim;

    public EditListener mEditListener;
    public static final int NONE = 0;
    public static final int TRIM = 1;
    public static final int ROTATE = 2;
    public static final int TRANSLATE = 3;
    public int mEditMode;

    /**
     * Instantiate protected strings to hold the names of glsl variables in the shaders.
     */
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_GOODCOLOR = "u_GoodColor";
    protected static final String U_BADCOLOR = "u_BadColor";
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLORBOOL = "a_ColorBool";

    /**
     * Instantiate integers to use as references to the glsl variables in the shader program.
     */
    private int uMatrixLocation;
    private int uGoodColorLocation;
    private int uBadColorLocation;
    private int aPositionLocation;
    private int aColorBoolLocation;

    /**
     * Instantiate matrices to hold transformations of the vertices being rendered.
     */
    private final float[] projectionMatrix = new float[16];

    private final float[] positionMatrix = new float[16];
    private final float[] transformationMatrix = new float[16];
    private final float[] transModelMatrix = new float[16];
    private final float[] transRModelMatrix = new float[16];

    private final float[] modelMatrix = new float[16];
    private final float[] refModelMatrix = new float[16];
    private final float[] modelProjectionMatrix = new float[16];
    private final float[] revMPMatrix = new float[16];

    private final float[] xReflectMatrix = {
            -1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private final float[] identityMatrix = new float[16];

    public Edge edge;
    private Centerline centerline;
    private Gridpoints grid;

    private int programObjectId;

    public EdgeManipRenderer(Context context, AlterPageFragment f, ImageProject p) {
        this.context = context;
        mActivity = (VesselBuilderActivity) context;
        mParentFragment = f;
        mProject = p;
        mEditListener = new EditListener();
        mEditMode = 0;
        mPreviousAngle = 0.0f;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST); // Likely unnecessary for just drawing lines in a plane.

        edge = new Edge(context, mProject.getEdgeVertexArray()); // Test using pre-generated data, figure out how I want to import real data later.
        edge.makeVertexBuffer();
        edge.makeColorBuffer();
        centerline = new Centerline(context);
        centerline.makeVertexBuffer();
        grid = new Gridpoints(context);
        grid.makeVertexBuffer();

        setIdentityM(identityMatrix, 0);

        String vertexShaderCode = TextResourceReader.readTextFileFromResource(context, R.raw.vertex_shader);
        String fragmentShaderCode = TextResourceReader.readTextFileFromResource(context, R.raw.fragment_shader);
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

        /**
         * Retrieval/Assignment of glsl variable locations goes here.
         */
        uMatrixLocation = glGetUniformLocation(programObjectId, U_MATRIX);
        uGoodColorLocation = glGetUniformLocation(programObjectId, U_GOODCOLOR);
        uBadColorLocation = glGetUniformLocation(programObjectId, U_BADCOLOR);
        aPositionLocation = glGetAttribLocation(programObjectId, A_POSITION);
        aColorBoolLocation = glGetAttribLocation(programObjectId, A_COLORBOOL);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);

        // Don't want a projection matrix transformation, but may want to resize with window change.
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;

        /**
         * Adjust the projection matrix to the aspect ratio of the screen being used.
         */

        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

        setIdentityM(modelMatrix, 0);
        multiplyMM(refModelMatrix, 0, modelMatrix, 0, xReflectMatrix, 0);
        setIdentityM(transformationMatrix, 0);

        //setIdentityM(positionMatrix, 0);
        //translateM(positionMatrix, 0, 0.5f, 0.5f, 0.0f);

        //setIdentityM(transformationMatrix, 0);
        //translateM(transformationMatrix, 0, 1.0f, 0.0f, 0.0f);
        //rotateM(transformationMatrix, 0, 10f, 0.0f, 0.0f, 1.0f);
        //translateM(transformationMatrix, 0, -1.0f, 0.0f, 0.0f);

        //multiplyMM(transPosModelMatrix, 0, modelMatrix, 0, transformationMatrix, 0);

        //multiplyMM(posModelMatrix, 0, modelMatrix, 0, positionMatrix, 0);
        //multiplyMM(posRModelMatrix, 0, refModelMatrix, 0, positionMatrix, 0);





        //transformEdge(TRANSLATE, 1.0f);
        //transformEdge(ROTATE, 10f);
        //transformEdge(TRANSLATE, -1.0f);
        //translateM(modelMatrix, 0, 0.0f, edge.startY, 0.0f);
        //translateM(refModelMatrix, 0, 0.0f, edge.startY, 0.0f);

    }

    /**
     * Rotate: Rotates around the origin.
     * Translate: Translates right (for positive numbers)
     * Translate + Rotate: Rotates around the origin, then translates right (for positive numbers).
     *  - i.e. The edge (0.0, 0.0), (1.0, 0.0) is tilted up 10 degrees from horizontal, and the point (0.0, 0.0) is now at (0.5, 0.0)
     * Rotate + Translate: Translates right, then rotates around the origin.
     *  - i.e. The same edge as above is tilted up more (as it was farther away), and the point (0.0, 0.0) is now above the x-axis.
     * Translate(-1), Rotate(10), Translate(1): The shape is rotated some, but the point (0.0, 0.0) is now above the x-axis.
     *  - It appears that the shape was translated to the right 1 unit, then rotated about the origin, then translated left 1 unit.
     *      This makes it seem like the operations are occurring in the reverse order of how they are written.
     * Translate(1), Rotate(10), Translate(-1): The shape is rotated and the point (1.0, 0.0) is in the same spot.
     *  - It appears that the shape was translated left 1 unit, then rotated about the origin, then translated right 1 unit.
     *      This confirms that the operations are occurring in reverse order.
     *
     * As a result of these experiments, it would seem the trouble I've been having is due to the fact that I need to apply
     * the transformations I'm doing in the reverse order of how they should take effect. So positioning the edge in the right
     * hemisphere should be applied last. Transformations should be applied first. Rotations about specific points should
     * involve operations in code for a translation away from the origin first, followed by rotation, followed by translation
     * toward the origin, as these operations will be performed in reverse order to the set of vertices.
     *
     * Apply Translate(1), Rotate(10), Translate(-1) to modelMatrix, then apply xReflect to get refModelMatrix:
     *  - This seems to apply the same to transformations to refModelMatrix that are applied to modelMatrix,
     *      which is not what we want. We want to the reflection across the y-axis to occur after the transformations,
     *      thus giving us a mirror of the transformations.
     *
     * Apply the above transformations after applying the xReflect to get refModelMatrix:
     *  - This seems to apply the transformations only to the modelMatrix. So refModelMatrix is the original shape,
     *      reflected across the y-axis with no transformations applied. Thus, it would seem we need to do the reflection,
     *      then apply mirror transformations to the two matrices, doubling the number of calculations.
     *
     * Multiply a positioning matrix to the modelMatrix:
     *  - Translates the model up 0.5 and right 0.5 as expected.
     *
     * Multiply the same positioning matrix to the refModelMatrix:
     *  - Translate the model up 0.5 and left 0.5, doing the desired mirror transformation.
     *
     * Multiply [transformationMatrix][positionMatrix][modelMatrix] or [positionMatrix][transformationMatrix][modelMatrix]
     *  - Multiplying the transformation matrix after the position matrix seems to apply the positioning matrix and then
     *      the transformation matrix. So instead of getting the desired transformation, moved to the desired spot,
     *      we get a different transformation.
     *
     * Try these with the refModelMatrix as well:
     *
     *
     * Options:
     *  - Apply the positioning operations every frame after all transformations (more processing but the same amount of code).
     *  - Incorporate the removing and reapplying of the positioning operations into the transformations (even more processing more better organized code).
     *  - Apply the positioning operations directly to the vertex data once, before any transformations happen (more front end processing, less continual, simpler code).
     *
     */

    @Override
    public void onDrawFrame(GL10 glUnused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Likely don't need the depth buffer.

        /**
         * Setting/Altering of perspective and model matrices goes here.
         */

        /**
         * This is also where translation and rotation matrices are
         * applied, as set by user interaction.
         */
        // Translate to center, rotate it to match the SeekBar, and translate it back to (0.5, 0)
        // translateM(modelMatrix, 0, -0.5f, 0f, 0f);
        //transformEdge(ROTATE, -dAngle);
        //transformEdge(TRANSLATE, dDistance);
        // translateM(modelMatrix, 0, 0.5f, 0f, 0f);

        if(mEditMode == ROTATE) {
            transformEdge(ROTATE, -dAngle, 0f, 0f);
        } else if (mEditMode == TRANSLATE) {
            transformEdge(TRANSLATE, 0f, dDistance, 0f);
        }
        multiplyMM(transModelMatrix, 0, modelMatrix, 0, transformationMatrix, 0);
        multiplyMM(transRModelMatrix, 0, refModelMatrix, 0, transformationMatrix, 0);

        multiplyMM(modelProjectionMatrix, 0, projectionMatrix, 0, transModelMatrix, 0);
        multiplyMM(revMPMatrix, 0, projectionMatrix, 0, transRModelMatrix, 0);

        glUseProgram(programObjectId);

        /**
         * Importing of uniforms to the glsl program goes here.
         */
        glUniform4f(uGoodColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        glUniform4f(uBadColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glUniformMatrix4fv(uMatrixLocation, 1, false, modelProjectionMatrix, 0);

        /**
         * Importing of attributes to the glsl program goes here.
         */
        edge.vertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, Edge.POSITION_COMPONENTS_PER_VERTEX, GL_FLOAT, false, edge.POSITION_STRIDE, edge.vertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        edge.vertexBuffer.position(0);

        edge.colorBuffer.position(0);
        glVertexAttribPointer(aColorBoolLocation, Edge.COLORBOOL_COMPONENTS_PER_VERTEX, GL_FLOAT, false, edge.COLOR_STRIDE, edge.colorBuffer);
        glEnableVertexAttribArray(aColorBoolLocation);
        edge.colorBuffer.position(0);

        glDrawArrays(GL_LINES, 0, edge.numVertices);

        /**
         * Import new uniforms to render the reflection.
         */
        glUniform4f(uBadColorLocation, 0.0f, 0.0f, 0.0f, 0.0f);
        glUniformMatrix4fv(uMatrixLocation, 1, false, revMPMatrix, 0);

        edge.vertexBuffer.position(0);
        glDrawArrays(GL_LINES, 0, edge.numVertices);

        /**
         * Import new uniforms and attributes to render the centerline.
         */

        glUniform4f(uGoodColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glUniformMatrix4fv(uMatrixLocation, 1, false, identityMatrix, 0);

        centerline.vertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, Centerline.POSITION_COMPONENTS_PER_VERTEX, GL_FLOAT, false, centerline.STRIDE, centerline.vertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        centerline.vertexBuffer.position(0);

        centerline.vertexBuffer.position(Centerline.POSITION_COMPONENTS_PER_VERTEX);
        glVertexAttribPointer(aColorBoolLocation, Centerline.COLORBOOL_COMPONENTS_PER_VERTEX, GL_FLOAT, false, centerline.STRIDE, centerline.vertexBuffer);
        glEnableVertexAttribArray(aColorBoolLocation);
        centerline.vertexBuffer.position(0);

        glDrawArrays(GL_LINES, 0, centerline.numVertices);

        /**
         * Import new uniforms and attributes to render the grid.
         */

        glUniform4f(uGoodColorLocation, 1.0f, 1.0f, 1.0f, 0.5f);
        glUniform4f(uBadColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        grid.vertexBuffer.position(0);
        glVertexAttribPointer(aPositionLocation, Gridpoints.POSITION_COMPONENTS_PER_VERTEX, GL_FLOAT, false, grid.STRIDE, grid.vertexBuffer);
        glEnableVertexAttribArray(aPositionLocation);
        grid.vertexBuffer.position(0);

        grid.vertexBuffer.position(Gridpoints.POSITION_COMPONENTS_PER_VERTEX);
        glVertexAttribPointer(aColorBoolLocation, Gridpoints.COLORBOOL_COMPONENTS_PER_VERTEX, GL_FLOAT, false, grid.STRIDE, grid.vertexBuffer);
        glEnableVertexAttribArray(aColorBoolLocation);
        grid.vertexBuffer.position(0);

        glDrawArrays(GL_POINTS, 0, grid.numVertices);
    }

    /**
     * Need to somehow set it up so that the change happens in pairs, as each vertex is defined twice (except the ends)
     * and we are not technically trimming vertices, we are trimming line segments, defined by pairs of vertices.
     */
    public boolean trimHandler(boolean top) {
        if (top) {
            if (mPreviousTopTrim < edge.topTrimLimit) {
                return false;
            } else {
                edge.topTrim = mPreviousTopTrim;
                edge.updateColorData();
                return true;
            }
        } else {
            if (mPreviousBottomTrim > edge.bottomTrimLimit) {
                return false;
            } else {
                edge.bottomTrim = mPreviousBottomTrim;
                edge.updateColorData();
                return true;
            }
        }
    }

    public boolean rotateHandler(float angle) {
        float minAngle = edge.getMinAngle();
        if (mPreviousAngle >= minAngle) {
            if (angle >= minAngle) {
                // If both start and end angle are in the acceptable range.
                // Do the rotation.
                dAngle = angle - mPreviousAngle;
                return true;
            } else {
                // If start angle is acceptable, but end angle is not.
                // Rotate to minAngle and no further.
                dAngle = minAngle - mPreviousAngle;
                return false;
            }
        } else {
            if (angle >= minAngle) {
                // If start angle is unacceptable, but end angle is.
                // Rotate from minAngle;
                dAngle = angle - minAngle;
                return true;
            } else {
                // If neither start nor end angle are in the acceptable range.
                // Don't do the rotation.
                dAngle = 0;
                return false;
            }
        }
    }

    public boolean translateHandler(float distance) {
        float minDistance = edge.getMinDistance();
        if (mPreviousDistance >= minDistance) {
            if (distance >= minDistance) {
                // If both start and end distance are in the acceptable range.
                // Do the translation.
                dDistance = distance - mPreviousDistance;
                return true;
            } else {
                // If start distance is acceptable, but end distance is not.
                // Translate to minDistance and no further.
                dDistance = minDistance - mPreviousDistance;
                return false;
            }
        } else {
            if (distance >= minDistance) {
                // If start distance is unacceptable, but end distance is.
                // Translate from minDistance.
                dDistance = distance - minDistance;
                return true;
            } else {
                // If neither start nor end distance are in the acceptable range.
                // Don't do the translation.
                dDistance = 0;
                return false;
            }
        }
    }

    public void trimData() {
        edge.trim();
    }

    public void translateData() {
        edge.translate(mPreviousDistance);
        mPreviousDistance = 0;
        dDistance = 0;
    }

    public void rotateData() {
        edge.rotate(mPreviousAngle);
        mPreviousAngle = 0;
        dAngle = 0;
    }

    public void setEditMode(int mode) {
        if (mode == TRIM) {
            mEditMode = TRIM;
            mPreviousTopTrim = edge.topTrim;
            mPreviousBottomTrim = edge.bottomTrim;
            edge.calcTrimBounds();
        } else if (mode == ROTATE) {
            mEditMode = ROTATE;
            edge.calcRotateBounds();
        } else if (mode == TRANSLATE) {
            mEditMode = TRANSLATE;
            edge.calcTranslateBounds(mPreviousDistance);
        } else {
            mEditMode = NONE;
        }
    }

    public void resetMatrices() {
        setIdentityM(transformationMatrix, 0);
    }

    private void transformEdge(int operation, float angle, float xAmount, float yAmount) {
        if (operation == TRIM) {

        } else if (operation == ROTATE) {
            translateM(transformationMatrix, 0, edge.getPivotX(), edge.getPivotY(), 0);
            rotateM(transformationMatrix, 0, angle, 0.0f, 0.0f, 1.0f);
            translateM(transformationMatrix, 0, -edge.getPivotX(), -edge.getPivotY(), 0);
        } else if (operation == TRANSLATE) {
            translateM(transformationMatrix, 0, xAmount, yAmount, 0.0f);
        } else {

        }
    }

    private class EditListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            boolean allowed;

            switch(mEditMode) {
                case TRIM:
                    if (v == mActivity.findViewById(R.id.trim_bottom_button)) {
                        mPreviousBottomTrim -= 1;
                        allowed = trimHandler(false);
                    } else if (v == mActivity.findViewById(R.id.untrim_bottom_button)) {
                        mPreviousBottomTrim += 1;
                        allowed = trimHandler(false);
                    } else if (v == mActivity.findViewById(R.id.trim_top_button)) {
                        mPreviousTopTrim += 1;
                        allowed = trimHandler(true);
                    } else if (v == mActivity.findViewById(R.id.untrim_top_button)) {
                        mPreviousTopTrim -= 1;
                        allowed = trimHandler(true);
                    } else {
                        allowed = false;
                    }

                    // mParentFragment.trimSuite.updateText(mPreviousTopTrim, mPreviousBottomTrim, allowed);
                    mParentFragment.requestRender();
                    break;

                case ROTATE:
                    float angleChange = 0.0f;
                    if (v == mActivity.findViewById(R.id.rotate_counteronedeg_button)) {
                        angleChange = -1.0f;
                    } else if (v == mActivity.findViewById(R.id.rotate_countertenthdeg_button)) {
                        angleChange = -0.1f;
                    } else if (v == mActivity.findViewById(R.id.rotate_clocktenthdeg_button)) {
                        angleChange = 0.1f;
                    } else if (v == mActivity.findViewById(R.id.rotate_clockonedeg_button)) {
                        angleChange = 1.0f;
                    } else {
                        // It should be impossible to reach this case.
                    }

                    float newAngle = mPreviousAngle + angleChange;
                    allowed = rotateHandler(newAngle);
                    mPreviousAngle = newAngle;
                    // mParentFragment.rotateSuite.updateText(newAngle, allowed);
                    mParentFragment.requestRender();
                    break;

                case TRANSLATE:
                    float distanceChange = 0.0f;
                    if (v == mActivity.findViewById(R.id.translate_lefttenth_button)) {
                        distanceChange = -0.1f;
                    } else if (v == mActivity.findViewById(R.id.translate_lefthundredth_button)) {
                        distanceChange = -0.01f;
                    } else if (v == mActivity.findViewById(R.id.translate_righthundredth_button)) {
                        distanceChange = 0.01f;
                    } else if (v == mActivity.findViewById(R.id.translate_righttenth_button)) {
                        distanceChange = 0.1f;
                    } else {
                        // It should be impossible to reach this case.
                    }

                    float newDistance = mPreviousDistance + distanceChange;
                    allowed = translateHandler(newDistance);
                    mPreviousDistance = newDistance;
                    // mParentFragment.translateSuite.updateText(newDistance, allowed);
                    mParentFragment.requestRender();
                    break;

                default:
                    // It should be impossible to reach this case.
                    break;
                    // Do Nothing? Make a Toast?
            }
        }
    }
}
