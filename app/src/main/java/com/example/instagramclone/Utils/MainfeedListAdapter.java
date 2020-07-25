package com.example.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.Profile.ProfileActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Share.PhotoFragment;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.AlgorithmConstraints;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;
    private static final String TAG = "MainfeedListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private DatabaseReference myRef;
    private String currentUsername;


    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        layoutResource = resource;
        myRef = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder{
        CircleImageView mProfileImage;
        String likesString;
        TextView username,timeDelta,caption ,likes,comments;
        SquareImageView image;
        ImageView heartRed,heartWhite,comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector,detectorPhoto;
        Photo photo;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "getView: ");

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();
            holder.username = convertView.findViewById(R.id.username);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.image_heart_white);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.caption = convertView.findViewById(R.id.image_caption);
            holder.timeDelta = convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite,holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext,new GestureListener(holder));
            holder.detectorPhoto = new GestureDetector(mContext,new GestureListenerPhoto(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        //get the current user name(need for checking likes string)
        getCurrentUsername();

        //get the likes string
        getLikesString(holder);

        //set the caption
        holder.caption.setText(getItem(position).getCaption());

        //set the comment
         List<Comment> comments = getItem(position).getComments();
         holder.comments.setText("View all " + comments.size() + " comments");
         holder.comments.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id());

                 ((HomeActivity)mContext).onCommentThreadSelected(getItem(position),mContext.getString(R.string.home_activity));

                 //going to need to do something else
                 ((HomeActivity)mContext).hideLayout();

             }
         });

         //set the time when it was posted
        String timeStampDifference = getTimeStampDifference(getItem(position));
        if(timeStampDifference.equals("0"))
            holder.timeDelta.setText("TODAY");
        else holder.timeDelta.setText(timeStampDifference + " DAYS AGO");

        //set the Post Image
        UniversalImageLoader.setImage(getItem(position).getImage_path(),holder.image,null,"");

        //get the profile image and username of user who posted it
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to the user profile of: " + holder.username.getText());
                            Intent intent = new Intent(mContext,ProfileActivity.class);

                            if(!holder.user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            }
                            mContext.startActivity(intent);
                        }
                    });
                    holder.settings =singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext).onCommentThreadSelected(getItem(position),mContext.getString(R.string.home_activity));

                            //another thing

                            ((HomeActivity)mContext).hideLayout();
                        }
                    });
                    UniversalImageLoader.setImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mProfileImage,null,"");
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to the user profile of: " + holder.username.getText());
                            Intent intent = new Intent(mContext,ProfileActivity.class);
                            if(!holder.user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                intent.putExtra(mContext.getString(R.string.calling_activity),
                                        mContext.getString(R.string.home_activity));
                                intent.putExtra(mContext.getString(R.string.intent_user),holder.user);
                            }
                            mContext.startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //get the user objects
        Query userQuery = myRef.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(User.class).getUsername());
                    holder.user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(reachedEndOfList(position)){
            loadMoreData();
        }


        return convertView;
    }


    private boolean reachedEndOfList(int position){
        return position == getCount() - 1;
    }
    private void loadMoreData(){
        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener)getContext();
        }catch (ClassCastException e){
            Log.d(TAG, "loadMoreData: ClassCastException " + e.getMessage());
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.d(TAG, "loadMoreData: NullPointerException " + e.getMessage());
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving current user.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder){
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Toast.makeText(mContext, "Toggle heart", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onDoubleTap: double tap confirmed");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String KeyID = singleSnapshot.getKey();

                        //case 1:The user already liked the photo
                        if(mHolder.likedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            myRef.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                            myRef.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                           mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        else if(!mHolder.likedByCurrentUser)
                        {
                            //add like
                            addNewLike(mHolder);
                            break;
                        }
                        //case 2: the user has not liked the photo
                    }
                    if(!dataSnapshot.exists()){
                        //add like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }

    }

    public class GestureListenerPhoto extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListenerPhoto(ViewHolder holder){
            mHolder = holder;
        }
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap confirmed");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String KeyID = singleSnapshot.getKey();

                        //case 1:The user already liked the photo
                        if(mHolder.likedByCurrentUser&& singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            /*myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(KeyID)
                                    .removeValue();
                            mHeart.toggleLike();
                            getLikesString();*/
                        }
                        else if(!mHolder.likedByCurrentUser)
                        {
                            //add like
                            addNewLike(mHolder);
                            break;
                        }
                        //case 2: the user has not liked the photo
                    }
                    if(!dataSnapshot.exists()){
                        //add like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new likes");
        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        holder.heart.toggleLike();
        getLikesString(holder);

    }


    private void getLikesString(final ViewHolder holder){
        Log.d(TAG, "getLikesString: getting likes string");
        
        try{

            //setup likes string

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference.child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                                    Log.d(TAG, "onDataChange: found likes  "+ singleSnapshot.getValue(User.class).getUsername() );
                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }
                                String [] splitUsers = holder.users.toString().split(",");

                                if(holder.users.toString().contains(currentUsername + ",")){
                                    holder.likedByCurrentUser = true;
                                }
                                else holder.likedByCurrentUser = false;

                                int length = splitUsers.length;
                                if(length == 1){
                                    holder.likesString = "Liked by " + splitUsers[0];
                                }
                                else if(length == 2){
                                    holder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                }
                                else if(length == 3){
                                    holder.likesString = "Liked by " + splitUsers[0] + ","
                                            + splitUsers[1] + " and " + splitUsers[2];
                                }
                                else if(length >= 4){
                                    holder.likesString = "Liked by " + splitUsers[0] + ","
                                            + splitUsers[1] + "," + splitUsers[2] + " and " + (length-3) + " others.";
                                }
                                //setup likes string
                                setupLikesString(holder,holder.likesString);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    if(!dataSnapshot.exists()){
                        holder.likesString = "No Likes";
                        holder.likedByCurrentUser = false;
                        //setup likes string
                        setupLikesString(holder,holder.likesString);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }catch (NullPointerException e){
            Log.d(TAG, "getLikesString: NullPointerException " + e.getMessage());
            holder.likesString = "No likes";
            holder.likedByCurrentUser = false;
            //Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            //setup likes string
            setupLikesString(holder,holder.likesString);
        }


    }


    private void setupLikesString(final ViewHolder holder,String likesString){
        Log.d(TAG, "setupLikesString: likes string " + holder.likesString);

        if(holder.likedByCurrentUser){
            Log.d(TAG, "setupLikesString: photo is liked by current user. ");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);

            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        else{

            Log.d(TAG, "setupLikesString: photo is not liked by current user. ");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);

            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });

            holder.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detectorPhoto.onTouchEvent(event);
                }
            });

        }
        holder.likes.setText(likesString);


    }



    /**
     * return the string represent the number of days the post was made
     * @return
     */
    private String getTimeStampDifference(Photo photo){
        Log.d(TAG, "getTimeStampDifference: getting time stamp difference ");

        String difference = "0";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date timestamp;
        Date today = c.getTime();
        sdf.format(today);
        final String photoTimestamp = photo.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round((today.getTime()-timestamp.getTime())/1000 /60 / 60 /24));
        }catch (ParseException e){
            Log.d(TAG, "getTimeStampDifference: ParseException " + e.getMessage());
            difference = "0";
        }
        return difference;

    }




}
