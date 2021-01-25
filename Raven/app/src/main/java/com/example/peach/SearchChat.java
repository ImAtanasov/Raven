package com.example.peach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SearchChat extends AppCompatActivity {
    SearchView searchV, searchAdd;
    static List<Profile> searchList = null;
    static RecyclerView rv = null;
    List<Profile> mList = null;
    Adapter adapter = null;
    Adapter searchAdapter = null;
    List<Profile> newSearchListOnSearch = null;


    //fab
    private FloatingActionButton fab_main, fab1_mail, fab2_share, fab_signout;
    private Animation fab_open, fab_close, fab_clock, fab_anticlock;
    Boolean isOpen = false;
    //fab

    //add new layout
    private RelativeLayout persona_layout;
    private RelativeLayout add_new_char;
    private ImageButton profile_pic;
    public static final int GET_FROM_GALLERY = 3;
    Bitmap bitmap_prof_pic = null;
    private Animation personal_open_anim, personal_close_anim;
    Boolean isOpenSearch = false;
    Boolean isOpenProfile = false;
    Uri selectedImage = null;
    //add new layout

    //Database
    FirebaseUser user = null;

    //
    FriendList frList = null;

    static NotificationManager notificationManager = null;
    static boolean theme_boolean = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeThemeStyle();
        setContentView(R.layout.search_chat);

        Intent intent = getIntent();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (intent.getStringExtra("from") == null) {
            this.overridePendingTransition(R.anim.slide_right, R.anim.anim_slide_out_right);
        }

        Objects.requireNonNull(getSupportActionBar()).hide();


        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //fabs
        fab_main = findViewById(R.id.fab);
        fab1_mail = findViewById(R.id.fab1);
        fab2_share = findViewById(R.id.fab2);
        fab_signout = findViewById(R.id.fab_signout);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);

        //fabs

        //add new layout

        add_new_char = findViewById(R.id.relative_add_search);
        add_new_char.setVisibility(View.INVISIBLE);
        persona_layout = findViewById(R.id.persona_layout);
        persona_layout.setVisibility(View.INVISIBLE);
        profile_pic = findViewById(R.id.imageView2);
        profile_pic.setVisibility(View.INVISIBLE);
        personal_open_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.personal_open_anim);
        personal_close_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.personal_close_anim);
        Button accept_profile = findViewById(R.id.btn_personal_accept);
        profile_pic.setOnClickListener(v -> onCHangeProfilePhoto());
        accept_profile.setOnClickListener(v -> acceptButton());
        //add new layout
        loadProfileImage();

        rv = findViewById(R.id.mainRecycler);

        searchList = new ArrayList<>();
        adapter = new Adapter(this, searchList);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        rv.addItemDecoration(new VerticalSpaceItemDecoration(20));

        getUserSearchList();

        searchV = findViewById(R.id.searchMain);
        searchV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                if (query.equals("")) {
                    searchList.clear();
                    searchList.addAll(mList);
                } else {
                    for (Iterator<Profile> iterator = searchList.iterator(); iterator.hasNext(); ) {
                        Profile p = iterator.next();
                        if (!p.getUsername().toLowerCase().contains(" " + query.toLowerCase()) && !p.getUsername().toLowerCase().startsWith(query.toLowerCase())) {
                            iterator.remove();
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (text.equals("")) {
                    searchList.clear();
                    searchList.addAll(mList);
                } else {
                    for (Iterator<Profile> iterator = searchList.iterator(); iterator.hasNext(); ) {
                        Profile p = iterator.next();
                        if (!p.getUsername().toLowerCase().contains(" " + text.toLowerCase()) && !p.getUsername().toLowerCase().startsWith(text.toLowerCase())) {
                            iterator.remove();
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                return false;
            }
        });


        SwipeHelper swipeHelper = new SwipeHelper(this, rv, searchList) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Remove",
                        0,
                        Color.TRANSPARENT,
                        pos -> deleteUserFromSearchList(searchList.get(pos).getUserID())

                ));
            }
        };


        RecyclerView rvAdd = findViewById(R.id.found_new_chat);
        searchAdd = findViewById(R.id.search_global);


        RecyclerView.OnItemTouchListener rvAddListener = (new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                    case MotionEvent.ACTION_UP:
                        return false;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (child != null) {
                        child.setSelected(true);
                    }
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    int position = 0;
                    if (child != null) {
                        child.setSelected(false);
                        position = rv.getChildAdapterPosition(child);
                    }

                    Intent intent1 = new Intent(SearchChat.this, ChatWithExtendable.class);
                    intent1.putExtra("friend_id", newSearchListOnSearch.get(position).getUserID());
                    addUserToSearch(newSearchListOnSearch.get(position).getUserID());
                    startActivity(intent1);
                    addNewInFriendList(newSearchListOnSearch.get(position).getUserID());
                    searchList.add(newSearchListOnSearch.get(position));
                    searchAdd.setQuery("", false);
                    new Handler().postDelayed(() -> closingFloatButtons(), 310);

                }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    if (child != null) {
                        child.setSelected(false);
                    }
                }
                if (e.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (child != null) {
                        child.setSelected(false);
                    }
                }
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }


        });

