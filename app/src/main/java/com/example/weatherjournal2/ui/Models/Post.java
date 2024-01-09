package com.example.weatherjournal2.ui.Models;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Post implements Comparable<Post>{
    private String postDescription;
    private String postImageUri;
    private String userPhoto;
    private String username;
    private GeoPoint location;
    private Timestamp timeStamp;

    public Post(String postDescription, String postImageUri, String userPhoto, String username, Timestamp timeStamp, GeoPoint location) {
        this.postDescription = postDescription;
        this.postImageUri = postImageUri;
        this.userPhoto = userPhoto;
        this.username = username;
        this.timeStamp = timeStamp;
        this.location = location;
    }

    public Post() {
    }

    @Override
    public int compareTo(Post otherPost) {
        // Compare posts based on timestamp
        return otherPost.timeStamp.compareTo(this.timeStamp);
    }

    public Date getTimeStampDate() {
        if (timeStamp != null) {
            return timeStamp.toDate();
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getPostImageUri() {
        return postImageUri;
    }

    public void setPostImageUri(String postImageUri) {
        this.postImageUri = postImageUri;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }
}
