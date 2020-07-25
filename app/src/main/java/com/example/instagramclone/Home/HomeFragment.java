package com.example.instagramclone.Home;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.MainfeedListAdapter;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private Context mContext;
    private int mResults;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        mListView = view.findViewById(R.id.listView);
        mContext = getActivity();
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();


        getFollowing();



        return view;
    }


    private void getFollowing(){

        Log.d(TAG, "getCurrentUsername: retrieving current user.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user account: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());

                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get the photos
                getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        for(int i=0;i<mFollowing.size();++i){
            final int count = i;
            Query query = reference.child(mContext.getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));


            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Map<String ,Object> objectMap = (Map<String, Object>)singleSnapshot.getValue();
                        Photo photo = new Photo();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());

                        List<Comment> mComments = new ArrayList<>();

                        for(DataSnapshot dataSnapshot1: singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dataSnapshot1.getValue(Comment.class).getUser_id());
                            comment.setDate_created(dataSnapshot1.getValue(Comment.class).getDate_created());
                            comment.setComment(dataSnapshot1.getValue(Comment.class).getComment());
                            mComments.add(comment);
                        }
                        photo.setComments(mComments);
                        mPhotos.add(photo);
                    }

                    if(count >= mFollowing.size()-1){
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }




    }

    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();

        if(mPhotos!=null){
            try{
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mPhotos.size();
                if(iterations>10){
                    iterations = 10;
                }
                mResults = iterations;
                for(int i=0;i<iterations;++i){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mAdapter = new MainfeedListAdapter(mContext, R.layout.layout_mainfeed_list_item,mPaginatedPhotos);
                mListView.setAdapter(mAdapter);

            }
            catch (NullPointerException e){
                Log.d(TAG, "displayPhotos: NullPointerException " + e.getMessage());
            }
            catch (IndexOutOfBoundsException e){
                Log.d(TAG, "displayPhotos: IndexOutOfBoundsException " + e.getMessage());
            }

        }
    }

    public void displayMorePhotos(){
        try{
            if(mPhotos.size() > mResults && mPhotos.size() >0){

                int iterations;
                if(mPhotos.size() > mResults+10){
                    Log.d(TAG, "displayMorePhotos: there are more than 10 photos ");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: less the 10 photos");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated result
                for(int i = mResults; i<mResults+iterations;++i){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults =mResults +iterations;
                mAdapter.notifyDataSetChanged();
            }

        }catch(NullPointerException e){
            Log.d(TAG, "displayMorePhotos: NullPointerException " + e.getMessage());
        }catch (IndexOutOfBoundsException e){
            Log.d(TAG, "displayMorePhotos: IndexOutOfBoundsException " + e.getMessage());
        }
    }

}
