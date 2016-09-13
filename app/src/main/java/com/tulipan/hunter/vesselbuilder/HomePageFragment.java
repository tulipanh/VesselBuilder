package com.tulipan.hunter.vesselbuilder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.tulipan.hunter.vesselbuilder.structures.ImageProject;

/**
 * Created by Hunter on 7/6/2016.
 */
public class HomePageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_page_fragment, container, false);

        ImageButton createModel = (ImageButton) v.findViewById(R.id.create_model_button);
        createModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentActivity.replaceFragment(new CreatePageFragment());
            }
        });
        ImageButton viewModel = (ImageButton) v.findViewById(R.id.view_model_button);
        viewModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentActivity.replaceFragment(new ViewPageFragment());
            }
        });

        Button test = (Button) v.findViewById(R.id.testbutton);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageProject project = new ImageProject(mCurrentActivity);
                project.setCleanImage(BitmapFactory.decodeResource(getResources(), R.drawable.androidtest));
                mCurrentActivity.replaceFragment(ExtractPageFragment.newInstance(project));
            }
        });

        return v;
    }
}
