package com.asifqureshi.aivoicestudio.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.ui.auth.LoginActivity;
import com.asifqureshi.aivoicestudio.ui.main.MainActivity;
import com.asifqureshi.aivoicestudio.ui.onboarding.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeToNextScreen, 1200);
    }

    private void routeToNextScreen() {
        if (isFinishing() || isDestroyed()) return;

        PreferencesManager prefs = new PreferencesManager(this);
        Intent intent;
        if (!prefs.isOnboardingCompleted()) {
            intent = new Intent(this, OnboardingActivity.class);
        } else if (!prefs.isLoggedIn()) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
