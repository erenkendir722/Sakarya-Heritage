package com.sakaryamiras.app.adapter;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.util.EraColorUtil;
import com.sakaryamiras.app.util.LocaleUtil;

import java.util.ArrayList;
import java.util.List;

public class EraAdapter extends RecyclerView.Adapter<EraAdapter.EraViewHolder> {

    public interface OnEraActionListener {
        void onEdit(@NonNull Era era);

        void onDelete(@NonNull Era era);
    }

    private final List<Era> items = new ArrayList<>();
    @Nullable
    private final OnEraActionListener listener;

    public EraAdapter(@Nullable OnEraActionListener listener) {
        this.listener = listener;
    }

    public void setItems(@NonNull List<Era> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_era, parent, false);
        return new EraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EraViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class EraViewHolder extends RecyclerView.ViewHolder {
        final View colorSwatch;
        final TextView nameView;
        final TextView yearsView;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        EraViewHolder(@NonNull View itemView) {
            super(itemView);
            colorSwatch = itemView.findViewById(R.id.color_swatch);
            nameView = itemView.findViewById(R.id.era_name);
            yearsView = itemView.findViewById(R.id.era_years);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(@NonNull Era era) {
            nameView.setText(LocaleUtil.localizedEraName(itemView.getContext(), era));

            StringBuilder years = new StringBuilder();
            if (era.getStartYear() != null) years.append(era.getStartYear());
            if (era.getStartYear() != null || era.getEndYear() != null) years.append(" — ");
            if (era.getEndYear() != null) years.append(era.getEndYear());
            yearsView.setText(years.toString());

            Drawable bg = colorSwatch.getBackground();
            if (bg instanceof GradientDrawable) {
                ((GradientDrawable) bg.mutate()).setColor(EraColorUtil.colorOf(era));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(era);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(era);
            });
        }
    }
}
