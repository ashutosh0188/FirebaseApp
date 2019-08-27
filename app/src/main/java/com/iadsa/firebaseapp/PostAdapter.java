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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private static final String TAG = PostAdapter.class.getSimpleName();
    private Context context;
    private List<Post> posts;
    private String uid;
    private StorageReference storageReference;
    private PostOperationListener postOperationListener;

    public PostAdapter(Context context, List<Post> posts, String uid, StorageReference storageReference) {
        this.context = context;
        this.posts = posts;
        this.uid = uid;
        this.storageReference = storageReference;
        this.postOperationListener = (PostActivity) context;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView etTitle;
        private TextView etBody;
        private ImageView ivPostImage;
        private CardView cvPostItem;

        public PostViewHolder(View view) {
            super(view);
            etTitle = (TextView) view.findViewById(R.id.etTitle);
            etBody = (TextView) view.findViewById(R.id.etBody);
            ivPostImage = (ImageView) view.findViewById(R.id.ivProfile);
            cvPostItem = (CardView) view.findViewById(R.id.cvPostItem);
            ivPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Uri url = posts.get(getAdapterPosition()).getUrl();
                    if(url!=null) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                        View mView = LayoutInflater.from(context).inflate(R.layout.alert_large_photo_view, null);
                        ImageView photoView = mView.findViewById(R.id.imageView);
                        Picasso.with(context).load(url).into(photoView);
                        mBuilder.setView(mView);
                        AlertDialog mDialog = mBuilder.create();
                        mDialog.show();
                    }

                }
            });
            cvPostItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                    View mView = LayoutInflater.from(context).inflate(R.layout.alert_edit_delete_view, null);
                    Button btnEdit = mView.findViewById(R.id.btnEdit);
                    Button btnDelete = mView.findViewById(R.id.btnDelete);
                    mBuilder.setView(mView);
                    final AlertDialog mDialog = mBuilder.create();
                    mDialog.show();

                    btnEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDialog.dismiss();
                            Utils.showToast("Not impletemented yer", context);
                            postOperationListener.edit(getAdapterPosition(), new Post());
                        }
                    });

                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDialog.dismiss();
                            Utils.showToast("Not impletemented yer", context);
                            postOperationListener.delete(getAdapterPosition(), posts.get(getAdapterPosition()).getPostId());
                        }
                    });
                }
            });
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
        setPicture(post.getPostId(), holder, position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void setPicture(final String postId, final PostAdapter.PostViewHolder holder, final int position) {
        if (storageReference != null) {
            String fileName = storageReference.child(DocumentConstants.IMAGE_POST_PATH).child(uid).child(postId).getPath();
            Log.d(TAG, "fileName:"+fileName);
            storageReference.child(DocumentConstants.IMAGE_POST_PATH).child(uid).child(postId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful() && task.getException()==null && task.getResult()!=null) {
                        Log.d(TAG, "Image retrieval successfully. :" +task.getResult());
                        Picasso.with(context).load(task.getResult()).into(holder.ivPostImage);
                        posts.get(position).setUrl(task.getResult());
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
        //holder.ivPostImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher_round));
    }

    private void editPost() {

    }
}
