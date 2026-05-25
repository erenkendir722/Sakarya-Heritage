package com.sakaryamiras.app.util;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public final class QrCodeUtil {

    public static final String SCHEME = "sakaryamiras";
    public static final String HOST_LOCATION = "location";

    private QrCodeUtil() {
    }

    @NonNull
    public static String buildLocationUri(@NonNull String locationId) {
        return SCHEME + "://" + HOST_LOCATION + "/" + locationId;
    }

    @Nullable
    public static String parseLocationId(@Nullable String qrContent) {
        if (qrContent == null) return null;
        String prefix = SCHEME + "://" + HOST_LOCATION + "/";
        if (qrContent.startsWith(prefix)) {
            String rest = qrContent.substring(prefix.length());
            int slash = rest.indexOf('/');
            return slash >= 0 ? rest.substring(0, slash) : rest;
        }
        return null;
    }

    @Nullable
    public static Bitmap generate(@NonNull String content, int sizePx) {
        try {
            return new BarcodeEncoder().encodeBitmap(content, BarcodeFormat.QR_CODE, sizePx, sizePx);
        } catch (WriterException e) {
            return null;
        }
    }
}
