package com.example.peach;

import android.media.MediaPlayer;
import android.widget.SeekBar;
import java.util.Date;


public class AudioMessage {
    private MediaPlayer mPlayer; // message body
    private String userID;
    private String friendID;
    private String audioPath;
    private long creationDate;
    private String tag;
    private SeekBar sBar;
    private boolean unseen;


    public AudioMessage(MediaPlayer mPlayer, String userID, String friendID) {
        this.mPlayer = mPlayer;
        this.userID = userID;
        this.friendID = friendID;
        this.creationDate = new Date().getTime();
        this.tag = "No Tag";
    }

    public AudioMessage(String userID, String friendID, String audioPath, long creationDate) {
        this.userID = userID;
        this.friendID = friendID;
        this.audioPath = audioPath;
        this.creationDate = creationDate;
        this.tag = "No Tag";
    }

    public AudioMessage(MediaPlayer mPlayer, String userID, String friendID, String audioPath) {
        this.mPlayer = mPlayer;
        this.userID = userID;
        this.friendID = friendID;
        this.audioPath = audioPath;
        this.creationDate = new Date().getTime();
        this.tag = "No Tag";
    }

    public AudioMessage(){
    }

    public MediaPlayer getMedia() {
        return mPlayer;
    }

    public void setMedia(MediaPlayer mPlayer) {
        this.mPlayer = mPlayer;
    }

    public String getUserID() {
        return userID;
    }

    public String getFriendID() {
        return friendID;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String geTag() {
        return tag;
    }

    public SeekBar getSeekBark() {
        return sBar;
    }

    public boolean getUnseen() {
        return unseen;
    }

}