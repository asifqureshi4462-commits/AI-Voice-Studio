package com.asifqureshi.aivoicestudio.ui.admin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private TextView tvAdminApiHealth, tvAdminTierInfo, tvTotalProjectsGenerated;
    private ProjectDatabaseHelper dbHelper;
    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new ProjectDatabaseHelper(this);
        prefs = new PreferencesManager(this);

        tvAdminApiHealth = findViewById(R.id.tvAdminApiHealth);
        tvAdminTierInfo = findViewById(R.id.tvAdminTierInfo);
        tvTotalProjectsGenerated = findViewById(R.id.tvTotalProjectsGenerated);

        findViewById(R.id.btnBackAdmin).setOnClickListener(v -> finish());

        findViewById(R.id.btnRefreshAdmin).setOnClickListener(v -> {
            loadTelemetry();
            Toast.makeText(this, "Telemetry refreshed", Toast.LENGTH_SHORT).show();
        });

        loadTelemetry();
    }

    private void loadTelemetry() {
        List<ProjectItem> projects = dbHelper.getAllProjects();
        tvTotalProjectsGenerated.setText(String.format("Total Projects & Generations: %d", projects.size()));

        String apiKey = prefs.getApiKey();
        if (apiKey.isEmpty()) {
            tvAdminApiHealth.setText("Provider Mode: Backend Gateway Active");
            tvAdminTierInfo.setText("ElevenLabs API Key configured on backend proxy");
        } else {
            tvAdminApiHealth.setText("Provider Status: Direct Key Mode (Active)");
            tvAdminTierInfo.setText("Client API Key configured (" + apiKey.substring(0, Math.min(6, apiKey.length())) + "…)");
        }
    }
}
