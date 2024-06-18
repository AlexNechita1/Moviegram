package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.moviegram.Objects.CategoryItem;
import com.example.moviegram.R;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.SectionViewHolder> {
    private List<CategoryItem> itemList,originalList;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(CategoryItem item);
    }
    public CategoryAdapter(List<CategoryItem> itemList) {
        this.itemList = itemList;
        this.originalList = itemList;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView txTitle;

        public SectionViewHolder(View itemView) {
            super(itemView);
            txTitle = itemView.findViewById(R.id.txTitle);
        }
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_category, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        CategoryItem item = itemList.get(position);
        holder.txTitle.setText(item.getDen());
        holder.txTitle.setOnClickListener(new View.OnClickListener() {
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

    public void updateOriginalList(List<CategoryItem> newOriginalList) {
        this.originalList = newOriginalList;
        this.itemList = new ArrayList<>(newOriginalList);
        notifyDataSetChanged();
    }
}

