package com.example.moviegram.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviegram.Activities.DetailActivity;
import com.example.moviegram.Objects.SliderItem;
import com.example.moviegram.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
    private List<SliderItem> sliderItems;
    private ViewPager2 viewPager2;
    private Context context;
    private int currentPosition = 0;

    public SliderAdapter(List<SliderItem> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
    }
    @NonNull
    @Override
    public SliderAdapter.SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new SliderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.main_slider_container,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SliderAdapter.SliderViewHolder holder, int position) {
        SliderItem item = sliderItems.get(position);
        holder.setImage(sliderItems.get(position).getImageUrl());
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ITEM_TITLE", item.getTitle());
                bundle.putString("START_LOCATION", "MainActivity");
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        if (position == currentPosition) {
            holder.sliderTitle.setVisibility(View.VISIBLE);
            holder.sliderRating.setVisibility(View.VISIBLE);
            holder.sliderDuration.setVisibility(View.VISIBLE);
            holder.sliderPlot.setVisibility(View.VISIBLE);
            holder.gradient.setVisibility(View.VISIBLE);
            holder.gradientLeft.setVisibility(View.VISIBLE);
            holder.gradientRight.setVisibility(View.GONE);


        } else {
            holder.sliderTitle.setVisibility(View.GONE);
            holder.sliderRating.setVisibility(View.GONE);
            holder.sliderDuration.setVisibility(View.GONE);
            holder.sliderPlot.setVisibility(View.GONE);
            holder.gradient.setVisibility(View.GONE);
            holder.gradientLeft.setVisibility(View.GONE);
            holder.gradientRight.setVisibility(View.VISIBLE);

        }

        holder.sliderTitle.setText(sliderItems.get(position).getTitle());
        holder.sliderRating.setText(sliderItems.get(position).getAggregateRating());
        holder.sliderDuration.setText(sliderItems.get(position).getReleaseYear());
        holder.sliderPlot.setText(sliderItems.get(position).getMainPlot());
        if (position == sliderItems.size() - 2) {
            viewPager2.post(runnable);
        }
    }

    public void setCurrentPosition(int position) {
        currentPosition = position;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return sliderItems.size();
    }


    public class SliderViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView,gradient,gradientRight,gradientLeft;
        private TextView sliderTitle, sliderRating, sliderDuration,sliderPlot;
        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlider);
            gradient = itemView.findViewById(R.id.gradient);
            gradientRight = itemView.findViewById(R.id.gradient_right);
            gradientLeft = itemView.findViewById(R.id.gradient_left);
            sliderTitle = itemView.findViewById(R.id.slider_title);
            sliderRating = itemView.findViewById(R.id.slider_rating);
            sliderDuration = itemView.findViewById(R.id.slider_duration);
            sliderPlot = itemView.findViewById(R.id.slider_main_plot);
        }
        void setImage(String imageUrl) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(60));

            Glide.with(context).load(imageUrl).apply(requestOptions).into(imageView);
        }
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sliderItems.addAll(sliderItems);
            notifyDataSetChanged();
        }
    };
}
