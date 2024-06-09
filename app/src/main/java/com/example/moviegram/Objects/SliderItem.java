package com.example.moviegram.Objects;

import android.util.Log;

public class SliderItem {


    private String imageUrl;
    private String title;
    private String mainPlot;
    private String releaseYear;
    private String aggregateRating;

    public SliderItem(String imageUrl, String title, String mainPlot, String releaseYear, String aggregateRating) {

        this.imageUrl = imageUrl;
        this.title = title;
        this.mainPlot = mainPlot;
        this.releaseYear = releaseYear;
        this.aggregateRating = aggregateRating;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getMainPlot() {
        return mainPlot;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public String getAggregateRating() {
        return aggregateRating;
    }


}
