package com.example.moviegram.Activities;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Adapters.CastAdapter;
import com.example.moviegram.Adapters.CategoryAdapter;
import com.example.moviegram.Adapters.FirstMoviesAdapter;
import com.example.moviegram.Adapters.SearchMovieAdapter;
import com.example.moviegram.Objects.CastItem;
import com.example.moviegram.Objects.CategoryItem;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.Objects.MovieSearchResult;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirstSelectionActivity extends AppCompatActivity {

    private CategoryAdapter availableAdapter;
    private List<CategoryItem> availableCategories;
    private List<String> selectedMovies;
    private SearchMovieAdapter movieAdapter;
    private FirstMoviesAdapter selectedMovieAdapter;
    private FirebaseAuth mAuth;
    RecyclerView resRecycler;
    private Button nextBtn;
    private FirebaseFirestore db;
    private ProgressBar pbProfilePic;
    private List<MovieSearchResult> allMovies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_selection);
        initView();
    }

    private void initView() {

        db = FirebaseFirestore.getInstance();
        allMovies = new ArrayList<>();
        FirebaseStorage storage;
        StorageReference storageRef;
        mAuth=FirebaseAuth.getInstance();
        pbProfilePic = findViewById(R.id.progress_profile_pic);

        Button uploadBtn = findViewById(R.id.edit_profile_button);
        RecyclerView adableRecycler = findViewById(R.id.addable_category_recycler);
        resRecycler = findViewById(R.id.recycler_filtered_movies);
        EditText editMovieSearch = findViewById(R.id.edit_movie_search);
        RecyclerView recyclerMovieSearch = findViewById(R.id.recycler_movie_search);
        recyclerMovieSearch.setLayoutManager(new LinearLayoutManager(FirstSelectionActivity.this));
        recyclerMovieSearch.setVisibility(View.GONE);
        selectedMovies = new ArrayList<>();
        nextBtn = findViewById(R.id.next_button);
        Button finishBtn = findViewById(R.id.finish_button);

        movieAdapter = new SearchMovieAdapter(allMovies, movie -> {
            if(!selectedMovies.contains(movie.getTitle())){
                selectedMovies.add(movie.getTitle());
                Toast.makeText(FirstSelectionActivity.this, "Movie added", Toast.LENGTH_SHORT).show();
            }
            hideKeyboard(this);
        });
        recyclerMovieSearch.setAdapter(movieAdapter);


        editMovieSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBackgrounds(s.toString(),recyclerMovieSearch);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        loadAllMovies();

        availableCategories = new ArrayList<>();
        availableAdapter = new CategoryAdapter(availableCategories);

        availableAdapter.setListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CategoryItem item) {
                String category = item.getDen();
                List<Movie> filteredMovies = new ArrayList<>();

                db.collection("Movies")
                        .whereArrayContains("genres", category)
                        .orderBy("aggregateRating", Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Movie movie = document.toObject(Movie.class);
                                        filteredMovies.add(movie);
                                    }
                                    selectedMovieAdapter = new FirstMoviesAdapter(filteredMovies, FirstSelectionActivity.this);
                                    resRecycler.setLayoutManager(new LinearLayoutManager(FirstSelectionActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                    resRecycler.setAdapter(selectedMovieAdapter);

                                    movieAdapter.notifyDataSetChanged();

                                } else {
                                    Log.d("Firestore", "Error getting documents: ", task.getException());
                                }
                            }
                        });


            }
        });
        adableRecycler.setAdapter(availableAdapter);
        adableRecycler.setLayoutManager(new LinearLayoutManager(FirstSelectionActivity.this, LinearLayoutManager.HORIZONTAL, false));
        initCategoryRecycler();

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbProfilePic.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });



        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToMoviePopup(findViewById(R.id.main), findViewById(R.id.profile_constraint), findViewById(R.id.likedmovies_constraint));

            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!selectedMovieAdapter.getSelectedMovies().isEmpty()){
                    for(int i = 0; i < selectedMovieAdapter.getSelectedMovies().size();i++){
                        if(!selectedMovies.contains(selectedMovieAdapter.getSelectedMovies().get(i))){
                            selectedMovies.add(selectedMovieAdapter.getSelectedMovies().get(i));
                        }
                    }
                }

                if (!selectedMovies.isEmpty()) {
                    DocumentReference userRef = db.collection("Users").document(mAuth.getUid());
                    userRef.update("liked", FieldValue.arrayUnion(selectedMovies.toArray()))
                            .addOnSuccessListener(aVoid -> {
                                for (String movieTitle : selectedMovies) {
                                    db.collection("Movies")
                                            .whereEqualTo("title", movieTitle)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                    if (document.exists()) {
                                                        List<String> genresList = new ArrayList<>();
                                                        List<Object> genres = (List<Object>) document.get("genres");
                                                        if (genres != null) {
                                                            for (Object obj : genres) {
                                                                if (obj instanceof String) {
                                                                    genresList.add((String) obj);
                                                                }
                                                            }
                                                        }
                                                        updateGenreCount(mAuth.getUid(), genresList);
                                                    } else {
                                                        Log.w(TAG, "Error getting documents.");
                                                    }
                                                }
                                                startActivity(new Intent(FirstSelectionActivity.this, MainActivity.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error getting documents", e);
                                                startActivity(new Intent(FirstSelectionActivity.this, MainActivity.class));
                                            });
                                }
                                Log.d("Firestore", "Liked movies successfully added!");
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Firestore", "Error adding liked movies", e);
                            });
                }


            }
        });

    }
    public void updateGenreCount(String userId, List<String> genres) {
        DocumentReference userRef = db.collection("Users").document(userId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot document = transaction.get(userRef);

                    if (document.exists()) {
                        Map<String, Object> map = document.getData();
                        Map<String, Object> genreCounts = map != null && map.containsKey("genreCounts") ?
                                (Map<String, Object>) map.get("genreCounts") : new HashMap<>();

                        for (String genre : genres) {
                            Long count = genreCounts.containsKey(genre) ? (Long) genreCounts.get(genre) + 1 : 1;
                            genreCounts.put(genre, count);
                        }

                        transaction.update(userRef, "genreCounts", genreCounts);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                    return null;
                }).addOnSuccessListener(aVoid -> Log.d(TAG, "Genre counts updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating genre counts", e));
    }


    private void initSelected() {
        String category = "Action";
        List<Movie> filteredMovies = new ArrayList<>();

        db.collection("Movies")
                .whereArrayContains("genres", category)
                .orderBy("aggregateRating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Movie movie = document.toObject(Movie.class);
                                filteredMovies.add(movie);
                            }
                            selectedMovieAdapter = new FirstMoviesAdapter(filteredMovies, FirstSelectionActivity.this);
                            resRecycler.setLayoutManager(new LinearLayoutManager(FirstSelectionActivity.this, LinearLayoutManager.HORIZONTAL, false));
                            resRecycler.setAdapter(selectedMovieAdapter);

                            movieAdapter.notifyDataSetChanged();

                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void initCategoryRecycler() {
        db.collection("Movies")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Set<String> uniqueGenres = new HashSet<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            List<Object> genres = (List<Object>) document.get("genres");
                            if (genres != null) {
                                for (Object obj : genres) {
                                    if (obj instanceof String) {
                                        uniqueGenres.add((String) obj);
                                    }
                                }
                            }
                        }
                        availableCategories.clear();
                        for (String genre : uniqueGenres) {
                            availableCategories.add(new CategoryItem(genre));
                        }
                        availableAdapter.updateOriginalList(new ArrayList<>(availableCategories));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting documents", e);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();

            StorageReference imagesRef = storageRef.child("profile_pic/" + mAuth.getUid());

            UploadTask uploadTask = imagesRef.putFile(selectedImage);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(FirstSelectionActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    pbProfilePic.setVisibility(View.GONE);
                    nextBtn.setVisibility(View.VISIBLE);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();
                            String uid = mAuth.getUid();

                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put("profile_picture", downloadUrl);


                            db.collection("Users").document(uid)
                                    .set(userUpdates, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(FirstSelectionActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                                            pbProfilePic.setVisibility(View.GONE);
                                            nextBtn.setVisibility(View.VISIBLE);
                                            transitionToMoviePopup(findViewById(R.id.main), findViewById(R.id.profile_constraint),findViewById(R.id.likedmovies_constraint));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(FirstSelectionActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                }
            });
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void filterBackgrounds(String query, RecyclerView recycler) {
        List<MovieSearchResult> filteredList = new ArrayList<>();
        if (query.isEmpty()){
            recycler.setVisibility(View.GONE);
        }else {
            recycler.setVisibility(View.VISIBLE);
            for (MovieSearchResult movie : allMovies) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(movie);
                }
            }
        }
        movieAdapter.filterList(filteredList);
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
    private void transitionToMoviePopup(ConstraintLayout parent, ConstraintLayout startConstraint, ConstraintLayout stopConstraint){
        Transition transition = new Slide(Gravity.END);
        transition.setDuration(450);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(parent, transition);

        startConstraint.setVisibility(View.GONE);
        stopConstraint.setVisibility(View.VISIBLE);
        initSelected();
    }
}