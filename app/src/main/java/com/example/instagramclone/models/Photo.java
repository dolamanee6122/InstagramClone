package com.example.instagramclone.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Photo implements Parcelable {

    private String caption;
    private String date_created;
    private String image_path;
    private String user_id;
    private String tags;
    private String photo_id;
    private List<Like> likes;
    private List<Comment> comments;

    public Photo() {
    }


    public Photo(String caption, String date_created, String image_path, String user_id,
                 String tags, String photo_id, List<Like> likes, List<Comment> comments) {
        this.caption = caption;
        this.date_created = date_created;
        this.image_path = image_path;
        this.user_id = user_id;
        this.tags = tags;
        this.photo_id = photo_id;
        this.likes = likes;
        this.comments = comments;
    }

    protected Photo(Parcel in) {
        caption = in.readString();
        date_created = in.readString();
        image_path = in.readString();
        user_id = in.readString();
        tags = in.readString();
        photo_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caption);
        dest.writeString(date_created);
        dest.writeString(image_path);
        dest.writeString(user_id);
        dest.writeString(tags);
        dest.writeString(photo_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "caption='" + caption + '\'' +
                ", date_created='" + date_created + '\'' +
                ", image_path='" + image_path + '\'' +
                ", user_id='" + user_id + '\'' +
                ", tags='" + tags + '\'' +
                ", photo_id='" + photo_id + '\'' +
                ", likes=" + likes +
                '}';
    }
}
