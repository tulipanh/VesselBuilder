package com.tulipan.hunter.vesselbuilder;

import android.app.Activity;
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

import com.tulipan.hunter.vesselbuilder.structures.ImageProject;

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
        updatePhotoView(mProject.getPhotoFile());
    }

    private boolean canTakePhoto() {
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getActivity().getPackageManager();
        return mProject.getPhotoFile() != null && captureImage.resolveActivity(packageManager) != null;
    }

    private class CreateInterface implements View.OnClickListener {
        private View mView;
        private Button getButton;
        private Button filterButton;
        private Button acceptButton;
        private Button resetButton;

        private LinearLayout getLayout;
        private Button galleryButton;
        private Button cameraButton;

        private LinearLayout filterLayout;
        private Button lowerMinusButton;
        private Button lowerPlusButton;
        private Button upperMinusButton;
        private Button upperPlusButton;

        public CreateInterface(View view) {
            mView = view;

            getButton = (Button) mView.findViewById(R.id.getpage_get_button);
            filterButton = (Button) mView.findViewById(R.id.getpage_filter_button);
            acceptButton = (Button) mView.findViewById(R.id.getpage_accept_button);
            resetButton = (Button) mView.findViewById(R.id.getpage_reset_button);

            getLayout = (LinearLayout) mView.findViewById(R.id.getpage_get_layout);
            galleryButton = (Button) mView.findViewById(R.id.getpage_gallery_button);
            cameraButton = (Button) mView.findViewById(R.id.getpage_camera_button);

            filterLayout = (LinearLayout) mView.findViewById(R.id.getpage_filter_layout);
            lowerMinusButton = (Button) mView.findViewById(R.id.getpage_lower_minus_button);
            lowerPlusButton = (Button) mView.findViewById(R.id.getpage_lower_plus_button);
            upperMinusButton = (Button) mView.findViewById(R.id.getpage_upper_minus_button);
            upperPlusButton = (Button) mView.findViewById(R.id.getpage_upper_plus_button);

            getButton.setOnClickListener(this);
            filterButton.setOnClickListener(this);
            acceptButton.setOnClickListener(this);
            resetButton.setOnClickListener(this);
            galleryButton.setOnClickListener(this);
            cameraButton.setOnClickListener(this);
            lowerMinusButton.setOnClickListener(this);
            lowerPlusButton.setOnClickListener(this);
            upperMinusButton.setOnClickListener(this);
            upperPlusButton.setOnClickListener(this);

            filterButton.setEnabled(false);
            acceptButton.setEnabled(false);
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
                case R.id.getpage_get_button:
                    filterLayout.setVisibility(View.GONE);
                    getLayout.setVisibility(View.VISIBLE);
                    break;

                case R.id.getpage_filter_button:
                    if (mProject.filterChanged()) {
                        mProject.applyFilter();
                        mProject.writeFilterToFile();
                    }
                    updatePhotoView(mProject.getFilterFile());
                    getLayout.setVisibility(View.GONE);
                    filterLayout.setVisibility(View.VISIBLE);
                    acceptButton.setEnabled(true);
                    break;

                case R.id.getpage_accept_button:
                    /* TODO: This should pass the ImageProject to a new CropPageFragment */
                    /* It should only become available after a filter has been applied to an image. */
                    mCurrentActivity.replaceFragment(CropPageFragment.newInstance(mProject));
                    break;

                case R.id.getpage_reset_button:
                    /* TODO: This should reset the current CreatePageFragment to its initial state. */
                    /* It should do this either by deleting the current ImageProject and creating a new one
                        or by calling some kind of reset method of the current ImageProject. It should also
                        reset the page's interface somehow. Either by reverting the state of the current
                        interface or by simply popping off the current CreatePageFragment and loading a
                        new one.
                     */
                    break;

                case R.id.getpage_gallery_button:
                    /* TODO: This should allow the user to select a picture from their Gallery app. */
                    /* If this is much more complicated than getting a picture from the Camera, then I may
                        remove this feature.
                     */
                    break;

                case R.id.getpage_camera_button:
                    getCameraImage();
                    filterButton.setEnabled(true);
                    break;

                case R.id.getpage_lower_minus_button:
                    /* TODO: This should decrease the lower threshold of the canny edge-detection and display the new filtered image. */
                    mProject.setLowerThreshold(mProject.getLowerThreshold() - 5);
                    mProject.applyFilter();
                    updatePhotoView(mProject.getFilterImage());
                    break;

                case R.id.getpage_lower_plus_button:
                    /* TODO: This should increase the lower threshold of the canny edge-detection and display the new filtered image. */
                    mProject.setLowerThreshold(mProject.getLowerThreshold() + 5);
                    mProject.applyFilter();
                    updatePhotoView(mProject.getFilterImage());
                    break;

                case R.id.getpage_upper_minus_button:
                    /* TODO: This should decrease the upper threshold of the canny edge-detection and display the new filtered image. */
                    mProject.setLowerThreshold(mProject.getUpperThreshold() - 5);
                    mProject.applyFilter();
                    updatePhotoView(mProject.getFilterImage());
                    break;

                case R.id.getpage_upper_plus_button:
                    /* TODO: This should increase the upper threshold of the canny edge-detection and display the new filtered image. */
                    mProject.setLowerThreshold(mProject.getUpperThreshold() + 5);
                    mProject.applyFilter();
                    updatePhotoView(mProject.getFilterImage());
                    break;
            }
        }
    }
}
