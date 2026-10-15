package com.asifqureshi.aivoicestudio.ui.studio;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import com.asifqureshi.aivoicestudio.ui.common.WaveformVisualizerView;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.util.List;

public class AudioStudioActivity extends AppCompatActivity {
    private TextView tvTrackTitle, tvCurrentTime, tvTotalTime, tvTrimStartVal, tvTrimEndVal, tvMusicVolVal;
    private WaveformVisualizerView studioWaveform;
    private ImageButton btnPlayPause;
    private SeekBar sbTrimStart, sbTrimEnd, sbMusicVolume;
    private MaterialButton btnExportAudio;

    private AudioPlayerManager playerManager;
    private ProjectDatabaseHelper dbHelper;
    private String currentAudioPath;
    private String currentTitle;
    private long totalDurationMs = 24000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_studio);

        playerManager = AudioPlayerManager.getInstance(this);
        dbHelper = new ProjectDatabaseHelper(this);

        currentAudioPath = getIntent().getStringExtra("AUDIO_PATH");
        currentTitle = getIntent().getStringExtra("AUDIO_TITLE");

        if (currentAudioPath == null) {
            // Pick most recent project from DB if not provided
            List<ProjectItem> list = dbHelper.getAllProjects();
            if (!list.isEmpty()) {
                currentAudioPath = list.get(0).getAudioPath();
                currentTitle = list.get(0).getTitle();
                totalDurationMs = list.get(0).getDurationMs();
            }
        }

        tvTrackTitle = findViewById(R.id.tvStudioTrackTitle);
        tvCurrentTime = findViewById(R.id.tvStudioCurrentTime);
        tvTotalTime = findViewById(R.id.tvStudioTotalTime);
        studioWaveform = findViewById(R.id.studioWaveform);
        btnPlayPause = findViewById(R.id.btnStudioPlayPause);
        tvTrimStartVal = findViewById(R.id.tvTrimStartVal);
        tvTrimEndVal = findViewById(R.id.tvTrimEndVal);
        tvMusicVolVal = findViewById(R.id.tvMusicVolumeVal);
        sbTrimStart = findViewById(R.id.sbTrimStart);
        sbTrimEnd = findViewById(R.id.sbTrimEnd);
        sbMusicVolume = findViewById(R.id.sbMusicVolume);
        btnExportAudio = findViewById(R.id.btnExportAudio);

        findViewById(R.id.btnBackStudio).setOnClickListener(v -> finish());

        if (currentTitle != null) {
            tvTrackTitle.setText(currentTitle);
        }

        setupTrimmer();
        setupPlayer();
        setupExport();
    }

    private void setupTrimmer() {
        sbTrimStart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float seconds = (progress / 100.0f) * (totalDurationMs / 1000.0f);
                tvTrimStartVal.setText(String.format("%.1fs", seconds));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbTrimEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float seconds = (progress / 100.0f) * (totalDurationMs / 1000.0f);
                tvTrimEndVal.setText(String.format("%.1fs", seconds));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbMusicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMusicVolVal.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupPlayer() {
        btnPlayPause.setOnClickListener(v -> {
            if (currentAudioPath != null && new File(currentAudioPath).exists()) {
                if (playerManager.isPlaying()) {
                    playerManager.pause();
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    studioWaveform.setPlaying(false);
                } else {
                    playerManager.play(currentAudioPath);
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    studioWaveform.setPlaying(true);
                }
            } else {
                Toast.makeText(this, "No audio file loaded", Toast.LENGTH_SHORT).show();
            }
        });

        playerManager.setListener(new AudioPlayerManager.PlayerListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
                studioWaveform.setPlaying(isPlaying);
            }

            @Override
            public void onProgressUpdate(long currentPositionMs, long durationMs) {
                if (durationMs > 0) {
                    totalDurationMs = durationMs;
                    studioWaveform.setProgress(currentPositionMs / (float) durationMs);
                    tvCurrentTime.setText(AudioProcessingUtils.formatDuration(currentPositionMs));
                    tvTotalTime.setText(AudioProcessingUtils.formatDuration(durationMs));
                }
            }

            @Override
            public void onCompleted() {
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                studioWaveform.setPlaying(false);
                studioWaveform.setProgress(0f);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AudioStudioActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExport() {
        btnExportAudio.setOnClickListener(v -> {
            if (currentAudioPath != null) {
                File src = new File(currentAudioPath);
                String name = (currentTitle != null ? currentTitle : "MasterAudio") + "_StudioExport";
                Uri uri = AudioProcessingUtils.exportAudioToDownloads(this, src, name);
                if (uri != null) {
                    Toast.makeText(this, "Master audio exported to Downloads folder!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Exported audio saved to storage", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No audio track available to export", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
