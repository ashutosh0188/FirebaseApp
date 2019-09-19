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
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignupPhoneNumberActivity extends AppCompatActivity {

    private static final String TAG = SignupActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private EditText etPhoneNumber;
    private EditText etPassword;
    private Button btnSignup;
    private Button btnVerify;
    private EditText etOtp;
    private LinearLayout llPhone;
    private LinearLayout llOtp;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_phone_number);
        init();
        initListeners();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnVerify = findViewById(R.id.btnVerify);
        etOtp = findViewById(R.id.etOtp);
        llOtp = findViewById(R.id.llOtp);
        llPhone = findViewById(R.id.llPhone);
    }

    private void initListeners() {
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth==null) {
                    Log.d(TAG, "Firebaseauth is null, may be internet connection");
                    Utils.showToast("Cannot create user now", SignupPhoneNumberActivity.this);
                    return;
                }
                validate();
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onVerify pressed:");
                String code = etOtp.getText().toString();
                if(code.isEmpty()) {
                    return;
                }
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                Utils.hideProgressDialog(progressDialog);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request

                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                Utils.hideProgressDialog(progressDialog);
                Utils.showToast("code sent:"+verificationId, SignupPhoneNumberActivity.this);
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                showUIForOtp();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                Log.d(TAG, "could not auto retrieve code:" + s);
                Utils.hideProgressDialog(progressDialog);
            }
        };
    }

    private void showUIForOtp() {
        llPhone.setVisibility(View.GONE);
        llOtp.setVisibility(View.VISIBLE);
    }

    private void hideUIForOtp() {
        llPhone.setVisibility(View.VISIBLE);
        llOtp.setVisibility(View.GONE);
    }

    private void clearUIElements() {
        etPhoneNumber.setText(null);
        etPassword.setText(null);
        etOtp.setText(null);
    }

    private void validate() {
        String phone = etPhoneNumber.getText().toString();
        String password = etPassword.getText().toString();
        if(phone.isEmpty()) {
            etPhoneNumber.setError("Phone number required");
            return;
        }
        else if(password.isEmpty()) {
            etPassword.setError("Password required");
            return;
        }
        else if(password.length()<6) {
            etPassword.setError("Password should be of minimum 6 characters");
            return;
        }
        phone = "+91"+phone;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60, TimeUnit.SECONDS, this, mCallbacks);
        progressDialog = Utils.showDialog(SignupPhoneNumberActivity.this, "Please wait, creating user", false);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog = Utils.showDialog(SignupPhoneNumberActivity.this, "Creating user.. Please wait..", false);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        clearUIElements();
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            moveToHome();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            hideUIForOtp();
                            clearUIElements();
                        }
                    }
                });
    }

    private void moveToHome() {
        startActivity(new Intent(SignupPhoneNumberActivity.this, HomeActivity.class));
        SignupPhoneNumberActivity.this.finish();
    }
}
