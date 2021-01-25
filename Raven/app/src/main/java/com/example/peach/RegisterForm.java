package com.example.peach;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterForm extends AppCompatActivity {

    int onStartCount = 0;
    public LinearLayout ll = null;
    Button registerBtn = null;
    EditText name = null;
    EditText email = null;
    EditText pass = null;
    EditText pass2 = null;
    TextView dialogMessage = null;
    private FirebaseAuth mAuth;
    public static final String TAG = "myLogs";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);

        /*FrameLayout frameGif = (FrameLayout)findViewById(R.id.loadingFrameGif);
        frameGif.setZ(-1);
        ImageView imageView = findViewById(R.id.imageGif);
        imageView.setClickable(false);
        Glide.with(this).load(R.raw.star).into(imageView); */

        Objects.requireNonNull(getSupportActionBar()).hide();
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        onStartCount = 1;
        if (savedInstanceState == null) // 1st time
        {
            this.overridePendingTransition(R.anim.slide_left,
                    R.anim.anim_slide_out_left);
        } else // already created so reverse animation
        {
            onStartCount = 2;
        }

        //progressBar = (RelativeLayout) findViewById(R.id.loadingPanel);
        //progressBar.setVisibility(View.INVISIBLE);

        ll = (LinearLayout) findViewById(R.id.linear_register);

        ll.setOnTouchListener(new OnSwipeTouchListener(RegisterForm.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(RegisterForm.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        registerBtn = (Button) findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(v -> {
            dialogMessage.setText("");
            //progressBar.setVisibility(View.VISIBLE);
            if(!filedsEmpty()){
                return;
            }

            makeRegister();
            //progressBar.setVisibility(View.INVISIBLE);
        });

        name = (EditText) findViewById(R.id.et_name);
        email = (EditText) findViewById(R.id.et_email);
        pass = (EditText) findViewById(R.id.et_password);
        pass2 = (EditText) findViewById(R.id.et_repassword);
        dialogMessage = (TextView) findViewById(R.id.tv_dialog);
        name.setAlpha((float) 0.8);
        pass.setAlpha((float) 0.8);
        pass2.setAlpha((float) 0.8);
        email.setAlpha((float) 0.8);
        registerBtn.setAlpha((float) 0.8);

        mAuth = FirebaseAuth.getInstance();



    }

    @SuppressLint("SetTextI18n")
    private boolean filedsEmpty() {

        if(name.getText().toString().matches("")){
            dialogMessage.setText("Name is empty");
            return false;
        }
        if(email.getText().toString().matches("")){
            dialogMessage.setText("Email is empty");
            return false;
        }
        if(pass.getText().toString().matches("")){
            dialogMessage.setText("Password is empty");
            return false;
        }
        if(pass2.getText().toString().matches("")){
            dialogMessage.setText("Verification password is empty");
            return false;
        }

        return true;
    }

    private void updateUI(FirebaseUser user) {
        //Intent intent = new Intent(RegisterForm.this, MainActivity.class);
        //startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void makeRegister() {
        if(!checkEmail()){
            dialogMessage.setText("Email is not valid!");
            return;
        }
        if(!isPasswordMatch()){
            dialogMessage.setText("Password does not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(String.valueOf(name.getText()))
                                .build();
                        user.updateProfile(profileUpdates);
                        user.sendEmailVerification();

                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        //dialogMessage.setText("Authentication failed");
                        Toast.makeText(RegisterForm.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        dialogMessage.setText( task.getException().getMessage());
                    }

                    // ...
                });


    }

    private boolean isPasswordMatch() {
        return pass.getText().toString().equals(pass2.getText().toString());
    }

    private boolean checkEmail() {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email.getText().toString());
        return matcher.matches();
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

}
