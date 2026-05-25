package com.sakaryamiras.app.ui.routes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.adapter.RouteStopAdapter;
import com.sakaryamiras.app.model.HeritageRoute;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.model.RouteStop;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.repository.RouteRepository;
import com.sakaryamiras.app.ui.detail.LocationDetailActivity;
import com.sakaryamiras.app.util.OfflineMapManager;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE_ID = "route_id";

    private MapView miniMap;
    private ImageView coverView;
    private TextView nameView;
    private TextView durationView;
    private TextView distanceView;
    private TextView difficultyView;
    private TextView descriptionView;
    private RecyclerView stopsList;
    private RouteStopAdapter stopAdapter;

    private final RouteRepository routeRepo = new RouteRepository();
    private final LocationRepository locationRepo = new LocationRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OfflineMapManager.initOsmdroid(this);
        setContentView(R.layout.activity_route_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coverView = findViewById(R.id.route_cover);
        miniMap = findViewById(R.id.route_mini_map);
        nameView = findViewById(R.id.route_name);
        durationView = findViewById(R.id.route_duration);
        distanceView = findViewById(R.id.route_distance);
        difficultyView = findViewById(R.id.route_difficulty);
        descriptionView = findViewById(R.id.route_description);
        stopsList = findViewById(R.id.stops_list);

        miniMap.setTileSource(TileSourceFactory.MAPNIK);
        miniMap.setMultiTouchControls(true);

        stopAdapter = new RouteStopAdapter((stop, location) -> {
            if (location == null) return;
            Intent intent = new Intent(this, LocationDetailActivity.class);
            intent.putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, location.getId());
            startActivity(intent);
        });
        stopsList.setLayoutManager(new LinearLayoutManager(this));
        stopsList.setAdapter(stopAdapter);

        String routeId = getIntent().getStringExtra(EXTRA_ROUTE_ID);
        if (routeId == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadRoute(routeId);
    }

    private void loadRoute(@NonNull String routeId) {
        routeRepo.getById(routeId)
                .addOnSuccessListener(this::bindRoute)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    finish();
                });
        routeRepo.getStops(routeId)
                .addOnSuccessListener(this::onStopsLoaded);
    }

    private void bindRoute(@Nullable HeritageRoute route) {
        if (route == null) {
            finish();
            return;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(route.getName());
        }
        nameView.setText(route.getName());
        descriptionView.setText(route.getDescription() != null ? route.getDescription() : "");

        durationView.setText(route.getDurationMinutes() != null
                ? getString(R.string.route_duration, route.getDurationMinutes()) : "—");
        distanceView.setText(route.getDistanceKm() != null
                ? getString(R.string.route_distance, route.getDistanceKm()) : "—");
        difficultyView.setText(difficultyLabel(route.getDifficulty()));

        if (route.getCoverImageUrl() != null && !route.getCoverImageUrl().isEmpty()) {
            Glide.with(this).load(route.getCoverImageUrl()).into(coverView);
        }
    }

    private String difficultyLabel(@Nullable String difficulty) {
        if (difficulty == null) return "—";
        switch (difficulty) {
            case HeritageRoute.DIFFICULTY_EASY:
                return getString(R.string.difficulty_easy);
            case HeritageRoute.DIFFICULTY_MEDIUM:
                return getString(R.string.difficulty_medium);
            case HeritageRoute.DIFFICULTY_HARD:
                return getString(R.string.difficulty_hard);
            default:
                return difficulty;
        }
    }

    private void onStopsLoaded(@NonNull List<RouteStop> stops) {
        stopAdapter.setStops(stops);
        if (stops.isEmpty()) return;

        List<String> locationIds = new ArrayList<>();
        for (RouteStop stop : stops) {
            if (stop.getLocationId() != null) locationIds.add(stop.getLocationId());
        }
        locationRepo.getByIds(locationIds)
                .addOnSuccessListener(this::onLocationsLoaded);
    }

    private void onLocationsLoaded(@NonNull List<Location> locations) {
        stopAdapter.setLocations(locations);
        drawRouteOnMap(locations);
    }

    private void drawRouteOnMap(@NonNull List<Location> locations) {
        if (locations.isEmpty()) return;

        miniMap.getOverlays().clear();
        List<GeoPoint> points = new ArrayList<>();
        for (Location loc : locations) {
            GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            points.add(point);
            Marker marker = new Marker(miniMap);
            marker.setPosition(point);
            marker.setTitle(loc.getName());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            miniMap.getOverlays().add(marker);
        }

        Polyline polyline = new Polyline();
        polyline.setPoints(points);
        polyline.getOutlinePaint().setColor(getResources().getColor(R.color.brand_primary, null));
        polyline.getOutlinePaint().setStrokeWidth(8f);
        miniMap.getOverlays().add(polyline);

        BoundingBox box = BoundingBox.fromGeoPoints(points);
        miniMap.post(() -> miniMap.zoomToBoundingBox(box.increaseByScale(1.3f), false, 50));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (miniMap != null) miniMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (miniMap != null) miniMap.onPause();
    }
}
