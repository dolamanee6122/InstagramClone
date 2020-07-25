package com.example.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.instagramclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class GridImageAdapter extends ArrayAdapter<String > {
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private String mAppend;
    private ArrayList<String> imgURLs;

    public GridImageAdapter(Context mContext, int layoutResource, String mAppend, ArrayList<String> imgURLs) {
        super(mContext, layoutResource, imgURLs);
        this.mContext = mContext;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        this.mAppend = mAppend;
        this.imgURLs = imgURLs;
    }

    private static class ViewHolder{
        SquareImageView image;
        ProgressBar mProgressBar;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        /**
         * view holder build pattern(similar to recycler view)
         */

        final ViewHolder holder;
        if(convertView==null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.gridImageProgressBar);
            holder.image = (SquareImageView)convertView.findViewById(R.id.gridImageView);

            convertView.setTag(holder);
        }
        else{
            holder =(ViewHolder)convertView.getTag();
        }
        String imgURL = getItem(position);
        UniversalImageLoader.setImage(imgURL,holder.image,holder.mProgressBar,mAppend);
        /*ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imgURL, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(holder.mProgressBar!= null){
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                    //Toast.makeText(view.getContext(),"loading",Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(holder.mProgressBar!= null){
                    holder.mProgressBar.setVisibility(View.GONE);
                    //Toast.makeText(view.getContext(),"loading",Toast.LENGTH_SHORT);
                }

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(holder.mProgressBar!= null){
                    holder.mProgressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(holder.mProgressBar!= null){
                    holder.mProgressBar.setVisibility(View.GONE);
                }

            }
        });*/
        return convertView;
    }
}
