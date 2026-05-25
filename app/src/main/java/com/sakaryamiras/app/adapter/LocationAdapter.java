package com.sakaryamiras.app.adapter;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.util.EraColorUtil;
import com.sakaryamiras.app.util.LocaleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    public interface OnLocationClickListener {
        void onLocationClick(@NonNull Location location);
    }

    public interface OnLocationActionListener {
        void onEdit(@NonNull Location location);

        void onDelete(@NonNull Location location);
    }

    private final List<Location> items = new ArrayList<>();
    private final Map<String, Era> erasById = new HashMap<>();
    private final boolean adminMode;

    @Nullable
    private final OnLocationClickListener clickListener;
    @Nullable
    private final OnLocationActionListener actionListener;

    public LocationAdapter(@Nullable OnLocationClickListener clickListener) {
        this(clickListener, null, false);
    }

    public LocationAdapter(@Nullable OnLocationClickListener clickListener,
                           @Nullable OnLocationActionListener actionListener,
                           boolean adminMode) {
        this.clickListener = clickListener;
        this.actionListener = actionListener;
        this.adminMode = adminMode;
    }

    public void setItems(@NonNull List<Location> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setEras(@NonNull List<Era> eras) {
        erasById.clear();
        for (Era e : eras) {
            erasById.put(e.getId(), e);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location loc = items.get(position);
        holder.bind(loc, erasById.get(loc.getEraId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;
        final TextView eraBadge;
        final TextView district;
        final TextView year;
        final LinearLayout adminActions;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.location_image);
            name = itemView.findViewById(R.id.location_name);
            eraBadge = itemView.findViewById(R.id.location_era);
            district = itemView.findViewById(R.id.location_district);
            year = itemView.findViewById(R.id.location_year);
            adminActions = itemView.findViewById(R.id.admin_actions);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(@NonNull Location loc, @Nullable Era era) {
            String displayName = LocaleUtil.localizedName(itemView.getContext(), loc);
            name.setText(displayName != null ? displayName : "");
            district.setText(loc.getDistrict() != null ? loc.getDistrict() : "");
            year.setText(loc.getBuiltYear() != null ? String.valueOf(loc.getBuiltYear()) : "");

            String imageUrl = loc.getCurrentImageUrl() != null
                    ? loc.getCurrentImageUrl() : loc.getHistoricalImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(image.getContext()).load(imageUrl).into(image);
            } else {
                image.setImageDrawable(null);
            }

            if (era != null) {
                eraBadge.setVisibility(View.VISIBLE);
                eraBadge.setText(era.getName());
                Drawable bg = eraBadge.getBackground();
                if (bg instanceof GradientDrawable) {
                    ((GradientDrawable) bg.mutate()).setColor(EraColorUtil.colorOf(era));
                }
            } else {
                eraBadge.setVisibility(View.GONE);
            }

            adminActions.setVisibility(adminMode ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onLocationClick(loc);
            });

            if (adminMode && actionListener != null) {
                btnEdit.setOnClickListener(v -> actionListener.onEdit(loc));
                btnDelete.setOnClickListener(v -> actionListener.onDelete(loc));
            }
        }
    }
}
