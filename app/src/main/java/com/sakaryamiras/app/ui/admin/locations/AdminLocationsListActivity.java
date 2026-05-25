package com.sakaryamiras.app.ui.admin.locations;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.LocationAdapter;
import com.sakaryamiras.app.repository.EraRepository;
import com.sakaryamiras.app.repository.LocationRepository;

public class AdminLocationsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private LocationAdapter adapter;

    private final LocationRepository locationRepo = new LocationRepository();
    private final EraRepository eraRepo = new EraRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.admin_locations);
        }

        recyclerView = findViewById(R.id.list);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        emptyView = findViewById(R.id.empty_view);

        adapter = new LocationAdapter(
                location -> openForm(location.getId()),
                new LocationAdapter.OnLocationActionListener() {
                    @Override
                    public void onEdit(com.sakaryamiras.app.model.Location location) {
                        openForm(location.getId());
                    }

                    @Override
                    public void onDelete(com.sakaryamiras.app.model.Location location) {
                        confirmDelete(location.getId());
                    }
                },
                true
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> openForm(null));

        swipeRefresh.setOnRefreshListener(this::load);

        eraRepo.getAll().addOnSuccessListener(adapter::setEras);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        locationRepo.getAll()
                .addOnSuccessListener(locations -> {
                    swipeRefresh.setRefreshing(false);
                    if (locations.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setItems(locations);
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                });
    }

    private void openForm(String locationId) {
        Intent intent = new Intent(this, AdminLocationFormActivity.class);
        if (locationId != null) {
            intent.putExtra(AdminLocationFormActivity.EXTRA_LOCATION_ID, locationId);
        }
        startActivity(intent);
    }

    private void confirmDelete(String locationId) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (d, w) -> deleteLocation(locationId))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteLocation(String locationId) {
        locationRepo.delete(locationId)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, R.string.form_save_success, Toast.LENGTH_SHORT).show();
                    load();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        getString(R.string.form_save_error, e.getMessage()),
                        Toast.LENGTH_LONG).show());
    }
}
