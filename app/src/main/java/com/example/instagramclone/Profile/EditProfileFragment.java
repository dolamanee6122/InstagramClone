package com.example.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Dialog.ConfirmPasswordDialog;
import com.example.instagramclone.Login.LoginActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Share.ShareActivity;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{
    private static final String TAG = "EditProfileFragment";


    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: " + password);

        FirebaseUser user = mAuth.getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d(TAG, "User re-authenticated.");


                                    //////////////////////check to see if the email not already exists.
                                    mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                            
                                            if(task.isSuccessful()){

                                                try{
                                                    if(task.getResult().getSignInMethods().size()>=1){
                                                        Log.d(TAG, "onComplete: Already in use email");
                                                        Toast.makeText(getActivity(), "Email already exist", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Log.d(TAG, "onComplete: email available");

                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                        ///////////the email is available so update it.
                                                        user.updateEmail(mEmail.getText().toString())
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.d(TAG, "User email address updated.");
                                                                            Toast.makeText(getActivity(), "Email Updated", Toast.LENGTH_SHORT).show();
                                                                            mFirebaseMethods.updateEmail(mEmail.getText().toString());

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }catch (NullPointerException e){
                                                    Log.d(TAG, "onComplete: NullPointerException " + e.getMessage());

                                                }

                                            }

                                        }
                                    });


                                }
                                else{
                                    Log.d(TAG, "User re-authentication failed");
                                    Toast.makeText(mContext, "Invalid Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private FirebaseMethods mFirebaseMethods;

    //edit profile fragment widgets
    private EditText mDisplayName,mUsername,mWebsite,mDescription,mEmail,mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    private Context mContext;
    private String userID;

    //var
    private UserSettings mUserSettings;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile,container,false);

        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText)view.findViewById(R.id.display_name);
        mUsername =(EditText)view.findViewById(R.id.username);
        mWebsite =(EditText)view.findViewById(R.id.website);
        mDescription=(EditText)view.findViewById(R.id.description);
        mEmail = (EditText)view.findViewById(R.id.email);
        mPhoneNumber = (EditText)view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView)view.findViewById(R.id.changeProfilePhoto);

        mContext = getActivity();
        mFirebaseMethods=new FirebaseMethods(mContext);

        setupFirebaseAuth();
        //setupProfileImage();

        //back arrow for navigating to "profile Activity"
        ImageView backArrow = (ImageView)view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to Profile Activity");
                getActivity().finish();
            }
        });

        ImageView checkMark = (ImageView)view.findViewById(R.id.saveChanges);
            checkMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: attempting to save changes.");
                    saveProfileSettings();
                    Toast.makeText(mContext, "Profile Updated", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });

        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submit it to the database
     *Before doing so it checks the chosen username is unique
     */
    private void saveProfileSettings(){
        final String display_name = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phone_number = Long.parseLong(mPhoneNumber.getText().toString());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                Log.d(TAG, "onDataChange: CURRENT USERNAME: " + mUserSettings.getUser().getUsername());

                //case 1: the user did change their username
                if(!mUserSettings.getUser().getUsername().equals(username)){
                    checkIfUsernameExists(username);
                }
                //case 2: the user changes their email
                if(!mAuth.getCurrentUser().getEmail().equals(email)){
                    //step 1: Re authenticate
                    //        --Confirm the password and email

                    ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                    dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
                    dialog.setTargetFragment(EditProfileFragment.this,1);
                    //step 2: check if the email already exists
                    //         --fetchProvidersForEmail(String email)
                    //step 3: change the email
                    //          -- submit the new email to the database and authentication

                }
                /**
                 * change the rest of the settings which does not require uniqueness
                 */

                if(!mUserSettings.getSettings().getDisplay_name().equals(display_name)){
                    //update display name
                    mFirebaseMethods.updateDisplayName(display_name);
                }
                if(!mUserSettings.getSettings().getDescription().equals(description)){
                    //update description
                    mFirebaseMethods.updateDescription(description);
                }
                if(!mUserSettings.getSettings().getWebsite().equals(website)){
                    //update website
                    mFirebaseMethods.updateWebsite(website);
                }
                if(!String.valueOf(mUserSettings.getUser().getPhone_number()).equals(String.valueOf(phone_number))){
                    //update phone_number
                    mFirebaseMethods.updatePhoneNumber(phone_number);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * Check if @param username already exists in the database
     * @param username
     */
    private void checkIfUsernameExists(final String username){
        Log.d(TAG, "checkIfUsernameExists: checking if " + username +" already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //add the username
                if(!dataSnapshot.exists()){
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(mContext, "saved username.", Toast.LENGTH_SHORT).show();
                }
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH. " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(mContext, "That username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        User user = userSettings.getUser();
        mUserSettings = userSettings;
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(user.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

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
                    Intent intent = new Intent(getActivity(), LoginActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //retrieve user information from database;

                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for user in question


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
