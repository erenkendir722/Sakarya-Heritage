package com.sakaryamiras.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.repository.AuthRepository;
import com.sakaryamiras.app.repository.EraRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.ui.about.AboutActivity;
import com.sakaryamiras.app.ui.admin.AdminDashboardActivity;
import com.sakaryamiras.app.ui.auth.AuthActivity;
import com.sakaryamiras.app.ui.detail.LocationDetailActivity;
import com.sakaryamiras.app.ui.favorites.FavoritesActivity;
import com.sakaryamiras.app.ui.routes.RoutesListActivity;
import com.sakaryamiras.app.util.EraColorUtil;
import com.sakaryamiras.app.util.OfflineMapManager;
import com.sakaryamiras.app.util.QrCodeUtil;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOCATION_PERMISSION = 1001;
    private static final GeoPoint SAKARYA_CENTER = new GeoPoint(40.7831, 30.4023);
    private static final double DEFAULT_ZOOM = 11.0;
    private static final String FILTER_ALL = "__all__";

    private DrawerLayout drawerLayout;
    private MapView mapView;
    private LinearProgressIndicator loadingBar;
    private LinearLayout eraFilterContainer;

    private LinearLayout authLoggedOutSection;
    private LinearLayout authLoggedInSection;
    private TextView drawerUserEmail;
    private MaterialButton btnAdminPanel;

    private final LocationRepository locationRepo = new LocationRepository();
    private final EraRepository eraRepo = new EraRepository();
    private final AuthRepository authRepo = new AuthRepository();

    private final List<Marker> activeMarkers = new ArrayList<>();
    private final Map<String, Era> erasById = new HashMap<>();
    private List<Location> allLocations = new ArrayList<>();
    private String selectedEraId = FILTER_ALL;

    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient locationClient;
    @Nullable
    private AlertDialog progressDialog;

    private final ActivityResultLauncher<ScanOptions> qrScanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result == null || result.getContents() == null) return;
                String locationId = QrCodeUtil.parseLocationId(result.getContents());
                if (locationId == null) {
                    Toast.makeText(this, R.string.qr_scan_invalid, Toast.LENGTH_SHORT).show();
                    return;
                }
                openLocationDetail(locationId);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OfflineMapManager.initOsmdroid(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        mapView = findViewById(R.id.map_view);
        loadingBar = findViewById(R.id.map_loading);
        eraFilterContainer = findViewById(R.id.era_filter_container);

        FloatingActionButton fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> centerOnMyLocation());

        bindDrawer();
        applyBottomInsetToFilter();

        setupMap();
        loadErasAndLocations();

        locationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void bindDrawer() {
        findViewById(R.id.nav_routes).setOnClickListener(v ->
                navigateAndClose(new Intent(this, RoutesListActivity.class)));
        findViewById(R.id.nav_favorites).setOnClickListener(v ->
                navigateAndClose(new Intent(this, FavoritesActivity.class)));
        findViewById(R.id.nav_search).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.END);
            Toast.makeText(this, "Arama yakında", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.nav_qr_scan).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.END);
            launchQrScan();
        });
        findViewById(R.id.nav_language).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.END);
            showLanguageDialog();
        });
        findViewById(R.id.nav_offline).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.END);
            startOfflineDownload();
        });
        findViewById(R.id.nav_about).setOnClickListener(v ->
                navigateAndClose(new Intent(this, AboutActivity.class)));

        authLoggedOutSection = findViewById(R.id.auth_logged_out_section);
        authLoggedInSection = findViewById(R.id.auth_logged_in_section);
        drawerUserEmail = findViewById(R.id.drawer_user_email);
        btnAdminPanel = findViewById(R.id.btn_admin_panel);

        findViewById(R.id.btn_drawer_login).setOnClickListener(v -> openAuth(AuthActivity.MODE_LOGIN));
        findViewById(R.id.btn_drawer_signup).setOnClickListener(v -> openAuth(AuthActivity.MODE_SIGNUP));
        findViewById(R.id.btn_drawer_logout).setOnClickListener(v -> {
            authRepo.signOut();
            refreshAuthState();
            Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();
        });
        btnAdminPanel.setOnClickListener(v ->
                navigateAndClose(new Intent(this, AdminDashboardActivity.class)));
    }

    private void navigateAndClose(Intent intent) {
        drawerLayout.closeDrawer(Gravity.END);
        startActivity(intent);
    }

    private void openAuth(String mode) {
        drawerLayout.closeDrawer(Gravity.END);
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra(AuthActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void refreshAuthState() {
        if (authRepo.isLoggedIn()) {
            authLoggedOutSection.setVisibility(View.GONE);
            authLoggedInSection.setVisibility(View.VISIBLE);
            drawerUserEmail.setText(getString(R.string.account_logged_in_as,
                    authRepo.currentEmail() != null ? authRepo.currentEmail() : "-"));
            btnAdminPanel.setVisibility(View.GONE);
            String uid = authRepo.currentUid();
            if (uid != null) {
                authRepo.isAdmin(uid).addOnSuccessListener(isAdmin -> {
                    btnAdminPanel.setVisibility(Boolean.TRUE.equals(isAdmin) ? View.VISIBLE : View.GONE);
                });
            }
        } else {
            authLoggedOutSection.setVisibility(View.VISIBLE);
            authLoggedInSection.setVisibility(View.GONE);
        }
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(DEFAULT_ZOOM);
        mapView.getController().setCenter(SAKARYA_CENTER);
        mapView.setMinZoomLevel(8.0);
        mapView.setMaxZoomLevel(19.0);
    }

    private void applyBottomInsetToFilter() {
        View target = (View) eraFilterContainer.getParent();
        final int basePad = target.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(target, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), basePad + bars.bottom);
            return insets;
        });
    }

    private void loadErasAndLocations() {
        loadingBar.setVisibility(View.VISIBLE);
        eraRepo.getAll().addOnSuccessListener(eras -> {
            erasById.clear();
            for (Era e : eras) {
                erasById.put(e.getId(), e);
            }
            buildEraFilterChips(eras);
            loadLocations();
        }).addOnFailureListener(e -> {
            loadingBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadLocations() {
        locationRepo.getAll().addOnSuccessListener(locations -> {
            loadingBar.setVisibility(View.GONE);
            allLocations = locations;
            renderMarkers();
        }).addOnFailureListener(e -> {
            loadingBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        });
    }

    private void buildEraFilterChips(List<Era> eras) {
        eraFilterContainer.removeAllViews();
        addEraChip(FILTER_ALL, getString(R.string.filter_all_eras),
                ContextCompat.getColor(this, R.color.brand_primary_dark));
        for (Era era : eras) {
            addEraChip(era.getId(), era.getName(), EraColorUtil.colorOf(era));
        }
    }

    private void addEraChip(String eraId, String label, int color) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setTextColor(Color.WHITE);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(16), dp(8), dp(16), dp(8));
        chip.setTextSize(13);
        chip.setAllCaps(false);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        bg.setColor(eraId.equals(selectedEraId) ? color : Color.LTGRAY);
        chip.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        chip.setLayoutParams(lp);

        chip.setOnClickListener(v -> {
            selectedEraId = eraId;
            buildEraFilterChips(currentEras());
            renderMarkers();
        });

        eraFilterContainer.addView(chip);
    }

    private List<Era> currentEras() {
        return new ArrayList<>(erasById.values());
    }

    private void renderMarkers() {
        for (Marker m : activeMarkers) {
            mapView.getOverlays().remove(m);
        }
        activeMarkers.clear();

        for (Location loc : allLocations) {
            if (!FILTER_ALL.equals(selectedEraId)
                    && (loc.getEraId() == null || !loc.getEraId().equals(selectedEraId))) {
                continue;
            }
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            marker.setTitle(loc.getName());
            marker.setSubDescription(loc.getDistrict() != null ? loc.getDistrict() : "");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            Era era = loc.getEraId() != null ? erasById.get(loc.getEraId()) : null;
            int eraColor = EraColorUtil.colorOf(era);
            marker.setIcon(buildMarkerIcon(eraColor));

            marker.setOnMarkerClickListener((m, mv) -> {
                openLocationDetail(loc.getId());
                return true;
            });
            mapView.getOverlays().add(marker);
            activeMarkers.add(marker);
        }
        mapView.invalidate();
    }

    private android.graphics.drawable.Drawable buildMarkerIcon(int color) {
        android.graphics.drawable.Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_map_pin);
        if (icon != null) {
            icon = icon.mutate();
            icon.setColorFilter(new android.graphics.PorterDuffColorFilter(
                    color, android.graphics.PorterDuff.Mode.SRC_IN));
        }
        return icon;
    }

    private void openLocationDetail(String locationId) {
        Intent intent = new Intent(this, LocationDetailActivity.class);
        intent.putExtra(LocationDetailActivity.EXTRA_LOCATION_ID, locationId);
        startActivity(intent);
    }

    private void showLanguageDialog() {
        final String[] codes = {"tr", "en"};
        String[] labels = {getString(R.string.language_turkish), getString(R.string.language_english)};
        int current = 0;
        LocaleListCompat applied = AppCompatDelegate.getApplicationLocales();
        String currentTag = !applied.isEmpty() && applied.get(0) != null
                ? applied.get(0).getLanguage() : java.util.Locale.getDefault().getLanguage();
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentTag)) {
                current = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_language_title)
                .setSingleChoiceItems(labels, current, (d, which) -> {
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(codes[which]));
                    d.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void launchQrScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.qr_scan_prompt));
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);
        qrScanLauncher.launch(options);
    }

    private void centerOnMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION_PERMISSION);
            return;
        }
        enableMyLocationOverlay();
        locationClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                mapView.getController().animateTo(point);
                mapView.getController().setZoom(15.0);
            } else {
                Toast.makeText(this, "Konum bulunamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableMyLocationOverlay() {
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            centerOnMyLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_drawer) {
            if (drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawer(Gravity.END);
            } else {
                drawerLayout.openDrawer(Gravity.END);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.END)) {
            drawerLayout.closeDrawer(Gravity.END);
        } else {
            super.onBackPressed();
        }
    }

    private void startOfflineDownload() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.offline_download_title)
                .setMessage(R.string.offline_download_message)
                .setPositiveButton(android.R.string.ok, (d, w) -> runDownload())
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void runDownload() {
        View progressView = getLayoutInflater().inflate(R.layout.dialog_offline_progress, null);
        TextView progressText = progressView.findViewById(R.id.progress_text);
        progressDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.offline_download_title)
                .setView(progressView)
                .setCancelable(false)
                .create();
        progressDialog.show();

        OfflineMapManager.downloadSakaryaArea(mapView, new OfflineMapManager.ProgressListener() {
            @Override
            public void onStart(int totalTiles) {
                runOnUiThread(() -> progressText.setText(
                        getString(R.string.offline_downloading, 0, totalTiles)));
            }

            @Override
            public void onProgress(int downloaded, int total) {
                runOnUiThread(() -> progressText.setText(
                        getString(R.string.offline_downloading, downloaded, total)));
            }

            @Override
            public void onComplete() {
                runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.offline_done, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailed(int errors) {
                runOnUiThread(() -> {
                    if (progressDialog != null) progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.offline_failed, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
        refreshAuthState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
