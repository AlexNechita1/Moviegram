package com.example.moviegram.Objects;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MovieQueryHelper {

    private FirebaseFirestore db;
    private CollectionReference moviesRef;

    public MovieQueryHelper() {
        db = FirebaseFirestore.getInstance();
        moviesRef = db.collection("Movies");
    }

    public void getFilteredMovies(float minRating, float maxRating,
                                  List<String> selectedCategories, boolean includeAllCategories,
                                  List<String> selectedCast, boolean includeAllCast,
                                  Set<Integer> selectedYears,
                                  OnMoviesRetrievedListener listener) {

        Query query = moviesRef;


        query = query.whereGreaterThanOrEqualTo("aggregateRating", minRating);
        query = query.whereLessThanOrEqualTo("aggregateRating", maxRating);


        if (!selectedYears.isEmpty()) {
            query = query.whereIn("releaseYear", new ArrayList<>(selectedYears));
        }


        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Movie> movies = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    Movie movie = document.toObject(Movie.class);
                    movies.add(movie);
                }


                if (!selectedCategories.isEmpty()) {
                    movies = filterMoviesByCategories(movies, selectedCategories, includeAllCategories);
                }


                if (!selectedCast.isEmpty()) {
                    movies = filterMoviesByCast(movies, selectedCast, includeAllCast);
                }

                listener.onMoviesRetrieved(movies);
            } else {
                listener.onError(task.getException());
            }
        });
    }

    private List<Movie> filterMoviesByCategories(List<Movie> movies, List<String> selectedCategories, boolean includeAllCategories) {
        List<Movie> filteredMovies = new ArrayList<>();

        for (Movie movie : movies) {
            boolean match = includeAllCategories ? containsAllCategories(movie.getGenres(), selectedCategories) : containsAnyCategory(movie.getGenres(), selectedCategories);
            if (match) {
                filteredMovies.add(movie);
            }
        }

        return filteredMovies;
    }

    private boolean containsAllCategories(List<String> genres, List<String> selectedCategories) {
        return genres.containsAll(selectedCategories);
    }

    private boolean containsAnyCategory(List<String> genres, List<String> selectedCategories) {
        for (String category : selectedCategories) {
            if (genres.contains(category)) {
                return true;
            }
        }
        return false;
    }

    private List<Movie> filterMoviesByCast(List<Movie> movies, List<String> selectedCast, boolean includeAllCast) {
        List<Movie> filteredMovies = new ArrayList<>();

        for (Movie movie : movies) {
            boolean match = includeAllCast ? containsAllCast(movie.getCast(), selectedCast) : containsAnyCast(movie.getCast(), selectedCast);
            if (match) {
                filteredMovies.add(movie);
            }
        }

        return filteredMovies;
    }

    private boolean containsAllCast(List<Map<String, Object>> castList, List<String> selectedCast) {
        for (String castMember : selectedCast) {
            boolean found = false;
            for (Map<String, Object> cast : castList) {
                if (castMember.equals(cast.get("name"))) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean containsAnyCast(List<Map<String, Object>> castList, List<String> selectedCast) {
        for (Map<String, Object> cast : castList) {
            if (selectedCast.contains(cast.get("name"))) {
                return true;
            }
        }
        return false;
    }

    public interface OnMoviesRetrievedListener {
        void onMoviesRetrieved(List<Movie> movies);
        void onError(Exception e);
    }
}
