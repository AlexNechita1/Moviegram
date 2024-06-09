package com.example.moviegram.Objects;

public class Post {
    private String author,timestamp,imageUrl,authorImageUrl,title,content,movieTitle;

    public Post(String author, String timestamp, String imageUrl, String authorImageUrl, String title, String content, String movieTitle) {
        this.author = author;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.authorImageUrl = authorImageUrl;
        this.title = title;
        this.content = content;
        this.movieTitle = movieTitle;
    }

    public Post() {
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
