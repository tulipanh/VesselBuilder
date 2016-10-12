package com.tulipan.hunter.vesselbuilder;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tulipan.hunter.vesselbuilder.structures.EdgeOBJ;
import com.tulipan.hunter.vesselbuilder.structures.ImageProject;
import com.tulipan.hunter.vesselbuilder.structures.ModelFile;

/**
 * Created by Hunter on 9/2/2016.
 */
public class AlterPageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;

    private GLSurfaceView glSurfaceView;
    private EdgeManipRenderer mRenderer = null;
    private boolean rendererSet = false;
    private static ImageProject mProject;

    private LinearLayout trimLayout;
    private LinearLayout rotateLayout;
    private LinearLayout translateLayout;
    public TrimSuite trimSuite;
    public RotateSuite rotateSuite;
    public TranslateSuite translateSuite;
    private Button trimButton;
    private Button rotateButton;
    private Button translateButton;
    private Button meshButton;
    private boolean trimActive;
    private boolean rotateActive;
    private boolean translateActive;

    public static AlterPageFragment newInstance(ImageProject project) {
        AlterPageFragment fragment = new AlterPageFragment();
        mProject = project;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.alter_page_fragment, container, false);

        glSurfaceView = (GLSurfaceView) v.findViewById(R.id.gl_surface);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        final ActivityManager activityManager = (ActivityManager) mCurrentActivity.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")));
        if (supportsEs2) {
            glSurfaceView.setEGLContextClientVersion(2);
            mRenderer = new EdgeManipRenderer(mCurrentActivity, this, mProject);
            glSurfaceView.setRenderer(mRenderer);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            rendererSet = true;
        } else {
            Toast.makeText(mCurrentActivity, "This device does not support OpenGl ES 2.0.", Toast.LENGTH_LONG).show();
            return null;
        }


        trimLayout = (LinearLayout) v.findViewById(R.id.trim_layout);
        trimSuite = new TrimSuite(mRenderer, v);

        rotateLayout = (LinearLayout) v.findViewById(R.id.rotate_layout);
        rotateSuite = new RotateSuite(mRenderer, v);

        translateLayout = (LinearLayout) v.findViewById(R.id.translate_layout);
        translateSuite = new TranslateSuite(mRenderer, v);

        trimActive = false;
        rotateActive = false;
        translateActive = false;

        trimButton = (Button) v.findViewById(R.id.alterpage_trim_button);
        trimButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveTransformation();
                trimLayout.setVisibility(View.VISIBLE);
                trimActive = true;
                mRenderer.setEditMode(EdgeManipRenderer.TRIM);
                trimButton.setSelected(true);
            }
        });

        rotateButton = (Button) v.findViewById(R.id.alterpage_rotate_button);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveTransformation();
                rotateLayout.setVisibility(View.VISIBLE);
                rotateActive = true;
                mRenderer.setEditMode(EdgeManipRenderer.ROTATE);
                rotateButton.setSelected(true);
            }
        });

        translateButton = (Button) v.findViewById(R.id.alterpage_translate_button);
        translateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveTransformation();
                translateLayout.setVisibility(View.VISIBLE);
                translateActive = true;
                mRenderer.setEditMode(EdgeManipRenderer.TRANSLATE);
                translateButton.setSelected(true);
            }
        });

        meshButton = (Button) v.findViewById(R.id.alterpage_export_button);
        meshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveTransformation();
                EdgeOBJ edgeObject = new EdgeOBJ(mCurrentActivity, mRenderer.edge.exportEdgeData(), mProject.getPhotoImage());
                /* TODO: Coordinate EdgeOBJ with ImageProject to save OBJ file to appropriate file name */
                ModelFile objectFile = new ModelFile(edgeObject.getFilePath());
                mCurrentActivity.replaceFragment(ModelRendererFragment.newInstance(objectFile));
            }
        });

        return v;
    }

    private void saveTransformation() {
        if (trimActive) {
            mRenderer.trimData();
            trimLayout.setVisibility(View.GONE);
            trimActive = false;
        }
        if (rotateActive) {
            mRenderer.rotateData();
            mRenderer.resetMatrices();
            // rotateSuite.updateText(0, true);
            rotateLayout.setVisibility(View.GONE);
            rotateActive = false;
        }
        if (translateActive) {
            mRenderer.translateData();
            mRenderer.resetMatrices();
            // translateSuite.updateText(0, true);
            translateLayout.setVisibility(View.GONE);
            translateActive = false;
        }
        trimButton.setSelected(false);
        rotateButton.setSelected(false);
        translateButton.setSelected(false);
    }

    public void requestRender() {
        glSurfaceView.requestRender();
    }

    public class TrimSuite {
        private EdgeManipRenderer mRenderer;
        private View mView;
        private Button bottomTrim;
        private Button bottomUntrim;
        private Button topTrim;
        private Button topUntrim;

        /**
         * Starting text in the TextView will depend on how many vertices the edge has.
         */

        public TrimSuite(EdgeManipRenderer renderer, View v){
            mRenderer = renderer;
            mView = v;
            bottomTrim = (Button) mView.findViewById(R.id.trim_bottom_button);
            bottomUntrim = (Button) mView.findViewById(R.id.untrim_bottom_button);
            topTrim = (Button) mView.findViewById(R.id.trim_top_button);
            topUntrim = (Button) mView.findViewById(R.id.untrim_top_button);

            bottomTrim.setOnClickListener(mRenderer.mEditListener);
            bottomUntrim.setOnClickListener(mRenderer.mEditListener);
            topTrim.setOnClickListener(mRenderer.mEditListener);
            topUntrim.setOnClickListener(mRenderer.mEditListener);
        }

        /*
        public void updateText(int topVertex, int bottomVertex, boolean allowed) {
            String topNum = String.format("%03d", topVertex);
            String bottomNum = String.format("%03d", bottomVertex);
            String text = topNum + " - " + bottomNum;
            if (allowed) {
                trimText.setTextColor(Color.WHITE);
            } else {
                trimText.setTextColor(Color.RED);
            }
            trimText.setText(text);
        }
        */
    }

    public class RotateSuite {
        private EdgeManipRenderer mRenderer;
        private View mView;
        private Button largeCounterwise;
        private Button smallCounterwise;
        private Button smallClockwise;
        private Button largeClockwise;

        public RotateSuite(EdgeManipRenderer renderer, View v) {
            mRenderer = renderer;
            mView = v;
            largeCounterwise = (Button) mView.findViewById(R.id.rotate_counteronedeg_button);
            smallCounterwise = (Button) mView.findViewById(R.id.rotate_countertenthdeg_button);
            smallClockwise = (Button) mView.findViewById(R.id.rotate_clocktenthdeg_button);
            largeClockwise = (Button) mView.findViewById(R.id.rotate_clockonedeg_button);

            largeCounterwise.setOnClickListener(mRenderer.mEditListener);
            smallCounterwise.setOnClickListener(mRenderer.mEditListener);
            smallClockwise.setOnClickListener(mRenderer.mEditListener);
            largeClockwise.setOnClickListener(mRenderer.mEditListener);
        }

        /*
        public void updateText(float newAngle, boolean allowed) {
            boolean negative;
            String num;

            if (newAngle < 0) negative = true;
            else negative = false;
            if (negative) num = "-";
            else num = "+";
            newAngle = Math.abs(newAngle);
            num += String.format("%04.1f", newAngle);
            if (allowed) {
                rotateText.setTextColor(Color.WHITE);
            } else {
                rotateText.setTextColor(Color.RED);
            }
            rotateText.setText(num);
        }
        */
    }

    public class TranslateSuite {
        private EdgeManipRenderer mRenderer;
        private View mView;
        private Button largeLeft;
        private Button smallLeft;
        private Button smallRight;
        private Button largeRight;

        public TranslateSuite(EdgeManipRenderer renderer, View v) {
            mRenderer = renderer;
            mView = v;
            largeLeft = (Button) mView.findViewById(R.id.translate_lefttenth_button);
            smallLeft = (Button) mView.findViewById(R.id.translate_lefthundredth_button);
            smallRight = (Button) mView.findViewById(R.id.translate_righthundredth_button);
            largeRight = (Button) mView.findViewById(R.id.translate_righttenth_button);

            largeLeft.setOnClickListener(mRenderer.mEditListener);
            smallLeft.setOnClickListener(mRenderer.mEditListener);
            smallRight.setOnClickListener(mRenderer.mEditListener);
            largeRight.setOnClickListener(mRenderer.mEditListener);
        }

        /*
        public void updateText(float newDistance, boolean allowed) {
            boolean negative;
            String num;

            if (newDistance < 0) negative = true;
            else negative = false;
            if (negative) num = "-";
            else num = "+";
            num += String.format("%04.2f", Math.abs(newDistance));
            if (allowed) {
                translateText.setTextColor(Color.WHITE);
            } else {
                translateText.setTextColor(Color.RED);
            }
            translateText.setText(num);
        }
        */
    }
}
