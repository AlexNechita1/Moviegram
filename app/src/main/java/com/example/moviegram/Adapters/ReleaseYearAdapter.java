package com.example.moviegram.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.moviegram.Activities.BrowseActivity;
import com.example.moviegram.Interfaces.OnYearSelectedListener;
import com.example.moviegram.R;

public class ReleaseYearAdapter extends RecyclerView.Adapter<ReleaseYearAdapter.SectionViewHolder> {
    private List<String> itemList;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnYearSelectedListener listener;

    public void updateYearList(List<String> newYears) {
        this.itemList.clear();
        this.itemList.addAll(newYears);
        notifyDataSetChanged();
    }

    public ReleaseYearAdapter(List<String> itemList, OnYearSelectedListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView txTitle;
        ConstraintLayout layout;
        ImageView icon;

        public SectionViewHolder(View itemView) {
            super(itemView);
            txTitle = itemView.findViewById(R.id.txTitle);
            layout = itemView.findViewById(R.id.layout);
            icon = itemView.findViewById(R.id.sign);
        }
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_release_year, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String item = itemList.get(position);
        int year = Integer.parseInt(item);
        holder.txTitle.setText(item);

        holder.icon.setVisibility(selectedPositions.contains(position) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    listener.onYearUnselected(year);

                } else {
                    selectedPositions.add(position);
                    listener.onYearSelected(year);
                }
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public Set<String> getSelectedYears() {
        Set<String> selectedYears = new HashSet<>();
        for (Integer position : selectedPositions) {
            selectedYears.add(itemList.get(position));
        }
        return selectedYears;
    }
}
