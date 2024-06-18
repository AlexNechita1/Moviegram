package com.example.moviegram.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviegram.Adapters.PostAdapter;
import com.example.moviegram.Objects.Post;
import com.example.moviegram.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {
    private RecyclerView postRecycler;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private ImageView bkButton;
    private DatabaseReference postsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        initView();
    }

    private void initView() {
        postRecycler = findViewById(R.id.posts_recycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList,this);
        postRecycler.setAdapter(postAdapter);

        bkButton = findViewById(R.id.backBtn);
        bkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://moviegram-f31cf-default-rtdb.europe-west1.firebasedatabase.app/");
        postsRef = database.getReference("posts");

        fetchPosts();
    }

    private void fetchPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
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

                    Post post = new Post(key,uid,author, timestamp, imageUrl, authorImageUrl, title, content,movieTitle,likes);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}