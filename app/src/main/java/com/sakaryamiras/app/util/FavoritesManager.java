package com.sakaryamiras.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {

    private static final String PREFS_NAME = "sakarya_miras_favorites";
    private static final String KEY_FAVORITES = "favorite_location_ids";

    private final SharedPreferences prefs;

    public FavoritesManager(@NonNull Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public Set<String> getAll() {
        Set<String> stored = prefs.getStringSet(KEY_FAVORITES, null);
        return stored == null ? new HashSet<>() : new HashSet<>(stored);
    }

    public boolean isFavorite(@NonNull String locationId) {
        return getAll().contains(locationId);
    }

    public boolean toggle(@NonNull String locationId) {
        Set<String> favorites = getAll();
        boolean nowFavorite;
        if (favorites.contains(locationId)) {
            favorites.remove(locationId);
            nowFavorite = false;
        } else {
            favorites.add(locationId);
            nowFavorite = true;
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
        return nowFavorite;
    }

    public void add(@NonNull String locationId) {
        Set<String> favorites = getAll();
        if (favorites.add(locationId)) {
            prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
        }
    }

    public void remove(@NonNull String locationId) {
        Set<String> favorites = getAll();
        if (favorites.remove(locationId)) {
            prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
        }
    }

    public void clear() {
        prefs.edit().remove(KEY_FAVORITES).apply();
    }

    public int count() {
        return getAll().size();
    }
}
