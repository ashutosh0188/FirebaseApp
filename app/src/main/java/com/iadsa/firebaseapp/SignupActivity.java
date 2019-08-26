package com.iadsa.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = SignupActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
        initListeners();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnSignup = (Button) findViewById(R.id.btnSignup);
    }

    private void initListeners() {

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth==null) {
                    Log.d(TAG, "Firebaseauth is null, may be internet connection");
                    Utils.showToast("Cannot create user now", SignupActivity.this);
                    return;
                }
                validate();
            }
        });
    }

    private void validate() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if(email.isEmpty()) {
            etEmail.setError("Email required");
            return;
        }
        else if(password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }
        firebaseCreateUser(email, password);
    }

    private void firebaseCreateUser(String email, String password) {
        final ProgressDialog progressDialog = Utils.showDialog(SignupActivity.this, "Please wait, creating user", false);
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()) {
                    Log.d(TAG, "Created user");
                    Utils.showToast("User created successfully", SignupActivity.this);
                    moveToHome();
                }else {
                    Log.d(TAG, "Created user failed"+task.getException() + " & "+task);
                    Utils.showToast("User creation failed", SignupActivity.this);
                }
            }
        });
    }

    private void moveToHome() {
        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
        SignupActivity.this.finish();
    }
}
