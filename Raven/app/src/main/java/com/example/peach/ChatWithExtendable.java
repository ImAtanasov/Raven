package com.example.peach;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ChatWithExtendable extends AppCompatActivity implements MediaRecorder.OnInfoListener {
    int onStartCount = 0;
    public ImageButton imButton = null;
    private MediaRecorder recorder = null;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<AudioMessage> listDataHeader;
    public HashMap<AudioMessage, Pair<String, String>> listDataChild;

    public LinearLayout llFront = null;

    //Firebase
    FirebaseStorage storage = FirebaseStorage.getInstance();
    // Create a storage reference from our app
    StorageReference storageRef = storage.getReference();
    StorageReference ref = null;
    FirebaseUser user = null;

    DatabaseReference mDatabase = null;
    DatabaseReference audioRef = null;

    ParcelFileDescriptor parcelRead = null;

    String friend_id = null;
    InputStream inputStream = null;
    byte[] data;

    String audioCode = null;
    String audioCodeDate = null;
    long creationTime = 0;
    int standartCount = 8;
    int itemsLoadedTo = standartCount;

    int count = 0;

    DatabaseReference unreadRef = null;
    ChildEventListener childListener = null;


    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_item);
        //Toast.makeText(ChatWithExtendable.this,  "BEGINNNNNNNNNNNNNNN " , Toast.LENGTH_SHORT).show();
        Objects.requireNonNull(getSupportActionBar()).hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //FrameLayout frameGif = findViewById(R.id.loadingFrameGif);
        //frameGif.setZ(-1);
        //ImageView imageView = findViewById(R.id.imageGif);
        //imageView.setClickable(false);
        //Glide.with(this).load(R.raw.star).into(imageView);
        llFront = findViewById(R.id.chatCoordinator);
        llFront.setAlpha((float) 0.8);

        onStartCount = 1;
        if (savedInstanceState == null) // 1st time
        {
            this.overridePendingTransition(R.anim.slide_left,
                    R.anim.anim_slide_out_left);
        } else // already created so reverse animation
        {
            onStartCount = 2;
        }

        expListView = findViewById(R.id.expandable_list);

        //


        expListView.setOnTouchListener(new OnSwipeTouchListener(ChatWithExtendable.this) {
            public void onSwipeRight() {

                //Intent intent = new Intent(ChatWithExtendable.this, SearchChat.class);
                //startActivity(intent);
                finish();
            }

            public void onSwipeBottom() {
                expListView.setOnScrollListener(new AbsListView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }


                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (firstVisibleItem == 0) {

                            // check if we reached the top or bottom of the list
                            View v = expListView.getChildAt(0);
                            int offset = (v == null) ? 0 : v.getTop();
                            if (offset == 0) {
                                //Toast.makeText(ChatWithExtendable.this, "top "+ totalItemCount, Toast.LENGTH_SHORT).show();
                                addNextItems();
                            }
                        } /*else if (totalItemCount - visibleItemCount == firstVisibleItem) {
                            View v = expListView.getChildAt(totalItemCount - 1);
                            int offset = (v == null) ? 0 : v.getTop();
                            if (offset == 0) {
                                // reached the bottom:
                                return;
                            }
                        } */
                    }
                });

            }
        });


        Bundle b = getIntent().getExtras();

        if (b != null) {
            friend_id = (String) b.get("friend_id");
        }

        int compare = user.getUid().compareTo(friend_id);
        if (compare < 0) {
            audioCode = user.getUid() + friend_id;
        } else {
            audioCode = friend_id + user.getUid();
        }

        imButton = findViewById(R.id.image_button);
        imButton.setVisibility(View.VISIBLE);
        imButton.setClickable(true);

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        prepareListData();


        imButton.setOnTouchListener((v, event) -> {
            // TODO Auto-generated method stub
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    setUnread(friend_id);
                    break;
            }
            return false;
        });


        refresherListener();
        cancelNotification();

    }

    private void refresherListener() {

        DatabaseReference unreadRefAdd = FirebaseDatabase.getInstance().getReference().child("unread").child(user.getUid()).child(friend_id);
        unreadRef = FirebaseDatabase.getInstance().getReference().child("unread").child(user.getUid());
        childListener = unreadRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                //Unread un = dataSnapshot.getValue(Unread.class);
                //Toast.makeText(ChatWithExtendable.this, "onChildAddedddddd " + dataSnapshot, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {

                Unread un = dataSnapshot.getValue(Unread.class);
                if (un.getSenderId().equals(friend_id)) {

                    int newMessages;
                    try {
                        //newMessages = dataSnapshot.getValue(Integer.class);
                        newMessages = un.getCount();
                    } catch (Exception e) {
                        newMessages = 0;
                    }
                    if (newMessages > 0) {
                        audioRef = mDatabase.child("audioMessages/" + audioCode);
                        Query query = audioRef.limitToLast(newMessages);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    AudioMessage currAudioMessage = singleSnapshot.getValue(AudioMessage.class);
                                    MediaPlayer mediaPlayer = new MediaPlayer();

                                    if (currAudioMessage != null) {
                                        storageRef.child(currAudioMessage.getAudioPath()).getDownloadUrl().addOnSuccessListener(uri -> {
                                            //storageRef.child("audioMessages/ne.mp3").getDownloadUrl().addOnSuccessListener(uri -> {
                                            try {
                                                mediaPlayer.setDataSource(ChatWithExtendable.this, uri);
                                                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                                                });
                                                mediaPlayer.prepareAsync();
                                                currAudioMessage.setMedia(mediaPlayer);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).addOnFailureListener(exception -> {

                                        });
                                    }

                                    listDataHeader.add(currAudioMessage);
                                    if (currAudioMessage != null) {
                                        listDataChild.put(currAudioMessage, new Pair<>(currAudioMessage.geTag(), new SimpleDateFormat("HH:mm:ss").format(currAudioMessage.getCreationDate())));
                                    }
                                    listAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        un.setCountZero();
                        unreadRefAdd.setValue(un);

                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setUnread(String friend_id) {

        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference().child("unread").child(friend_id).child(user.getUid());
        unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null) {

                    Toast.makeText(ChatWithExtendable.this, "sendr  " + user.getUid(), Toast.LENGTH_SHORT).show();
                    Unread un = new Unread(user.getUid(), 1, new Date().getTime());
                    unreadRef.setValue(un);
                    Profile finalValue = null;
                    for (Iterator<Profile> iterator = SearchChat.searchList.iterator(); iterator.hasNext(); ) {
                        Profile value = iterator.next();
                        if (value.userID.equals(friend_id)) {
                            iterator.remove();
                            finalValue = value;
                            break;
                        }
                    }
                    if (finalValue != null) {
                        SearchChat.searchList.add(0, finalValue);
                        SearchChat.rv.getAdapter().notifyDataSetChanged();
                    }
                } else {

                    Unread un = dataSnapshot.getValue(Unread.class);
                    if (un != null) {
                        un.setPlusOne();
                        un.setDateTime(new Date().getTime());
                    }
                    unreadRef.setValue(un);


                    Profile finalValue = null;
                    for (Iterator<Profile> iterator = SearchChat.searchList.iterator(); iterator.hasNext(); ) {
                        Profile value = iterator.next();
                        if (value.userID.equals(friend_id)) {
                            iterator.remove();
                            finalValue = value;
                            break;
                        }
                    }
                    if (finalValue != null) {
                        SearchChat.searchList.add(0, finalValue);
                        SearchChat.rv.getAdapter().notifyDataSetChanged();
                    }
                }
                //Toast.makeText(ChatWithExtendable.this,  "dataSnapshot " +dataSnapshot, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

    private void addNextItems() {
        audioRef = mDatabase.child("audioMessages/" + audioCode);
        itemsLoadedTo += 8;
        count -= 8;
        Query query = audioRef.startAt(count).endAt(count - 8);
        //Query query2 = query2.limitToFirst(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    AudioMessage currAudioMessage = singleSnapshot.getValue(AudioMessage.class);

                    MediaPlayer mediaPlayer = new MediaPlayer();

                    if (currAudioMessage != null) {
                        storageRef.child(currAudioMessage.getAudioPath()).getDownloadUrl().addOnSuccessListener(uri -> {
                            //storageRef.child("audioMessages/ne.mp3").getDownloadUrl().addOnSuccessListener(uri -> {
                            try {
                                mediaPlayer.setDataSource(ChatWithExtendable.this, uri);

                                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                                });
                                mediaPlayer.prepareAsync();
                                currAudioMessage.setMedia(mediaPlayer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).addOnFailureListener(exception -> {

                        });
                    }

                    listDataHeader.add(currAudioMessage);
                    if (currAudioMessage != null) {
                        listDataChild.put(currAudioMessage, new Pair<String, String>(currAudioMessage.geTag(), new SimpleDateFormat("HH:mm:ss").format(currAudioMessage.getCreationDate())));
                    }
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void prepareListData() {

        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference().child("unread").child(user.getUid()).child(friend_id);

        unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Toast.makeText(ChatWithExtendable.this, "new Place " + dataSnapshot, Toast.LENGTH_SHORT).show();
                Unread unr = dataSnapshot.getValue(Unread.class);
                if (unr != null) {
                    if (itemsLoadedTo < unr.getCount()) {
                        itemsLoadedTo = unr.getCount() + standartCount / 2;
                    }
                    unr.setCountZero();
                    unreadRef.setValue(unr);
                } else {
                    unreadRef.setValue(new Unread(friend_id, 0));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        //unreadRef.setValue(new Unread(friend_id, 0));
        //unreadRef.child("count").setValue(0);

        audioRef = mDatabase.child("audioMessages/" + audioCode);
        Query query = audioRef.limitToLast(itemsLoadedTo);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    AudioMessage currAudioMessage = singleSnapshot.getValue(AudioMessage.class);
                    MediaPlayer mediaPlayer = new MediaPlayer();

                    if (currAudioMessage != null) {
                        storageRef.child(currAudioMessage.getAudioPath()).getDownloadUrl().addOnSuccessListener(uri -> {
                            //storageRef.child("audioMessages/ne.mp3").getDownloadUrl().addOnSuccessListener(uri -> {
                            try {
                                mediaPlayer.setDataSource(ChatWithExtendable.this, uri);
                                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                                });
                                mediaPlayer.prepareAsync();
                                currAudioMessage.setMedia(mediaPlayer);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).addOnFailureListener(exception -> {

                        });
                    }

                    listDataHeader.add(currAudioMessage);
                    if (currAudioMessage != null) {
                        listDataChild.put(currAudioMessage, new Pair<>(currAudioMessage.geTag(), new SimpleDateFormat("HH:mm:ss").format(currAudioMessage.getCreationDate())));
                    }
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        query = audioRef;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    count = (int) dataSnapshot.getChildrenCount();
                    //Toast.makeText(ChatWithExtendable.this, "count " + count, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (onStartCount > 1) {
            this.overridePendingTransition(R.anim.slide_right,
                    R.anim.anim_slide_out_right);

        } else if (onStartCount == 1) {
            onStartCount++;
        }

    }


    private void startRecording() {

        creationTime = new Date().getTime();
        audioCodeDate = "/audioMessages/" + audioCode + "/" + creationTime + ".amr";
        ref = storageRef.child(audioCodeDate);

        ParcelFileDescriptor[] descriptors = new ParcelFileDescriptor[0];
        try {
            descriptors = ParcelFileDescriptor.createPipe();
        } catch (IOException e) {
            e.printStackTrace();
        }
        parcelRead = new ParcelFileDescriptor(descriptors[0]);
        ParcelFileDescriptor parcelWrite = new ParcelFileDescriptor(descriptors[1]);
        inputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelRead);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(parcelWrite.getFileDescriptor());
        recorder.setMaxDuration(5000);
        recorder.setOnInfoListener(this);


        try {
            recorder.prepare();
            recorder.start();

        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }


    private void stopRecording() {
        if (null != recorder) {
            try {

                recorder.stop();
                recorder.release();
                recorder = null;
                data = new byte[inputStream.available()];
                inputStream.read(data);
                inputStream.close();

                MediaPlayer mediaPlayer = new MediaPlayer();

                uploadFile(ref, data);

                mediaPlayer.setDataSource(parcelRead.getFileDescriptor());
                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
                });

                AudioMessage newAudioMessage = new AudioMessage(user.getUid(), friend_id, audioCodeDate, creationTime);
                mDatabase.child("audioMessages").child(audioCode).child(String.valueOf(creationTime)).setValue(newAudioMessage);

                newAudioMessage.setMedia(mediaPlayer);


                listDataHeader.add(newAudioMessage);
                listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), new Pair<>("None", new SimpleDateFormat("HH:mm:ss").format(new Date())));
                listAdapter.notifyDataSetChanged();

            } catch (RuntimeException | IOException e) {
                e.printStackTrace();
            }

        }
    }


    private void uploadFile(StorageReference ref, byte[] data) {

        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
        }).addOnSuccessListener(taskSnapshot -> {
        });

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecording();
        }
    }

    public void cancelNotification()
    {
        try {
            SearchChat.notificationManager.cancel(friend_id, 234);
        }catch (Exception e){

        }
    }


    @Override
    public void finish() {
        int pos = 0;
        for (Profile pr : SearchChat.searchList) {
            if (pr.userID.equals(friend_id)) {
                pr.setLamp(0);
                SearchChat.rv.getAdapter().notifyDataSetChanged();
            }
            pos = pos + 1;
        }
        unreadRef.removeEventListener(childListener);
        super.finish();
        overridePendingTransition(R.anim.slide_right, R.anim.anim_slide_out_right);
    }
}