package com.tulipan.hunter.vesselbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hunter on 7/6/2016.
 */
public class ViewPageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private ModelManager mModelManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentActivity = (VesselBuilderActivity) getActivity();

        mModelManager = new ModelManager(mCurrentActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_page_fragment, container, false);

        loadFilesList();

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.view_files_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mCurrentActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new FileAdapter(mModelManager.getModels()));

        return v;
    }

    private class FileHolder extends RecyclerView.ViewHolder {
        private Button mButton;
        private TextView mText;
        private ModelFile mModelFile;

        public FileHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.files_list_item, container, false));

            mButton = (Button) itemView.findViewById(R.id.files_list_item_button);
            mButton.setText("Preview");
            mButton.setEnabled(false);
            mText = (TextView) itemView.findViewById(R.id.files_list_item_text);
        }

        public void bindModelFile(ModelFile modelFile) {
            mModelFile = modelFile;
            mText.setText(modelFile.getName());
            if (mModelFile.getPreviewPath() != null) {
                mButton.setEnabled(true);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * This needs to perform whatever actions occur to view the preview image.
                         */
                    }
                });
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentActivity.replaceFragment(ModelRendererFragment.newInstance(mModelFile));
                }
            });
        }
    }

    private class FileAdapter extends RecyclerView.Adapter<FileHolder> {
        private ArrayList<ModelFile> mModelFiles;

        public FileAdapter(ArrayList<ModelFile> files) {
            mModelFiles = files;
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mCurrentActivity);
            return new FileHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            ModelFile file = mModelFiles.get(position);
            holder.bindModelFile(file);
        }

        @Override
        public int getItemCount() {
            return mModelFiles.size();
        }
    }

    private void loadFilesList() {
        /**
         * This function may no longer be needed, or may be adapted to one that simply checks if
         * there are files where they should be and retrieves the directory file of where the models
         * and preview images are. This will allow the file holders to do what they need to do.
         */
    }
}
