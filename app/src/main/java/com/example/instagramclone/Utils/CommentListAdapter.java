package com.example.instagramclone.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.instagramclone.R;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.Inflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, int resource,
                              @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder{
        TextView comment,username,timeStamp,reply,likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if(convertView== null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(layoutResource,parent,false);

            holder.comment = (TextView)convertView.findViewById(R.id.comment);
            holder.timeStamp = (TextView)convertView.findViewById(R.id.comment_time_posted);
            holder.reply = (TextView)convertView.findViewById(R.id.comment_reply);
            holder.username = (TextView)convertView.findViewById(R.id.comment_username);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);
            holder.like = (ImageView)convertView.findViewById(R.id.comment_like);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        //set the comment
        holder.comment.setText(getItem(position).getComment());

        //set the time stamp difference
        String timeStampDiff = getTimeStampDifference(getItem(position));
        if(!timeStampDiff.equals("0")){
            holder.timeStamp.setText(timeStampDiff+"d");
        }
        else holder.timeStamp.setText("today");

        //set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    UniversalImageLoader.setImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.profileImage,null,"");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(position == 0){
            holder.like.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * return the string represent the number of days the post was made
     * @return
     */
    private String getTimeStampDifference(Comment comment){
        Log.d(TAG, "getTimeStampDifference: getting time stamp difference ");

        String difference = "0";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date timestamp;
        Date today = c.getTime();
        sdf.format(today);
        final String photoTimestamp = comment.getDate_created();
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



