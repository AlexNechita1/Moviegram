package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviegram.R;

import java.util.ArrayList;
import java.util.List;

public class SelectedMoviesAdapter extends RecyclerView.Adapter<SelectedMoviesAdapter.MovieViewHolder> {
    private List<String> selectedMovies = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String movieTitle);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setSelectedMovies(List<String> movies) {
        selectedMovies.clear();
        selectedMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void addMovie(String movieTitle) {
        if (!selectedMovies.contains(movieTitle)) {
            selectedMovies.add(movieTitle);
            notifyDataSetChanged();
        }
    }

    public void removeMovie(String movieTitle) {
        selectedMovies.remove(movieTitle);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_actor, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        String movieTitle = selectedMovies.get(position);
        holder.bind(movieTitle);
    }

    @Override
    public int getItemCount() {
        return selectedMovies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSelectedMovieTitle;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSelectedMovieTitle = itemView.findViewById(R.id.text_selected_actor_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        String movieTitle = selectedMovies.get(position);
                        listener.onItemClick(movieTitle);
                    }
                }
            });
        }

        public void bind(String movieTitle) {
            textViewSelectedMovieTitle.setText(movieTitle);
        }
    }
}
