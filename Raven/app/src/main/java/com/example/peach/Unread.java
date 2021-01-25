package com.example.peach;


public class Unread {

    private String senderId;
    private int count;
    long dateTime;


    public Unread(String senderId, int count, long dateTime) {
        this.senderId = senderId;
        this.count = count;
        this.dateTime = dateTime;

    }

    public Unread(String senderId, int count) {
        this.senderId = senderId;
        this.count = count;

    }

    public Unread(String senderId) {
        this.senderId = senderId;

    }

    public Unread() {

    }

    public int getCount() {
        return count;
    }

    public String getSenderId() {
        return senderId;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dt) {
        this.dateTime =  dt;
    }

    public void setPlusOne() {
        this.count += 1;
    }

    public void setCountZero(){
        this.count = 0;
    }
}
