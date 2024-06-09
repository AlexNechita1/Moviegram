package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.moviegram.Objects.CategoryItem;
import com.example.moviegram.R;

public class FilterCategoryAdapter extends RecyclerView.Adapter<FilterCategoryAdapter.SectionViewHolder> {
    private List<CategoryItem> itemList;
    private List<CategoryItem> originalList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CategoryItem item);
    }

    public FilterCategoryAdapter(List<CategoryItem> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.originalList = itemList;
        this.listener = listener;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView txTitle;
        ImageView icon;

        public SectionViewHolder(View itemView) {
            super(itemView);
            txTitle = itemView.findViewById(R.id.txTitle);
            icon = itemView.findViewById(R.id.sign);
        }
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_filter_category, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        CategoryItem item = itemList.get(position);
        holder.txTitle.setText(item.getDen());
        holder.icon.setImageResource(item.getIcon());
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void filterList(List<CategoryItem> filteredList) {
        itemList = filteredList;
        notifyDataSetChanged();
    }

    public void updateOriginalList(List<CategoryItem> newOriginalList) {
        this.originalList = newOriginalList;
        this.itemList = new ArrayList<>(newOriginalList);
        notifyDataSetChanged();
    }

    public List<CategoryItem> getOriginalList() {
        return originalList;
    }
}
