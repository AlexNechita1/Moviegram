package com.example.moviegram.Objects;

public class MovieSearchResult {
    private String title,url;

    public MovieSearchResult(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
