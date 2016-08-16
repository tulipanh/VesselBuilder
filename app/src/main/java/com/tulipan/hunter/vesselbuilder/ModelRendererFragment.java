package com.tulipan.hunter.vesselbuilder;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Hunter on 7/8/2016.
 */
public class ModelRendererFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private GLSurfaceView glSurfaceView;
    private ModelRenderer mRenderer;
    private boolean rendererSet = false;
    private String mModelPath;
    private RotationInterface mRotationInterface;
    private ZoomInterface mZoomInterface;
    private Button mResetButton;

    public static final String FILE_PATH = "filePath";

    public static ModelRendererFragment newInstance(ModelFile modelFile) {
        ModelRendererFragment fragment = new ModelRendererFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FILE_PATH, modelFile.getFilePath());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();

        mModelPath = getArguments().getString(FILE_PATH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.model_renderer_fragment, container, false);

        glSurfaceView = (GLSurfaceView) v.findViewById(R.id.model_renderer_surface);
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

            File modelFile = new File(mModelPath);
            OBJModel model = new OBJModel(mCurrentActivity, modelFile);
            mRenderer = new ModelRenderer(mCurrentActivity, this, model);

            glSurfaceView.setRenderer(mRenderer);
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            rendererSet = true;

            mRotationInterface = new RotationInterface(v, mRenderer);
            mZoomInterface = new ZoomInterface(v, mRenderer);

            mResetButton = (Button) v.findViewById(R.id.renderer_reset_button);
            mResetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRenderer.reset();
                }
            });

        } else {
            Toast.makeText(mCurrentActivity, "This device does not support OpenGl ES 2.0.", Toast.LENGTH_LONG).show();
        }

        return v;
    }

    private class ZoomInterface implements View.OnClickListener {
        private ModelRenderer mRenderer;
        private View mView;

        private LinearLayout zoomButtonsLayout;
        private Button zoomInButton;
        private Button zoomOutButton;

        public ZoomInterface(View view, ModelRenderer renderer) {
            mRenderer = renderer;
            mView = view;

            zoomButtonsLayout = (LinearLayout) view.findViewById(R.id.renderer_zoom_buttons_layout);
            zoomInButton = (Button) view.findViewById(R.id.renderer_zoom_in_button);
            zoomOutButton = (Button) view.findViewById(R.id.renderer_zoom_out_button);

            zoomInButton.setOnClickListener(this);
            zoomOutButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.renderer_zoom_in_button:
                    if (!mRenderer.zoomIn(0.3f)) Toast.makeText(mCurrentActivity, "Zoom Failed", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.renderer_zoom_out_button:
                    if (!mRenderer.zoomOut(0.3f)) Toast.makeText(mCurrentActivity, "Zoom Failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class RotationInterface implements View.OnClickListener {
        private ModelRenderer mRenderer;
        private View mView;

        private Button xButton;
        private LinearLayout xLayout;
        private ImageButton xClock;
        private ImageButton xCounter;
        private Button yButton;
        private LinearLayout yLayout;
        private ImageButton yClock;
        private ImageButton yCounter;
        private Button zButton;
        private LinearLayout zLayout;
        private ImageButton zClock;
        private ImageButton zCounter;

        private boolean xActive;
        private boolean yActive;
        private boolean zActive;

        public RotationInterface(View view, ModelRenderer renderer) {
            mRenderer = renderer;
            mView = view;

            xButton = (Button) view.findViewById(R.id.renderer_x_rotate_button);
            xLayout = (LinearLayout) view.findViewById(R.id.renderer_rotate_x_buttons_layout);
            xClock = (ImageButton) view.findViewById(R.id.renderer_x_rotate_c_button);
            xCounter = (ImageButton) view.findViewById(R.id.renderer_x_rotate_cc_button);
            yButton = (Button) view.findViewById(R.id.renderer_y_rotate_button);
            yLayout = (LinearLayout) view.findViewById(R.id.renderer_rotate_y_buttons_layout);
            yClock = (ImageButton) view.findViewById(R.id.renderer_y_rotate_c_button);
            yCounter = (ImageButton) view.findViewById(R.id.renderer_y_rotate_cc_button);
            zButton = (Button) view.findViewById(R.id.renderer_z_rotate_button);
            zLayout = (LinearLayout) view.findViewById(R.id.renderer_rotate_z_buttons_layout);
            zClock = (ImageButton) view.findViewById(R.id.renderer_z_rotate_c_button);
            zCounter = (ImageButton) view.findViewById(R.id.renderer_z_rotate_cc_button);

            xButton.setOnClickListener(this);
            xClock.setOnClickListener(this);
            xCounter.setOnClickListener(this);
            yButton.setOnClickListener(this);
            yClock.setOnClickListener(this);
            yCounter.setOnClickListener(this);
            zButton.setOnClickListener(this);
            zClock.setOnClickListener(this);
            zCounter.setOnClickListener(this);

            xActive = false;
            yActive = false;
            zActive = false;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.renderer_x_rotate_button:
                    if (xActive) {
                        setXYZ(false, false, false);
                    } else {
                        setXYZ(true, false, false);
                    }
                    break;

                case R.id.renderer_x_rotate_c_button:
                    if (!mRenderer.rotateModel(1f, 0f, 0f, -30f)) Toast.makeText(mCurrentActivity, "Rotation Failed", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.renderer_x_rotate_cc_button:
                    if (!mRenderer.rotateModel(1f, 0f, 0f, 30f)) Toast.makeText(mCurrentActivity, "Rotation Failed", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.renderer_y_rotate_button:
                    if (yActive) {
                        setXYZ(false, false, false);
                    } else {
                        setXYZ(false, true, false);
                    }
                    break;

                case R.id.renderer_y_rotate_c_button:
                    if (!mRenderer.rotateModel(0f, 1f, 0f, -30f)) Toast.makeText(mCurrentActivity, "Rotation Failed", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.renderer_y_rotate_cc_button:
                    if (!mRenderer.rotateModel(0f, 1f, 0f, 30f)) Toast.makeText(mCurrentActivity, "Rotation Failed", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.renderer_z_rotate_button:
                    if (zActive) {
                        setXYZ(false, false, false);
                    } else {
                        setXYZ(false, false, true);
                    }
                    break;

                case R.id.renderer_z_rotate_c_button:
                    if (!mRenderer.rotateModel(0f, 0f, 1f, -30f)) Toast.makeText(mCurrentActivity, "Rotation Failed", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.renderer_z_rotate_cc_button:
                    if (!mRenderer.rotateModel(0f, 0f, 1f, 30f)) Toast.makeText(mCurrentActivity, "Rotation Failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private void setXYZ(boolean xOn, boolean yOn, boolean zOn) {
            if (xOn) {
                xLayout.setVisibility(View.VISIBLE);
                xButton.setSelected(true);
                xActive = true;
            } else {
                xLayout.setVisibility(View.GONE);
                xButton.setSelected(false);
                xActive = false;
            }

            if (yOn) {
                yLayout.setVisibility(View.VISIBLE);
                yButton.setSelected(true);
                yActive = true;
            } else {
                yLayout.setVisibility(View.GONE);
                yButton.setSelected(false);
                yActive = false;
            }

            if (zOn) {
                zLayout.setVisibility(View.VISIBLE);
                zButton.setSelected(true);
                zActive = true;
            } else {
                zLayout.setVisibility(View.GONE);
                zButton.setSelected(false);
                zActive = false;
            }
        }
    }

    public void requestRender() {glSurfaceView.requestRender();}
}