//fab actions

        fab_signout.setOnClickListener(view -> signOut());


        fab_main.setOnClickListener(view -> {

            if (isOpen) {
                closingFloatButtons();

                searchV.setClickable(false);
                searchV.setVisibility(View.VISIBLE);
                searchV.setAlpha(1f);
                searchAdd.setClickable(true);

                rv.setClickable(false);
                rv.setVisibility(View.VISIBLE);
                rv.setAlpha(1f);
                rvAdd.setClickable(true);
                rvAdd.setVisibility(View.INVISIBLE);


            } else {
                fab2_share.startAnimation(fab_open);
                fab1_mail.startAnimation(fab_open);
                fab_signout.startAnimation(fab_open);
                fab_main.startAnimation(fab_clock);
                fab2_share.setClickable(true);
                fab1_mail.setClickable(true);
                fab_signout.setClickable(true);
                isOpen = true;

            }

        });

        fab2_share.setOnClickListener(view -> {

            if (isOpenProfile) {
                rv.setClickable(false);
                rv.setVisibility(View.VISIBLE);
                rv.setAlpha(1f);
                searchV.setClickable(false);
                searchV.setVisibility(View.VISIBLE);
                searchV.setAlpha(1f);

                persona_layout.setVisibility(View.INVISIBLE);
                profile_pic.setVisibility(View.INVISIBLE);
                persona_layout.startAnimation(personal_close_anim);
                profile_pic.startAnimation(personal_close_anim);
                persona_layout.postDelayed(() -> persona_layout.clearAnimation(), 310);
                profile_pic.postDelayed(() -> profile_pic.clearAnimation(), 310);

                searchAdd.setClickable(true);
                searchAdd.setAlpha(0f);

                isOpenProfile = false;

            } else {
                readProfileInformation();
                switchMethod();
                searchAdd.setClickable(false);
                searchAdd.setVisibility(View.VISIBLE);
                searchAdd.setAlpha(1f);
                rv.setClickable(true);
                rv.setVisibility(View.INVISIBLE);
                rv.setAlpha(0.1f);
                searchV.setClickable(true);
                searchV.setVisibility(View.INVISIBLE);
                searchV.setAlpha(0.1f);
                persona_layout.setVisibility(View.VISIBLE);
                profile_pic.setVisibility(View.VISIBLE);
                persona_layout.startAnimation(personal_open_anim);
                profile_pic.startAnimation(personal_open_anim);
                persona_layout.postDelayed(() -> persona_layout.clearAnimation(), 310);
                profile_pic.postDelayed(() -> profile_pic.clearAnimation(), 310);
                isOpenProfile = true;
                if (isOpenSearch) {
                    add_new_char.setVisibility(View.INVISIBLE);
                    add_new_char.startAnimation(personal_close_anim);
                    add_new_char.postDelayed(() -> add_new_char.clearAnimation(), 310);
                    isOpenSearch = false;
                }
            }
        });

        fab1_mail.setOnClickListener(view -> {

            if (isOpenSearch) {
                rv.setClickable(false);
                rv.setVisibility(View.VISIBLE);
                rv.setAlpha(1f);

                rvAdd.setClickable(true);
                rvAdd.setVisibility(View.INVISIBLE);
                rvAdd.setAlpha(0f);

                searchV.setClickable(false);
                searchV.setVisibility(View.VISIBLE);
                searchV.setAlpha(1f);

                add_new_char.setVisibility(View.INVISIBLE);
                add_new_char.startAnimation(personal_close_anim);
                add_new_char.postDelayed(() -> add_new_char.clearAnimation(), 310);


                searchAdd.setClickable(true);
                //searchAdd.setVisibility(View.INVISIBLE);
                //searchAdd.setAlpha(0f);


                isOpenSearch = false;
            } else {

                searchAdd.setClickable(false);
                searchAdd.setVisibility(View.VISIBLE);
                searchAdd.setAlpha(1f);

                rvAdd.setClickable(false);
                rvAdd.setVisibility(View.VISIBLE);
                rvAdd.setAlpha(1f);

                rv.setClickable(true);
                rv.setVisibility(View.INVISIBLE);
                rv.setAlpha(0.1f);
                searchV.setClickable(true);
                searchV.setVisibility(View.INVISIBLE);
                searchV.setAlpha(0.1f);

                add_new_char.setVisibility(View.VISIBLE);
                add_new_char.startAnimation(personal_open_anim);
                add_new_char.postDelayed(() -> add_new_char.clearAnimation(), 310);

                isOpenSearch = true;
                if (isOpenProfile) {
                    persona_layout.setVisibility(View.INVISIBLE);
                    profile_pic.setVisibility(View.INVISIBLE);
                    persona_layout.startAnimation(personal_close_anim);
                    profile_pic.startAnimation(personal_close_anim);
                    persona_layout.postDelayed(() -> persona_layout.clearAnimation(), 310);
                    profile_pic.postDelayed(() -> profile_pic.clearAnimation(), 310);
                    isOpenProfile = false;
                }
                //functionallity on open to search new person


                newSearchListOnSearch = new ArrayList<>();
                searchAdapter = new Adapter(this, newSearchListOnSearch);
                rvAdd.setAdapter(searchAdapter);
                rvAdd.setLayoutManager(new LinearLayoutManager(this));
                rvAdd.addItemDecoration(new VerticalSpaceItemDecoration(20));

                searchAdd.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(final String query) {
                        if (query.equals("")) {
                            newSearchListOnSearch.clear();
                            searchAdapter.notifyDataSetChanged();
                        } else {
                            newSearchListOnSearch.clear();
                            showFoundUsers(query);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                SwipeHelperAdd SwipeHelperAdd = new SwipeHelperAdd(this, rvAdd, newSearchListOnSearch, searchList) {
                    @Override
                    public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                    }
                };

            }

        });
//fab actions


        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference().child("unread").child(user.getUid());
        unreadRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Unread un = dataSnapshot.getValue(Unread.class);
                if (un != null && un.getCount() > 0) {
                    boolean isFriend = false;
                    Profile finalValue = null;
                    for (Iterator<Profile> iterator = searchList.iterator(); iterator.hasNext(); ) {
                        Profile value = iterator.next();
                        if (value.userID.equals(un.getSenderId())) {
                            iterator.remove();
                            finalValue = value;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    if (finalValue != null) {
                        searchList.add(0, finalValue);
                        adapter.notifyDataSetChanged();
                    }
                    if (!isFriend) {
                        if (un.getSenderId() != null) {
                            DatabaseReference addRef = FirebaseDatabase.getInstance().getReference().child("users").child(un.getSenderId());
                            addRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Profile p = dataSnapshot.getValue(Profile.class);
                                    if (p != null) {
                                        p.setLamp(1);
                                    }
                                    for (Profile prIter : searchList) {
                                        if (p != null && prIter.getUserID().equals(p.getUserID())) {
                                            searchList.remove(prIter);
                                            break;
                                        }
                                    }
                                    searchList.add(0, p);
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }

                        DatabaseReference addRefToBase = FirebaseDatabase.getInstance().getReference().child("friendlist").child(user.getUid());
                        addRefToBase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                frList = dataSnapshot.getValue(FriendList.class);
                                if (frList != null) {
                                    frList.addFriend(un.getSenderId());
                                }
                                addRefToBase.setValue(frList);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Unread un = dataSnapshot.getValue(Unread.class);
                if (un != null && un.getCount() > 0) {
                    boolean isFriend = false;
                    Profile finalValue = null;
                    for (Iterator<Profile> iterator = searchList.iterator(); iterator.hasNext(); ) {
                        Profile value = iterator.next();
                        if (value.userID.equals(un.getSenderId())) {
                            iterator.remove();
                            finalValue = value;
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    if (finalValue != null) {
                        searchList.add(0, finalValue);
                        adapter.notifyDataSetChanged();
                        NotificationShow(finalValue.getUsername(), un.getSenderId());
                    }
                    if (!isFriend) {
                        if (un.getSenderId() != null) {
                            DatabaseReference addRef = FirebaseDatabase.getInstance().getReference().child("users").child(un.getSenderId());
                            addRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Profile p = dataSnapshot.getValue(Profile.class);
                                    if (p != null) {
                                        p.setLamp(1);
                                    }
                                    for (Profile prIter : searchList) {
                                        if (p != null && prIter.getUserID().equals(p.getUserID())) {
                                            searchList.remove(prIter);
                                            break;
                                        }
                                    }
                                    searchList.add(0, p);
                                    adapter.notifyDataSetChanged();
                                    if (p != null) {
                                        NotificationShow(p.getUsername(), un.getSenderId());
                                    }
                                    //cancelNotification(user.getUid());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }

                        DatabaseReference addRefToBase = FirebaseDatabase.getInstance().getReference().child("friendlist").child(user.getUid());
                        addRefToBase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                frList = dataSnapshot.getValue(FriendList.class);

                                if (frList != null) {
                                    frList.addFriend(un.getSenderId());
                                }
                                addRefToBase.setValue(frList);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
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

    private void switchMethod() {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch s = findViewById(R.id.switchTheme);
        SharedPreferences mPrefs = getSharedPreferences("THEME", 0);
        boolean theme_boolean = mPrefs.getBoolean("theme_boolean", true);
        s.setChecked(theme_boolean);

        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor mEditor = mPrefs.edit();
            if (isChecked) {
                mEditor.putBoolean("theme_boolean", true).apply();
                setTheme(R.style.Theme2);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            } else {
                mEditor.putBoolean("theme_boolean", false).apply();
                setContentView(R.layout.search_chat);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    private void changeThemeStyle() {
        SharedPreferences mPrefs = getSharedPreferences("THEME", 0);
        theme_boolean = mPrefs.getBoolean("theme_boolean", true);
        if (theme_boolean) {
            //night
            setTheme(R.style.Theme2);
        } else {
            //day
            setTheme(R.style.Theme1);
        }

    }


    private void readProfileInformation() {

        DatabaseReference upgradeProf = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        upgradeProf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile prof = dataSnapshot.getValue(Profile.class);
                EditText username_editText = findViewById(R.id.editTextTextPersonName);
                username_editText.setText(prof.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadProfileImage() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String image_path = getApplicationContext().getFilesDir() + File.separator + getString(R.string.app_name) + File.separator + "images" + File.separator + "profile_image";
        File f = new File(image_path);
        if (f.exists()) {
            Bitmap bitmap_image = BitmapFactory.decodeFile(f.getPath());
            profile_pic.setImageBitmap(bitmap_image);
        } else {
            storageRef.child("images/" + user.getUid()).getDownloadUrl().addOnSuccessListener(uri ->
                    Picasso.get().load(uri).into(profile_pic)).addOnFailureListener(exception -> {
            });
        }
    }

    private void acceptButton() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        DatabaseReference upgradeProf = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        upgradeProf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile prof = dataSnapshot.getValue(Profile.class);
                if (selectedImage != null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference().child("images").child(user.getUid());
                    storageRef.putFile(selectedImage)
                            .addOnSuccessListener(taskSnapshot -> {
                                //progressDialog.dismiss();
                                Toast.makeText(SearchChat.this, "Updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                //progressDialog.dismiss();
                                Toast.makeText(SearchChat.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            });
                }
                if (prof != null) {
                    EditText username_edittext = findViewById(R.id.editTextTextPersonName);
                    String username_text = String.valueOf(username_edittext.getText());
                    prof.setUsername(username_text);
                    prof.setPicture_image(user.getUid());
                }
                upgradeProf.setValue(prof);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    private void onCHangeProfilePhoto() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            selectedImage = data.getData();
            try {
                bitmap_prof_pic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //get the size of image
                if (bitmap_prof_pic.getByteCount() > 40_000_000) {
                    Toast.makeText(SearchChat.this, "Image is way too big. Sorry! ", Toast.LENGTH_SHORT).show();
                    selectedImage = null;
                    bitmap_prof_pic = null;
                } else {
                    profile_pic.setImageBitmap(bitmap_prof_pic);
                    String image_path = getApplicationContext().getFilesDir() + File.separator + getString(R.string.app_name) + File.separator + "images" + File.separator;
                    File dirs = new File(image_path);
                    if (!dirs.exists()) {
                        dirs.mkdirs();
                    }
                    File f = new File(image_path + "profile_image");
                    f.createNewFile();
                    OutputStream os;
                    os = new FileOutputStream(f);
                    bitmap_prof_pic.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void deleteUserFromSearchList(String friendID) {

        for (Iterator<Profile> iterator = searchList.iterator(); iterator.hasNext(); ) {
            Profile value = iterator.next();
            if (value.userID.equals(friendID)) {
                iterator.remove();
                adapter.notifyDataSetChanged();
            }
        }

        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference().child("friendlist").child(user.getUid());
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                frList = dataSnapshot.getValue(FriendList.class);

                if (frList != null) {
                    frList.deleteFriend(friendID);
                }
                friendRef.setValue(frList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        DatabaseReference unreadRef = FirebaseDatabase.getInstance().getReference().child("unread").child(user.getUid()).child(friendID);
        unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    public void closingFloatButtons() {

        fab2_share.startAnimation(fab_close);
        fab1_mail.startAnimation(fab_close);
        fab_signout.startAnimation(fab_close);
        fab_main.startAnimation(fab_anticlock);

        fab2_share.setClickable(false);
        fab1_mail.setClickable(false);
        fab_signout.setClickable(false);
        fab2_share.postDelayed(() -> fab2_share.clearAnimation(), 310);
        fab1_mail.postDelayed(() -> fab1_mail.clearAnimation(), 310);
        fab_signout.postDelayed(() -> fab_signout.clearAnimation(), 310);
        fab_main.postDelayed(() -> fab_main.clearAnimation(), 310);


        searchAdd.setQuery("", false);
        searchAdd.clearFocus();

        isOpen = false;

        //other floating buttons

        if (isOpenProfile) {
            rv.setClickable(false);
            rv.setVisibility(View.VISIBLE);
            rv.setAlpha(1f);
            searchV.setClickable(false);
            searchV.setVisibility(View.VISIBLE);
            searchV.setAlpha(1f);
            persona_layout.setVisibility(View.INVISIBLE);
            profile_pic.setVisibility(View.INVISIBLE);
            persona_layout.startAnimation(personal_close_anim);
            profile_pic.startAnimation(personal_close_anim);
            persona_layout.postDelayed(() -> persona_layout.clearAnimation(), 310);
            profile_pic.postDelayed(() -> profile_pic.clearAnimation(), 310);
            isOpenProfile = false;
        }

        if (isOpenSearch) {
            rv.setClickable(false);
            rv.setVisibility(View.VISIBLE);
            rv.setAlpha(1f);
            searchV.setClickable(false);
            searchV.setVisibility(View.VISIBLE);
            searchV.setAlpha(1f);
            add_new_char.setVisibility(View.INVISIBLE);
            add_new_char.startAnimation(personal_close_anim);
            add_new_char.postDelayed(() -> add_new_char.clearAnimation(), 310);
            isOpenSearch = false;
        }
        //other floating buttons

    }

    public void addNewInFriendList(String userID) {

        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference().child("friendlist").child(user.getUid());
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                frList = dataSnapshot.getValue(FriendList.class);

                if (frList != null) {
                    frList.addFriend(userID);
                }
                friendRef.setValue(frList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addUserToSearch(String user_id) {
        DatabaseReference addRef = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);

        for (Profile p : searchList) {
            if (p.getUserID().equals(user_id)) {
                return;
            }
        }

        addRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUserSearchList() {

        DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference().child("friendlist").child(user.getUid());
        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                frList = dataSnapshot.getValue(FriendList.class);
                ArrayList<String> readTemp;
                if (frList != null && frList.getFriends() != null) {
                    readTemp = frList.getFriends();
                    for (String s : readTemp) {
                        DatabaseReference searchRef = FirebaseDatabase.getInstance().getReference().child("users").child(s);
                        searchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                searchList.add(dataSnapshot.getValue(Profile.class));
                                adapter.notifyDataSetChanged();


                                DatabaseReference unreadList = FirebaseDatabase.getInstance().getReference().child("unread").child(user.getUid());
                                unreadList.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        List<Unread> uList = new ArrayList<>();
                                        for (DataSnapshot childData : dataSnapshot.getChildren()) {
                                            Unread u = childData.getValue(Unread.class);
                                            uList.add(u);
                                            if (u != null && u.getCount() > 0) {

                                                for (Profile p : searchList) {
                                                    if (p.getUserID().equals(u.getSenderId())) {
                                                        p.setLamp(1);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }


                                        Collections.sort(uList, new UnreadComparator());
                                        ArrayList<Profile> arrP = new ArrayList<>();
                                        for (Unread u : uList) {
                                            for (Iterator<Profile> iterator = searchList.iterator(); iterator.hasNext(); ) {
                                                Profile value = iterator.next();
                                                if (value.userID.equals(u.getSenderId())) {
                                                    iterator.remove();
                                                    arrP.add(value);
                                                    break;
                                                }
                                            }
                                        }
                                        for (Profile prof : arrP) {
                                            searchList.add(0, prof);
                                        }
                                        adapter.notifyDataSetChanged();
                                        mList = new ArrayList<>(searchList);

                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void showFoundUsers(String query) {
        //Start loading
        DatabaseReference searchingRef = FirebaseDatabase.getInstance().getReference().child("users");
        searchingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Profile p = singleSnapshot.getValue(Profile.class);
                    if (p != null) {
                        if (p.getEmail() != null && p.getEmail().contains(query)) {
                            newSearchListOnSearch.add(p);
                        } else if (p.getUsername() != null && p.getUsername().contains(query)) {
                            newSearchListOnSearch.add(p);
                        }
                    }
                }
                searchAdapter.notifyDataSetChanged();
                //End loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
        //End loading
    }


    public void NotificationShow(String title, String userID) {

        int NOTIFICATION_ID = 234;
        notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "my_channel_01";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.white_search_icon)
                .setContentTitle(title)
                .setContentText("New audio message");

        Intent resultIntent = new Intent(getApplicationContext(), SearchChat.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(userID, NOTIFICATION_ID, builder.build());


    }


    private void signOut() {

        FirebaseAuth firebaseAuth;
        FirebaseAuth.AuthStateListener authStateListener = firebaseAuth1 -> {
            if (firebaseAuth1.getCurrentUser() == null) {
                //Do anything here which needs to be done after signout is complete
                signOutCompleteFirebase();
            }
        };
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);
        firebaseAuth.signOut();

    }

    private void signOutCompleteFirebase() {
        Intent intent = new Intent(SearchChat.this, MainActivity.class);
        intent.putExtra("from", "SearchChat");
        startActivity(intent);

        finish();
    }


}