package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviegram.Objects.CastMember;
import com.example.moviegram.R;

import java.util.List;

public class CastMemberAdapter extends RecyclerView.Adapter<CastMemberAdapter.CastMemberViewHolder> {

    private List<CastMember> castMemberList;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(CastMember castMember);
    }

    public CastMemberAdapter(List<CastMember> castMemberList, OnItemClickListener listener) {
        this.castMemberList = castMemberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CastMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cast_member, parent, false);
        return new CastMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastMemberViewHolder holder, int position) {
        CastMember castMember = castMemberList.get(position);
        holder.tvActorName.setText(castMember.getName());
        Glide.with(holder.itemView.getContext())
                .load(castMember.getImageUrl())
                .into(holder.ivActorImage);
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(castMember);
            }
        });
    }

    @Override
    public int getItemCount() {
        return castMemberList.size();
    }

    public void filterList(List<CastMember> filteredList) {
        castMemberList = filteredList;
        notifyDataSetChanged();
    }

    static class CastMemberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivActorImage;
        TextView tvActorName;
        LinearLayout linearLayout;

        CastMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActorImage = itemView.findViewById(R.id.iv_actor_image);
            tvActorName = itemView.findViewById(R.id.tv_actor_name);
            linearLayout = itemView.findViewById(R.id.linearlayout);
        }
    }
}
