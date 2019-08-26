package com.iadsa.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static com.iadsa.firebaseapp.DocumentConstants.DATA_POST_PATH;

public class CreatePostActivity extends AppCompatActivity {
    private static final String TAG = CreatePostActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private EditText etTitle;
    private EditText etBody;
    private Button btnSubmit;
    private Button btnUploadPicture;
    private ImageView ivProfile;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        init();
        initListeners();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        etTitle = (EditText) findViewById(R.id.etTitle);
        etBody = (EditText) findViewById(R.id.etBody);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnUploadPicture = (Button) findViewById(R.id.btnUploadPicture);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
    }

    private void initListeners() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
        //select image from gallery
        btnUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
    }

    private void validate() {
        String title = etTitle.getText().toString();
        String body = etBody.getText().toString();
        if (title.isEmpty()) {
            etTitle.setError("Title required");
            return;
        } else if (body.isEmpty()) {
            etBody.setError("Body required");
            return;
        }
        Post post = new Post();
        post.setTitle(etTitle.getText().toString());
        post.setBody(etBody.getText().toString());
        post.setPostId(String.valueOf(System.currentTimeMillis()));
        createPost(post);
    }

    private void chooseImage() {
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
            setImage(filePath);
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

    private void createPost(Post post) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null && filePath != null) {
            final String uid = firebaseUser.getUid();
            uploadImage(uid, post);
        }
    }

    private void uploadImage(final String uid, final Post post) {
        if (filePath != null && storageReference != null) {
            final ProgressDialog progressDialog = Utils.showDialog(CreatePostActivity.this, "Please wait, creating post...", false);
            storageReference.
                    child(DocumentConstants.IMAGE_POST_PATH)
                    .child(uid)
                    .child(post.getPostId())
                    .putFile(filePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful() && task.getException() == null) {
                                Log.d(TAG, "Post uploaded image uri:");
                                addPost(uid, progressDialog, post);
                            } else {
                                Utils.hideProgressDialog(progressDialog);
                                Utils.showToast("Failed creating post", CreatePostActivity.this);
                                Log.d(TAG, "Post image upload failed:" + task.getException());
                            }
                        }
                    });
        }else {
            final ProgressDialog progressDialog = Utils.showDialog(CreatePostActivity.this, "Please wait, creating post...", false);
            addPost(uid, progressDialog, post);
        }
    }

    private void addPost(final String uid, final ProgressDialog progressDialog, final Post post) {
        if (databaseReference != null) {
            databaseReference
                    .child(DATA_POST_PATH)
                    .child(uid)
                    .child(post.getPostId())
                    .setValue(post, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d(TAG, "Error creating post:" + databaseError.getDetails());
                                Utils.hideProgressDialog(progressDialog);
                            } else {
                                Log.d(TAG, "OnCompleted of updating user successfully:" + uid);
                                Utils.showToast("Post created successfully", CreatePostActivity.this);
                                clearFields();
                                Utils.hideProgressDialog(progressDialog);
                            }
                        }
                    });
        }
    }

    //TODO: not working
    private void uploadImageAndGetDownloadUrl(final String uid, final Post post, final ProgressDialog progressDialog) {
        if (filePath != null && storageReference != null) {
            UploadTask uploadTask = storageReference.child(DocumentConstants.IMAGE_POST_PATH).child(uid).child(post.getPostId()).putFile(filePath);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Utils.hideProgressDialog(progressDialog);
                        Utils.showToast("Failed creating post", CreatePostActivity.this);
                        Log.d(TAG, "Post image upload failed:" + task.getException());
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri taskResult = task.getResult();
                        Log.d(TAG, "Post uploaded image uri:" + taskResult);
                        post.setUrl(taskResult);
                        addPost(uid, progressDialog, post);
                    } else {
                        Utils.hideProgressDialog(progressDialog);
                        Utils.showToast("Failed creating post", CreatePostActivity.this);
                        Log.d(TAG, "Post image upload failed--:" + task.getException());
                    }
                }
            });
        }
    }

    private void clearFields() {
        etTitle.setText(null);
        etBody.setText(null);
        filePath = null;
        ivProfile.setImageResource(0);
    }
}
