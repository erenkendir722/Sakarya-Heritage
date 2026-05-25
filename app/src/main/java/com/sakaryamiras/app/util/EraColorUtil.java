package com.sakaryamiras.app.util;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sakaryamiras.app.model.Era;

public final class EraColorUtil {

    public static final int DEFAULT_COLOR = Color.parseColor("#666666");

    private EraColorUtil() {
    }

    public static int parseHex(@Nullable String hex) {
        if (hex == null || hex.isEmpty()) return DEFAULT_COLOR;
        try {
            String value = hex.startsWith("#") ? hex : "#" + hex;
            return Color.parseColor(value);
        } catch (IllegalArgumentException e) {
            return DEFAULT_COLOR;
        }
    }

    public static int colorOf(@Nullable Era era) {
        if (era == null) return DEFAULT_COLOR;
        return parseHex(era.getColorHex());
    }

    @NonNull
    public static String formatYearRange(@Nullable Era era) {
        if (era == null) return "";
        Integer start = era.getStartYear();
        Integer end = era.getEndYear();
        if (start == null && end == null) return "";
        if (start == null) return "→ " + end;
        if (end == null) return start + " →";
        return start + " — " + end;
    }
}
