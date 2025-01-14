package com.example.moviegram.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Adapters.CastAdapter;
import com.example.moviegram.Adapters.MovieItemAdapter;
import com.example.moviegram.Adapters.MovieSectionedCastAdapter;
import com.example.moviegram.Adapters.PostAdapter;
import com.example.moviegram.Objects.CastItem;
import com.example.moviegram.Objects.Movie;
import com.example.moviegram.Objects.Post;
import com.example.moviegram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button signoutBtn;
    private FirebaseFirestore db;
    private DatabaseReference postsRef;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private String currentUserId;
    private ImageView backgroundImg,profileImg,settingsBtn,backBtn;
    private TextView usernameTx,titleTx,followersTx;
    private RecyclerView topMoviesRecycler,favoriteCastRecycler,postRecycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initView();
    }

    private void initView() {
        backBtn = findViewById(R.id.backBtn);
        settingsBtn = findViewById(R.id.settingBtn);
        favoriteCastRecycler = findViewById(R.id.favorite_actors_recycler);
        topMoviesRecycler = findViewById(R.id.top_recomandation_recycler);
        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        backgroundImg = findViewById(R.id.image_backgorund);
        profileImg = findViewById(R.id.profile_image);
        usernameTx = findViewById(R.id.tx_username);
        titleTx = findViewById(R.id.tx_title);
        followersTx = findViewById(R.id.tx_followers);
        signoutBtn = findViewById(R.id.button_signout);
        postRecycler = findViewById(R.id.posts_recycler);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(AccountActivity.this, IntroActivity.class));
            }
        });
        retrieveDatabase();
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        postRecycler.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList,this);
        postRecycler.setAdapter(postAdapter);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://moviegram-f31cf-default-rtdb.europe-west1.firebasedatabase.app/");
        postsRef = database.getReference("posts");
        fetchPosts();

    }

    private void fetchPosts() {
        Query query = postsRef.orderByChild("uid").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.child("uid").getValue(String.class);
                    String author = snapshot.child("author").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String authorImageUrl = snapshot.child("authorImageUrl").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);
                    String content = snapshot.child("content").getValue(String.class);
                    String movieTitle = snapshot.child("movieTitle").getValue(String.class);
                    String likes = snapshot.child("likes").getValue(String.class);
                    String key = snapshot.getKey();

                    Post post = new Post(key, uid, author, timestamp, imageUrl, authorImageUrl, title, content, movieTitle, likes);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Error fetching posts", databaseError.toException());
            }
        });
    }

    private void retrieveDatabase() {
        db.collection("Users").document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String backgroundPic = document.getString("profile_background") != null ? document.getString("profile_background") : "";
                            String profilePic = document.getString("profile_picture") != null ? document.getString("profile_picture") : "";
                            String username = document.getString("username") != null ? document.getString("username") : "";
                            String title = document.getString("title") != null ? document.getString("title") : "";
                            String followers = String.valueOf((int) Math.floor(document.getDouble("followers")));

                            List<String> favoriteActors = document.contains("favorite_actors") ? (List<String>) document.get("favorite_actors") : new ArrayList<>();
                            List<String> topMovies = document.contains("top_movies") ? (List<String>) document.get("top_movies") : new ArrayList<>();

                            usernameTx.setText(username);
                            titleTx.setText(title);
                            followersTx.setText(followers);

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

                        } else {

                        }
                    } else {

                    }
                });
    }

    private void fetchMovies(List<String> movieTitles) {
        List<Movie> movieList = new ArrayList<>();
        db.collection("Movies").whereIn("title", movieTitles).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movie movie = document.toObject(Movie.class);
                            movieList.add(movie);
                        }
                        MovieItemAdapter movieAdapter = new MovieItemAdapter(movieList, this);
                        topMoviesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        topMoviesRecycler.setAdapter(movieAdapter);

                        movieAdapter.notifyDataSetChanged();

                    } else {

                    }
                });
    }

    private void fetchActors(List<String> actorNames) {
        db.collection("Movies").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CastItem> castList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Object> cast = (List<Object>) document.get("cast");
                            if (cast != null) {
                                for (Object obj : cast) {
                                    if (obj instanceof Map) {
                                        Map<String, Object> castMap = (Map<String, Object>) obj;
                                        String name = (String) castMap.get("name");
                                        if (actorNames.contains(name)) {
                                            String url = (String) castMap.get("url");
                                            CastItem castItem = new CastItem(name, url);
                                            castList.add(castItem);
                                            actorNames.remove(name);
                                        }
                                    }
                                }
                            }
                        }


                        CastAdapter castAdapter = new CastAdapter(castList);
                        favoriteCastRecycler.setAdapter(castAdapter);
                        favoriteCastRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    } else {

                    }
                });
    }

}