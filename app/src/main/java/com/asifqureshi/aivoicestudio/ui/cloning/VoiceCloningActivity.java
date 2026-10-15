package com.asifqureshi.aivoicestudio.ui.cloning;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.VoiceRepository;
import com.google.android.material.button.MaterialButton;

public class VoiceCloningActivity extends AppCompatActivity {
    private EditText etVoicePrompt, etCloneVoiceName;
    private CheckBox cbCloneConsent;
    private MaterialButton btnGenerateVoiceDesign, btnUploadCloneSample;
    private PreferencesManager prefs;
    private VoiceRepository voiceRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_cloning);

        prefs = new PreferencesManager(this);
        voiceRepository = new VoiceRepository(this);

        etVoicePrompt = findViewById(R.id.etVoicePrompt);
        etCloneVoiceName = findViewById(R.id.etCloneVoiceName);
        cbCloneConsent = findViewById(R.id.cbCloneConsent);
        btnGenerateVoiceDesign = findViewById(R.id.btnGenerateVoiceDesign);
        btnUploadCloneSample = findViewById(R.id.btnUploadCloneSample);

        findViewById(R.id.btnBackCloning).setOnClickListener(v -> finish());

        btnGenerateVoiceDesign.setOnClickListener(v -> {
            String prompt = etVoicePrompt.getText() != null ? etVoicePrompt.getText().toString().trim() : "";
            if (prompt.isEmpty()) {
                Toast.makeText(this, "Please describe the desired voice characteristics.", Toast.LENGTH_SHORT).show();
                return;
            }

            String apiKey = prefs.getApiKey();
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please configure your ElevenLabs API Key in Settings to design custom voices.", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(this, "Synthesizing custom voice preview from description…", Toast.LENGTH_LONG).show();
        });

        btnUploadCloneSample.setOnClickListener(v -> {
            String voiceName = etCloneVoiceName.getText() != null ? etCloneVoiceName.getText().toString().trim() : "";
            if (voiceName.isEmpty()) {
                Toast.makeText(this, "Please enter a name for the cloned voice.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbCloneConsent.isChecked()) {
                Toast.makeText(this, "You must check and confirm legal consent to clone voices.", Toast.LENGTH_LONG).show();
                return;
            }

            String apiKey = prefs.getApiKey();
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please configure your ElevenLabs API Key in Settings for voice cloning.", Toast.LENGTH_LONG).show();
                return;
            }

            // Successfully validated legal consent and parameters
            Toast.makeText(this, "Voice clone request created for '" + voiceName + "'!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
