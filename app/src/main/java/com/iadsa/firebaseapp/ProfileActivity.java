package com.iadsa.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;


public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private EditText etName;
    private EditText etMobile;
    private EditText etAge;
    private Button btnSubmit;
    private Button btnUploadPicture;
    private ImageView ivProfile;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 1001;
    private boolean isChoosingImage; //to avoid re-calling setUpData() method, once user chooses to choose picture

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
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);
        ivProfile = findViewById(R.id.ivProfile);
    }

    private void initListeners() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
        //choose picture from gallery
        btnUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void validate() {
        String name = etName.getText().toString();
        String mobile = etMobile.getText().toString();
        String age = etAge.getText().toString();
        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        } else if (mobile.isEmpty()) {
            etMobile.setError("Mobile required");
            return;
        } else if (age.isEmpty()) {
            etAge.setError("Age required");
            return;
        } else if (age.length() > 3) {
            etAge.setError("Hey champ! Don't live more than 999 years ;)");
            return;
        }
        User user = new User();
        user.setName(etName.getText().toString());
        user.setMobile(etMobile.getText().toString());
        user.setAge(Integer.parseInt(etAge.getText().toString()));
        createUser(user);
    }

    private void setUpData() {
        if(isChoosingImage) {
            return;
        }
        if (databaseReference != null && firebaseAuth != null) {
            final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                final ProgressDialog progressDialog = Utils.showDialog(ProfileActivity.this, "Please wait, fetching user details", false);
                Log.d(TAG, firebaseUser.getUid());
                // Attach a listener to read the data at our user uid
                databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Utils.hideProgressDialog(progressDialog);
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            Log.d(TAG, user.toString());
                            etName.setText(user.getName());
                            etMobile.setText(user.getMobile());
                            etAge.setText(String.valueOf(user.getAge()));
                            getPicture(firebaseUser.getUid(), progressDialog);
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
    private void createUser(final User user) {
        final ProgressDialog progressDialog = Utils.showDialog(ProfileActivity.this, "Please wait, updating user details", false);
        if (databaseReference != null && firebaseAuth != null) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                final String uid = firebaseUser.getUid();
                databaseReference.child(DocumentConstants.DATA_USER_PATH).child(uid).setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Error updating user:" + databaseError.getDetails());
                            Utils.hideProgressDialog(progressDialog);
                        } else {
                            Log.d(TAG, "OnCompleted of updating user successfully:" + uid);
                            uploadImage(uid, progressDialog);
                        }
                    }
                });
            }
        }
    }

    private void chooseImage() {
        isChoosingImage = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            isChoosingImage = false;
            setImage(filePath);
        }
    }

    private void uploadImage(String uid, final ProgressDialog progressDialog) {
        if (filePath != null && storageReference != null) {
            storageReference.
                    child(DocumentConstants.IMAGE_PROFILE_PATH).
                    child(uid).
                    putFile(filePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            Utils.showToast("User profile updated successfully", ProfileActivity.this);
                            Utils.hideProgressDialog(progressDialog);
                        }
                    });
        }
    }

    private void getPicture(String uid, final ProgressDialog progressDialog) {
        if (storageReference != null) {
            String fileName = storageReference.child(DocumentConstants.IMAGE_PROFILE_PATH).child(uid).getName();
            Log.d(TAG, "fileName:"+fileName);
            storageReference.child(DocumentConstants.IMAGE_PROFILE_PATH).child(uid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Log.d(TAG, "Image retrieval successfully.");
                    Utils.hideProgressDialog(progressDialog);
                    if(task.isSuccessful() && task.getException()==null) {
                        Picasso.with(ProfileActivity.this).load(task.getResult()).into(ivProfile);
                    }
                }
            });
        }
    }

    private void setImage(Uri filePath) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            ivProfile.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
