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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class AuthenticationActivity extends AppCompatActivity {
    private static final String TAG = AuthenticationActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignup;
    private Button btnSignupPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        init();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        moveToHome(firebaseUser);
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignupPhone = (Button) findViewById(R.id.btnSignupPhone);
    }

    private void initListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singup();
            }
        });

        btnSignupPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singupPhone();
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
        firebaseLogin(email, password);
    }

    private void firebaseLogin(String email, String password) {
        final ProgressDialog progressDialog = Utils.showDialog(AuthenticationActivity.this, "Please wait, verifying credentials", false);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Utils.hideProgressDialog(progressDialog);

                if(task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    Utils.showToast("Login successfully", AuthenticationActivity.this);
                    moveToHome(task.getResult().getUser());
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Utils.showToast("Authentication failed.", AuthenticationActivity.this);
                }
            }
        });
    }

    private void singup() {
        startActivity(new Intent(AuthenticationActivity.this, SignupActivity.class));
        AuthenticationActivity.this.finish();
    }

    private void singupPhone() {
        startActivity(new Intent(AuthenticationActivity.this, SignupPhoneNumberActivity.class));
        AuthenticationActivity.this.finish();
    }

    private void moveToHome(FirebaseUser firebaseUser) {
        if(firebaseUser!=null) {
            startActivity(new Intent(AuthenticationActivity.this, HomeActivity.class));
            AuthenticationActivity.this.finish();
        }
    }
}
