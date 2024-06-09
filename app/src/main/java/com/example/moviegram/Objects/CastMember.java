package com.example.moviegram.Objects;

public class CastMember {
    private String name;
    private String imageUrl;

    public CastMember(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
