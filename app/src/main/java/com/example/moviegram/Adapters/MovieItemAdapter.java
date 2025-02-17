package com.example.moviegram.Adapters;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviegram.Activities.BrowseActivity;
import com.example.moviegram.Activities.DetailActivity;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.R;

import java.util.List;

public class MovieItemAdapter extends RecyclerView.Adapter<MovieItemAdapter.MovieViewHolder> {
    private List<Movie> movies;
    private Context context;

    public MovieItemAdapter(List<Movie> movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.movieTitle.setText(movie.getTitle());
        holder.movieMainPlot.setText(movie.getMainPlot());
        holder.movieRating.setText(Float.toString(movie.getAggregateRating()));

        Log.d("BIND_VIEW_HOLDER", "Movie: " + movie.getTitle());

        holder.setImage(movie.getDownlaoadURL());
        holder.moviePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ITEM_TITLE", movie.getTitle());
                bundle.putString("START_LOCATION", "BrowseActivityFilter");
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView movieTitle, movieMainPlot, movieRating;
        ImageView moviePoster;

        MovieViewHolder(View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.movie_title);
            movieMainPlot = itemView.findViewById(R.id.movie_main_plot);
            movieRating = itemView.findViewById(R.id.movie_rating);
            moviePoster = itemView.findViewById(R.id.movie_poster);
        }

        void setImage(String imageUrl) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(60));

            Glide.with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .placeholder(R.drawable.big_logo)
                    .error(R.drawable.login_bk)
                    .into(moviePoster);
        }
    }
}
