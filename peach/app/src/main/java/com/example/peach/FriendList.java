package com.example.peach;

import java.util.ArrayList;

public class FriendList {
    private String userID;
    private ArrayList<String> friends = null;


    public FriendList(String userID, ArrayList<String> friends) {
        this.userID = userID;
        this.friends = friends;
    }

    public FriendList(String userID) {
        this.userID = userID;
        this.friends = new ArrayList<String>();
    }

    public FriendList() {
    }

    public String getUserID() {
        return userID;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }
    public void addFriend(String friend){
        if(this.friends == null){
            this.friends = new ArrayList<String>();
        }else if(this.friends.contains(friend)){
            return;
        }
        this.friends.add(friend);
    }

    public void deleteFriend(String friend){
        if(friends !=null){
            this.friends.remove(friend);
        }
    }

}
