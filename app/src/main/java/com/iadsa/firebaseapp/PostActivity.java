package com.iadsa.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = PostActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private RecyclerView rvPost;
    private Button fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        init();
        initListeners();
        getData();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        rvPost = (RecyclerView) findViewById(R.id.rvPost);
        fab = (Button) findViewById(R.id.fab);
        LinearLayoutManager layoutManager = new LinearLayoutManager(PostActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvPost.setLayoutManager(layoutManager);
    }

    private void initListeners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, CreatePostActivity.class));
            }
        });
    }

    private void getData() {
        if(databaseReference!=null && firebaseAuth!=null) {
            if(firebaseAuth.getCurrentUser()!=null) {
                final ProgressDialog progressDialog = Utils.showDialog(PostActivity.this, "Please wait, fetching posts...", false);
                databaseReference
                        .child(DocumentConstants.DATA_POST_PATH)
                        .child(firebaseAuth.getCurrentUser().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Utils.hideProgressDialog(progressDialog);
                                Map<String, Object> objectMap = (Map<String,Object>) dataSnapshot.getValue();
                                Log.d(TAG, "data:"+objectMap);
                                if(objectMap!=null) {
                                    List<Post> posts = new ArrayList<>();
                                    for (Object obj : objectMap.values()) {
                                        if (obj instanceof Map) {
                                            Map<String, Object> mapObj = (Map<String, Object>) obj;
                                            Post post = new Post();
                                            post.setPostId((String) mapObj.get(Post.POST_ID));
                                            post.setTitle((String) mapObj.get(Post.TITLE));
                                            post.setBody((String) mapObj.get(Post.BODY));
                                            posts.add(post);
                                        }
                                    }
                                    setUpData(posts);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Utils.hideProgressDialog(progressDialog);
                                Utils.showToast("Could not fetched posts", PostActivity.this);
                                Log.e(TAG, "error fetching posts:"+databaseError.getDetails());
                            }
                        });
            } else {
                Log.d(TAG, "FirebaseCurrent user is null");
            }
        } else {
            Log.d(TAG, "DatabaseRefrence or FirebaseAuth is null");
        }
    }

    private void setUpData(List<Post> posts) {
        rvPost.setAdapter(new PostAdapter(PostActivity.this, posts, firebaseAuth.getUid(), storageReference));
    }

}
