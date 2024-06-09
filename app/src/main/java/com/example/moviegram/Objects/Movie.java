package com.example.moviegram.Objects;

import java.util.List;
import java.util.Map;

public class Movie {
    private float aggregateRating;
    private List<Map<String, Object>> cast;  // List of maps for cast members
    private String downlaoadURL;
    private List<String> genres;
    private String mainPlot;
    private List<String> plots;
    private String releaseDate;
    private int releaseYear;
    private String title;
    private int topRanking;
    private int voteCount;

    // Default constructor (required by Firestore)
    public Movie() {
    }

    // Getters and Setters
    public float getAggregateRating() {
        return aggregateRating;
    }

    public void setAggregateRating(float aggregateRating) {
        this.aggregateRating = aggregateRating;
    }

    public List<Map<String, Object>> getCast() {
        return cast;
    }

    public void setCast(List<Map<String, Object>> cast) {
        this.cast = cast;
    }

    public String getDownlaoadURL() {
        return downlaoadURL;
    }

    public void setDownlaoadURL(String downlaoadURL) {
        this.downlaoadURL = downlaoadURL;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getMainPlot() {
        return mainPlot;
    }

    public void setMainPlot(String mainPlot) {
        this.mainPlot = mainPlot;
    }

    public List<String> getPlots() {
        return plots;
    }

    public void setPlots(List<String> plots) {
        this.plots = plots;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTopRanking() {
        return topRanking;
    }

    public void setTopRanking(int topRanking) {
        this.topRanking = topRanking;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
}
