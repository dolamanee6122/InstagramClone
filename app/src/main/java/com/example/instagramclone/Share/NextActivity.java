package com.example.instagramclone.Share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Login.LoginActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NextActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private FirebaseMethods mFirebaseMethods;

    private Context mContext;

    //widgets
    private EditText mCaption;
    private ProgressBar mProgressBar;

    private String userID;

    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgURL;
    private Intent intent;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mContext = NextActivity.this;
        mFirebaseMethods = new FirebaseMethods(mContext);
        mCaption = (EditText)findViewById(R.id.caption);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBarNext);
        mProgressBar.setVisibility(View.GONE);
        setupFirebaseAuth();


        ImageView backArrow = (ImageView)findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the Activity");
                finish();
            }
        });

        TextView share = (TextView)findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final sharing screen");
                //upload the image to the firebase
                String caption = mCaption.getText().toString();
                mProgressBar.setVisibility(View.VISIBLE);

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgURL = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgURL,null );
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap = (Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap );
                }

                Toast.makeText(mContext, "Attempting to upload new photo ", Toast.LENGTH_SHORT).show();
            }
        });

        setImage();
    }



    private  void someMethods(){
        /*
          step 1)
          create a data model for photos

          step 2)
          add properties to the photo objects(caption date, actual image url,unique photo id,store user id)

          step 3)
          count the number of photos user already has

          step 4)
          a)upload photo to the firebase storage and insert two new nodes in the firebase database
          b) insert into photos  node
          c) insert into user_photos  node

         */
    }
    /**
     * get the image url from incoming intent and displays the chosen image
     */

    private void setImage(){
        intent = getIntent();
        ImageView image = (ImageView)findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgURL = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url " + imgURL);
            UniversalImageLoader.setImage(imgURL,image,null,mAppend);
        }
        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap = (Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap image ");
            image.setImageBitmap(bitmap);
        }
    }

       /*
    -----------------------------------------firebase-------------------------------------------
     */

    private void setupFirebaseAuth(){

        Log.d(TAG, "setupFirebaseAuth: setting up firebase Auth ");
        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){

                }
                else{
                    Log.d(TAG, "onAuthStateChanged: Navigating to login Activity");
                    Intent intent = new Intent(mContext, LoginActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //retrieve user information from database;

                //setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for user in question

                 imageCount = mFirebaseMethods.getImageCount(dataSnapshot);

                Log.d(TAG, "onDataChange: image count is: " + imageCount);
                    Toast.makeText(mContext, "image count " + imageCount, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
