package com.example.moviegram.Objects;

public class Post {
    private String postkey,uid,author,timestamp,imageUrl,authorImageUrl,title,content,movieTitle,likes;

    public Post(String postkey,String uid,String author, String timestamp, String imageUrl, String authorImageUrl, String title, String content, String movieTitle,String likes) {
        this.uid = uid;
        this.author = author;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.authorImageUrl = authorImageUrl;
        this.title = title;
        this.content = content;
        this.movieTitle = movieTitle;
        this.likes = likes;
        this.postkey = postkey;
    }

    public Post() {
    }

    public String getPostkey() {
        return postkey;
    }

    public String getLikes() {
        return likes;
    }

    public String getUid() {
        return uid;
    }

    public String getAuthor() {
        return author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAuthorImageUrl() {
        return authorImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getMovieTitle() {
        return movieTitle;
    }
}
