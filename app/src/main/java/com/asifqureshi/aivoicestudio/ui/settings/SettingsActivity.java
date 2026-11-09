package com.asifqureshi.aivoicestudio.ui.settings;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.network.ApiClient;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import java.io.File;

public class SettingsActivity extends AppCompatActivity {
    private EditText etBackendUrl, etApiKey, etOpenRouterApiKey;
    private android.widget.RadioGroup rgTtsProvider, rgThemeMode;
    private TextView tvCacheSize;
    private MaterialButton btnSaveSettings;
    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PreferencesManager(this);

        etBackendUrl = findViewById(R.id.etBackendUrl);
        etApiKey = findViewById(R.id.etApiKey);
        etOpenRouterApiKey = findViewById(R.id.etOpenRouterApiKey);
        rgTtsProvider = findViewById(R.id.rgTtsProvider);
        rgThemeMode = findViewById(R.id.rgThemeMode);
        tvCacheSize = findViewById(R.id.tvCacheSize);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        findViewById(R.id.btnBackSettings).setOnClickListener(v -> finish());

        etBackendUrl.setText(prefs.getBackendUrl());
        etApiKey.setText(prefs.getApiKey());
        etOpenRouterApiKey.setText(prefs.getOpenRouterApiKey());

        if ("OpenRouter".equalsIgnoreCase(prefs.getTtsProvider())) {
            rgTtsProvider.check(R.id.rbOpenRouter);
        } else {
            rgTtsProvider.check(R.id.rbElevenLabs);
        }

        String themeMode = prefs.getThemeMode();
        if ("light".equalsIgnoreCase(themeMode)) {
            rgThemeMode.check(R.id.rbThemeLight);
        } else if ("system".equalsIgnoreCase(themeMode)) {
            rgThemeMode.check(R.id.rbThemeSystem);
        } else {
            rgThemeMode.check(R.id.rbThemeDark);
        }

        calculateCacheSize();

        findViewById(R.id.btnClearCache).setOnClickListener(v -> {
            clearAudioCache();
        });

        btnSaveSettings.setOnClickListener(v -> {
            String url = etBackendUrl.getText() != null ? etBackendUrl.getText().toString().trim() : "";
            String key = etApiKey.getText() != null ? etApiKey.getText().toString().trim() : "";
            String orKey = etOpenRouterApiKey.getText() != null ? etOpenRouterApiKey.getText().toString().trim() : "";

            String provider = rgTtsProvider.getCheckedRadioButtonId() == R.id.rbOpenRouter ? "OpenRouter" : "ElevenLabs";
            String mode = "dark";
            int checkedThemeId = rgThemeMode.getCheckedRadioButtonId();
            if (checkedThemeId == R.id.rbThemeLight) {
                mode = "light";
            } else if (checkedThemeId == R.id.rbThemeSystem) {
                mode = "system";
            }

            prefs.setBackendUrl(url);
            prefs.setApiKey(key);
            prefs.setOpenRouterApiKey(orKey);
            prefs.setTtsProvider(provider);
            prefs.setThemeMode(mode);
            ApiClient.resetClient();

            if ("light".equalsIgnoreCase(mode)) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            } else if ("system".equalsIgnoreCase(mode)) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            }

            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void calculateCacheSize() {
        File dir = new File(getFilesDir(), "audio_studio");
        long size = 0;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += f.length();
                }
            }
        }
        tvCacheSize.setText(AudioProcessingUtils.formatFileSize(size));
    }

    private void clearAudioCache() {
        File dir = new File(getFilesDir(), "audio_studio");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
        calculateCacheSize();
        Toast.makeText(this, "Audio cache cleared!", Toast.LENGTH_SHORT).show();
    }
}
