package com.example.moviegram.Activities;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviegram.Adapters.CastMemberAdapter;
import com.example.moviegram.Adapters.FilterCategoryAdapter;
import com.example.moviegram.Adapters.MovieItemAdapter;
import com.example.moviegram.Adapters.MovieSectionedCastAdapter;
import com.example.moviegram.Adapters.MovieSectionedCategoryAdapter;
import com.example.moviegram.Adapters.ReleaseYearAdapter;
import com.example.moviegram.Adapters.SearchMovieAdapter;
import com.example.moviegram.Adapters.SelectedActorsAdapter;
import com.example.moviegram.Interfaces.OnYearSelectedListener;
import com.example.moviegram.Objects.CastMember;
import com.example.moviegram.Objects.CategoryItem;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.Objects.MovieQueryHelper;
import com.example.moviegram.Objects.MovieSearchResult;
import com.example.moviegram.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrowseActivity extends AppCompatActivity implements OnYearSelectedListener {
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private FrameLayout bottomSheet;
    private FirebaseFirestore db;
    private RecyclerView adableRecycler, selectedRecycler,adableCastRecycler,selectedActorsRecycler,releaseYearRecycler,resRecycler,searchRecycler;
    private ProgressBar pbCategory,pbResults;
    private TextView txMin,txMax,txResults;
    private CheckBox cbCategory,cbCast;
    private SeekBar skMin,skMax;
    private EditText editCategory,editCast,editSearch;
    private FilterCategoryAdapter availableAdapter, selectedAdapter;
    private List<CategoryItem> availableCategories, selectedCategories;
    private CastMemberAdapter adapter;
    private SearchMovieAdapter searchAdapter;
    private SelectedActorsAdapter selectedActorsAdapter;
    private ReleaseYearAdapter releaseYearAdapter;
    private List<String> years,selectedCastName,allAvailableYears;
    private List<CastMember> allCastMembers;
    private List<MovieSearchResult> allMovies;
    private Button filterBtn,showFiltersBtn;
    private ImageView bkButton;
    private Handler handler;
    private Runnable updateFilterRunnable;
    private RadioButton rbCategories,rbCast,rbReleaseYear;
    List<Movie> filteredMovies;

    private float selectedMinRating,selectedMaxRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

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
        allMovies = new ArrayList<>();
        searchRecycler = findViewById(R.id.search_recycler);
        filteredMovies = new ArrayList<>();
        resRecycler = findViewById(R.id.results_recycler);
        pbResults = findViewById(R.id.pb_results);
        cbCategory = findViewById(R.id.cb_category);
        cbCast = findViewById(R.id.cb_cast);
        filterBtn = findViewById(R.id.filter_button);
        txResults = findViewById(R.id.tx_results);
        txMin = findViewById(R.id.tx_min_rating);
        txMax = findViewById(R.id.tx_max_rating);
        skMin = findViewById(R.id.seekbar_min);
        skMax = findViewById(R.id.seekbar_max);
        rbCategories = findViewById(R.id.radio_categories);
        rbCast = findViewById(R.id.radio_cast);
        rbReleaseYear = findViewById(R.id.radio_release_year);
        releaseYearRecycler = findViewById(R.id.release_year_recycler);
        selectedActorsRecycler = findViewById(R.id.selected_cast_recycler);
        editCast = findViewById(R.id.edit_cast);
        editSearch = findViewById(R.id.editSearch);
        adableCastRecycler = findViewById(R.id.addable_cast_recycler);
        editCategory = findViewById(R.id.edit_category);
        pbCategory = findViewById(R.id.pb_category);
        adableRecycler = findViewById(R.id.addable_category_recycler);
        selectedRecycler = findViewById(R.id.selected_category_recycler);
        bottomSheet = findViewById(R.id.bottom_sheet);
        selectedCastName = new ArrayList<>();
        allAvailableYears = new ArrayList<>();
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        db = FirebaseFirestore.getInstance();


        handler = new Handler();
        updateFilterRunnable = new Runnable() {
            @Override
            public void run() {
                updateFilter();
            }
        };

        initRatingSliders();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        showFiltersBtn = findViewById(R.id.filterButton);
        showFiltersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheet.setVisibility(View.GONE);
                } else {
                    bottomSheet.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        availableCategories = new ArrayList<>();
        selectedCategories = new ArrayList<>();

        availableAdapter = new FilterCategoryAdapter(availableCategories, new FilterCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CategoryItem item) {
                moveItemToSelected(item);
            }
        });

        selectedAdapter = new FilterCategoryAdapter(selectedCategories, new FilterCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CategoryItem item) {
                moveItemToAvailable(item);
            }
        });

        adableRecycler.setAdapter(availableAdapter);
        adableRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        selectedRecycler.setAdapter(selectedAdapter);
        selectedRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        initCategoryRecycler();
        initSearchFunctionality();

        allCastMembers = new ArrayList<>();
        adapter = new CastMemberAdapter(allCastMembers, new CastMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CastMember castMember) {
                selectedActorsAdapter.addActor(castMember.getName());
                selectedCastName.add(castMember.getName());
                updateRadioButtons();
                txResults.setVisibility(View.GONE);
                pbResults.setVisibility(View.VISIBLE);
                handler.removeCallbacks(updateFilterRunnable);
                handler.postDelayed(updateFilterRunnable, 3000);
            }
        });
        adableCastRecycler.setAdapter(adapter);
        adableCastRecycler.setLayoutManager(new LinearLayoutManager(this));
        adableCastRecycler.setVisibility(View.GONE);
        initCastRecycler();


        searchAdapter = new SearchMovieAdapter(allMovies, new SearchMovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieSearchResult movie) {
                Intent intent = new Intent(BrowseActivity.this, DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ITEM_TITLE", movie.getTitle());
                intent.putExtras(bundle);
                BrowseActivity.this.startActivity(intent);
            }
        });
        searchRecycler.setAdapter(searchAdapter);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchRecycler.setVisibility(View.GONE);
        initSearchRecycler();

        years = new ArrayList<>();
        releaseYearAdapter = new ReleaseYearAdapter(years,this);
        releaseYearRecycler.setAdapter(releaseYearAdapter);
        initYearRecycler();

        selectedActorsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedActorsAdapter = new SelectedActorsAdapter();
        selectedActorsAdapter.setOnItemClickListener(new SelectedActorsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String actorName) {
                selectedActorsAdapter.removeActor(actorName);
                selectedCastName.remove(actorName);
                updateRadioButtons();
                txResults.setVisibility(View.GONE);
                pbResults.setVisibility(View.VISIBLE);
                handler.removeCallbacks(updateFilterRunnable);
                handler.postDelayed(updateFilterRunnable, 3000);
            }
        });
        selectedActorsRecycler.setAdapter(selectedActorsAdapter);

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rbCategories.isChecked()){
                    displayResultsCategories();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else if(rbCast.isChecked()){
                    displayResultsCast();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else if(rbReleaseYear.isChecked()){
                    displayResultsReleaseYear();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }else {
                    displayResults();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });
        cbCast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateRadioButtons();
                txResults.setVisibility(View.GONE);
                pbResults.setVisibility(View.VISIBLE);
                handler.removeCallbacks(updateFilterRunnable);
                handler.postDelayed(updateFilterRunnable, 3000);
            }
        });

        cbCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateRadioButtons();
                txResults.setVisibility(View.GONE);
                pbResults.setVisibility(View.VISIBLE);
                handler.removeCallbacks(updateFilterRunnable);
                handler.postDelayed(updateFilterRunnable, 3000);
            }
        });

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void updateRadioButtons(){
        if (!cbCast.isChecked()){
            if(selectedCastName.size()>1){
                rbCast.setEnabled(true);
            } else {
                rbCast.setChecked(false);
                rbCast.setEnabled(false);

            }
        } else {
            rbCast.setChecked(false);
            rbCast.setEnabled(false);
        }
        if(!cbCategory.isChecked()){
            if(selectedCategories.size()>1){
                rbCategories.setEnabled(true);
            }else {
                rbCategories.setChecked(false);
                rbCategories.setEnabled(false);
            }
        } else{
            rbCategories.setChecked(false);
            rbCategories.setEnabled(false);
        }
        if(releaseYearAdapter.getSelectedYears().size()>1){
            rbReleaseYear.setEnabled(true);
        }else {
            rbReleaseYear.setChecked(false);
            rbReleaseYear.setEnabled(false);
        }

    }

    private void updateFilter() {

        List<String> categories = new ArrayList<>();
        for(int i = 0; i < selectedCategories.size();i++){
            categories.add(selectedCategories.get(i).getDen());
        }

        Set<Integer> selectedYears = new HashSet<>();
        List<String> years = new ArrayList<>(releaseYearAdapter.getSelectedYears());
        for(int i = 0; i < years.size();i++){
            selectedYears.add(Integer.parseInt(years.get(i)));
        }
        Log.d("CB",""+cbCategory.isChecked());
        MovieQueryHelper movieQueryHelper = new MovieQueryHelper();
                movieQueryHelper.getFilteredMovies(selectedMinRating, selectedMaxRating, categories,cbCategory.isChecked(), selectedCastName,cbCast.isChecked(),selectedYears , new MovieQueryHelper.OnMoviesRetrievedListener() {
            @Override
            public void onMoviesRetrieved(List<Movie> movies) {
                Log.d("LISTA",""+movies.size());

                filteredMovies = movies;
                txResults.setText("Nr of results: "+ movies.size());
                pbResults.setVisibility(View.GONE);
                txResults.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(Exception e) {
                pbResults.setVisibility(View.GONE);
            }
        });
    }

    private void displayResultsReleaseYear(){
        Map<String, List<Movie>> categorizedMovies = organizeMoviesByReleaseYear(filteredMovies, new ArrayList<>(releaseYearAdapter.getSelectedYears()));
        MovieSectionedCategoryAdapter adapter = new MovieSectionedCategoryAdapter(this, categorizedMovies);
        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(adapter);
    }
    private void displayResultsCategories(){
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < selectedCategories.size(); i++) {
            categories.add(selectedCategories.get(i).getDen());
        }
        Map<String, List<Movie>> categorizedMovies = organizeMoviesByCategory(filteredMovies, categories);

        MovieSectionedCategoryAdapter adapter = new MovieSectionedCategoryAdapter(this, categorizedMovies);
        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(adapter);
    }
    private void displayResults(){

        MovieItemAdapter movieAdapter = new MovieItemAdapter(filteredMovies, this);
        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(movieAdapter);

        movieAdapter.notifyDataSetChanged();
    }
    private void displayResultsCast() {


        Map<String, List<Movie>> castMoviesMap = organizeMoviesByCast(filteredMovies, selectedCastName);


        MovieSectionedCastAdapter adapter = new MovieSectionedCastAdapter(this, castMoviesMap);

        resRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resRecycler.setAdapter(adapter);


    }

    private void initYearRecycler() {
        db.collection("Ranges")
                .document("releaseYear")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<Long> yearList = (List<Long>) documentSnapshot.get("year");
                            if (yearList != null) {
                                for (Long year : yearList) {
                                    allAvailableYears.add(String.valueOf(year));
                                }
                                releaseYearAdapter.updateYearList(allAvailableYears);
                                releaseYearAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting document", e);
                    }
                });
    }


    private void initRatingSliders() {
        selectedMinRating = (float) skMin.getProgress()/10;
        selectedMaxRating = (float) skMax.getProgress()/10;
        skMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float floatValue = (float) progress / 10;
                selectedMinRating = floatValue;
                txMin.setText("Min: " +String.format("%.1f", floatValue));
                txResults.setVisibility(View.GONE);
                pbResults.setVisibility(View.VISIBLE);
                handler.removeCallbacks(updateFilterRunnable);
                handler.postDelayed(updateFilterRunnable, 3000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        skMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float floatValue = (float) progress / 10;
                selectedMaxRating = floatValue;
                txMax.setText("Max: " + String.format("%.1f", floatValue));
                txResults.setVisibility(View.GONE);
                pbResults.setVisibility(View.VISIBLE);
                handler.removeCallbacks(updateFilterRunnable);
                handler.postDelayed(updateFilterRunnable, 3000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void filterCast(String text){
        if (text.isEmpty()) {
            adableCastRecycler.setVisibility(View.GONE);

        } else {
            List<CastMember> filteredList = new ArrayList<>();
            for (CastMember member : allCastMembers) {
                if (member.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(member);
                }
            }
            adapter.filterList(filteredList);
            adableCastRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void initSearchRecycler(){
        db.collection("Movies")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String title = document.getString("title") != null ? document.getString("title") : "";
                            String imageUrl = document.getString("downlaoadURL") != null ? document.getString("downlaoadURL") : "";
                            allMovies.add(new MovieSearchResult(title,imageUrl));
                        }
                        searchAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting documents", e);
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

                        adapter.notifyDataSetChanged();
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
                        pbCategory.setVisibility(View.GONE);
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

    private void initSearchFunctionality() {
        editCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void filter(String text) {
        List<CategoryItem> filteredList = new ArrayList<>();
        for (CategoryItem item : availableAdapter.getOriginalList()) {
            if (item.getDen().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        availableAdapter.filterList(filteredList);
    }

    private void filterSearch(String text) {
        if (text.isEmpty()) {
            searchRecycler.setVisibility(View.GONE);
            showFiltersBtn.setVisibility(View.VISIBLE);
            resRecycler.setVisibility(View.VISIBLE);
        } else {
            List<MovieSearchResult> filteredList = new ArrayList<>();
            for (MovieSearchResult member : allMovies) {
                if (member.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(member);
                }
            }
            searchAdapter.filterList(filteredList);
            showFiltersBtn.setVisibility(View.GONE);
            resRecycler.setVisibility(View.GONE);
            searchRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void moveItemToSelected(CategoryItem item) {
        List<CategoryItem> originalList = availableAdapter.getOriginalList();
        originalList.remove(item);
        availableAdapter.updateOriginalList(originalList);

        item.setIcon(R.drawable.x);
        selectedCategories.add(item);
        updateRadioButtons();

        availableAdapter.notifyDataSetChanged();
        selectedAdapter.notifyDataSetChanged();

        txResults.setVisibility(View.GONE);
        pbResults.setVisibility(View.VISIBLE);
        handler.removeCallbacks(updateFilterRunnable);
        handler.postDelayed(updateFilterRunnable, 3000);

    }

    private void moveItemToAvailable(CategoryItem item) {

        selectedCategories.remove(item);

        item.setIcon(R.drawable.plus);
        List<CategoryItem> originalList = availableAdapter.getOriginalList();
        originalList.add(item);
        availableAdapter.updateOriginalList(originalList);
        updateRadioButtons();
        availableAdapter.notifyDataSetChanged();
        selectedAdapter.notifyDataSetChanged();

        txResults.setVisibility(View.GONE);
        pbResults.setVisibility(View.VISIBLE);
        handler.removeCallbacks(updateFilterRunnable);
        handler.postDelayed(updateFilterRunnable, 3000);
    }

    @Override
    public void onYearSelected(int year) {
        updateRadioButtons();
        txResults.setVisibility(View.GONE);
        pbResults.setVisibility(View.VISIBLE);
        handler.removeCallbacks(updateFilterRunnable);
        handler.postDelayed(updateFilterRunnable, 3000);
    }

    @Override
    public void onYearUnselected(int year) {
        updateRadioButtons();
        txResults.setVisibility(View.GONE);
        pbResults.setVisibility(View.VISIBLE);
        handler.removeCallbacks(updateFilterRunnable);
        handler.postDelayed(updateFilterRunnable, 3000);
    }
    private Map<String, List<Movie>> organizeMoviesByCategory(List<Movie> movies, List<String> selectedCategories) {
        Map<String, List<Movie>> categorizedMovies = new HashMap<>();

        for (String category : selectedCategories) {
            categorizedMovies.put(category, new ArrayList<>());
        }

        for (Movie movie : movies) {
            for (String category : movie.getGenres()) {
                if (categorizedMovies.containsKey(category)) {
                    categorizedMovies.get(category).add(movie);
                }
            }
        }

        return categorizedMovies;
    }

    private Map<String, List<Movie>> organizeMoviesByReleaseYear(List<Movie> movies, List<String> selectedRealeaseYear) {
        Map<String, List<Movie>> categorizedMovies = new HashMap<>();

        for (String year : selectedRealeaseYear) {
            categorizedMovies.put(year, new ArrayList<>());
            Log.d("Year",year);
        }

        for (Movie movie : movies) {
                if (categorizedMovies.containsKey(Integer.toString(movie.getReleaseYear()))) {
                    Log.d("Film",movie.getTitle());
                    categorizedMovies.get(Integer.toString(movie.getReleaseYear())).add(movie);
                }

        }

        return categorizedMovies;
    }

    public Map<String, List<Movie>> organizeMoviesByCast(List<Movie> movies, List<String> selectedCastNames) {
        Map<String, List<Movie>> castMoviesMap = new HashMap<>();

        for (String castName : selectedCastNames) {
            List<Movie> moviesWithCast = new ArrayList<>();

            for (Movie movie : movies) {
                List<Map<String, Object>> castList = movie.getCast();
                if (castList != null) {
                    for (Map<String, Object> castMember : castList) {
                        String name = (String) castMember.get("name");
                        if (castName.equals(name)) {
                            moviesWithCast.add(movie);
                            Log.d("Film",movie.getTitle());
                            break;
                        }
                    }
                }
            }

            if (!moviesWithCast.isEmpty()) {
                castMoviesMap.put(castName, moviesWithCast);
            }
        }

        return castMoviesMap;
    }

}
