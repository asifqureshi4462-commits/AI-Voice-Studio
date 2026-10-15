package com.asifqureshi.aivoicestudio.ui.tts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.SpeechRepository;
import com.asifqureshi.aivoicestudio.data.repository.VoiceRepository;
import com.asifqureshi.aivoicestudio.ui.common.WaveformVisualizerView;
import com.asifqureshi.aivoicestudio.ui.studio.AudioStudioActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TtsFragment extends Fragment {
    private static final int MAX_CHARS = 5000;

    private EditText etScriptInput;
    private TextView tvCharCount, tvActiveVoiceName;
    private MaterialButton btnGenerateSpeech;
    private MaterialCardView cardGeneratingState, cardAudioResult;
    private TextView tvResultTitle, tvResultDuration;
    private WaveformVisualizerView waveformVisualizer;
    private SeekBar sbAudioProgress;
    private ImageButton btnPlayGenerated, btnDownloadAudio, btnShareAudio;
    private MaterialButton btnOpenInStudio;

    private PreferencesManager prefs;
    private SpeechRepository speechRepository;
    private VoiceRepository voiceRepository;
    private AudioPlayerManager playerManager;
    private ProjectItem lastGeneratedProject;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        loadTextFromUri(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tts, container, false);

        prefs = new PreferencesManager(requireContext());
        speechRepository = new SpeechRepository(requireContext());
        voiceRepository = new VoiceRepository(requireContext());
        playerManager = AudioPlayerManager.getInstance(requireContext());

        etScriptInput = view.findViewById(R.id.etScriptInput);
        tvCharCount = view.findViewById(R.id.tvCharCount);
        tvActiveVoiceName = view.findViewById(R.id.tvActiveVoiceName);
        btnGenerateSpeech = view.findViewById(R.id.btnGenerateSpeech);
        cardGeneratingState = view.findViewById(R.id.cardGeneratingState);
        cardAudioResult = view.findViewById(R.id.cardAudioResult);
        tvResultTitle = view.findViewById(R.id.tvResultTitle);
        tvResultDuration = view.findViewById(R.id.tvResultDuration);
        waveformVisualizer = view.findViewById(R.id.waveformVisualizer);
        sbAudioProgress = view.findViewById(R.id.sbAudioProgress);
        btnPlayGenerated = view.findViewById(R.id.btnPlayGenerated);
        btnDownloadAudio = view.findViewById(R.id.btnDownloadAudio);
        btnShareAudio = view.findViewById(R.id.btnShareAudio);
        btnOpenInStudio = view.findViewById(R.id.btnOpenInStudio);

        setupVoiceDisplay();
        setupTextEditor(view);
        setupAudioPlayer();

        view.findViewById(R.id.btnVoiceSettings).setOnClickListener(v -> {
            VoiceSettingsBottomSheet sheet = VoiceSettingsBottomSheet.newInstance();
            sheet.show(getChildFragmentManager(), "VoiceSettings");
        });

        view.findViewById(R.id.cardVoicePicker).setOnClickListener(v -> {
            // Select voice from quick dialog
            showVoiceSelectionDialog();
        });

        btnGenerateSpeech.setOnClickListener(v -> generateSpeech());

        return view;
    }

    private void setupVoiceDisplay() {
        String voiceId = prefs.getSelectedVoiceId();
        Voice voice = voiceRepository.findVoiceById(voiceId);
        if (voice != null) {
            tvActiveVoiceName.setText(String.format("%s (%s • %s)", voice.getName(), voice.getAccent(), voice.getGender()));
        } else {
            tvActiveVoiceName.setText(prefs.getSelectedVoiceName());
        }
    }

    private void setupTextEditor(View view) {
        etScriptInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int len = s.length();
                tvCharCount.setText(String.format("%d / %d chars", len, MAX_CHARS));
                if (len > MAX_CHARS) {
                    tvCharCount.setTextColor(requireContext().getColor(R.color.color_error));
                } else {
                    tvCharCount.setTextColor(requireContext().getColor(R.color.text_secondary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.btnSampleScripts).setOnClickListener(v -> showSampleScriptsDialog());

        view.findViewById(R.id.btnImportScript).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            filePickerLauncher.launch(intent);
        });

        view.findViewById(R.id.btnClearScript).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), R.style.CustomBottomSheetDialogTheme)
                    .setTitle("Clear Script?")
                    .setMessage("Are you sure you want to clear your current script text?")
                    .setPositiveButton("Clear", (dialog, which) -> etScriptInput.setText(""))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showSampleScriptsDialog() {
        String[] titles = new String[]{
                "Audiobook Narration",
                "Product Commercial Promo",
                "Podcast Intro & Welcome",
                "Tech Tutorial Explanation"
        };
        String[] samples = new String[]{
                "The old cobblestone alley was bathed in silver moonlight as Evelyn pulled her woolen cloak tighter. Somewhere in the quiet night, the clock tower struck twelve.",
                "Transform the way you create content. With AI Voice Studio, experience the power of lifelike voice generation in over twenty-nine languages in just seconds.",
                "Welcome to another episode of The Tech Frontier. Today, we're diving deep into the future of voice synthesis and generative artificial intelligence.",
                "In this lesson, we will explore how deep neural networks process text and synthesize expressive waveforms with custom stability and style parameters."
        };

        new AlertDialog.Builder(requireContext(), R.style.CustomBottomSheetDialogTheme)
                .setTitle("Select Sample Template")
                .setItems(titles, (dialog, which) -> {
                    etScriptInput.setText(samples[which]);
                })
                .show();
    }

    private void showVoiceSelectionDialog() {
        java.util.List<Voice> voices = voiceRepository.getCachedVoices();
        String[] names = new String[voices.size()];
        for (int i = 0; i < voices.size(); i++) {
            Voice v = voices.get(i);
            names[i] = String.format("%s - %s (%s)", v.getName(), v.getCategory(), v.getAccent());
        }

        new AlertDialog.Builder(requireContext(), R.style.CustomBottomSheetDialogTheme)
                .setTitle("Select Speaker Voice")
                .setItems(names, (dialog, which) -> {
                    Voice selected = voices.get(which);
                    prefs.setSelectedVoiceId(selected.getVoiceId());
                    prefs.setSelectedVoiceName(selected.getName());
                    setupVoiceDisplay();
                })
                .show();
    }

    private void loadTextFromUri(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                etScriptInput.setText(sb.toString().trim());
                Toast.makeText(requireContext(), "Script imported successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to read file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateSpeech() {
        String text = etScriptInput.getText() != null ? etScriptInput.getText().toString().trim() : "";
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter script text first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (text.length() > MAX_CHARS) {
            Toast.makeText(requireContext(), "Text exceeds maximum character limit (5,000 chars).", Toast.LENGTH_LONG).show();
            return;
        }

        // Show generating state
        btnGenerateSpeech.setEnabled(false);
        cardGeneratingState.setVisibility(View.VISIBLE);
        cardAudioResult.setVisibility(View.GONE);

        String voiceId = prefs.getSelectedVoiceId();
        String voiceName = prefs.getSelectedVoiceName();
        String modelId = prefs.getSelectedModelId();
        String format = prefs.getOutputFormat();

        speechRepository.generateSpeech(text, voiceId, voiceName, modelId, prefs.getVoiceSettings(), format, new SpeechRepository.SpeechCallback() {
            @Override
            public void onSuccess(ProjectItem project) {
                if (!isAdded()) return;
                btnGenerateSpeech.setEnabled(true);
                cardGeneratingState.setVisibility(View.GONE);
                cardAudioResult.setVisibility(View.VISIBLE);

                lastGeneratedProject = project;
                tvResultTitle.setText(project.getTitle());
                tvResultDuration.setText(AudioProcessingUtils.formatDuration(project.getDurationMs()));

                // Auto-play generated audio
                playerManager.play(project.getAudioPath());
                btnPlayGenerated.setImageResource(R.drawable.ic_pause);
                waveformVisualizer.setPlaying(true);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                btnGenerateSpeech.setEnabled(true);
                cardGeneratingState.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupAudioPlayer() {
        btnPlayGenerated.setOnClickListener(v -> {
            if (lastGeneratedProject != null && lastGeneratedProject.getAudioPath() != null) {
                if (playerManager.isPlaying()) {
                    playerManager.pause();
                    btnPlayGenerated.setImageResource(R.drawable.ic_play_arrow);
                    waveformVisualizer.setPlaying(false);
                } else {
                    playerManager.play(lastGeneratedProject.getAudioPath());
                    btnPlayGenerated.setImageResource(R.drawable.ic_pause);
                    waveformVisualizer.setPlaying(true);
                }
            }
        });

        sbAudioProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && lastGeneratedProject != null) {
                    long duration = playerManager.getDuration();
                    long seekPos = (duration * progress) / 100;
                    playerManager.seekTo(seekPos);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        playerManager.setListener(new AudioPlayerManager.PlayerListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                if (!isAdded()) return;
                btnPlayGenerated.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
                waveformVisualizer.setPlaying(isPlaying);
            }

            @Override
            public void onProgressUpdate(long currentPositionMs, long durationMs) {
                if (!isAdded()) return;
                if (durationMs > 0) {
                    int percent = (int) ((currentPositionMs * 100) / durationMs);
                    sbAudioProgress.setProgress(percent);
                    waveformVisualizer.setProgress(currentPositionMs / (float) durationMs);
                    tvResultDuration.setText(String.format("%s / %s",
                            AudioProcessingUtils.formatDuration(currentPositionMs),
                            AudioProcessingUtils.formatDuration(durationMs)));
                }
            }

            @Override
            public void onCompleted() {
                if (!isAdded()) return;
                btnPlayGenerated.setImageResource(R.drawable.ic_play_arrow);
                waveformVisualizer.setPlaying(false);
                waveformVisualizer.setProgress(0f);
                sbAudioProgress.setProgress(0);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        btnDownloadAudio.setOnClickListener(v -> {
            if (lastGeneratedProject != null && lastGeneratedProject.getAudioPath() != null) {
                File file = new File(lastGeneratedProject.getAudioPath());
                Uri uri = AudioProcessingUtils.exportAudioToDownloads(requireContext(), file, lastGeneratedProject.getTitle());
                if (uri != null) {
                    Toast.makeText(requireContext(), R.string.toast_download_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to export audio file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnShareAudio.setOnClickListener(v -> {
            if (lastGeneratedProject != null && lastGeneratedProject.getAudioPath() != null) {
                File audioFile = new File(lastGeneratedProject.getAudioPath());
                if (audioFile.exists()) {
                    try {
                        Uri uri = androidx.core.content.FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", audioFile);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("audio/mpeg");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share Audio via"));
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Share error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnOpenInStudio.setOnClickListener(v -> {
            if (lastGeneratedProject != null && lastGeneratedProject.getAudioPath() != null) {
                Intent intent = new Intent(requireContext(), AudioStudioActivity.class);
                intent.putExtra("AUDIO_PATH", lastGeneratedProject.getAudioPath());
                intent.putExtra("AUDIO_TITLE", lastGeneratedProject.getTitle());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupVoiceDisplay();
    }
}
