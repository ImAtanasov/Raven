package com.example.peach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.Objects;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 0;
    public static final String TAG = "myLogs";
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;
    RelativeLayout progressBar = null;
    Button loginBtn = null;
    RelativeLayout mainRelative = null;
    EditText email = null;
    EditText password = null;
    TextView dialogMessage = null;
    private FirebaseAuth mAuth;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //Toast.makeText(MainActivity.this, "xxxxxxxx ", Toast.LENGTH_SHORT).show();

        Intent intent = getIntent();
        if (intent.getStringExtra("from") == null) {
            this.overridePendingTransition(R.anim.slide_right,
                    R.anim.anim_slide_out_right);
        } else {
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        }


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_RECORD_AUDIO = 1;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);

            //Toast.makeText(MainActivity.this, "Start recording 1111", Toast.LENGTH_SHORT).show();
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }

        /*FrameLayout frameGif = (FrameLayout) findViewById(R.id.loadingFrameGif);
        frameGif.setZ(-1);
        ImageView imageView = findViewById(R.id.imageGif);
        imageView.setClickable(false);
        Glide.with(this).load(R.raw.star).into(imageView); */

        Objects.requireNonNull(getSupportActionBar()).hide();

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        progressBar = (RelativeLayout) findViewById(R.id.loadingPanel);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setZ(5);

        email = (EditText) findViewById(R.id.et_email);
        password = (EditText) findViewById(R.id.et_password);
        email.setAlpha((float) 0.8);
        password.setAlpha((float) 0.8);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setAlpha((float) 0.8);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        loginBtn = (Button) findViewById(R.id.btn_login);
        loginBtn.setAlpha((float) 0.8);
        mainRelative = (RelativeLayout) findViewById(R.id.main_relative);
        dialogMessage = (TextView) findViewById(R.id.tv_dialog);

        mainRelative.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, RegisterForm.class);
                startActivity(intent);
                //finish();
            }
        });


        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {
            dialogMessage.setText("");
            login();
        });

        setGooglePlusButtonText(signInButton);


    }


    @SuppressLint("SetTextI18n")
    private void login() {
        FirebaseUser user = mAuth.getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        updateUICustom(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        dialogMessage.setText("Authentication failed");
                        progressBar.setVisibility(View.INVISIBLE);
                        //updateUICustom(null);
                        // ...
                    }

                    // ...
                });

    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUIalready(currentUser);
        }


    }

    private void updateUICustom(FirebaseUser account) {

        Intent intent = new Intent(MainActivity.this, SearchChat.class);
        intent.putExtra("from", "MainActivity");
        startActivity(intent);
        progressBar.setVisibility(View.INVISIBLE);
        addOrUpdateUser();
        finish();

    }

    private void updateUIalready(FirebaseUser account) {

        Intent intent = new Intent(MainActivity.this, SearchChat.class);
        startActivity(intent);
        intent.putExtra("from", "MainActivity");
        progressBar.setVisibility(View.INVISIBLE);
        addOrUpdateUser();
        finish();
    }

    private void updateUI(GoogleSignInAccount account) {

        Intent intent = new Intent(MainActivity.this, SearchChat.class);
        startActivity(intent);
        intent.putExtra("from", "MainActivity");
        progressBar.setVisibility(View.INVISIBLE);
        addOrUpdateUser();
        finish();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            signIn();
            // ...
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            if (account != null) {
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }

            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @SuppressLint("SetTextI18n")
    protected void setGooglePlusButtonText(SignInButton signInButton) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Sign in with Google");
                return;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUICustom(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        dialogMessage.setText("Authentication Failed.");
                        updateUI(null);
                    }

                    // ...
                });
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public void addOrUpdateUser() {

        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference uidRef = mDatabase.child("users").child(Objects.requireNonNull(user).getUid());
        DatabaseReference frRef = mDatabase.child("friendlist").child(user.getUid());

        //Users
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //create new user
                    String locale = getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry();
                    long timestamp = new Date().getTime();
                    Profile p = new Profile(user.getUid(), user.getDisplayName(), user.getEmail(), timestamp, locale);
                    mDatabase.child("users").child(user.getUid()).setValue(p);

                } else {
                    String locale = getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry();
                    long timestamp = new Date().getTime();
                    mDatabase.child("users").child(user.getUid()).child("last_login").setValue(timestamp);
                    mDatabase.child("users").child(user.getUid()).child("fromCountry").setValue(locale);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        uidRef.addListenerForSingleValueEvent(eventListener);

        //Friendlist
        ValueEventListener eventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    FriendList frlist = new FriendList(user.getUid());
                    mDatabase.child("friendlist").child(user.getUid()).setValue(frlist);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        frRef.addListenerForSingleValueEvent(eventListener2);


    }


}