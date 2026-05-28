package com.sakaryamiras.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.model.RouteStop;
import com.sakaryamiras.app.util.LocaleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteStopAdapter extends RecyclerView.Adapter<RouteStopAdapter.StopViewHolder> {

    public interface OnStopClickListener {
        void onStopClick(@NonNull RouteStop stop, @Nullable Location location);
    }

    private final List<RouteStop> items = new ArrayList<>();
    private final Map<String, Location> locationsById = new HashMap<>();
    @Nullable
    private final OnStopClickListener listener;

    public RouteStopAdapter(@Nullable OnStopClickListener listener) {
        this.listener = listener;
    }

    public void setStops(@NonNull List<RouteStop> stops) {
        items.clear();
        items.addAll(stops);
        notifyDataSetChanged();
    }

    public void setLocations(@NonNull List<Location> locations) {
        locationsById.clear();
        for (Location loc : locations) {
            locationsById.put(loc.getId(), loc);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_stop, parent, false);
        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        RouteStop stop = items.get(position);
        Location location = locationsById.get(stop.getLocationId());
        holder.bind(stop, location, position + 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class StopViewHolder extends RecyclerView.ViewHolder {
        final TextView orderView;
        final TextView nameView;
        final TextView noteView;
        final TextView timeView;

        StopViewHolder(@NonNull View itemView) {
            super(itemView);
            orderView = itemView.findViewById(R.id.stop_order);
            nameView = itemView.findViewById(R.id.stop_name);
            noteView = itemView.findViewById(R.id.stop_note);
            timeView = itemView.findViewById(R.id.stop_time);
        }

        void bind(@NonNull RouteStop stop, @Nullable Location location, int displayOrder) {
            orderView.setText(String.valueOf(displayOrder));
            String localized = LocaleUtil.localizedName(itemView.getContext(), location);
            nameView.setText(localized != null ? localized : "—");

            if (stop.getNote() != null && !stop.getNote().isEmpty()) {
                noteView.setVisibility(View.VISIBLE);
                noteView.setText(stop.getNote());
            } else {
                noteView.setVisibility(View.GONE);
            }

            if (stop.getTimeAtStop() != null && stop.getTimeAtStop() > 0) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(itemView.getContext()
                        .getString(R.string.route_duration, stop.getTimeAtStop()));
            } else {
                timeView.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStopClick(stop, location);
            });
        }
    }
}
