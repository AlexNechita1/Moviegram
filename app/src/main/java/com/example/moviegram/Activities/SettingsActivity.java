package com.example.moviegram.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Adapters.CastAdapter;
import com.example.moviegram.Adapters.CastMemberAdapter;
import com.example.moviegram.Adapters.MovieItemAdapter;
import com.example.moviegram.Adapters.SearchMovieAdapter;
import com.example.moviegram.Adapters.SelectedActorsAdapter;
import com.example.moviegram.Adapters.SelectedMoviesAdapter;
import com.example.moviegram.Objects.CastItem;
import com.example.moviegram.Objects.CastMember;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.Objects.MovieSearchResult;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<CastMember> allCastMembers;
    private List<MovieSearchResult> allMovies;
    private CastMemberAdapter castAdapter;
    private SearchMovieAdapter movieAdapter;
    private SelectedActorsAdapter selectedActorsAdapter;
    private SelectedMoviesAdapter selectedMoviesAdapter;
    private List<String> selectedCastName,selectedMovieTitles;
    private RecyclerView selectedActorsRecycler, adableCastRecycler, adableMoviesRecycler, selectedMoviesRecycler;
    private EditText editCast, editMovie;
    private ImageView profileImg,backgroundImg;
    private String currentUserId;
    private Button editProfile, editBk, saveBtn;
    private static final int MAX_SELECTED_ACTORS = 3;
    private static final int MAX_SELECTED_MOVIES = 3;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
        initSearchFunctionality();
    }

    private void initView() {
        saveBtn = findViewById(R.id.save_button);
        editProfile = findViewById(R.id.edit_profile_button);
        profileImg = findViewById(R.id.profile_image);
        backgroundImg = findViewById(R.id.image_backgorund);
        editCast = findViewById(R.id.edit_cast);
        editMovie = findViewById(R.id.edit_movies);
        db = FirebaseFirestore.getInstance();
        allCastMembers = new ArrayList<>();
        allMovies = new ArrayList<>();
        selectedCastName = new ArrayList<>();
        selectedMovieTitles = new ArrayList<>();
        editBk = findViewById(R.id.edit_bk_button);
        adableCastRecycler = findViewById(R.id.addable_cast_recycler);
        adableMoviesRecycler = findViewById(R.id.addable_movie_recycler);
        selectedActorsRecycler = findViewById(R.id.selected_cast_recycler);
        selectedMoviesRecycler = findViewById(R.id.selected_movies_recycler);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelectedItems();
                Intent intent = new Intent(SettingsActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        editBk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMovieSearchPopup();
            }
        });

        castAdapter = new CastMemberAdapter(allCastMembers, new CastMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CastMember castMember) {
                if (selectedCastName.size() < MAX_SELECTED_ACTORS) {
                    selectedActorsAdapter.addActor(castMember.getName());
                    selectedCastName.add(castMember.getName());
                } else {
                    Toast.makeText(SettingsActivity.this, "You can only select up to 3 actors.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        adableCastRecycler.setAdapter(castAdapter);
        adableCastRecycler.setLayoutManager(new LinearLayoutManager(this));
        adableCastRecycler.setVisibility(View.GONE);

        movieAdapter = new SearchMovieAdapter(allMovies, new SearchMovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieSearchResult movie) {
                if (selectedMovieTitles.size() < MAX_SELECTED_MOVIES) {
                    selectedMoviesAdapter.addMovie(movie.getTitle());
                    selectedMovieTitles.add(movie.getTitle());
                } else {
                    Toast.makeText(SettingsActivity.this, "You can only select up to 3 movies.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        adableMoviesRecycler.setAdapter(movieAdapter);
        adableMoviesRecycler.setLayoutManager(new LinearLayoutManager(this));
        adableMoviesRecycler.setVisibility(View.GONE);  // Initially hidden
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initCastRecycler();
        initMovieRecycler(currentUserId);

        selectedActorsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedActorsAdapter = new SelectedActorsAdapter();
        selectedActorsAdapter.setOnItemClickListener(new SelectedActorsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String actorName) {
                selectedActorsAdapter.removeActor(actorName);
                selectedCastName.remove(actorName);
            }
        });
        selectedActorsRecycler.setAdapter(selectedActorsAdapter);

        selectedMoviesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedMoviesAdapter = new SelectedMoviesAdapter();
        selectedMoviesAdapter.setOnItemClickListener(new SelectedMoviesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String movieTitle) {
                selectedMoviesAdapter.removeMovie(movieTitle);
                selectedMovieTitles.remove(movieTitle);
            }
        });
        selectedMoviesRecycler.setAdapter(selectedMoviesAdapter);
        retrieveDatabase();
    }

    private void saveSelectedItems() {
        db.collection("Users").document(currentUserId)
                .update("favorite_actors", selectedCastName,
                        "top_movies", selectedMovieTitles)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsActivity.this, "Selected items saved successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Failed to save selected items", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void showMovieSearchPopup() {
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_movie_search, null);

        EditText editMovieSearch = popupView.findViewById(R.id.edit_movie_search);
        RecyclerView recyclerMovieSearch = popupView.findViewById(R.id.recycler_movie_search);

        recyclerMovieSearch.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme);
        builder.setView(popupView)
                .setTitle("Search for a Movie")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the adapter with the dialog reference for dismissal
        movieAdapter = new SearchMovieAdapter(allMovies, movie -> {
            updateProfileBackground(movie.getUrl());
            dialog.dismiss(); // Dismiss the dialog when a movie is selected
        });

        recyclerMovieSearch.setAdapter(movieAdapter);

        editMovieSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBackgrounds(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        loadAllMovies();
    }

    private void loadAllMovies() {
        db.collection("Movies").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allMovies.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String title = document.getString("title");
                String imageUrl = document.getString("downlaoadURL");
                allMovies.add(new MovieSearchResult(title, imageUrl));
            }
            movieAdapter.notifyDataSetChanged();
        });
    }

    private void updateProfileBackground(String imageUrl) {
        DocumentReference userRef = db.collection("Users").document(currentUserId);
        userRef.update("profile_background", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SettingsActivity.this, "Profile background updated", Toast.LENGTH_SHORT).show();
                    Glide.with(backgroundImg.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.big_logo)
                            .error(R.drawable.big_logo)
                            .centerCrop()
                            .into(backgroundImg);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SettingsActivity.this, "Failed to update profile background", Toast.LENGTH_SHORT).show();
                });
    }




    private void retrieveDatabase() {
        db.collection("Users").document(currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {

                    String backgroundPic = document.getString("profile_background") != null ? document.getString("profile_background") : "";
                    String profilePic = document.getString("profile_picture") != null ? document.getString("profile_picture") : "";
                    //String username = document.getString("username") != null ? document.getString("username") : "";
                    //String title = document.getString("title") != null ? document.getString("title") : "";
                    //String followers = String.valueOf(document.getDouble("followers"));


                    List<String> favoriteActors = document.contains("favorite_actors") ? (List<String>) document.get("favorite_actors") : new ArrayList<>();
                    List<String> topMovies = document.contains("top_movies") ? (List<String>) document.get("top_movies") : new ArrayList<>();

                    //usernameTx.setText(username);
                    //titleTx.setText(title);
                    //followersTx.setText(followers);

                    Glide.with(backgroundImg.getContext())
                            .load(backgroundPic)
                            .placeholder(R.drawable.big_logo)
                            .error(R.drawable.big_logo)
                            .centerCrop()
                            .into(backgroundImg);
                    Glide.with(profileImg.getContext())
                            .load(profilePic)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .centerCrop()
                            .into(profileImg);


                    if (!topMovies.isEmpty()) {
                        fetchMovies(topMovies);
                    }

                    if (!favoriteActors.isEmpty()) {
                        fetchActors(favoriteActors);
                    }


                    selectedActorsAdapter.notifyDataSetChanged();
                    selectedMoviesAdapter.notifyDataSetChanged();

                } else {

                }
            } else {

            }
        });
    }
    private void fetchMovies(List<String> movieTitles) {
        db.collection("Movies").whereIn("title", movieTitles).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> selectedMoviesList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            selectedMoviesList.add(title);
                        }
                        selectedMovieTitles = selectedMoviesList;
                        selectedMoviesAdapter.setSelectedMovies(selectedMoviesList);
                    } else {

                    }
                });
    }


    private void fetchActors(List<String> actorNames) {
        db.collection("Movies").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> selectedActorsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> cast = (List<Map<String, Object>>) document.get("cast");
                            if (cast != null) {
                                for (Map<String, Object> castMember : cast) {
                                    String name = (String) castMember.get("name");
                                    if (actorNames.contains(name)) {
                                        selectedActorsList.add(name);
                                        actorNames.remove(name);
                                    }
                                }
                            }
                        }
                        selectedCastName = selectedActorsList;
                        selectedActorsAdapter.setSelectedActors(selectedActorsList);
                    } else {

                    }
                });
    }




    private void initCastRecycler() {
        db.collection("Movies")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<CastMember> uniqueCast = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            List<Map<String, Object>> castList = (List<Map<String, Object>>) document.get("cast");
                            if (castList != null) {
                                for (Map<String, Object> cast : castList) {
                                    String name = (String) cast.get("name");
                                    String imageUrl = (String) cast.get("url");
                                    if (checkName(name, uniqueCast)) {
                                        uniqueCast.add(new CastMember(name, imageUrl));
                                    }
                                }
                            }
                        }
                        allCastMembers.clear();
                        allCastMembers.addAll(uniqueCast);
                        castAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void initMovieRecycler(String userId) {
        db.collection("Users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                List<String> likedMovies = (List<String>) document.get("liked");

                if (likedMovies != null && !likedMovies.isEmpty()) {
                    db.collection("Movies").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<MovieSearchResult> uniqueMovies = new ArrayList<>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String title = document.getString("title");
                                if (likedMovies.contains(title)) {
                                    String imageUrl = document.getString("downlaoadURL");
                                    uniqueMovies.add(new MovieSearchResult(title, imageUrl));
                                }
                            }
                            allMovies.clear();
                            allMovies.addAll(uniqueMovies);
                            movieAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    // Handle case where the user has no liked movies
                    allMovies.clear();
                    movieAdapter.notifyDataSetChanged();
                }
            } else {
                // Handle error
                allMovies.clear();
                movieAdapter.notifyDataSetChanged();
            }
        });
    }


    private boolean checkName(String name, List<CastMember> uniqueCast) {
        for (CastMember castMember : uniqueCast) {
            if (castMember.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private void initSearchFunctionality() {
        editCast.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCast(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editMovie.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterBackgrounds(String query) {
        List<MovieSearchResult> filteredList = new ArrayList<>();
        for (MovieSearchResult movie : allMovies) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(movie);
            }
        }
        movieAdapter.filterList(filteredList);
    }
    private void filterCast(String text) {
        if (text.isEmpty()) {
            adableCastRecycler.setVisibility(View.GONE);
        } else {
            List<CastMember> filteredList = new ArrayList<>();
            for (CastMember member : allCastMembers) {
                if (member.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(member);
                }
            }
            castAdapter.filterList(filteredList);  // Ensure this method is implemented in CastMemberAdapter
            adableCastRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void filterMovies(String text) {
        if (text.isEmpty()) {
            adableMoviesRecycler.setVisibility(View.GONE);
        } else {
            List<MovieSearchResult> filteredList = new ArrayList<>();
            for (MovieSearchResult movie : allMovies) {
                if (movie.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(movie);
                }
            }
            movieAdapter.filterList(filteredList);  // Ensure this method is implemented in SearchMovieAdapter
            adableMoviesRecycler.setVisibility(View.VISIBLE);
        }
    }
}
