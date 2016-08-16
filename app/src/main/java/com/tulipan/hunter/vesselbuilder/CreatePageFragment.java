package com.tulipan.hunter.vesselbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Hunter on 7/6/2016.
 */
public class CreatePageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private Button mCameraButton;
    private Button mGalleryButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.get_image_fragment, container, false);

        mCameraButton = (Button) v.findViewById(R.id.getpage_camera_button);
        mGalleryButton = (Button) v.findViewById(R.id.getpage_gallery_button);

        return v;
    }
}
