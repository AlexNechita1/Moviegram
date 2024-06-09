package com.example.moviegram.Activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moviegram.Adapters.SliderAdapter;
import com.example.moviegram.Objects.SliderItem;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 preferencesSlider,matchesSlider;
    private Handler sliderHandler = new Handler();
    private ProgressBar loadingMatchSlider;
    private ImageView accountButton,filteredSearchButton,watchListBtn;
    private TextView welcomeName;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void banners(ViewPager2 viewPager2, List<SliderItem> sliderItems) {
        viewPager2.setAdapter(new SliderAdapter(sliderItems,viewPager2));
        viewPager2.setClipToPadding(false);
        viewPager2.setOffscreenPageLimit(4);
        viewPager2.setClipChildren(false);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1-Math.abs(position);
                page.setScaleY(0.85f+r*0.15f);
            }
        });
        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.setCurrentItem(1);
        ((SliderAdapter) viewPager2.getAdapter()).setCurrentPosition(1);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ((SliderAdapter) viewPager2.getAdapter()).setCurrentPosition(position);
                /*sliderHandler.removeCallbacks(sliderRunnable);*/
            }
        });
    }

    private void initView() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        preferencesSlider = findViewById(R.id.preferences_slider);
        matchesSlider = findViewById(R.id.match_slider);
        loadingMatchSlider = findViewById(R.id.loading_match_slider);
        welcomeName = findViewById(R.id.textview_name);
        accountButton = findViewById(R.id.imagebutton_account);
        filteredSearchButton = findViewById(R.id.img_search);
        watchListBtn = findViewById(R.id.watch_list);

        watchListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WatchlistActivity.class));
            }
        });

        accountButton.setOnClickListener(new View.OnClickListener() {
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
                                Log.d("titlu",username);
                                welcomeName.setText(username);
                            } else {
                                Log.w(TAG, "Error getting documents.");
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


        /*SliderItem movie = new SliderItem("https://m.media-amazon.com/images/M/MV5BZDM3YTg4MGUtZmUxNi00YmEyLTllNTctNjYyNjZlZGViNmFhXkEyXkFqcGdeQXVyMTUzMTg2ODkz._V1_.jpg"
                , "Argylle","A reclusive author who writes espionage novels about a secret agent and a global spy syndicate realizes the plot of the new book she's writing starts to mirror real-world events, in real time."
                ,"90"
                ,"5.7");
        sliderItems.add(movie);

        SliderItem movie2 = new SliderItem("https://m.media-amazon.com/images/M/MV5BODNlMDM2N2UtOTE5ZS00ODViLTljMDEtOTEwNjU5YTNkNGNlXkEyXkFqcGdeQXVyMTIyNzY0NTMx._V1_.jpg"
                , "Hanu Man"
                ,"An imaginary place called Anjanadri where the protagonist gets the powers of Hanuman and fights for Anjanadri."
                ,"80"
                ,"8");
        sliderItems.add(movie2);

        SliderItem movie3 = new SliderItem("https://m.media-amazon.com/images/M/MV5BZjU1ODRhYmYtNzYzYi00Y2UyLWJhYWQtMzQ3ODlhMzUwMDljXkEyXkFqcGdeQXVyNTA2MzMwMjA@._V1_.jpg"
                , "Malaikottai Vaaliban"
                ,"Journey of Malaikottai Vaaliban, an undisputed warrior, transcends time and geographies, triumphing over every opponent he encounters."
                ,"90"
                ,"6.3");
        sliderItems.add(movie3);*/

        //querryByReleaseYear(db,2023);


    }
    private void querryByReleaseYear(FirebaseFirestore db, long releaseYear) {
        db.collection("Movies")
                .whereEqualTo("releaseYear", releaseYear)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<SliderItem> list = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.exists()) {
                                String title = document.getString("title") != null ? document.getString("title") : "";
                                Log.d("titlu",title);
                                String mainPlot = document.getString("mainPlot") != null ? document.getString("mainPlot") : "";
                                Log.d("mainPlot",mainPlot);
                                String imageUrl = document.getString("downlaoadURL") != null ? document.getString("downlaoadURL") : "";
                                Log.d("imageUrl",imageUrl);
                                String releaseYear = document.getLong("releaseYear") != null ? Long.toString(document.getLong("releaseYear")) : "";
                                Log.d("releaseYear",releaseYear);
                                String aggregateRating = document.getDouble("aggregateRating") != null ? Double.toString(document.getDouble("aggregateRating")) : "";
                                Log.d("aggregateRating",aggregateRating);
                                list.add(new SliderItem(imageUrl,title,mainPlot,releaseYear,aggregateRating));
                                if(list.size()==queryDocumentSnapshots.size()){
                                    Log.d("size",list.size()+"");
                                    banners(matchesSlider,list);
                                    loadingMatchSlider.setVisibility(View.GONE);
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.");
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
    }

}