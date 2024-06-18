package com.example.moviegram.Adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviegram.Activities.DetailActivity;
import com.example.moviegram.Activities.ProfileActivity;
import com.example.moviegram.Objects.Post;
import com.example.moviegram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        String text = post.getAuthor()+" - "+post.getMovieTitle();
        holder.usernameTx.setText(text);
        holder.contentTx.setText(post.getContent());

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = inputFormat.parse(post.getTimestamp());
            String formattedDate = outputFormat.format(date);
            holder.timestampTx.setText(formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.likesTx.setText(post.getLikes());
        holder.setImage(post.getAuthorImageUrl(), holder.profileImg, R.drawable.logo, context);
        holder.setImage(post.getImageUrl(), holder.posterImg, R.drawable.big_logo, context);

        holder.posterImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ITEM_TITLE",post.getMovieTitle());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);

            }
        });
        holder.profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(!currentUserId.equals(post.getUid())){
                    Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("UID",post.getUid());
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }
            }
        });
        holder.likeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost(post.getPostkey(), holder.likesTx,holder.likeImg);
            }
        });
        //checkIfLiked(post.getPostkey(), holder.likeImg);
    }
    private void checkIfLiked(String postKey, ImageView likeImg) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userLikesRef = firestore.collection("Users").document(currentUserId);

        userLikesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> likedPosts = (List<String>) task.getResult().get("liked_posts");
                if (likedPosts != null && likedPosts.contains(postKey)) {
                    likeImg.setImageResource(R.drawable.red_heart);
                } else {
                    likeImg.setImageResource(R.drawable.white_heart);
                }
            } else {
                Log.e("PostAdapter", "Failed to check liked posts", task.getException());
            }
        });
    }

    private void likePost(String postKey, TextView likesTx, ImageView likeImg) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://moviegram-f31cf-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        DatabaseReference postRef = databaseRef.child("posts").child(postKey);
        DatabaseReference postLikesRef = postRef.child("post_likes").child(currentUserId);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userLikesRef = firestore.collection("Users").document(currentUserId);

        postLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postLikesRef.removeValue();
                    postRef.child("likes").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            String currentLikesStr = mutableData.getValue(String.class);
                            if (currentLikesStr == null) {
                                return Transaction.success(mutableData);
                            }

                            try {
                                int currentLikes = Integer.parseInt(currentLikesStr);
                                mutableData.setValue(String.valueOf(currentLikes - 1));
                            } catch (NumberFormatException e) {
                                Log.e("PostAdapter", "Failed to parse likes count: " + currentLikesStr, e);
                                return Transaction.abort();
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                Log.w("PostAdapter", "Transaction failed.", databaseError.toException());
                            } else {
                                if (dataSnapshot != null) {
                                    String updatedLikesStr = dataSnapshot.getValue(String.class);
                                    if (updatedLikesStr != null) {
                                        likesTx.setText(updatedLikesStr);
                                        likeImg.setImageResource(R.drawable.white_heart);
                                        userLikesRef.update("liked_posts", FieldValue.arrayRemove(postKey))
                                                .addOnFailureListener(e -> Log.e("PostAdapter", "Failed to remove post from liked_posts", e));
                                    }
                                }
                            }
                        }
                    });
                } else {
                    postLikesRef.setValue(true);
                    postRef.child("likes").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            String currentLikesStr = mutableData.getValue(String.class);
                            if (currentLikesStr == null) {
                                return Transaction.success(mutableData);
                            }

                            try {
                                int currentLikes = Integer.parseInt(currentLikesStr);
                                mutableData.setValue(String.valueOf(currentLikes + 1));
                            } catch (NumberFormatException e) {
                                Log.e("PostAdapter", "Failed to parse likes count: " + currentLikesStr, e);
                                return Transaction.abort();
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                Log.w("PostAdapter", "Transaction failed.", databaseError.toException());
                            } else {
                                if (dataSnapshot != null) {
                                    String updatedLikesStr = dataSnapshot.getValue(String.class);
                                    if (updatedLikesStr != null) {
                                        likesTx.setText(updatedLikesStr);
                                        likeImg.setImageResource(R.drawable.red_heart);
                                        userLikesRef.update("liked_posts", FieldValue.arrayUnion(postKey))
                                                .addOnFailureListener(e -> Log.e("PostAdapter", "Failed to add post to liked_posts", e));
                                    }
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("PostAdapter", "Error checking if user liked post.", databaseError.toException());
            }
        });
    }




    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTx, timestampTx, contentTx,likesTx;
        ImageView profileImg, posterImg,likeImg;

        PostViewHolder(View itemView) {
            super(itemView);
            usernameTx = itemView.findViewById(R.id.tx_username);
            timestampTx = itemView.findViewById(R.id.tx_timestamp);
            contentTx = itemView.findViewById(R.id.tx_content);
            profileImg = itemView.findViewById(R.id.profile_image);
            posterImg = itemView.findViewById(R.id.movie_poster);
            likesTx = itemView.findViewById(R.id.likes_textview);
            likeImg = itemView.findViewById(R.id.like_button);
        }

        void setImage(String imageUrl, ImageView target, int placeholder, Context context) {
            RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop());
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .apply(requestOptions)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(target);
            }
        }
    }
}
