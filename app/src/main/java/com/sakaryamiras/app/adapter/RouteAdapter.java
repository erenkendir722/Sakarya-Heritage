package com.sakaryamiras.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.HeritageRoute;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(@NonNull HeritageRoute route);
    }

    private final List<HeritageRoute> items = new ArrayList<>();
    @Nullable
    private final OnRouteClickListener listener;

    public RouteAdapter(@Nullable OnRouteClickListener listener) {
        this.listener = listener;
    }

    public void setItems(@NonNull List<HeritageRoute> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class RouteViewHolder extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView name;
        final TextView description;
        final TextView duration;
        final TextView distance;
        final TextView difficulty;

        RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.route_cover);
            name = itemView.findViewById(R.id.route_name);
            description = itemView.findViewById(R.id.route_description);
            duration = itemView.findViewById(R.id.route_duration);
            distance = itemView.findViewById(R.id.route_distance);
            difficulty = itemView.findViewById(R.id.route_difficulty);
        }

        void bind(@NonNull HeritageRoute route) {
            name.setText(route.getName());
            description.setText(route.getDescription() != null ? route.getDescription() : "");

            if (route.getCoverImageUrl() != null && !route.getCoverImageUrl().isEmpty()) {
                Glide.with(cover.getContext()).load(route.getCoverImageUrl()).into(cover);
            } else {
                cover.setImageDrawable(null);
            }

            duration.setText(route.getDurationMinutes() != null
                    ? itemView.getContext().getString(R.string.route_duration, route.getDurationMinutes())
                    : "—");
            distance.setText(route.getDistanceKm() != null
                    ? itemView.getContext().getString(R.string.route_distance, route.getDistanceKm())
                    : "—");
            difficulty.setText(difficultyLabel(route.getDifficulty()));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRouteClick(route);
            });
        }

        private String difficultyLabel(@Nullable String difficulty) {
            if (difficulty == null) return "—";
            switch (difficulty) {
                case HeritageRoute.DIFFICULTY_EASY:
                    return itemView.getContext().getString(R.string.difficulty_easy);
                case HeritageRoute.DIFFICULTY_MEDIUM:
                    return itemView.getContext().getString(R.string.difficulty_medium);
                case HeritageRoute.DIFFICULTY_HARD:
                    return itemView.getContext().getString(R.string.difficulty_hard);
                default:
                    return difficulty;
            }
        }
    }
}
