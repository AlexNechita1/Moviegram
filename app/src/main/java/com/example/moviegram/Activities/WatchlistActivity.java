package com.example.moviegram.Activities;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviegram.Adapters.CustomSpinnerAdapter;
import com.example.moviegram.Adapters.MovieItemAdapter;
import com.example.moviegram.Adapters.MovieSectionedCategoryAdapter;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchlistActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton watchlistBtn, likedBtn;
    private RecyclerView watchlistRecycler, likedRecycler;
    private List<Movie> watchList, likedList;
    private ConstraintLayout watchlistConstr,likedConstr;
    private FirebaseFirestore db;
    private String currentUserId;
    private ImageView bkButton;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        initView();
    }

    private void initView() {
        bkButton = findViewById(R.id.backBtn);
        bkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        radioGroup = findViewById(R.id.radioGroup);
        watchlistBtn = findViewById(R.id.radio_watchlist);
        likedBtn = findViewById(R.id.radio_liked);
        watchlistRecycler = findViewById(R.id.watchlist_recycler);
        likedRecycler = findViewById(R.id.liked_recycler);
        watchlistConstr = findViewById(R.id.watchlist_constraint);
        likedConstr = findViewById(R.id.liked_constraint);

        watchList = new ArrayList<>();
        likedList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        watchlistConstr.setVisibility(View.VISIBLE);
        likedConstr.setVisibility(View.GONE);

        retrieveWatchListAndLikedList();

        underlineRadioButton(watchlistBtn);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (watchlistBtn.isChecked()) {
                removeUnderline(likedBtn);
                underlineRadioButton(watchlistBtn);
                transitionWatchlist();
            } else if (likedBtn.isChecked()) {
                removeUnderline(watchlistBtn);
                underlineRadioButton(likedBtn);
                transitionLiked();
            }
        });
        initSpinner();


    }

    private void initSpinner() {
        String[] options = {"No sort", "Sort by category", "Sort by Release"};
        Spinner spinner = findViewById(R.id.sort_spinner);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, R.layout.spinner_item_layout, options);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedOption = options[position];
                switch (position){
                    case 0:
                        displayResults(watchList,watchlistRecycler);
                        displayResults(likedList,likedRecycler);
                        break;
                    case 1:
                        displayResultsCategories(watchList,watchlistRecycler);
                        displayResultsCategories(likedList,likedRecycler);
                        break;
                    case 2:
                        displayResultsReleaseYear(watchList,watchlistRecycler);
                        displayResultsReleaseYear(likedList,likedRecycler);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void displayResultsReleaseYear(List<Movie> movieList, RecyclerView resRecycler){
        Map<String, List<Movie>> categorizedMovies = organizeMoviesByReleaseYear(movieList);
        MovieSectionedCategoryAdapter adapter = new MovieSectionedCategoryAdapter(this, categorizedMovies);
        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(adapter);
    }

    private Map<String, List<Movie>> organizeMoviesByReleaseYear(List<Movie> movieList) {
        Map<String, List<Movie>> moviesByReleaseYear = new HashMap<>();

        for (Movie movie : movieList) {
            String releaseYear = String.valueOf(movie.getReleaseYear());

            if (!moviesByReleaseYear.containsKey(releaseYear)) {
                moviesByReleaseYear.put(releaseYear, new ArrayList<>());
            }
            moviesByReleaseYear.get(releaseYear).add(movie);
        }

        return moviesByReleaseYear;
    }

    private Map<String, List<Movie>> organizeMoviesByCategory(List<Movie> movieList) {
        Map<String, List<Movie>> categorizedMovies = new HashMap<>();

        for (Movie movie : movieList) {
            for (String category : movie.getGenres()) {
                if (!categorizedMovies.containsKey(category)) {
                    categorizedMovies.put(category, new ArrayList<>());
                }
                categorizedMovies.get(category).add(movie);
            }
        }

        return categorizedMovies;
    }

    private void displayResultsCategories(List<Movie> movieList, RecyclerView resRecycler){

        Map<String, List<Movie>> categorizedMovies = organizeMoviesByCategory(movieList);

        MovieSectionedCategoryAdapter adapter = new MovieSectionedCategoryAdapter(this, categorizedMovies);
        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(adapter);
    }

    private void displayResults(List<Movie> movieList, RecyclerView resRecycler) {
        MovieItemAdapter movieAdapter = new MovieItemAdapter(movieList, this);
        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(movieAdapter);
    }

    private void transitionWatchlist() {
        ViewGroup parent = findViewById(R.id.main);
        Transition transition = new Slide(Gravity.START);
        transition.setDuration(450);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(parent, transition);

        likedConstr.setVisibility(View.GONE);
        watchlistConstr.setVisibility(View.VISIBLE);
    }

    private void transitionLiked() {
        ViewGroup parent = findViewById(R.id.main);
        Transition transition = new Slide(Gravity.END);
        transition.setDuration(450);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        TransitionManager.beginDelayedTransition(parent, transition);

        watchlistConstr.setVisibility(View.GONE);
        likedConstr.setVisibility(View.VISIBLE);
    }

    private void underlineRadioButton(RadioButton radioButton) {
        CharSequence text = radioButton.getText();
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.light_blue)), 0, spannableString.length(), 0);
        radioButton.setText(spannableString);
    }

    private void removeUnderline(RadioButton radioButton) {
        CharSequence text = radioButton.getText();
        SpannableString spannableString = new SpannableString(text);

        UnderlineSpan[] underlines = spannableString.getSpans(0, spannableString.length(), UnderlineSpan.class);
        for (UnderlineSpan underline : underlines) {
            spannableString.removeSpan(underline);
        }

        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white)), 0, spannableString.length(), 0);

        radioButton.setText(spannableString);
    }

    private void retrieveWatchListAndLikedList() {
        db.collection("Users").document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> watchlistTitles = (List<String>) document.get("watchlist");
                            List<String> likedTitles = (List<String>) document.get("liked");

                            fetchMovies(watchlistTitles, watchList, () -> {
                                displayResults(watchList, watchlistRecycler);

                                fetchMovies(likedTitles, likedList, () -> displayResults(likedList, likedRecycler));
                            });
                        } else {

                        }
                    } else {

                    }
                });
    }

    private void fetchMovies(List<String> titles, List<Movie> movieList, Runnable onComplete) {
        if (titles == null || titles.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        db.collection("Movies")
                .whereIn("title", titles)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Movie movie = doc.toObject(Movie.class);
                                movieList.add(movie);
                            }
                        }
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }
}
