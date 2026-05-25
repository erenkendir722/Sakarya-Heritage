package com.sakaryamiras.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.sakaryamiras.app.R;
import com.sakaryamiras.app.repository.AuthRepository;
import com.sakaryamiras.app.repository.CategoryRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.repository.RouteRepository;
import com.sakaryamiras.app.ui.admin.categories.AdminCategoriesActivity;
import com.sakaryamiras.app.ui.admin.locations.AdminLocationsListActivity;
import com.sakaryamiras.app.ui.admin.routes.AdminRoutesListActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView countLocations;
    private TextView countRoutes;
    private TextView countCategories;

    private final LocationRepository locationRepo = new LocationRepository();
    private final RouteRepository routeRepo = new RouteRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final AuthRepository authRepo = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        countLocations = findViewById(R.id.count_locations);
        countRoutes = findViewById(R.id.count_routes);
        countCategories = findViewById(R.id.count_categories);

        verifyAdminAccess();

        ((CardView) findViewById(R.id.card_locations)).setOnClickListener(v ->
                startActivity(new Intent(this, AdminLocationsListActivity.class)));
        ((CardView) findViewById(R.id.card_routes)).setOnClickListener(v ->
                startActivity(new Intent(this, AdminRoutesListActivity.class)));
        ((CardView) findViewById(R.id.card_categories)).setOnClickListener(v ->
                startActivity(new Intent(this, AdminCategoriesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCounts();
    }

    private void verifyAdminAccess() {
        String uid = authRepo.currentUid();
        if (uid == null) {
            android.widget.Toast.makeText(this, R.string.admin_not_authorized,
                    android.widget.Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        authRepo.isAdmin(uid).addOnSuccessListener(isAdmin -> {
            if (!Boolean.TRUE.equals(isAdmin)) {
                android.widget.Toast.makeText(this, R.string.admin_not_authorized,
                        android.widget.Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            android.widget.Toast.makeText(this, R.string.error_generic,
                    android.widget.Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadCounts() {
        locationRepo.getAll().addOnSuccessListener(list ->
                countLocations.setText(getString(R.string.admin_count_format, list.size())));
        routeRepo.getAll().addOnSuccessListener(list ->
                countRoutes.setText(getString(R.string.admin_count_format, list.size())));
        categoryRepo.getAll().addOnSuccessListener(list ->
                countCategories.setText(getString(R.string.admin_count_format, list.size())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            authRepo.signOut();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
