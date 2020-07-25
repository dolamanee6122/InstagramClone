package com.example.instagramclone.Share;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.FilePaths;
import com.example.instagramclone.Utils.FileSearch;
import com.example.instagramclone.Utils.GridImageAdapter;
import com.example.instagramclone.Utils.UniversalImageLoader;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    //constant
    private static final int NUM_GRID_COLS = 3;

    //widgets
    private GridView gridView;
    private Spinner directorySpinner;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;

    //vars
    private ArrayList<String> directories,temp;
    private String mAppend = "file:/";
    private String mSelectedImage;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);

        galleryImage = (ImageView)view.findViewById(R.id.galleryImageView);
        gridView =(GridView)view.findViewById(R.id.gridView);
        directorySpinner = (Spinner)view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: started ");

        ImageView shareClose = (ImageView)view.findViewById(R.id.ivClosesShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment ");
                getActivity().finish();
            }
        });

        TextView nextScreen = (TextView)view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mSelectedImage == null){
                    Toast.makeText(getActivity(), "Please Select an Image", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (isRootTask()) {
                        Log.d(TAG, "onClick: navigating to the final sharing screen");
                        Intent intent = new Intent(getActivity(), NextActivity.class);
                        intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                        startActivity(intent);
                    } else {
                        Log.d(TAG, "onClick: navigating to the Account Settings screen");
                        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                        intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                        intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }
        });

        init();
        return view;
    }

    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }
        return false;
    }

    private void init(){
        FilePaths filePaths = new FilePaths();

        if(FileSearch.getDirectoryPaths(filePaths.PICTURES)!=null){
            temp=FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        directories.add(filePaths.CAMERA);
        for(String st:temp){
            directories.add(st);
        }
        ArrayList<String> showDirectories = new ArrayList<>();
        for(String st:directories){
            int index = st.lastIndexOf("/");
            showDirectories.add(st.substring(index+1));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,showDirectories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected: " + directories.get(position));

                //setup the image grid for the directory chosen
                setupGridView(directories.get(position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void setupGridView(String selectedDirectory){
        Log.d(TAG, "setupGridView: Directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURL = FileSearch.getFilePaths(selectedDirectory);

        //set the grid columns width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLS;
        gridView.setColumnWidth(imageWidth);

        //use the grid adapter to adapt the image in grid view
        GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_image_view,mAppend,imgURL);
        gridView.setAdapter(adapter);

        //setup the first image
        try{
            setImage(imgURL.get(0), galleryImage, mAppend);
            mSelectedImage = imgURL.get(0);
        }catch (ArrayIndexOutOfBoundsException e){
            Log.d(TAG, "setupGridView: ArrayIndexOutOfBoundsException " + e.getMessage());
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: setting image at position "+ position);
                setImage(imgURL.get(position),galleryImage,mAppend);
                mSelectedImage = imgURL.get(position);
            }
        });

    }


    private void setImage(String imgURL,ImageView image,String append){
        Log.d(TAG, "setImage: setting image");
        UniversalImageLoader.setImage(imgURL,image,mProgressBar,append);
    }
}
