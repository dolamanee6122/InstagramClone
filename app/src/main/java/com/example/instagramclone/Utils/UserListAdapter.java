package com.example.instagramclone.Utils;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.instagramclone.R;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends ArrayAdapter<User> {
    private static final String TAG = "UserListAdapter";

    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutResource;
    private Context mContext;


    public UserListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUsers = objects;
    }
    private static class ViewHolder{
        TextView username, email;
        CircleImageView profileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.d(TAG, "getView: ");
        final ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.username =convertView.findViewById(R.id.username);
            holder.email = convertView.findViewById(R.id.email);
            holder.profileImage = convertView.findViewById(R.id.profile_photo);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        holder.username.setText(getItem(position).getUsername());
        holder.email.setText(getItem(position).getEmail());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue().toString());
                    UniversalImageLoader.setImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage,null,"");
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;
    }
}
