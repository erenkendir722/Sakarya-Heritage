package com.sakaryamiras.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.sakaryamiras.app.util.OfflineMapManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        OfflineMapManager.initOsmdroid(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::goToMain, SPLASH_DURATION_MS);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
