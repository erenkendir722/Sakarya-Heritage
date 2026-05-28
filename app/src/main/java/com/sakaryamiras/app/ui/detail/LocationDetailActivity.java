package com.sakaryamiras.app.ui.detail;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.Category;
import com.sakaryamiras.app.model.Era;
import com.sakaryamiras.app.model.Location;
import com.sakaryamiras.app.repository.CategoryRepository;
import com.sakaryamiras.app.repository.EraRepository;
import com.sakaryamiras.app.repository.LocationRepository;
import com.sakaryamiras.app.util.EraColorUtil;
import com.sakaryamiras.app.util.FavoritesManager;
import com.sakaryamiras.app.util.LocaleUtil;
import com.sakaryamiras.app.util.ShareHelper;

public class LocationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LOCATION_ID = "location_id";

    private BeforeAfterSliderView slider;
    private TextView nameView;
    private TextView eraBadge;
    private TextView categoryView;
    private TextView yearView;
    private TextView descriptionView;
    private LinearLayout btnFavorite;
    private ImageView icFavorite;
    private TextView labelFavorite;

    private final LocationRepository locationRepo = new LocationRepository();
    private final EraRepository eraRepo = new EraRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();

    private FavoritesManager favoritesManager;
    @Nullable
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        slider = findViewById(R.id.before_after_slider);
        nameView = findViewById(R.id.detail_name);
        eraBadge = findViewById(R.id.detail_era_badge);
        categoryView = findViewById(R.id.detail_category);
        yearView = findViewById(R.id.detail_year);
        descriptionView = findViewById(R.id.detail_description);
        btnFavorite = findViewById(R.id.btn_favorite);
        icFavorite = findViewById(R.id.ic_favorite);
        labelFavorite = findViewById(R.id.label_favorite);

        favoritesManager = new FavoritesManager(this);

        findViewById(R.id.btn_directions).setOnClickListener(v -> onDirectionsClicked());
        findViewById(R.id.btn_share).setOnClickListener(v -> onShareClicked());
        btnFavorite.setOnClickListener(v -> onFavoriteToggled());

        String locationId = resolveLocationId();
        if (locationId == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadLocation(locationId);
    }

    @Nullable
    private String resolveLocationId() {
        String id = getIntent().getStringExtra(EXTRA_LOCATION_ID);
        if (id != null) return id;
        android.net.Uri data = getIntent().getData();
        if (data != null && "sakaryamiras".equalsIgnoreCase(data.getScheme())
                && "location".equalsIgnoreCase(data.getHost())) {
            String path = data.getPath();
            if (path != null && path.length() > 1) {
                String trimmed = path.startsWith("/") ? path.substring(1) : path;
                int slash = trimmed.indexOf('/');
                return slash >= 0 ? trimmed.substring(0, slash) : trimmed;
            }
        }
        return null;
    }

    private void loadLocation(@NonNull String locationId) {
        locationRepo.getById(locationId)
                .addOnSuccessListener(this::bindLocation)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindLocation(@Nullable Location location) {
        if (location == null) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentLocation = location;

        String displayName = LocaleUtil.localizedName(this, location);
        String displayDescription = LocaleUtil.localizedDescription(this, location);

        nameView.setText(displayName != null ? displayName : "");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(displayName != null ? displayName : "");
        }

        if (location.getBuiltYear() != null) {
            yearView.setVisibility(View.VISIBLE);
            yearView.setText(getString(R.string.label_built_year, location.getBuiltYear()));
        } else {
            yearView.setVisibility(View.GONE);
        }
        descriptionView.setText(displayDescription != null ? displayDescription : "");

        loadImage(location.getHistoricalImageUrl(), true);
        loadImage(location.getCurrentImageUrl(), false);

        refreshFavoriteUI();

        if (location.getEraId() != null) {
            eraRepo.getById(location.getEraId())
                    .addOnSuccessListener(this::bindEra);
        } else {
            eraBadge.setVisibility(View.GONE);
        }
        if (location.getCategoryId() != null) {
            categoryRepo.getById(location.getCategoryId())
                    .addOnSuccessListener(this::bindCategory);
        }
    }

    private void bindEra(@Nullable Era era) {
        if (era == null) {
            eraBadge.setVisibility(View.GONE);
            return;
        }
        eraBadge.setVisibility(View.VISIBLE);
        eraBadge.setText(LocaleUtil.localizedEraName(this, era));
        Drawable bg = eraBadge.getBackground();
        if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg.mutate()).setColor(EraColorUtil.colorOf(era));
        }
    }

    private void bindCategory(@Nullable Category category) {
        if (currentLocation == null) return;
        StringBuilder sb = new StringBuilder();
        String localizedCat = LocaleUtil.localizedCategoryName(this, category);
        if (localizedCat != null) {
            sb.append(localizedCat);
        }
        if (currentLocation.getDistrict() != null) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(currentLocation.getDistrict());
        }
        categoryView.setText(sb.toString());
    }

    private void loadImage(@Nullable String url, boolean isHistorical) {
        if (url == null || url.isEmpty()) return;
        Glide.with(this).asBitmap().load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        if (isHistorical) {
                            slider.setBeforeBitmap(resource);
                        } else {
                            slider.setAfterBitmap(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void refreshFavoriteUI() {
        if (currentLocation == null) return;
        boolean isFavorite = favoritesManager.isFavorite(currentLocation.getId());
        icFavorite.setImageResource(isFavorite
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_outline);
        labelFavorite.setText(isFavorite
                ? R.string.action_unfavorite
                : R.string.action_favorite);
    }

    private void onFavoriteToggled() {
        if (currentLocation == null) return;
        boolean nowFav = favoritesManager.toggle(currentLocation.getId());
        Toast.makeText(this,
                nowFav ? R.string.favorite_added : R.string.favorite_removed,
                Toast.LENGTH_SHORT).show();
        refreshFavoriteUI();
    }

    private void onShareClicked() {
        if (currentLocation == null) return;
        ShareHelper.shareLocation(this, currentLocation);
    }

    private void onDirectionsClicked() {
        if (currentLocation == null) return;
        ShareHelper.openDirections(this,
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                currentLocation.getName());
    }

}
