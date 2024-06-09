package com.example.moviegram.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviegram.Objects.Movie;
import com.example.moviegram.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MovieSectionedCastAdapter extends RecyclerView.Adapter<MovieSectionedCastAdapter.SectionViewHolder> {
    private Map<String, List<Movie>> castMoviesMap;
    private List<String> castNames;
    private Context context;

    public MovieSectionedCastAdapter(Context context, Map<String, List<Movie>> castMoviesMap) {
        this.context = context;
        this.castMoviesMap = castMoviesMap;
        this.castNames = new ArrayList<>(castMoviesMap.keySet());
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_category, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        String castName = castNames.get(position);
        List<Movie> movies = castMoviesMap.get(castName);

        holder.categoryTitle.setText(castName);

        Log.d("BIND_VIEW_HOLDER", "Cast: " + castName + " Movies: " + movies.size());


        MovieItemAdapter movieAdapter = new MovieItemAdapter(movies, context);
        holder.movieRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.movieRecyclerView.setAdapter(movieAdapter);

        movieAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return castNames.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView movieRecyclerView;

        SectionViewHolder(View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            movieRecyclerView = itemView.findViewById(R.id.movieRecyclerView);
        }
    }
}