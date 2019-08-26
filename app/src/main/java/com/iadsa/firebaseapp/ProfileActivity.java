package com.iadsa.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText etName;
    private EditText etMobile;
    private EditText etAge;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        initListeners();
        setUpData();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        etName = (EditText) findViewById(R.id.etName);
        etMobile = (EditText) findViewById(R.id.etMobile);
        etAge = (EditText) findViewById(R.id.etAge);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
    }

    private void initListeners() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCreateUser();
            }
        });
    }

    private void setUpData() {
        final ProgressDialog progressDialog = Utils.showDialog(ProfileActivity.this, "Please wait, fetching user details", false);
        if(databaseReference!=null && firebaseAuth!=null) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser!=null) {
                Log.d(TAG, firebaseUser.getUid());
                // Attach a listener to read the data at our user uid
                databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       Utils.hideProgressDialog(progressDialog);
                        User user = dataSnapshot.getValue(User.class);
                        if(user!=null) {
                            Log.d(TAG, user.toString());
                            etName.setText(user.getName());
                            etMobile.setText(user.getMobile());
                            etAge.setText(user.getAge()+"");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Utils.hideProgressDialog(progressDialog);
                        Log.d(TAG, "The read failed: " + databaseError.getCode());
                    }
                });
            }

        }
    }

    /**
     * This method is calling firebase create user document under users collection.
     * And only logged in user can only modify his/her details.
     * This rule is set under firebase database rules section.
     */
    private void callCreateUser() {
        final ProgressDialog progressDialog = Utils.showDialog(ProfileActivity.this, "Please wait, updating user details", false);
        if(databaseReference!=null && firebaseAuth!=null) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser!=null) {
                final String uid = firebaseUser.getUid();
                User user = new User();
                user.setName(etName.getText().toString());
                user.setMobile(etMobile.getText().toString());
                user.setAge(Integer.parseInt(etAge.getText().toString()));
                databaseReference.child(uid).setValue(user, new DatabaseReference.CompletionListener(){
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Utils.hideProgressDialog(progressDialog);
                        if(databaseError!=null) {
                            Log.d(TAG, "Error updating user:"+databaseError.getDetails());
                        } else {
                            Log.d(TAG, "OnCompleted of updating user successfully:"+uid);
                            Utils.showToast("User profile updated successfully", ProfileActivity.this);
                        }
                    }
                });
            }
        }
    }
}
