package com.example.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.StringManipulation;
import com.example.instagramclone.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "registerActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Context mContext;
    private String email,username,password,confirm_password;
    private EditText mEmail,mUsername,mPassword,mConfirmPassword;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private TextView please_wait,linkLogin;

    private FirebaseMethods mFireBaseMethods;

    //Realtime Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: started");

        goLogin();
        initWidgets();
        mFireBaseMethods = new FirebaseMethods(RegisterActivity.this);
        setupFirebaseAuth();
        Init();
    }


    private void goLogin(){
        linkLogin = (TextView)findViewById(R.id.link_signIn);
        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext,LoginActivity.class));
                finish();
            }
        });
    }

    /**
     * initialise the activity widgets
     */
    private void initWidgets(){
        Log.d(TAG, "initWidgets: initialising widgets");

        mContext = RegisterActivity.this;
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        please_wait = (TextView)findViewById(R.id.please_wait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText)findViewById(R.id.input_password);
        mConfirmPassword = (EditText)findViewById(R.id.input_confirm_password);
        mUsername = (EditText)findViewById(R.id.input_full_name);
        btnRegister = (Button)findViewById(R.id.btn_register);


        mProgressBar.setVisibility(View.GONE);
        please_wait.setVisibility(View.GONE);

    }

    private void Init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: registering new user");
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();
                confirm_password = mConfirmPassword.getText().toString();


                if(checkInputs(email,username,password,confirm_password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    please_wait.setVisibility(View.VISIBLE);
                    //Toast.makeText(mContext, username + "  " + StringManipulation.condenseUsername(username), Toast.LENGTH_SHORT).show();

                    mFireBaseMethods.registerNewEmail(email,password, username,mProgressBar,please_wait);

                }
            }
        });
    }

    private boolean checkInputs(String email, String username, String password, String confirm_password){
        Log.d(TAG, "checkInputs: checking input for null values");
        if(email.equals("") || username.equals("") || password.equals("") || confirm_password.equals("")){
            Toast.makeText(mContext,"All fields must be fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!password.equals(confirm_password)){
            Toast.makeText(mContext, "confirm password does not matched with password",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }






    /*
------------------------------------------firebase-------------------------------
 */



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

                 for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH. " + singleSnapshot.getValue(User.class).getUsername());
                        //Toast.makeText(mContext, "That username already exists.", Toast.LENGTH_SHORT).show();
                        append = myRef.push().getKey().substring(3, 10);
                        Log.d(TAG, "onDataChange: username already exists appending random string to name " + append);
                        }
                    }

                 String mUsername = "";
                 mUsername = username + append;



                mFireBaseMethods.addNewUser(email, mUsername, "", "", "");
                Toast.makeText(mContext, "SignUp Successful, sending verification email.", Toast.LENGTH_SHORT).show();

                //signing out for verification of email
                mAuth.signOut();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setupFirebaseAuth() {

        Log.d(TAG, "setupFirebaseAuth: setting up with firebase auth");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();




        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    Log.d(TAG, "onAuthStateChanged: signed in " + currentUser.getUid());
                    //Toast.makeText(mContext, "used ID: 111 " , Toast.LENGTH_SHORT).show();
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    //Toast.makeText(mContext, "user is null", Toast.LENGTH_SHORT).show();
                }
            }
        };


    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    /*
            If user is already logged in then navigate to HomeActivity and call 'finish()'
             */
    private void updateUI(FirebaseUser mUser){
        mProgressBar.setVisibility(View.GONE);
        please_wait.setVisibility(View.GONE);
        if(mUser!=null){
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
