package com.iadsa.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private Button btnLogout;
    private Button btnProfile;
    private Button btnPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnPost = (Button) findViewById(R.id.btnPost);
    }

    private void initListeners() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToProfile();
            }
        });
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToPost();
            }
        });
    }

    private void logout() {
        if(firebaseAuth!=null) {
            firebaseAuth.signOut();
            Utils.showToast("Logging out", HomeActivity.this);
            startActivity(new Intent(HomeActivity.this, AuthenticationActivity.class));
            HomeActivity.this.finish();
        }
    }

    private void moveToProfile() {
        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
    }

    private void moveToPost() {
        startActivity(new Intent(HomeActivity.this, PostActivity.class));
    }
}
