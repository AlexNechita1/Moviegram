package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Objects.CastItem;
import com.example.moviegram.R;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.SectionViewHolder> {
    private List<CastItem> itemList;

    public CastAdapter(List<CastItem> itemList) {
        this.itemList = itemList;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView castName;
        ImageView castImage;

        public SectionViewHolder(View itemView) {
            super(itemView);
            castName = itemView.findViewById(R.id.cast_name);
            castImage = itemView.findViewById(R.id.cast_image);
        }
    }
    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cast, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        CastItem item = itemList.get(position);
        Glide.with(holder.castImage.getContext())
                .load(item.getImageUrl())
                .into(holder.castImage);
        holder.castName.setText(item.getCastName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
