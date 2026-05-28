package com.sakaryamiras.app.util;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sakaryamiras.app.model.Category;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.model.Location;

import java.util.Locale;

public final class LocaleUtil {

    private LocaleUtil() {
    }

    public static boolean isEnglish(@NonNull Context context) {
        Configuration config = context.getResources().getConfiguration();
        Locale locale = config.getLocales().get(0);
        return locale != null && "en".equalsIgnoreCase(locale.getLanguage());
    }

    @Nullable
    public static String localizedName(@NonNull Context context, @Nullable Location location) {
        if (location == null) return null;
        if (isEnglish(context) && hasText(location.getNameEn())) {
            return location.getNameEn();
        }
        return location.getName();
    }

    @Nullable
    public static String localizedDescription(@NonNull Context context, @Nullable Location location) {
        if (location == null) return null;
        if (isEnglish(context) && hasText(location.getDescriptionEn())) {
            return location.getDescriptionEn();
        }
        return location.getDescription();
    }

    @Nullable
    public static String localizedEraName(@NonNull Context context, @Nullable Era era) {
        if (era == null) return null;
        if (isEnglish(context) && hasText(era.getNameEn())) {
            return era.getNameEn();
        }
        return era.getName();
    }

    @Nullable
    public static String localizedCategoryName(@NonNull Context context, @Nullable Category category) {
        if (category == null) return null;
        if (isEnglish(context) && hasText(category.getNameEn())) {
            return category.getNameEn();
        }
        return category.getName();
    }

    private static boolean hasText(@Nullable String s) {
        return s != null && !s.isEmpty();
    }
}
