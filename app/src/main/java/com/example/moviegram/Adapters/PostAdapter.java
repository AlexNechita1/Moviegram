package com.example.moviegram.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviegram.Objects.Post;
import com.example.moviegram.R;

import java.util.List;

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
        Log.d("TEXT",text);
        holder.usernameTx.setText(text);
        holder.contentTx.setText(post.getContent());
        holder.titleTx.setText(post.getTitle());
        holder.setImage(post.getAuthorImageUrl(), holder.profileImg, R.drawable.logo, context);
        holder.setImage(post.getImageUrl(), holder.posterImg, R.drawable.big_logo, context);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTx, titleTx, contentTx;
        ImageView profileImg, posterImg;

        PostViewHolder(View itemView) {
            super(itemView);
            usernameTx = itemView.findViewById(R.id.tx_username);
            titleTx = itemView.findViewById(R.id.tx_title);
            contentTx = itemView.findViewById(R.id.tx_content);
            profileImg = itemView.findViewById(R.id.profile_image);
            posterImg = itemView.findViewById(R.id.movie_poster);
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
