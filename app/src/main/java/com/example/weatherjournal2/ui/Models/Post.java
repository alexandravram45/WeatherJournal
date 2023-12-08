package com.example.weatherjournal2.ui.Models;


public class Post {
    private String postDescription;
    private String postImageUri;
    private String userPhoto;
    private String location;
    private Object timeStamp;

    public Post(String postDescription, String postImageUri, String userPhoto) {
        this.postDescription = postDescription;
        this.postImageUri = postImageUri;
        this.userPhoto = userPhoto;
    }

    public Post() {
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }
}
