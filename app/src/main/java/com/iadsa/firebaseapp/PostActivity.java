package com.iadsa.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements PostOperationListener {
    private static final String TAG = PostActivity.class.getSimpleName();
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private RecyclerView rvPost;
    private FloatingActionButton fab;
    private ConstraintLayout layoutEmptyView;
    private PostAdapter postAdapter;

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
        rvPost = findViewById(R.id.rvPost);
        fab = findViewById(R.id.fab);
        LinearLayoutManager layoutManager = new LinearLayoutManager(PostActivity.this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvPost.setLayoutManager(layoutManager);
        layoutEmptyView = findViewById(R.id.layoutEmptyView);
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider);
        rvPost.addItemDecoration(new DividerItemDecoration(dividerDrawable));
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
                                    if(posts.size()>0) {
                                        layoutEmptyView.setVisibility(View.GONE);
                                        setUpData(posts);
                                    }else {
                                        layoutEmptyView.setVisibility(View.VISIBLE);
                                    }
                                }else {
                                    layoutEmptyView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Utils.hideProgressDialog(progressDialog);
                                Utils.showToast("Could not fetched posts", PostActivity.this);
                                Log.e(TAG, "error fetching posts:"+databaseError.getDetails());
                                layoutEmptyView.setVisibility(View.VISIBLE);
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
        postAdapter = new PostAdapter(PostActivity.this, posts, firebaseAuth.getUid(), storageReference);
        rvPost.setAdapter(postAdapter);
    }

    @Override
    public void delete(final int position, final String postId) {
        if(databaseReference!=null && firebaseAuth!=null) {
            if (firebaseAuth.getCurrentUser() != null) {
                final String uid = firebaseAuth.getCurrentUser().getUid();
                final ProgressDialog progressDialog = Utils.showDialog(PostActivity.this, "Please wait, deleting post...", false);
                databaseReference.child(DocumentConstants.DATA_POST_PATH).child(uid).child(postId).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Utils.hideProgressDialog(progressDialog);
                        if(databaseError==null) {
                            Log.d(TAG, "post deleted sucessfully:"+postId);
                            postAdapter.notifyDataSetChanged();
                            deleteImageFromFirebaseStorage(uid, postId);
                        }else {
                            Log.d(TAG, "post deletion failed:"+postId);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void edit(int position, Post nPost) {
        Log.d(TAG, "Edit options not implememted yet");
    }

    /*
     *This should execute in the background, and may be retry should also be implemented, if deletion of image is get failed.
     * Deleton of post image is compulsory, to avoid junk data, when actual post is get deleted from firebase database
     */
    private void deleteImageFromFirebaseStorage(String uid, String postId) {
        if(storageReference!=null) {
            storageReference.child(DocumentConstants.IMAGE_POST_PATH).child(uid).child(postId).delete();
            Log.d(TAG, "post image deleted sucessfully:"+postId);
        }
    }
}
