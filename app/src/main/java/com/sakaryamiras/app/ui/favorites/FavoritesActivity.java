package com.sakaryamiras.app.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.LocationAdapter;
import com.sakaryamiras.app.repository.EraRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.ui.detail.LocationDetailActivity;
import com.sakaryamiras.app.util.FavoritesManager;

import java.util.ArrayList;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private LocationAdapter adapter;

    private final LocationRepository locationRepo = new LocationRepository();
    private final EraRepository eraRepo = new EraRepository();
    private FavoritesManager favoritesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        favoritesManager = new FavoritesManager(this);
        recyclerView = findViewById(R.id.favorites_list);
        emptyView = findViewById(R.id.empty_view);

        adapter = new LocationAdapter(location -> {
            Intent intent = new Intent(this, LocationDetailActivity.class);
            intent.putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, location.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        eraRepo.getAll().addOnSuccessListener(adapter::setEras);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        Set<String> favoriteIds = favoritesManager.getAll();
        if (favoriteIds.isEmpty()) {
            adapter.setItems(new ArrayList<>());
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        locationRepo.getByIds(new ArrayList<>(favoriteIds))
                .addOnSuccessListener(locations -> {
                    if (locations.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setItems(locations);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, R.string.error_generic,
                        Toast.LENGTH_SHORT).show());
    }
}
