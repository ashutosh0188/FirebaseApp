package com.iadsa.firebaseapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.internal.Asserts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = PostAdapter.class.getSimpleName();
    private Context context;
    private List<Post> posts;
    private String uid;
    private StorageReference storageReference;

    public PostAdapter(Context context, List<Post> posts, String uid, StorageReference storageReference) {
        this.context = context;
        this.posts = posts;
        this.uid = uid;
        this.storageReference = storageReference;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView etTitle;
        private TextView etBody;
        private ImageView ivPostImage;

        public PostViewHolder(View view) {
            super(view);
            etTitle = (TextView) view.findViewById(R.id.etTitle);
            etBody = (TextView) view.findViewById(R.id.etBody);
            ivPostImage = (ImageView) view.findViewById(R.id.ivProfile);
        }
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.etTitle.setText(post.getTitle());
        holder.etBody.setText(post.getBody());
        setPicture(post.getPostId(), holder);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void setPicture(final String postId, final PostAdapter.PostViewHolder holder) {
        if (storageReference != null) {
            String fileName = storageReference.child(DocumentConstants.IMAGE_POST_PATH).child(uid).child(postId).getPath();
            Log.d(TAG, "fileName:"+fileName);
            storageReference.child(DocumentConstants.IMAGE_POST_PATH).child(uid).child(postId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful() && task.getException()==null && task.getResult()!=null) {
                        Log.d(TAG, "Image retrieval successfully. :" +task.getResult());
                        Picasso.with(context).load(task.getResult()).into(holder.ivPostImage);
                    } else {
                        onImageLoadFail(holder, postId, "onCompleted");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    onImageLoadFail(holder, postId, "onFailure");
                }
            });
        }
    }

    private void onImageLoadFail(PostAdapter.PostViewHolder holder, String postId, String message) {
        Log.e(TAG, "Url not found for postID:"+postId + " & msg:"+message);
        holder.ivPostImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher_round));
    }
}
