package com.example.instagramclone.Share;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebBackForwardList;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.Permissions;
import com.example.instagramclone.Utils.SectionPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private Context mContext = ShareActivity.this;

    private ViewPager mViewPager;

    //constant
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: starting");

        if(checkPermissionArray(Permissions.PERMISSIONS)){
            setupViewpager();
        }
        else{
            verifyPermission(Permissions.PERMISSIONS);
        }


        //setupBottomNavigationView();
    }

    public int getTask(){
        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }
    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }



    private  void setupViewpager(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));


    }
    /**
     * verify all permission passed to the array
     * @param permission
     */
    public void verifyPermission(String[] permission){
        Log.d(TAG, "verifyPermission: verifying the permission ");

        ActivityCompat.requestPermissions(ShareActivity.this,
                permission,
                VERIFY_PERMISSION_REQUEST);


    }
    /**
     * check an array of permission
     * @param permission
     * @return
     */
    public boolean checkPermissionArray(String[] permission){
        Log.d(TAG, "checkPermissionArray: checking permission array ");
        for(int i = 0; i < permission.length ; i++){
            String check = permission[i];
            if(!checkPermissionSingle(check))
                return false;
        }
        return true;
    }

    /**
     * check  a single permission is it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissionSingle(String permission){
        Log.d(TAG, "checkPermissionSingle: checking permission: " + permission);
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this,permission);
        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissionSingle: permission not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissionSingle: permission granted for : " + permission);
            return true;
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

    