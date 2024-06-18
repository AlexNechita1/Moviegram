package com.example.moviegram.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.moviegram.Adapters.SliderAdapter;
import com.example.moviegram.Objects.SliderItem;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 matchesSlider,topLikedSlider,topPostSlider;
    private List<ViewPager2> listOfGenresSlider;
    private List<TextView> listOfGenresTx;
    private List<ProgressBar> listOfGenresPb;
    private List<LinearLayout> listOfGenresLinear;
    private Handler sliderHandler = new Handler();
    private ProgressBar loadingMatchSlider,topLikedPb,topPostPb;
    private FirebaseFirestore db;
    private LinearLayout accLinear,matchLinear;
    private ImageView accountButton,filteredSearchButton,watchListBtn,feedBtn,homeButton;
    private TextView welcomeName;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void populateSliderWithMovies() {
        Map<String, Integer> movieCountMap = new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://moviegram-f31cf-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference postsRef = database.getReference("posts");
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String movieTitle = (String) postSnapshot.child("movieTitle").getValue();
                    if (movieTitle != null) {
                        movieCountMap.put(movieTitle, movieCountMap.getOrDefault(movieTitle, 0) + 1);
                    }
                }
                List<Map.Entry<String, Integer>> sortedMovies = new ArrayList<>(movieCountMap.entrySet());
                sortedMovies.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
                List<String> sortedMovieTitles = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : sortedMovies) {
                    sortedMovieTitles.add(entry.getKey());
                    if (sortedMovieTitles.size() >= 20) {
                        break;
                    }
                }
                Query query = db.collection("Movies")
                        .whereIn("title", sortedMovieTitles)
                        .limit(20);
                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SliderItem> sliderItems = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.exists()) {
                            String title = document.getString("title") != null ? document.getString("title") : "";
                            String mainPlot = document.getString("mainPlot") != null ? document.getString("mainPlot") : "";
                            String imageUrl = document.getString("downlaoadURL") != null ? document.getString("downlaoadURL") : "";
                            String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                            String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                            sliderItems.add(new SliderItem(imageUrl, title, mainPlot, releaseYear, aggregateRating));
                        }
                    }
                    if (!sliderItems.isEmpty()) {
                        banners(topPostSlider, sliderItems);
                        topPostPb.setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "unable to get data", databaseError.toException());
            }
        });
    }
    private void getMostLikedMovies() {
        db.collection("Users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Map<String, Integer> movieCountMap = new HashMap<>();
            for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                List<String> userLiked = (List<String>) userDoc.get("liked");
                if (userLiked != null) {
                    for (String movie : userLiked) {
                        movieCountMap.put(movie, movieCountMap.getOrDefault(movie, 0) + 1);
                    }
                }
            }
            List<Map.Entry<String, Integer>> sortedMovies = new ArrayList<>(movieCountMap.entrySet());
            sortedMovies.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            List<String> topMovies = new ArrayList<>();
            for (int i = 0; i < Math.min(20, sortedMovies.size()); i++) {
                topMovies.add(sortedMovies.get(i).getKey());
            }
            if (!topMovies.isEmpty()) {
                fetchMovieDetailsAndUpdateSlider(topMovies);
            }
        });
    }
    private void fetchMovieDetailsAndUpdateSlider(List<String> movieTitles) {
        Query query = db.collection("Movies").whereIn("title", movieTitles);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<SliderItem> sliderItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                if (document.exists()) {
                    String title = document.getString("title");
                    String mainPlot = document.getString("mainPlot");
                    String imageUrl = document.getString("downlaoadURL");
                    String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                    String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                    sliderItems.add(new SliderItem(imageUrl, title, mainPlot, releaseYear, aggregateRating));
                }
            }
            banners(topLikedSlider, sliderItems);
            topLikedPb.setVisibility(View.GONE);
        });
    }
    private void findCompatibleUsersAndMovies() {
        String currentUserId = mAuth.getUid();
        DocumentReference currentUserRef = db.collection("Users").document(currentUserId);
        currentUserRef.get().addOnSuccessListener(currentUserSnapshot -> {
            if (currentUserSnapshot.exists()) {
                List<String> currentUserLiked = (List<String>) currentUserSnapshot.get("liked");
                if (currentUserLiked == null) {
                    return;
                }
                db.collection("Users").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> compatibilityMap = new HashMap<>();
                    for (QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                        if (!userDoc.getId().equals(currentUserId)) {
                            List<String> userLiked = (List<String>) userDoc.get("liked");
                            if (userLiked != null) {
                                long commonCount = userLiked.stream()
                                        .filter(currentUserLiked::contains)
                                        .count();
                                if (commonCount >= 10) {
                                    compatibilityMap.put(userDoc.getId(), (int) commonCount);
                                }
                            }
                        }
                    }

                    if (compatibilityMap.isEmpty()) {
                        return;
                    }

                    List<Map.Entry<String, Integer>> sortedUsers = new ArrayList<>(compatibilityMap.entrySet());
                    sortedUsers.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                    List<String> recommendedMovies = new ArrayList<>();
                    fetchRecommendedMovies(sortedUsers, currentUserLiked, recommendedMovies, 0);

                });
            }
        });
    }
    private void fetchRecommendedMovies(List<Map.Entry<String, Integer>> sortedUsers, List<String> currentUserLiked, List<String> recommendedMovies, int index) {
        if (index >= sortedUsers.size() || recommendedMovies.size() >= 50) {
            if (!recommendedMovies.isEmpty()) {
                updateSliderWithMovies(recommendedMovies);
                matchLinear.setVisibility(View.VISIBLE);
            }
            return;
        }
        String userId = sortedUsers.get(index).getKey();
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(userSnapshot -> {
            if (userSnapshot.exists()) {
                List<String> userLiked = (List<String>) userSnapshot.get("liked");
                if (userLiked != null) {
                    for (String movie : userLiked) {
                        if (!currentUserLiked.contains(movie) && !recommendedMovies.contains(movie)) {
                            recommendedMovies.add(movie);
                            if (recommendedMovies.size() >= 50) break;
                        }
                    }
                }
            }
            fetchRecommendedMovies(sortedUsers, currentUserLiked, recommendedMovies, index + 1);
        }).addOnFailureListener(e -> {
            fetchRecommendedMovies(sortedUsers, currentUserLiked, recommendedMovies, index + 1);
        });
    }
    private void updateSliderWithMovies(List<String> movieTitles) {
        Query query = db.collection("Movies").whereIn("title", movieTitles)
                .orderBy("aggregateRating", Query.Direction.DESCENDING);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<SliderItem> sliderItems = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                if (document.exists()) {
                    String title = document.getString("title");
                    String mainPlot = document.getString("mainPlot");
                    String imageUrl = document.getString("downlaoadURL");
                    String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                    String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                    sliderItems.add(new SliderItem(imageUrl, title, mainPlot, releaseYear, aggregateRating));
                }
            }
            banners(matchesSlider, sliderItems);
            loadingMatchSlider.setVisibility(View.GONE);
        });
    }
    private void initGenresSlider() {
        String userId = mAuth.getUid();
        try {
            DocumentReference userRef = db.collection("Users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> likedTitles = (List<String>) documentSnapshot.get("liked");
                    Map<String, Object> genreCounts = (Map<String, Object>) documentSnapshot.get("genreCounts");
                    if (genreCounts != null && !genreCounts.isEmpty()) {
                        List<Map.Entry<String, Object>> genreList = new ArrayList<>(genreCounts.entrySet());
                        genreList.sort((e1, e2) -> Long.compare((Long) e2.getValue(), (Long) e1.getValue()));

                        List<String> topGenres = new ArrayList<>();
                        for (int i = 0; i < genreList.size(); i++) {
                            if ((Long) genreList.get(i).getValue() >= 5) {
                                topGenres.add(genreList.get(i).getKey());
                            }
                            if (topGenres.size() >= 3) {
                                break;
                            }
                        }
                        if(!topGenres.isEmpty()){
                            for(int i = 0; i < topGenres.size(); i++){
                                LinearLayout linearLayout = listOfGenresLinear.get(i);
                                ProgressBar progressBar = listOfGenresPb.get(i);
                                ViewPager2 slider = listOfGenresSlider.get(i);
                                TextView textView = listOfGenresTx.get(i);
                                textView.setText("From your favorite genres: "+ topGenres.get(i));
                                linearLayout.setVisibility(View.VISIBLE);
                                Query query = db.collection("Movies")
                                        .whereArrayContains("genres", topGenres.get(i))
                                        .whereGreaterThan("aggregateRating", 7.0)
                                        .orderBy("aggregateRating", Query.Direction.DESCENDING);
                                query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                                    List<SliderItem> list = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        if (document.exists()) {
                                            String title = document.getString("title") != null ? document.getString("title") : "";
                                            if (!likedTitles.contains(title)) {
                                                String mainPlot = document.getString("mainPlot") != null ? document.getString("mainPlot") : "";
                                                String imageUrl = document.getString("downlaoadURL") != null ? document.getString("downlaoadURL") : "";
                                                String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                                                String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                                                list.add(new SliderItem(imageUrl, title, mainPlot, releaseYear, aggregateRating));
                                            }
                                        }
                                    }
                                    banners(slider, list);
                                    progressBar.setVisibility(View.GONE);
                                });
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            Log.d("EXCEPTIE", e.toString());
        }
    }
    private void banners(ViewPager2 viewPager2, List<SliderItem> sliderItems) {
        SliderAdapter adapter = new SliderAdapter(sliderItems, viewPager2);
        viewPager2.setAdapter(adapter);
        viewPager2.setClipToPadding(false);
        viewPager2.setOffscreenPageLimit(4);
        viewPager2.setClipChildren(false);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });
        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.setCurrentItem(1);
        adapter.setCurrentPosition(1);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                adapter.setCurrentPosition(position);
                /*sliderHandler.removeCallbacks(sliderRunnable);*/
            }
        });
    }
    private void initView() {
        db = FirebaseFirestore.getInstance();
        matchesSlider = findViewById(R.id.match_slider);
        loadingMatchSlider = findViewById(R.id.loading_match_slider);
        welcomeName = findViewById(R.id.textview_name);
        accountButton = findViewById(R.id.imagebutton_account);
        filteredSearchButton = findViewById(R.id.img_search);
        feedBtn = findViewById(R.id.feed_button);
        watchListBtn = findViewById(R.id.watch_list);
        accLinear = findViewById(R.id.account_linear);
        homeButton = findViewById(R.id.home_button);
        topLikedSlider = findViewById(R.id.top_liked_slider);
        topLikedPb = findViewById(R.id.top_liked_pb);
        topPostSlider = findViewById(R.id.postedMovies_slider);
        topPostPb = findViewById(R.id.postedMovies_pb);
        listOfGenresSlider = new ArrayList<>(Arrays.asList(findViewById(R.id.genres_slider_1), findViewById(R.id.genres_slider_2), findViewById(R.id.genres_slider_3)));
        listOfGenresTx = new ArrayList<>(Arrays.asList(findViewById(R.id.genres_tx_1),findViewById(R.id.genres_tx_2),findViewById(R.id.genres_tx_3)));
        listOfGenresPb = new ArrayList<>(Arrays.asList(findViewById(R.id.genres_pb_1),findViewById(R.id.genres_pb_2),findViewById(R.id.genres_pb_3)));
        listOfGenresLinear = new ArrayList<>(Arrays.asList(findViewById(R.id.genres_linear_1),findViewById(R.id.genres_linear_2),findViewById(R.id.genres_linear_3)));
        matchLinear = findViewById(R.id.match_linear);
        feedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FeedActivity.class));
            }
        });
        watchListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WatchlistActivity.class));
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
        accLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
            }
        });
        filteredSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BrowseActivity.class));
            }
        });
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();
        db.collection("Users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.exists()) {
                                String username = document.getString("username") != null ? document.getString("username") : "";
                                String url = document.getString("profile_picture") != null ? document.getString("profile_picture") : "";
                                if (url != ""){
                                    Glide.with(accountButton.getContext())
                                            .load(url)
                                            .centerCrop()
                                            .into(accountButton);
                                }
                                welcomeName.setText(username);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("eroare 2", "Error getting documents", e);
                    }
                });
        initGenresSlider();
        findCompatibleUsersAndMovies();
        getMostLikedMovies();
        populateSliderWithMovies();

    }
}