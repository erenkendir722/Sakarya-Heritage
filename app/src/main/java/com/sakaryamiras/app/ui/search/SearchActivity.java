package com.sakaryamiras.app.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.LocationAdapter;
import com.sakaryamiras.app.model.Category;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.repository.CategoryRepository;
import com.sakaryamiras.app.repository.EraRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.ui.detail.LocationDetailActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText searchInput;
    private RecyclerView resultsView;
    private TextView emptyView;
    private LocationAdapter adapter;

    private final LocationRepository locationRepo = new LocationRepository();
    private final EraRepository eraRepo = new EraRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();

    private final List<Location> allLocations = new ArrayList<>();
    private final Map<String, Era> erasById = new HashMap<>();
    private final Map<String, Category> categoriesById = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        searchInput = findViewById(R.id.search_input);
        resultsView = findViewById(R.id.search_results);
        emptyView = findViewById(R.id.empty_view);

        adapter = new LocationAdapter(location -> {
            Intent intent = new Intent(this, LocationDetailActivity.class);
            intent.putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, location.getId());
            startActivity(intent);
        });
        resultsView.setLayoutManager(new LinearLayoutManager(this));
        resultsView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadData();
        searchInput.requestFocus();
    }

    private void loadData() {
        locationRepo.getAll()
                .addOnSuccessListener(locations -> {
                    allLocations.clear();
                    allLocations.addAll(locations);
                    applyFilter(searchInput.getText() != null
                            ? searchInput.getText().toString() : "");
                })
                .addOnFailureListener(e -> Toast.makeText(this, R.string.error_generic,
                        Toast.LENGTH_SHORT).show());

        eraRepo.getAll().addOnSuccessListener(list -> {
            erasById.clear();
            for (Era era : list) erasById.put(era.getId(), era);
            adapter.setEras(list);
        });

        categoryRepo.getAll().addOnSuccessListener(list -> {
            categoriesById.clear();
            for (Category c : list) categoriesById.put(c.getId(), c);
        });
    }

    private void applyFilter(String rawQuery) {
        String query = rawQuery.trim().toLowerCase(new Locale("tr", "TR"));
        if (query.isEmpty()) {
            adapter.setItems(new ArrayList<>());
            emptyView.setText(R.string.search_empty_initial);
            emptyView.setVisibility(View.VISIBLE);
            resultsView.setVisibility(View.GONE);
            return;
        }

        List<Location> matched = new ArrayList<>();
        for (Location loc : allLocations) {
            if (matches(loc, query)) matched.add(loc);
        }

        if (matched.isEmpty()) {
            emptyView.setText(R.string.search_no_results);
            emptyView.setVisibility(View.VISIBLE);
            resultsView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            resultsView.setVisibility(View.VISIBLE);
            adapter.setItems(matched);
        }
    }

    private boolean matches(Location loc, String query) {
        if (contains(loc.getName(), query)) return true;
        if (contains(loc.getNameEn(), query)) return true;
        if (contains(loc.getDescription(), query)) return true;
        if (contains(loc.getDescriptionEn(), query)) return true;
        if (contains(loc.getDistrict(), query)) return true;

        Era era = loc.getEraId() != null ? erasById.get(loc.getEraId()) : null;
        if (era != null) {
            if (contains(era.getName(), query)) return true;
            if (contains(era.getNameEn(), query)) return true;
        }

        Category category = loc.getCategoryId() != null
                ? categoriesById.get(loc.getCategoryId()) : null;
        if (category != null) {
            if (contains(category.getName(), query)) return true;
            if (contains(category.getNameEn(), query)) return true;
        }

        return false;
    }

    private boolean contains(String haystack, String needle) {
        if (haystack == null) return false;
        return haystack.toLowerCase(new Locale("tr", "TR")).contains(needle);
    }
}
