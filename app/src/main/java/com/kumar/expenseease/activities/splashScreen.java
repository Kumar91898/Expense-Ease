package com.kumar.expenseease.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.kumar.expenseease.R;
import com.kumar.expenseease.dataModels.NotificationHelper;

public class splashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        // Hide the navigation bar and status bar
        hideSystemUI();

        NotificationHelper.createNotificationChannel(this);

        new Handler().postDelayed(this::goToMain, 500);
    }

    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        Animatoo.INSTANCE.animateZoom(this);
        finish();
    }
}