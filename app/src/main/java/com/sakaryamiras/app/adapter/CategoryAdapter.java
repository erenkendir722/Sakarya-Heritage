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
import com.sakaryamiras.app.model.Category;
import com.sakaryamiras.app.util.EraColorUtil;
import com.sakaryamiras.app.util.LocaleUtil;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(@NonNull Category category);

        void onDelete(@NonNull Category category);
    }

    private final List<Category> items = new ArrayList<>();
    @Nullable
    private final OnCategoryActionListener listener;

    public CategoryAdapter(@Nullable OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setItems(@NonNull List<Category> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        final View colorSwatch;
        final TextView nameView;
        final TextView iconView;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorSwatch = itemView.findViewById(R.id.color_swatch);
            nameView = itemView.findViewById(R.id.category_name);
            iconView = itemView.findViewById(R.id.category_icon);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(@NonNull Category category) {
            nameView.setText(LocaleUtil.localizedCategoryName(itemView.getContext(), category));
            iconView.setText(category.getIcon() != null ? category.getIcon() : "");

            Drawable bg = colorSwatch.getBackground();
            if (bg instanceof GradientDrawable) {
                ((GradientDrawable) bg.mutate()).setColor(EraColorUtil.parseHex(category.getColorHex()));
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(category);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(category);
            });
        }
    }
}
