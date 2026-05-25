package com.sakaryamiras.app.ui.admin.routes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.HeritageRoute;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.model.RouteStop;
import com.sakaryamiras.app.repository.AuthRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.repository.RouteRepository;

import java.util.ArrayList;
import java.util.List;

public class AdminRouteFormActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE_ID = "route_id";

    private TextInputEditText inputName;
    private TextInputEditText inputDescription;
    private TextInputEditText inputTheme;
    private TextInputEditText inputDuration;
    private TextInputEditText inputDistance;
    private TextInputEditText inputCoverUrl;
    private Spinner spinnerDifficulty;
    private LinearLayout stopsContainer;
    private MaterialButton btnAddStop;
    private MaterialButton btnSave;
    private ProgressBar progress;

    private final RouteRepository routeRepo = new RouteRepository();
    private final LocationRepository locationRepo = new LocationRepository();
    private final AuthRepository authRepo = new AuthRepository();

    private final List<Location> allLocations = new ArrayList<>();
    private final List<Spinner> stopSpinners = new ArrayList<>();

    @Nullable
    private HeritageRoute editing;
    private List<RouteStop> existingStops = new ArrayList<>();

    private static final String[] DIFFICULTIES = {
            HeritageRoute.DIFFICULTY_EASY,
            HeritageRoute.DIFFICULTY_MEDIUM,
            HeritageRoute.DIFFICULTY_HARD
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_route_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        inputName = findViewById(R.id.input_name);
        inputDescription = findViewById(R.id.input_description);
        inputTheme = findViewById(R.id.input_theme);
        inputDuration = findViewById(R.id.input_duration);
        inputDistance = findViewById(R.id.input_distance);
        inputCoverUrl = findViewById(R.id.input_cover_url);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        stopsContainer = findViewById(R.id.stops_container);
        btnAddStop = findViewById(R.id.btn_add_stop);
        btnSave = findViewById(R.id.btn_save);
        progress = findViewById(R.id.save_progress);

        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_medium),
                getString(R.string.difficulty_hard)
        }));

        btnAddStop.setOnClickListener(v -> addStopRow(null));
        btnSave.setOnClickListener(v -> save());

        String routeId = getIntent().getStringExtra(EXTRA_ROUTE_ID);
        if (routeId != null) {
            setTitle("Rotayı Düzenle");
            loadExisting(routeId);
        } else {
            setTitle("Yeni Rota");
            loadLocations();
        }
    }

    private void loadLocations() {
        locationRepo.getAll().addOnSuccessListener(list -> {
            allLocations.clear();
            allLocations.addAll(list);
            for (Spinner spinner : stopSpinners) {
                spinner.setAdapter(makeLocationAdapter());
            }
            if (editing != null) bindStops();
        });
    }

    private ArrayAdapter<String> makeLocationAdapter() {
        List<String> names = new ArrayList<>();
        names.add("— Lokasyon seç —");
        for (Location loc : allLocations) names.add(loc.getName());
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
    }

    private void loadExisting(String id) {
        routeRepo.getById(id).addOnSuccessListener(route -> {
            if (route == null) {
                finish();
                return;
            }
            editing = route;
            inputName.setText(route.getName());
            inputDescription.setText(route.getDescription());
            inputTheme.setText(route.getTheme());
            if (route.getDurationMinutes() != null) {
                inputDuration.setText(String.valueOf(route.getDurationMinutes()));
            }
            if (route.getDistanceKm() != null) {
                inputDistance.setText(String.valueOf(route.getDistanceKm()));
            }
            inputCoverUrl.setText(route.getCoverImageUrl());

            for (int i = 0; i < DIFFICULTIES.length; i++) {
                if (DIFFICULTIES[i].equals(route.getDifficulty())) {
                    spinnerDifficulty.setSelection(i);
                    break;
                }
            }
            loadLocations();
            routeRepo.getStops(id).addOnSuccessListener(stops -> {
                existingStops = stops;
                bindStops();
            });
        });
    }

    private void bindStops() {
        if (allLocations.isEmpty()) return;
        stopsContainer.removeAllViews();
        stopSpinners.clear();
        for (RouteStop stop : existingStops) {
            addStopRow(stop.getLocationId());
        }
    }

    private void addStopRow(@Nullable String selectedLocationId) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_route_stop_edit,
                stopsContainer, false);
        TextView orderView = row.findViewById(R.id.stop_order);
        Spinner spinner = row.findViewById(R.id.spinner_location);
        ImageButton btnRemove = row.findViewById(R.id.btn_remove);

        spinner.setAdapter(makeLocationAdapter());

        if (selectedLocationId != null) {
            for (int i = 0; i < allLocations.size(); i++) {
                if (selectedLocationId.equals(allLocations.get(i).getId())) {
                    spinner.setSelection(i + 1);
                    break;
                }
            }
        }

        btnRemove.setOnClickListener(v -> {
            stopsContainer.removeView(row);
            stopSpinners.remove(spinner);
            renumber();
        });

        stopsContainer.addView(row);
        stopSpinners.add(spinner);
        orderView.setText(String.valueOf(stopSpinners.size()));
        renumber();
    }

    private void renumber() {
        for (int i = 0; i < stopsContainer.getChildCount(); i++) {
            View row = stopsContainer.getChildAt(i);
            TextView orderView = row.findViewById(R.id.stop_order);
            orderView.setText(String.valueOf(i + 1));
        }
    }

    private void save() {
        String name = textOf(inputName);
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.form_required, Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = authRepo.currentUid();
        if (uid == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        HeritageRoute route = editing != null ? editing : new HeritageRoute();
        route.setName(name);
        route.setDescription(textOf(inputDescription));
        route.setTheme(textOf(inputTheme));
        String durStr = textOf(inputDuration);
        route.setDurationMinutes(TextUtils.isEmpty(durStr) ? null : Integer.parseInt(durStr));
        String distStr = textOf(inputDistance);
        route.setDistanceKm(TextUtils.isEmpty(distStr) ? null : Double.parseDouble(distStr));
        route.setCoverImageUrl(textOf(inputCoverUrl));
        route.setDifficulty(DIFFICULTIES[spinnerDifficulty.getSelectedItemPosition()]);

        List<RouteStop> stops = new ArrayList<>();
        for (int i = 0; i < stopSpinners.size(); i++) {
            int pos = stopSpinners.get(i).getSelectedItemPosition();
            if (pos > 0 && pos - 1 < allLocations.size()) {
                Location loc = allLocations.get(pos - 1);
                stops.add(new RouteStop(loc.getId(), i + 1, null, null));
            }
        }

        setBusy(true);
        Task<Void> task = editing != null
                ? routeRepo.update(route, stops)
                : routeRepo.create(route, stops, uid);
        task.addOnSuccessListener(unused -> {
            setBusy(false);
            Toast.makeText(this, R.string.form_save_success, Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            setBusy(false);
            Toast.makeText(this, getString(R.string.form_save_error, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        });
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!busy);
    }

    private String textOf(@NonNull TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }
}
