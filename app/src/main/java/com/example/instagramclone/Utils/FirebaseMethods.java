package com.example.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;

    //realtime database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //firebase storage
    private StorageReference mStorageReference;
    private double mPhotoUploadProgress = 0;


    private Context mContext;
    private String userID;

    public FirebaseMethods(Context mContext) {
        this.mContext = mContext;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        if(mAuth.getCurrentUser()!= null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, final String caption, final int imageCount, final String imgUrl
            ,Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo");

        FilePaths filePaths = new FilePaths();

        //case 1: new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading a new photo ");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (imageCount + 1));

            //convert image to bitmap
            if(bm==null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getByteFromBitmap(bm,100);


            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri firebaseUrl = uri;
                            addPhotoToDatabase(caption,firebaseUrl.toString());

                            Intent intent = new Intent(mContext, HomeActivity.class);
                            mContext.startActivity(intent);
                        }
                    });

                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                    //add the new photo to photos node and user_photos node

                    //navigate to the main feed so that user can see their photo

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo Upload Failed");
                    Toast.makeText(mContext, "Photo Upload Failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100* taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();

                    if(progress - 15 >mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f",progress) +"%", Toast.LENGTH_LONG).show();
                        mPhotoUploadProgress =progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress +"%done");
                }
            });

        }
        //case 2: new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading a new user profile photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image to bitmap

            if(bm==null) {
                bm = ImageManager.getBitmap(imgUrl);
            }byte[] bytes = ImageManager.getByteFromBitmap(bm,100);


            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri firebaseUrl = uri;
                            updateProfilePhoto(firebaseUrl.toString());
                        }
                    });

                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                    //add the new photo to photos node and user_photos node

                    //navigate to the edit profile fragment so that user can see their photo
                    ((AccountSettingsActivity)mContext).setupViewPager(((AccountSettingsActivity)mContext).pagerAdapter
                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo Upload Failed");
                    Toast.makeText(mContext, "Photo Upload Failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100* taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f",progress) +"%", Toast.LENGTH_LONG).show();
                    /*if(progress - 5 >mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f",progress) +"%", Toast.LENGTH_LONG).show();
                        mPhotoUploadProgress =progress;
                    }*/

                    Log.d(TAG, "onProgress: upload progress: " + progress +"%done");
                }
            });


        }
    }

    private void addPhotoToDatabase(String caption,String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");

        String tags =StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos))
                .push().getKey();

        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setImage_path(url);
        photo.setDate_created(getTimeStamp());
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoKey).setValue(photo);
    }

    private void updateProfilePhoto(String imgUrl){
        Log.d(TAG, "updateProfilePhoto: setting new profile photo");
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(imgUrl);
    }
    private String getTimeStamp(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return simpleDateFormat.format(new Date());
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int imageCount = 0;
        for(DataSnapshot ds:dataSnapshot.child(mContext.getString(R.string.dbname_user_photos))
                            .child(userID).getChildren()){
                imageCount++;
        }
        return imageCount;
    }


    /**
     * update the display name with @param in user_account_settings node
     * @param display_name
     */
    public void updateDisplayName(String display_name){
        Log.d(TAG, "updateDisplayName: updating display name to: " + display_name);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_display_name))
                .setValue(display_name);
    }

    /**
     * update the Description with @param in user_account_settings node
     * @param description
     */
    public void updateDescription(String description){
        Log.d(TAG, "updateDisplayName: updating display name to: " + description);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_description))
                .setValue(description);
    }

    /**
     * update the website with @param in user_account_settings node
     * @param website
     */
    public void updateWebsite(String website){
        Log.d(TAG, "updateDisplayName: updating display name to: " + website);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_website))
                .setValue(website);
    }

    /**
     * update the phone_number with @param in users node
     * @param phone_number
     */
    public void updatePhoneNumber(long phone_number){
        Log.d(TAG, "updateDisplayName: updating display name to: " + phone_number);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_phone_number))
                .setValue(phone_number);
    }




    /**
     * update the username of users node and user_account_settings node
     * @param username
     */
    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: " + username);
         myRef.child(mContext.getString(R.string.dbname_users))
                 .child(userID)
                 .child(mContext.getString(R.string.field_username))
                 .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

    }

    /**
     * update the email in the users node
     * @param email
     */
    public void updateEmail(String email){
        Log.d(TAG, "updateUsername: updating email to: " + email);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }


//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
//        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");
//        //Toast.makeText(mContext, "checkIfUsernameExists: checking if " + username + " already exists", Toast.LENGTH_SHORT).show();
//
//        User user = new User();
//
//        for(DataSnapshot ds: dataSnapshot.child(userID).getChildren()){
//            Log.d(TAG, "checkIfUsernameExists: DataSnapshot " + ds);
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//
//            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
//                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUsername());
//                return true;
//            }
//        }
//        return false;
//    }



    /**
     * Register a New Email and Password for firebase Authentication
     * @param email
     * @param password
     * @param username
     */

    public void registerNewEmail(final String email, final String password, final String username, final ProgressBar mProgressBar, final TextView please_wait){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            //send verification mail
                            sendVerificationEmail();


                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = mAuth.getCurrentUser().getUid();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                            please_wait.setVisibility(View.GONE);
                        }

                        // ...
                    }
                });

    }

    /**
     * add user information to users node
     * and add user information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */

    public void addNewUser(String email, String username, String description, String website, String profile_photo){
        User user = new User(userID,1,email,StringManipulation.condenseUsername(username));

        //if(userID==null) Toast.makeText(mContext, "user id is null", Toast.LENGTH_SHORT).show();
        //Toast.makeText(mContext, "Adding the data to database  " , Toast.LENGTH_SHORT).show();

        myRef.child(mContext.getString(R.string.dbname_users))
          .child(userID).setValue(user);
        //myRef.push().setValue(user);

        UserAccountSettings settings = new UserAccountSettings(description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,userID);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);

    }

    public  void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }
                            else{
                                Toast.makeText(mContext, "couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    /**
     * Retrieves the user account settings for current user signed in
     * Database: user_account_settings node
     * @param dataSnapshot
     * @return
     */

    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account setting information from firebase. ");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds:dataSnapshot.getChildren()){

            //user_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))){
                Log.d(TAG, "getUserAccountSettings: dataSnapshot " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getDisplay_name()
                    );

                    settings.setUsername(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getUsername()
                    );

                    settings.setWebsite(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getWebsite()
                    );

                    settings.setDescription(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getDescription()
                    );

                    settings.setProfile_photo(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getProfile_photo()
                    );

                    settings.setPosts(
                    ds.child(userID).
                            getValue(UserAccountSettings.class).getPosts()
                    );

                    settings.setFollowers(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getFollowers()
                    );

                    settings.setFollowing(
                            ds.child(userID).
                                    getValue(UserAccountSettings.class).getFollowers()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information" + settings.toString());

                }
                catch (NullPointerException e){
                    Log.d(TAG, "getUserAccountSettings: NullPointer Exception: " + e.getMessage());
                }
            }

            //users node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUsers: dataSnapshot " + ds);

                user.setUsername(
                        ds.child(userID).
                                getValue(User.class).getUsername()
                );

                user.setEmail(
                        ds.child(userID).
                                getValue(User.class).getEmail()
                );
                user.setPhone_number(
                        ds.child(userID).
                                getValue(User.class).getPhone_number()
                );

                user.setUser_id(
                        ds.child(userID).
                                getValue(User.class).getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
            }
        }

        return new UserSettings(user,settings);
    }
}
