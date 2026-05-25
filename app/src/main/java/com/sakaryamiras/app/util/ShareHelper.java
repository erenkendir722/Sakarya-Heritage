package com.sakaryamiras.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sakaryamiras.app.R;
import com.sakaryamiras.app.model.Location;

import java.util.Locale;

public final class ShareHelper {

    private ShareHelper() {
    }

    public static void shareLocation(@NonNull Context context, @NonNull Location location) {
        String district = location.getDistrict() != null ? location.getDistrict() : "Sakarya";
        String text = context.getString(
                R.string.share_template,
                location.getName(),
                district,
                location.getLatitude(),
                location.getLongitude()
        );
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, location.getName());
        intent.putExtra(Intent.EXTRA_TEXT, text);

        Intent chooser = Intent.createChooser(intent, location.getName());
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }

    public static void openDirections(@NonNull Context context,
                                      double latitude,
                                      double longitude,
                                      @Nullable String label) {
        String uri = String.format(
                Locale.US,
                "geo:0,0?q=%f,%f(%s)",
                latitude,
                longitude,
                label != null ? Uri.encode(label) : "Hedef"
        );
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            String webUri = String.format(
                    Locale.US,
                    "https://www.google.com/maps/search/?api=1&query=%f,%f",
                    latitude,
                    longitude
            );
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        }
    }
}
