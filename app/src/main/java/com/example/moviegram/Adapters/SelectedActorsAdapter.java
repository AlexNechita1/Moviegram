package com.example.moviegram.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviegram.R;

import java.util.ArrayList;
import java.util.List;

public class SelectedActorsAdapter extends RecyclerView.Adapter<SelectedActorsAdapter.ActorViewHolder> {
    private List<String> selectedActors = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String actorName);
    }

    public void setSelectedActors(List<String> actors) {
        selectedActors.clear();
        selectedActors.addAll(actors);
        notifyDataSetChanged();
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void addActor(String actorName) {
        if (!selectedActors.contains(actorName)) {
            selectedActors.add(actorName);
            notifyDataSetChanged();
        }
    }

    public void removeActor(String actorName) {
        selectedActors.remove(actorName);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_actor, parent, false);
        return new ActorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActorViewHolder holder, int position) {
        String actorName = selectedActors.get(position);
        holder.bind(actorName);
    }

    @Override
    public int getItemCount() {
        return selectedActors.size();
    }

    public class ActorViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSelectedActorName;

        public ActorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSelectedActorName = itemView.findViewById(R.id.text_selected_actor_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        String actorName = selectedActors.get(position);
                        listener.onItemClick(actorName);
                    }
                }
            });
        }


        public void bind(String actorName) {
            textViewSelectedActorName.setText(actorName);
        }
    }
}
