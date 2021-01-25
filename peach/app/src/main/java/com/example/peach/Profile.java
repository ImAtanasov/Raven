package com.example.peach;

import java.util.HashMap;
import java.util.Map;

public class Profile {


    public String userID;
    public String username;
    public String email;
    public long date_joined;
    public long last_login;
    public Boolean is_active = true;
    public String picture_image;
    public String fromCountry;
    public int lamp = 0;

    public Profile() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Profile(String userID, String username, String email, long date_joined, String fromCountry) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.date_joined = date_joined;
        this.fromCountry = fromCountry;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userID", userID);
        result.put("username", username);
        result.put("email", email);
        return result;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public long getDate_joined() {
        return date_joined;
    }

    public long getLast_login() {
        return last_login;
    }

    public String getPicture_image() {
        return picture_image;
    }

    public String getFromCountry() {
        return fromCountry;
    }

    public int getLamp() {
        return lamp;
    }

    public void setLamp(int value) {
        lamp = value;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPicture_image(String path) {
        this.picture_image = path;
    }
}