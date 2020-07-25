package com.example.instagramclone.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.PointerIcon;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.instagramclone.Profile.ProfileActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.instagramclone.Utils.UserListAdapter;
import com.example.instagramclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private Context mContext = SearchActivity.this;

    private static final int ACTIVITY_NUM = 1;

    //widgets
    private EditText mSearchParam;
    private ListView mListView;




    //vars
    private List<User> mUsersList;
    private UserListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d(TAG, "onCreate: starting");

        mSearchParam = findViewById(R.id.search);
        mListView = findViewById(R.id.listView);

        hideSoftKeyBoard();
        setupBottomNavigationView();
        initTextListener();
    }

    private void initTextListener(){
        Log.d(TAG, "initTextListener: initialising");
        mUsersList = new ArrayList<>();
        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = mSearchParam.getText().toString();//.toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }



    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for keyword");
        mUsersList.clear();
        //update the users list
        if(keyword.length()!=0){
            String inputLower = keyword.toLowerCase();
            String inputUpper = keyword.toUpperCase();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .startAt(inputUpper)
                    .endAt(inputLower+"\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).toString());

                        //if(singleSnapshot.getValue(User.class).getUser_id().equals("5mVTEQKJ9AZOcggg2SUoOi6NEGK2"))continue;
                        mUsersList.add(singleSnapshot.getValue(User.class));
                        //update the user list view
                        updateUserList();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

    }


    private void updateUserList(){

        Log.d(TAG, "updateUserList: updating user list");
        adapter = new UserListAdapter(mContext,R.layout.layout_user_list_item,mUsersList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mUsersList.get(position).toString());

                ///navigate to their profile

                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                if(!mUsersList.get(position).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    intent.putExtra(getString(R.string.calling_activity),getString(R.string.search_activity));
                    intent.putExtra(getString(R.string.intent_user),mUsersList.get(position));
                }
                startActivity(intent);
            }
        });

    }
    private void hideSoftKeyBoard(){
        if(getCurrentFocus()!=null){
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }


    /**
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView(){
        Log.d(TAG,"setupBottomNavigationView setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }
}