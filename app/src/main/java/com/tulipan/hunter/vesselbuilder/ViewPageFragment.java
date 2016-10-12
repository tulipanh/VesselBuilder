package com.tulipan.hunter.vesselbuilder;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tulipan.hunter.vesselbuilder.structures.ModelFile;
import com.tulipan.hunter.vesselbuilder.structures.ModelManager;

import java.util.ArrayList;

import utils.PictureUtils;

/**
 * Created by Hunter on 7/6/2016.
 */
public class ViewPageFragment extends Fragment {
    private VesselBuilderActivity mCurrentActivity;
    private ModelManager mModelManager;
    private FileHolder mSelectedFile;
    private FileInterface mFileInterface;

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

        mFileInterface = new FileInterface(v);

        return v;
    }

    private class FileInterface implements View.OnClickListener {
        private View mView;

        private LinearLayout fileButtonsLayout;
        private Button previewButton;
        private Button openButton;
        private Button deleteButton;

        private LinearLayout previewLayout;
        private ImageView previewImageView;

        public FileInterface(View view) {
            mView = view;

            fileButtonsLayout = (LinearLayout) view.findViewById(R.id.files_buttons_layout);
            previewButton = (Button) view.findViewById(R.id.files_preview_button);
            openButton = (Button) view.findViewById(R.id.files_open_button);
            deleteButton = (Button) view.findViewById(R.id.files_delete_button);

            previewLayout = (LinearLayout) view.findViewById(R.id.files_preview_layout);
            previewImageView = (ImageView) view.findViewById(R.id.files_preview_imageview);

            previewButton.setOnClickListener(this);
            openButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);

            previewButton.setEnabled(false);
            openButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.files_preview_button:
                    /**
                     * Needs to make files_preview_layout visible and load the scaled
                     * preview image into the ImageView inside the layout.
                     */
                    if (previewLayout.getVisibility() == View.VISIBLE) {
                        previewLayout.setVisibility(View.GONE);
                    } else {
                        previewImageView.setImageBitmap(PictureUtils.getScaledBitmap(mSelectedFile.getPreviewPath(), previewImageView));
                        previewLayout.setVisibility(View.VISIBLE);
                    }
                    break;

                case R.id.files_open_button:
                    mCurrentActivity.replaceFragment(ModelRendererFragment.newInstance(mSelectedFile.getFile()));
                    break;

                case R.id.files_delete_button:
                    /**
                     * Needs to delete the file and its preview (if it exists) from the
                     * OBJFiles folder in Downloads.
                     */
                    break;
            }
        }

        public void setPreviewEnabled(boolean setValue) {
            previewButton.setEnabled(setValue);
        }

        public void setOpenEnabled(boolean setValue) {
            openButton.setEnabled(setValue);
        }

        public void setDeleteEnabled(boolean setValue) {
            deleteButton.setEnabled(setValue);
        }
    }

    private class FileHolder extends RecyclerView.ViewHolder {
        private TextView mText;
        private ModelFile mModelFile;
        private String mPreviewPath = null;

        public FileHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.files_list_item, container, false));

            mText = (TextView) itemView.findViewById(R.id.files_list_item_text);
        }

        public void bindModelFile(ModelFile modelFile) {
            mModelFile = modelFile;
            mText.setText(modelFile.getName());
            if (mModelFile.getPreviewPath() != null) {
                mPreviewPath = mModelFile.getPreviewPath();
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.isSelected()) {
                        v.setSelected(false);

                        mText.setTextColor(ContextCompat.getColor(mCurrentActivity, R.color.colorAccent));
                        mText.setTextSize(15);

                        mSelectedFile = null;

                        mFileInterface.setDeleteEnabled(false);
                        mFileInterface.setOpenEnabled(false);
                        mFileInterface.setPreviewEnabled(false);
                    } else {
                        v.setSelected(true);

                        mText.setTextColor(Color.WHITE);
                        mText.setTextSize(20);

                        mSelectedFile = FileHolder.this;

                        mFileInterface.setDeleteEnabled(true);
                        mFileInterface.setOpenEnabled(true);
                        mFileInterface.setPreviewEnabled(mPreviewPath != null);
                    }
                }
            });
        }

        public ModelFile getFile() {
            return mModelFile;
        }

        public String getPreviewPath() {
            return mPreviewPath;
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
