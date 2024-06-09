package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Objects.CastMember;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.Objects.MovieSearchResult;
import com.example.moviegram.R;

import java.util.List;

public class SearchMovieAdapter extends RecyclerView.Adapter<SearchMovieAdapter.SearchMovieAdapterViewHolder> {

    private List<MovieSearchResult> movieList;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(MovieSearchResult movie);
    }

    public SearchMovieAdapter(List<MovieSearchResult> movieList, OnItemClickListener listener) {
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchMovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cast_member, parent, false);
        return new SearchMovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchMovieAdapterViewHolder holder, int position) {
        MovieSearchResult movie = movieList.get(position);
        holder.tvActorName.setText(movie.getTitle());
        Glide.with(holder.itemView.getContext())
                .load(movie.getUrl())
                .placeholder(R.drawable.big_logo)
                .error(R.drawable.login_bk)
                .into(holder.ivActorImage);
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void filterList(List<MovieSearchResult> filteredList) {
        movieList = filteredList;
        notifyDataSetChanged();
    }

    static class SearchMovieAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivActorImage;
        TextView tvActorName;
        LinearLayout linearLayout;



        public SearchMovieAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActorImage = itemView.findViewById(R.id.iv_actor_image);
            tvActorName = itemView.findViewById(R.id.tv_actor_name);
            linearLayout = itemView.findViewById(R.id.linearlayout);

        }
    }
}
