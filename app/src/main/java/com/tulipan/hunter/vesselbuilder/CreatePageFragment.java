package com.tulipan.hunter.vesselbuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tulipan.hunter.vesselbuilder.structures.ImageProject;
import com.tulipan.hunter.vesselbuilder.views.DoubleSeekBarView;

import java.io.File;

import utils.PictureUtils;

/**
 * Created by Hunter on 7/6/2016.
 */
public class CreatePageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private CreateInterface mCreateInterface;
    private ImageView mImageView;
    private ImageProject mProject;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();
        mProject = new ImageProject(mCurrentActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.get_image_fragment, container, false);

        mCreateInterface = new CreateInterface(v);

        mImageView = (ImageView) v.findViewById(R.id.getpage_imageview);

        return v;
    }

    private void updatePhotoView(File file) {
        if (file == null || !file.exists()) {
            mImageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), mImageView);
            mImageView.setImageBitmap(bitmap);
        }
    }

    private void updatePhotoView(Bitmap img) {
        if (img == null) {
            mImageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.scaleBitmap(img, mImageView);
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            updatePhotoView(mProject.getPhotoFile());

        }
    }

    private void getCameraImage() {
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = Uri.fromFile(mProject.getPhotoFile());
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(captureImage, REQUEST_IMAGE_CAPTURE);
    }

    private boolean canTakePhoto() {
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getActivity().getPackageManager();
        return mProject.getPhotoFile() != null && captureImage.resolveActivity(packageManager) != null;
    }

    private class CreateInterface implements View.OnClickListener {
        private View mView;

        private LinearLayout getOptionsLayout;
        private Button acceptButton;
        private Button rejectButton;
        private Button rotateButton;
        private Button reflectButton;

        private LinearLayout getLayout;
        private Button galleryButton;
        private Button cameraButton;

        private LinearLayout filterOptionsLayout;
        private Button resetButton;
        private DoubleSeekBarView thresholdSeekbar;
        private TextView lowerTextView;
        private TextView upperTextView;
        //private Button moreButton;
        //private Button lessButton;

        private LinearLayout filterLayout;
        private Button showOrigButton;
        private Button showFiltButton;
        private Button continueButton;

        public CreateInterface(View view) {
            mView = view;

            getOptionsLayout = (LinearLayout) mView.findViewById(R.id.getpage_normal_buttons_layout);
            acceptButton = (Button) mView.findViewById(R.id.getpage_accept_button);
            rejectButton = (Button) mView.findViewById(R.id.getpage_reject_button);
            rotateButton = (Button) mView.findViewById(R.id.getpage_rotate_button);
            reflectButton = (Button) mView.findViewById(R.id.getpage_reflect_button);

            getLayout = (LinearLayout) mView.findViewById(R.id.getpage_get_layout);
            galleryButton = (Button) mView.findViewById(R.id.getpage_gallery_button);
            cameraButton = (Button) mView.findViewById(R.id.getpage_camera_button);

            filterOptionsLayout = (LinearLayout) mView.findViewById(R.id.getpage_filter_buttons_layout);
            resetButton = (Button) mView.findViewById(R.id.getpage_reset_button);
            thresholdSeekbar = (DoubleSeekBarView) mView.findViewById(R.id.getpage_threshold_seekbar);
            lowerTextView = (TextView) mView.findViewById(R.id.getpage_filter_lowerthresh_textview);
            upperTextView = (TextView) mView.findViewById(R.id.getpage_filter_upperthresh_textview);
            //moreButton = (Button) mView.findViewById(R.id.getpage_moresense_button);
            //lessButton = (Button) mView.findViewById(R.id.getpage_lesssense_button);

            filterLayout = (LinearLayout) mView.findViewById(R.id.getpage_filter_layout);
            showOrigButton = (Button) mView.findViewById(R.id.getpage_showorig_button);
            showFiltButton = (Button) mView.findViewById(R.id.getpage_showfilt_button);
            continueButton = (Button) mView.findViewById(R.id.getpage_continue_button);

            acceptButton.setOnClickListener(this);
            rejectButton.setOnClickListener(this);
            rotateButton.setOnClickListener(this);
            reflectButton.setOnClickListener(this);
            galleryButton.setOnClickListener(this);
            cameraButton.setOnClickListener(this);
            resetButton.setOnClickListener(this);
            //moreButton.setOnClickListener(this);
            //lessButton.setOnClickListener(this);
            showOrigButton.setOnClickListener(this);
            showFiltButton.setOnClickListener(this);
            continueButton.setOnClickListener(this);

            thresholdSeekbar.setActionExecutor(new DoubleSeekBarView.ActionExecutor() {
                @Override
                public void touchUp() {
                    thresholdSeekbar.setTouchable(false);
                    mProject.setUpperThreshold(thresholdSeekbar.getUpperValue());
                    mProject.setLowerThreshold(thresholdSeekbar.getLowerValue());
                    mProject.applyFilter();
                    mProject.writeFilterToFile();
                    updatePhotoView(mProject.getFilterFile());
                    thresholdSeekbar.setTouchable(true);
                }

                @Override
                public void touchMove() {
                    /* Perhaps alter some TextView displaying the threshold values. */
                    lowerTextView.setText(String.valueOf(thresholdSeekbar.getLowerValue()));
                    upperTextView.setText(String.valueOf(thresholdSeekbar.getUpperValue()));
                }
            });

            acceptButton.setEnabled(false);
            rejectButton.setEnabled(false);
            rotateButton.setEnabled(false);
            reflectButton.setEnabled(false);
            galleryButton.setEnabled(false);
            galleryButton.setAlpha(0.3f);

            if (!canTakePhoto()) {
                cameraButton.setEnabled(false);
                cameraButton.setAlpha(0.3f);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.getpage_accept_button:
                    thresholdSeekbar.setTouchable(false);
                    mProject.setAutoThreshold();
                    mProject.applyFilter();
                    mProject.writeFilterToFile();
                    updatePhotoView(mProject.getFilterFile());
                    getLayout.setVisibility(View.GONE);
                    getOptionsLayout.setVisibility(View.GONE);
                    filterLayout.setVisibility(View.VISIBLE);
                    filterOptionsLayout.setVisibility(View.VISIBLE);
                    thresholdSeekbar.setUpperValue(mProject.getUpperThreshold());
                    thresholdSeekbar.setLowerValue(mProject.getLowerThreshold());
                    lowerTextView.setText(String.valueOf(thresholdSeekbar.getLowerValue()));
                    upperTextView.setText(String.valueOf(thresholdSeekbar.getUpperValue()));
                    thresholdSeekbar.setTouchable(true);
                    break;

                case R.id.getpage_reject_button:
                    mImageView.setImageBitmap(null);
                    mProject.getPhotoFile().delete();
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    rotateButton.setEnabled(false);
                    reflectButton.setEnabled(false);
                    break;

                case R.id.getpage_rotate_button:
                    /* TODO: This should rotate the image 90 degrees clockwise (i.e. from landscape to portrait) */
                    mProject.rotateImage();
                    updatePhotoView(mProject.getPhotoFile());
                    acceptButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                    rotateButton.setEnabled(true);
                    reflectButton.setEnabled(true);
                    break;

                case R.id.getpage_reflect_button:
                    /* TODO: THis should reflect the image across the vertical axis */
                    mProject.reflectImage();
                    updatePhotoView(mProject.getPhotoFile());
                    acceptButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                    rotateButton.setEnabled(true);
                    reflectButton.setEnabled(true);
                    break;


                case R.id.getpage_gallery_button:
                    /* TODO: This should allow the user to select a picture from their Gallery app. */
                    /* If this is much more complicated than getting a picture from the Camera, then I may
                        remove this feature.
                     */
                    break;

                case R.id.getpage_camera_button:
                    getCameraImage();
                    updatePhotoView(mProject.getPhotoFile());
                    acceptButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                    rotateButton.setEnabled(true);
                    reflectButton.setEnabled(true);
                    break;

                case R.id.getpage_reset_button:
                    /* TODO: This should reset the current CreatePageFragment to its initial state. */
                    /* It should do this either by deleting the current ImageProject and creating a new one
                        or by calling some kind of reset method of the current ImageProject. It should also
                        reset the page's interface somehow. Either by reverting the state of the current
                        interface or by simply popping off the current CreatePageFragment and loading a
                        new one.
                     */
                    mProject.deallocate();
                    mProject = new ImageProject(mCurrentActivity);
                    mImageView.setImageBitmap(null);
                    filterLayout.setVisibility(View.GONE);
                    filterOptionsLayout.setVisibility(View.GONE);
                    getLayout.setVisibility(View.VISIBLE);
                    getOptionsLayout.setVisibility(View.VISIBLE);
                    acceptButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    rotateButton.setEnabled(false);
                    reflectButton.setEnabled(false);
                    break;

                /*
                case R.id.getpage_moresense_button:
                    if (!mProject.changeUpperThreshold(-0.2f)) {
                        Toast.makeText(mCurrentActivity, "Lower Limit Reached", Toast.LENGTH_SHORT).show();
                    }
                    mProject.applyFilter();
                    mProject.writeFilterToFile();
                    updatePhotoView(mProject.getFilterFile());
                    break;

                case R.id.getpage_lesssense_button:
                    if (!mProject.changeUpperThreshold(0.2f)) {
                        Toast.makeText(mCurrentActivity, "Upper Limit Reached", Toast.LENGTH_SHORT).show();
                    }
                    mProject.applyFilter();
                    mProject.writeFilterToFile();
                    updatePhotoView(mProject.getFilterFile());
                    break;
                */
                case R.id.getpage_showorig_button:
                    updatePhotoView(mProject.getPhotoFile());
                    showOrigButton.setVisibility(View.GONE);
                    showFiltButton.setVisibility(View.VISIBLE);
                    break;

                case R.id.getpage_showfilt_button:
                    updatePhotoView(mProject.getFilterFile());
                    showFiltButton.setVisibility(View.GONE);
                    showOrigButton.setVisibility(View.VISIBLE);
                    break;

                case R.id.getpage_continue_button:
                    mCurrentActivity.replaceFragment(CropPageFragment.newInstance(mProject));
                    break;

                default:
                    break;
            }
        }
    }
}
