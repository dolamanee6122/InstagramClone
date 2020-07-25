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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "loginActivity";

    //firebase
    private FirebaseAuth mAuth;

    private Context mContext;
    private EditText mEmail,mPassword;
    private Button btn_login;
    private TextView please_wait;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started");
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        please_wait = (TextView)findViewById(R.id.please_wait);
        mEmail = (EditText)findViewById(R.id.input_email);
        mPassword = (EditText)findViewById(R.id.input_password);
        mContext = LoginActivity.this;

        please_wait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
        linkSignUp();
    }

    private void linkSignUp(){
        TextView linkSignup = (TextView)findViewById(R.id.link_signup);
        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to Register Activity");
                Intent intent = new Intent(mContext,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private boolean isStringNull(String str){
        if(str.equals(""))return true;
        return false;
    }

    /*
------------------------------------------firebase-------------------------------
 */


    private  void init(){
        //initialise the button for logging in
        Button btnLogin = (Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(isStringNull(email) || isStringNull(password)){
                    Toast.makeText(mContext,"user must fill all the fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    please_wait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {


                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        try{
                                            if(user.isEmailVerified()){
                                                Log.d(TAG, "onComplete: success email verified");
                                                updateUI(user);
                                            }
                                            else{
                                                Toast.makeText(mContext, "Email not Verified.\n check your Email Inbox", Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                                please_wait.setVisibility(View.GONE);
                                                mAuth.signOut();
                                            }
                                        }
                                        catch (NullPointerException e){
                                            Log.e(TAG, "onComplete: NullPointerException"+e.getMessage());
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(mContext, getString(R.string.auth_failed),
                                                Toast.LENGTH_SHORT).show();
                                        updateUI(null);
                                    }

                                    // ...
                                }
                            });
                }

            }
        });
    }
    private void setupFirebaseAuth(){

        Log.d(TAG, "setupFirebaseAuth: setting up with firebase auth");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
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
