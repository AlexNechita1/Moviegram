package com.example.moviegram.Activities;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Adapters.CastAdapter;
import com.example.moviegram.Adapters.CategoryAdapter;
import com.example.moviegram.Adapters.CustomSpinnerAdapter;
import com.example.moviegram.Objects.CastItem;
import com.example.moviegram.Objects.CategoryItem;
import com.example.moviegram.Objects.Post;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DetailActivity extends AppCompatActivity {
    private String title,downloadUrl,username,userTitle,profileUrl;
    private TextView titleName,titleRating, titleReleaseYear,titleMainPlot;
    private RecyclerView recyclerCategory,recyclerCast;
    private ProgressBar loadingPoster;
    private ImageView poster,bkButton,plusBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button postBtn;
    private EditText contentEdit;
    private DatabaseReference postsRef;
    private Spinner spinner;
    private List<String> listOfGenres;

    private boolean isLiked,isInWatchlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Bundle extras = getIntent().getExtras();
        title = extras.getString("ITEM_TITLE");
        initView();
    }

    private void initView() {
        plusBtn = findViewById(R.id.add_title_btn);
        bkButton = findViewById(R.id.backBtn);
        poster = findViewById(R.id.image_backgorund);
        titleName = findViewById(R.id.title_name);
        titleRating = findViewById(R.id.title_rating);
        titleReleaseYear = findViewById(R.id.title_release_year);
        titleMainPlot = findViewById(R.id.title_main_plot);
        recyclerCategory = findViewById(R.id.recycler_category);
        recyclerCast = findViewById(R.id.recycler_cast);
        loadingPoster = findViewById(R.id.loading_poster);
        listOfGenres = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        isLiked = false;
        isInWatchlist = false;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = "";

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInWatchlist){
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference userDocRef = db.collection("Users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("watchlist", FieldValue.arrayRemove(title));
                    userDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Title removed successfully from watchlist");
                                    plusBtn.setImageResource(R.drawable.plus);
                                    isInWatchlist = false;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error removing title from watchlist", e);
                                }
                            });
                }else if(isLiked){
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference userDocRef = db.collection("Users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("liked", FieldValue.arrayRemove(title));
                    userDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Title removed successfully from liked");
                                    plusBtn.setImageResource(R.drawable.plus);
                                    isLiked = false;
                                    //updateGenreCounts(mAuth.getUid(),listOfGenres,false);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error removing title from liked", e);
                                }
                            });
                }
                spinner.setVisibility(View.VISIBLE);
            }
        });

        bkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (currentUser != null) {
            uid = currentUser.getUid();
            db.collection("Users").document(uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    username = document.getString("username") != null ? document.getString("username") : "";
                                    userTitle = document.getString("title") != null ? document.getString("title") : "";
                                    profileUrl = document.getString("profile_picture") != null ? document.getString("profile_picture") : "";

                                    List<String> likedTitles = new ArrayList<>();
                                    List<Object> liked = (List<Object>) document.get("liked");
                                    if (liked != null) {
                                        for (Object obj : liked) {
                                            if (obj instanceof String) {
                                                likedTitles.add((String) obj);
                                            }
                                        }
                                    }

                                    List<String> watchedTitles = new ArrayList<>();
                                    List<Object> watched = (List<Object>) document.get("watchlist");
                                    if (watched != null) {
                                        for (Object obj : watched) {
                                            if (obj instanceof String) {
                                                watchedTitles.add((String) obj);
                                            }
                                        }
                                    }
                                    if (watchedTitles.contains(title)) {
                                        isInWatchlist = true;
                                        plusBtn.setImageResource(R.drawable.check);
                                    } else {
                                    }
                                    if (likedTitles.contains(title)) {
                                        isLiked = true;
                                        plusBtn.setImageResource(R.drawable.red_heart);
                                    } else {
                                    }

                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        }

        db.collection("Movies")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.exists()) {
                                String title = document.getString("title") != null ? document.getString("title") : "";
                                String mainPlot = document.getString("mainPlot") != null ? document.getString("mainPlot") : "";
                                String imageUrl = document.getString("downlaoadURL") != null ? document.getString("downlaoadURL") : "";
                                downloadUrl = imageUrl;
                                String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                                String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                                List<CategoryItem> genresList = new ArrayList<>();
                                List<Object> genres = (List<Object>) document.get("genres");
                                if (genres != null) {
                                    for (Object obj : genres) {
                                        if (obj instanceof String) {
                                            genresList.add(new CategoryItem((String) obj));
                                            listOfGenres.add((String) obj);
                                        }
                                    }
                                }
                                CategoryAdapter adapter = new CategoryAdapter(genresList);
                                recyclerCategory.setAdapter(adapter);
                                recyclerCategory.setLayoutManager(new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

                                List<CastItem> castList = new ArrayList<>();
                                List<Object> cast = (List<Object>) document.get("cast");
                                if (cast != null) {
                                    for (Object obj : cast) {
                                        if (obj instanceof Map) {
                                            Map<String, Object> castMap = (Map<String, Object>) obj;
                                            String name = (String) castMap.get("name");
                                            String url = (String) castMap.get("url");
                                            CastItem castItem = new CastItem(name, url);
                                            castList.add(castItem);
                                        }
                                    }
                                }

                                CastAdapter castAdapter = new CastAdapter(castList);
                                recyclerCast.setAdapter(castAdapter);
                                recyclerCast.setLayoutManager(new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false));


                                Glide.with(poster.getContext())
                                        .load(imageUrl)
                                        .placeholder(R.drawable.intro_pic)
                                        .error(R.drawable.intro_pic)
                                        .centerCrop()
                                        .into(poster);
                                loadingPoster.setVisibility(View.GONE);
                                titleReleaseYear.setText(releaseYear);
                                titleName.setText(title);
                                titleMainPlot.setText(mainPlot);
                                titleRating.setText(aggregateRating);

                            } else {
                                Log.w(TAG, "Error getting documents.");
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting documents", e);
                    }
                });

        initSpinner();
        initPost();

    }

    private void initSpinner() {
        String[] options = {"","Already watched this title? Like!", "Plan to watch this title? Watchlist!"};
        spinner = findViewById(R.id.sort_spinner);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, R.layout.spinner_item_layout, options);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 1) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DocumentReference userDocRef = db.collection("Users").document(uid);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("liked", FieldValue.arrayUnion(title));
                        userDocRef.set(updates, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Liked updated successfully");
                                        plusBtn.setImageResource(R.drawable.red_heart);
                                        isLiked = true;
                                        spinner.setVisibility(View.GONE);
                                        updateGenreCount(mAuth.getUid(),listOfGenres);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error updating liked", e);
                                    }
                                });

                }else if(position == 2){

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DocumentReference userDocRef = db.collection("Users").document(uid);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("watchlist", FieldValue.arrayUnion(title));
                        userDocRef.set(updates, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Watchlist updated successfully");
                                        plusBtn.setImageResource(R.drawable.check);
                                        isInWatchlist = true;
                                        spinner.setVisibility(View.GONE);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Error updating watchlist", e);
                                    }
                                });
                    }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void updateGenreCount(String userId, List<String> genres) {
        DocumentReference userRef = db.collection("Users").document(userId);

        // Fetch current document and update genre counts
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get current data as a Map
                        Map<String, Object> map = document.getData();

                        // Initialize or update genre counts
                        Map<String, Object> genreCounts = map != null && map.containsKey("genreCounts") ?
                                (Map<String, Object>) map.get("genreCounts") : new HashMap<>();

                        // Update genre counts for the specified genres
                        for (String genre : genres) {
                            Long count = genreCounts.containsKey(genre) ? (Long) genreCounts.get(genre) + 1 : 1;
                            genreCounts.put(genre, count);
                        }

                        // Update or create the genreCounts map in Firestore
                        userRef.update("genreCounts", genreCounts)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Genre counts updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating genre counts", e));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.e(TAG, "Error getting document", task.getException());
                }
            }
        });
    }


    private void initPost() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://moviegram-f31cf-default-rtdb.europe-west1.firebasedatabase.app/");
        postsRef = database.getReference("posts");
        postBtn = findViewById(R.id.button_post);
        contentEdit = findViewById(R.id.edittext_post);
        postBtn.setOnClickListener(v -> {
            String content = contentEdit.getText().toString().trim();
            if (!content.isEmpty()) {
                addPostToDatabase(content);
            } else {
                Toast.makeText(DetailActivity.this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addPostToDatabase(String content) {

        String postId = postsRef.push().getKey();

        if (postId != null) {
            Post post = new Post(postId,mAuth.getUid(),username, getCurrentTimestamp(),downloadUrl,profileUrl,userTitle,content,titleName.getText().toString().trim(),"0");
            postsRef.child(postId).setValue(post).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(DetailActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                    contentEdit.setText("");
                } else {
                    Toast.makeText(DetailActivity.this, "Failed to add post", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(DetailActivity.this, "postId == null", Toast.LENGTH_SHORT).show();
        }
    }
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

}