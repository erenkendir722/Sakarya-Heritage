package com.sakaryamiras.app.util;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

public final class OfflineMapManager {

    public static final double SAKARYA_NORTH = 41.05;
    public static final double SAKARYA_SOUTH = 40.20;
    public static final double SAKARYA_EAST = 30.95;
    public static final double SAKARYA_WEST = 30.05;

    public static final int ZOOM_MIN = 10;
    public static final int ZOOM_MAX = 15;

    private OfflineMapManager() {
    }

    public static void initOsmdroid(@NonNull Context context) {
        Configuration.getInstance().load(
                context.getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
        );
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
    }

    public static BoundingBox sakaryaBoundingBox() {
        return new BoundingBox(SAKARYA_NORTH, SAKARYA_EAST, SAKARYA_SOUTH, SAKARYA_WEST);
    }

    public static void downloadSakaryaArea(@NonNull MapView mapView,
                                           @NonNull ProgressListener listener) {
        CacheManager cacheManager = new CacheManager(mapView);

        BoundingBox box = sakaryaBoundingBox();

        CacheManager.CacheManagerCallback callback = new CacheManager.CacheManagerCallback() {
            @Override
            public void onTaskComplete() {
                listener.onComplete();
            }

            @Override
            public void onTaskFailed(int errors) {
                listener.onFailed(errors);
            }

            @Override
            public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                listener.onProgress(progress, totalTiles(box, zoomMin, zoomMax));
            }

            @Override
            public void downloadStarted() {
                listener.onStart(totalTiles(box, ZOOM_MIN, ZOOM_MAX));
            }

            @Override
            public void setPossibleTilesInArea(int total) {
                listener.onStart(total);
            }
        };

        cacheManager.downloadAreaAsync(
                mapView.getContext(),
                box,
                ZOOM_MIN,
                ZOOM_MAX,
                callback
        );
    }

    private static int totalTiles(BoundingBox box, int zoomMin, int zoomMax) {
        int total = 0;
        for (int z = zoomMin; z <= zoomMax; z++) {
            total += approxTilesForZoom(box, z);
        }
        return total;
    }

    private static int approxTilesForZoom(BoundingBox box, int zoom) {
        double n = Math.pow(2, zoom);
        double xMin = (box.getLonWest() + 180.0) / 360.0 * n;
        double xMax = (box.getLonEast() + 180.0) / 360.0 * n;
        double latRadN = Math.toRadians(box.getLatNorth());
        double latRadS = Math.toRadians(box.getLatSouth());
        double yMin = (1.0 - Math.log(Math.tan(latRadN) + 1.0 / Math.cos(latRadN)) / Math.PI) / 2.0 * n;
        double yMax = (1.0 - Math.log(Math.tan(latRadS) + 1.0 / Math.cos(latRadS)) / Math.PI) / 2.0 * n;
        return (int) Math.max(1, Math.ceil((xMax - xMin) * (yMax - yMin)));
    }

    public interface ProgressListener {
        void onStart(int totalTiles);

        void onProgress(int downloaded, int total);

        void onComplete();

        void onFailed(int errors);
    }
}
