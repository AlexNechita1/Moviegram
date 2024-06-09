package com.example.moviegram.Objects;

public class CastItem {
    private String castName,imageUrl;

    public CastItem(String castName, String imageUrl) {
        this.castName = castName;
        this.imageUrl = imageUrl;
    }

    public String getCastName() {
        return castName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
