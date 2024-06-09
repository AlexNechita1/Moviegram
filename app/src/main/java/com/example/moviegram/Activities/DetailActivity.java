package com.example.moviegram.Activities;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Adapters.CastAdapter;
import com.example.moviegram.Adapters.CategoryAdapter;
import com.example.moviegram.Objects.CastItem;
import com.example.moviegram.Objects.CategoryItem;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private String title;
    private TextView titleName,titleRating, titleReleaseYear,titleMainPlot;
    private RecyclerView recyclerCategory,recyclerCast;
    private ProgressBar loadingPoster;
    private ImageView poster,addBtn,likeBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
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
        poster = findViewById(R.id.image_backgorund);
        titleName = findViewById(R.id.title_name);
        titleRating = findViewById(R.id.title_rating);
        titleReleaseYear = findViewById(R.id.title_release_year);
        titleMainPlot = findViewById(R.id.title_main_plot);
        recyclerCategory = findViewById(R.id.recycler_category);
        recyclerCast = findViewById(R.id.recycler_cast);
        loadingPoster = findViewById(R.id.loading_poster);
        addBtn = findViewById(R.id.add_title_btn);
        likeBtn = findViewById(R.id.like_title_btn);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        isLiked = false;
        isInWatchlist = false;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = "";

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
                                        addBtn.setImageResource(R.drawable.check);
                                        Log.d("WATCHLIST", "Title is in the watchlist");
                                    } else {
                                        Log.d("WATCHLIST", "Title is NOT in the watchlist");
                                    }
                                    if (likedTitles.contains(title)) {
                                        isLiked = true;
                                        likeBtn.setImageResource(R.drawable.red_heart);
                                        Log.d("LIKED", "Title is in the liked list");
                                    } else {
                                        Log.d("LIKED", "Title is NOT in the liked list");
                                    }

                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
        } else {
            Log.d(TAG, "No user is currently logged in");
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
                                String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                                String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                                List<CategoryItem> genresList = new ArrayList<>();
                                List<Object> genres = (List<Object>) document.get("genres");
                                if (genres != null) {
                                    for (Object obj : genres) {
                                        if (obj instanceof String) {
                                            genresList.add(new CategoryItem((String) obj));
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

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInWatchlist){
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference userDocRef = db.collection("Users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("watchlist", FieldValue.arrayUnion(title));
                    userDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Watchlist updated successfully");
                                    addBtn.setImageResource(R.drawable.check);
                                    isInWatchlist = true;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error updating watchlist", e);
                                }
                            });
                }else {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference userDocRef = db.collection("Users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("watchlist", FieldValue.arrayRemove(title));
                    userDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Title removed successfully from watchlist");
                                    addBtn.setImageResource(R.drawable.plus);
                                    isInWatchlist = false;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error removing title from watchlist", e);
                                }
                            });
                }

            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLiked){
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference userDocRef = db.collection("Users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("liked", FieldValue.arrayUnion(title));
                    userDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Liked updated successfully");
                                    likeBtn.setImageResource(R.drawable.red_heart);
                                    isLiked = true;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error updating liked", e);
                                }
                            });
                }else {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DocumentReference userDocRef = db.collection("Users").document(uid);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("liked", FieldValue.arrayRemove(title));
                    userDocRef.set(updates, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Title removed successfully from liked");
                                    likeBtn.setImageResource(R.drawable.white_heart);
                                    isLiked = false;
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Error removing title from liked", e);
                                }
                            });
                }
            }
        });

    }


}