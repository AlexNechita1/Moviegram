package com.example.moviegram.Objects;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.widget.ImageView;

import com.example.moviegram.R;

public class CategoryItem {
    private String den;
    private int icon;
    public CategoryItem(String den) {
        this.den = den;
        this.icon = R.drawable.plus;
    }

    public String getDen() {
        return den;
    }
    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

}
