package com.sakaryamiras.app.ui.admin.locations;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.Category;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.repository.AuthRepository;
import com.sakaryamiras.app.repository.CategoryRepository;
import com.sakaryamiras.app.repository.EraRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.util.LocaleUtil;
import com.sakaryamiras.app.util.OfflineMapManager;
import com.sakaryamiras.app.util.QrCodeUtil;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationFormActivity extends AppCompatActivity {

    public static final String EXTRA_LOCATION_ID = "location_id";

    private TextInputEditText inputName;
    private TextInputEditText inputNameEn;
    private TextInputEditText inputDescription;
    private TextInputEditText inputDescriptionEn;
    private TextInputEditText inputDistrict;
    private TextInputEditText inputBuiltYear;
    private TextInputEditText inputLatitude;
    private TextInputEditText inputLongitude;
    private TextInputEditText inputHistoricalUrl;
    private TextInputEditText inputCurrentUrl;
    private Spinner spinnerCategory;
    private Spinner spinnerEra;
    private MapView pickerMap;
    private MaterialButton btnSave;
    private MaterialButton btnShowQr;
    private ProgressBar progress;

    private final LocationRepository locationRepo = new LocationRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final EraRepository eraRepo = new EraRepository();
    private final AuthRepository authRepo = new AuthRepository();

    private final List<Category> categories = new ArrayList<>();
    private final List<Era> eras = new ArrayList<>();

    @Nullable
    private Location editing;
    @Nullable
    private Marker pickerMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OfflineMapManager.initOsmdroid(this);
        setContentView(R.layout.activity_admin_location_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        inputName = findViewById(R.id.input_name);
        inputNameEn = findViewById(R.id.input_name_en);
        inputDescription = findViewById(R.id.input_description);
        inputDescriptionEn = findViewById(R.id.input_description_en);
        inputDistrict = findViewById(R.id.input_district);
        inputBuiltYear = findViewById(R.id.input_built_year);
        inputLatitude = findViewById(R.id.input_latitude);
        inputLongitude = findViewById(R.id.input_longitude);
        inputHistoricalUrl = findViewById(R.id.input_historical_url);
        inputCurrentUrl = findViewById(R.id.input_current_url);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerEra = findViewById(R.id.spinner_era);
        pickerMap = findViewById(R.id.picker_map);
        btnSave = findViewById(R.id.btn_save);
        btnShowQr = findViewById(R.id.btn_show_qr);
        progress = findViewById(R.id.save_progress);

        setupMap();
        btnSave.setOnClickListener(v -> save());
        btnShowQr.setOnClickListener(v -> showQrDialog());

        String locationId = getIntent().getStringExtra(EXTRA_LOCATION_ID);
        if (locationId != null) {
            setTitle(R.string.title_edit_location);
            loadExisting(locationId);
        } else {
            setTitle(R.string.title_new_location);
            loadDropdowns();
        }
    }

    private void setupMap() {
        pickerMap.setTileSource(TileSourceFactory.MAPNIK);
        pickerMap.setMultiTouchControls(true);
        pickerMap.getController().setZoom(11.0);
        pickerMap.getController().setCenter(new GeoPoint(40.7831, 30.4023));

        MapEventsOverlay mapEvents = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                placePickerMarker(p);
                inputLatitude.setText(String.valueOf(p.getLatitude()));
                inputLongitude.setText(String.valueOf(p.getLongitude()));
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        pickerMap.getOverlays().add(mapEvents);
    }

    private void placePickerMarker(GeoPoint point) {
        if (pickerMarker == null) {
            pickerMarker = new Marker(pickerMap);
            pickerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            pickerMap.getOverlays().add(pickerMarker);
        }
        pickerMarker.setPosition(point);
        pickerMap.invalidate();
    }

    private void loadDropdowns() {
        categoryRepo.getAll().addOnSuccessListener(list -> {
            categories.clear();
            categories.addAll(list);
            bindCategorySpinner();
            if (editing != null) selectCategoryById(editing.getCategoryId());
        });
        eraRepo.getAll().addOnSuccessListener(list -> {
            eras.clear();
            eras.addAll(list);
            bindEraSpinner();
            if (editing != null) selectEraById(editing.getEraId());
        });
    }

    private void bindCategorySpinner() {
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.spinner_select_category));
        for (Category c : categories) names.add(LocaleUtil.localizedCategoryName(this, c));
        spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void bindEraSpinner() {
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.spinner_select_era));
        for (Era e : eras) names.add(LocaleUtil.localizedEraName(this, e));
        spinnerEra.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void selectCategoryById(@Nullable String id) {
        if (id == null) return;
        for (int i = 0; i < categories.size(); i++) {
            if (id.equals(categories.get(i).getId())) {
                spinnerCategory.setSelection(i + 1);
                return;
            }
        }
    }

    private void selectEraById(@Nullable String id) {
        if (id == null) return;
        for (int i = 0; i < eras.size(); i++) {
            if (id.equals(eras.get(i).getId())) {
                spinnerEra.setSelection(i + 1);
                return;
            }
        }
    }

    private void loadExisting(String id) {
        locationRepo.getById(id).addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            editing = location;
            btnShowQr.setVisibility(View.VISIBLE);
            inputName.setText(location.getName());
            inputNameEn.setText(location.getNameEn());
            inputDescription.setText(location.getDescription());
            inputDescriptionEn.setText(location.getDescriptionEn());
            inputDistrict.setText(location.getDistrict());
            if (location.getBuiltYear() != null) {
                inputBuiltYear.setText(String.valueOf(location.getBuiltYear()));
            }
            inputLatitude.setText(String.valueOf(location.getLatitude()));
            inputLongitude.setText(String.valueOf(location.getLongitude()));
            inputHistoricalUrl.setText(location.getHistoricalImageUrl());
            inputCurrentUrl.setText(location.getCurrentImageUrl());

            GeoPoint pt = new GeoPoint(location.getLatitude(), location.getLongitude());
            placePickerMarker(pt);
            pickerMap.getController().setCenter(pt);
            pickerMap.getController().setZoom(15.0);

            loadDropdowns();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void save() {
        String name = textOf(inputName);
        String description = textOf(inputDescription);
        String district = textOf(inputDistrict);
        String latStr = textOf(inputLatitude);
        String lngStr = textOf(inputLongitude);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr)) {
            Toast.makeText(this, R.string.form_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = authRepo.currentUid();
        if (uid == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        Location location = editing != null ? editing : new Location();
        location.setName(name);
        location.setNameEn(textOf(inputNameEn));
        location.setDescription(description);
        location.setDescriptionEn(textOf(inputDescriptionEn));
        location.setDistrict(district);
        try {
            location.setLatitude(Double.parseDouble(latStr));
            location.setLongitude(Double.parseDouble(lngStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.form_invalid_coordinates, Toast.LENGTH_SHORT).show();
            return;
        }
        String yearStr = textOf(inputBuiltYear);
        location.setBuiltYear(TextUtils.isEmpty(yearStr) ? null : Integer.parseInt(yearStr));

        int catPos = spinnerCategory.getSelectedItemPosition();
        location.setCategoryId(catPos > 0 ? categories.get(catPos - 1).getId() : null);

        int eraPos = spinnerEra.getSelectedItemPosition();
        location.setEraId(eraPos > 0 ? eras.get(eraPos - 1).getId() : null);

        location.setHistoricalImageUrl(textOf(inputHistoricalUrl));
        location.setCurrentImageUrl(textOf(inputCurrentUrl));

        setBusy(true);
        Task<Void> task = editing != null
                ? locationRepo.update(location)
                : locationRepo.create(location, uid);
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

    private String textOf(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private void showQrDialog() {
        if (editing == null || editing.getId() == null) return;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_show_qr, null);
        TextView nameView = view.findViewById(R.id.qr_location_name);
        ImageView qrImage = view.findViewById(R.id.qr_image);

        nameView.setText(editing.getName());
        String content = QrCodeUtil.buildLocationUri(editing.getId());
        Bitmap qr = QrCodeUtil.generate(content, 720);
        if (qr != null) {
            qrImage.setImageBitmap(qr);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.qr_show_title)
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton(R.string.action_share, (d, w) -> shareQrText(content))
                .show();
    }

    private void shareQrText(String content) {
        if (editing == null) return;
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                getString(R.string.qr_share_subject, editing.getName()));
        intent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        startActivity(android.content.Intent.createChooser(intent, editing.getName()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pickerMap != null) pickerMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pickerMap != null) pickerMap.onPause();
    }
}
