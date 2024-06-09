package com.example.moviegram.Adapters;

import android.content.Context;
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

public class MovieSectionedCategoryAdapter extends RecyclerView.Adapter<MovieSectionedCategoryAdapter.SectionViewHolder> {
    private Map<String, List<Movie>> categorizedMovies;
    private List<String> categories;
    private Context context;

    public MovieSectionedCategoryAdapter(Context context, Map<String, List<Movie>> categorizedMovies) {
        this.context = context;
        this.categorizedMovies = categorizedMovies;
        this.categories = new ArrayList<>(categorizedMovies.keySet());
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_category, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        String category = categories.get(position);
        List<Movie> movies = categorizedMovies.get(category);

        holder.categoryTitle.setText(category);


        MovieItemAdapter movieAdapter = new MovieItemAdapter(movies,context);
        holder.movieRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.movieRecyclerView.setAdapter(movieAdapter);
    }

    @Override
    public int getItemCount() {
        return categories.size();
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
