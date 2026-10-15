package com.asifqureshi.aivoicestudio.ui.dialogue;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.models.DialogueLine;
import com.asifqureshi.aivoicestudio.data.models.DialogueSpeaker;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import com.asifqureshi.aivoicestudio.data.repository.SpeechRepository;
import com.asifqureshi.aivoicestudio.data.repository.VoiceRepository;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DialogueActivity extends AppCompatActivity {
    private RecyclerView rvDialogueLines;
    private DialogueLineAdapter adapter;
    private TextView tvActiveSpeakers;
    private MaterialButton btnAddSpeaker, btnAddLine, btnGenerateFullDialogue;

    private final List<DialogueSpeaker> speakerList = new ArrayList<>();
    private VoiceRepository voiceRepository;
    private SpeechRepository speechRepository;
    private ProjectDatabaseHelper dbHelper;
    private AudioPlayerManager playerManager;
    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);

        voiceRepository = new VoiceRepository(this);
        speechRepository = new SpeechRepository(this);
        dbHelper = new ProjectDatabaseHelper(this);
        playerManager = AudioPlayerManager.getInstance(this);
        prefs = new PreferencesManager(this);

        rvDialogueLines = findViewById(R.id.rvDialogueLines);
        tvActiveSpeakers = findViewById(R.id.tvActiveSpeakers);
        btnAddSpeaker = findViewById(R.id.btnAddSpeaker);
        btnAddLine = findViewById(R.id.btnAddLine);
        btnGenerateFullDialogue = findViewById(R.id.btnGenerateFullDialogue);

        findViewById(R.id.btnBackDialogue).setOnClickListener(v -> finish());

        // Default speakers
        speakerList.add(new DialogueSpeaker("spk_1", "Speaker A", "21m00Tcm4TlvDq8ikWAM", "Rachel", "#00E5FF"));
        speakerList.add(new DialogueSpeaker("spk_2", "Speaker B", "pNInz6obpgDQGcFmaJgB", "Adam", "#8C52FF"));
        updateSpeakersDisplay();

        rvDialogueLines.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DialogueLineAdapter(this, new DialogueLineAdapter.OnLineActionListener() {
            @Override
            public void onPlayLine(DialogueLine line) {
                if (line.getAudioPath() != null && new File(line.getAudioPath()).exists()) {
                    playerManager.play(line.getAudioPath());
                } else {
                    synthesizeSingleLine(line);
                }
            }

            @Override
            public void onDeleteLine(int position) {}
        });
        rvDialogueLines.setAdapter(adapter);

        // Pre-fill initial dialogue scene
        adapter.addLine(new DialogueLine("line_1", "spk_1", "Speaker A (Rachel)", "21m00Tcm4TlvDq8ikWAM", "Welcome to AI Voice Studio. How does multi-speaker dialogue synthesis work?"));
        adapter.addLine(new DialogueLine("line_2", "spk_2", "Speaker B (Adam)", "pNInz6obpgDQGcFmaJgB", "Each speaker is assigned an individual neural voice model. The studio generates each turn and stitches them into a cinematic scene."));

        btnAddSpeaker.setOnClickListener(v -> showAddSpeakerDialog());

        btnAddLine.setOnClickListener(v -> showAddLineDialog());

        btnGenerateFullDialogue.setOnClickListener(v -> generateEntireDialogue());
    }

    private void updateSpeakersDisplay() {
        StringBuilder sb = new StringBuilder("Speakers: ");
        for (int i = 0; i < speakerList.size(); i++) {
            DialogueSpeaker s = speakerList.get(i);
            sb.append(s.getName()).append(" (").append(s.getVoiceName()).append(")");
            if (i < speakerList.size() - 1) sb.append(", ");
        }
        tvActiveSpeakers.setText(sb.toString());
    }

    private void showAddSpeakerDialog() {
        List<Voice> voices = voiceRepository.getCachedVoices();
        String[] voiceNames = new String[voices.size()];
        for (int i = 0; i < voices.size(); i++) {
            voiceNames[i] = voices.get(i).getName() + " (" + voices.get(i).getAccent() + ")";
        }

        new AlertDialog.Builder(this, R.style.CustomBottomSheetDialogTheme)
                .setTitle("Select Voice for New Speaker")
                .setItems(voiceNames, (dialog, which) -> {
                    Voice v = voices.get(which);
                    char letter = (char) ('A' + speakerList.size());
                    DialogueSpeaker newSpk = new DialogueSpeaker(
                            "spk_" + (speakerList.size() + 1),
                            "Speaker " + letter,
                            v.getVoiceId(),
                            v.getName(),
                            "#00E5FF"
                    );
                    speakerList.add(newSpk);
                    updateSpeakersDisplay();
                    Toast.makeText(this, "Added " + newSpk.getName() + " with voice " + v.getName(), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showAddLineDialog() {
        String[] names = new String[speakerList.size()];
        for (int i = 0; i < speakerList.size(); i++) {
            names[i] = speakerList.get(i).getName() + " (" + speakerList.get(i).getVoiceName() + ")";
        }

        new AlertDialog.Builder(this, R.style.CustomBottomSheetDialogTheme)
                .setTitle("Select Speaker for New Line")
                .setItems(names, (dialog, which) -> {
                    DialogueSpeaker spk = speakerList.get(which);
                    DialogueLine newLine = new DialogueLine(
                            "line_" + System.currentTimeMillis(),
                            spk.getId(),
                            spk.getName() + " (" + spk.getVoiceName() + ")",
                            spk.getVoiceId(),
                            ""
                    );
                    adapter.addLine(newLine);
                })
                .show();
    }

    private void synthesizeSingleLine(DialogueLine line) {
        if (line.getText() == null || line.getText().trim().isEmpty()) {
            Toast.makeText(this, "Line text is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Synthesizing " + line.getSpeakerName() + "…", Toast.LENGTH_SHORT).show();
        speechRepository.generateSpeech(
                line.getText(),
                line.getVoiceId(),
                line.getSpeakerName(),
                prefs.getSelectedModelId(),
                prefs.getVoiceSettings(),
                "mp3_44100_128",
                new SpeechRepository.SpeechCallback() {
                    @Override
                    public void onSuccess(ProjectItem project) {
                        line.setAudioPath(project.getAudioPath());
                        line.setGenerated(true);
                        playerManager.play(project.getAudioPath());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(DialogueActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void generateEntireDialogue() {
        List<DialogueLine> lines = adapter.getLines();
        if (lines.isEmpty()) {
            Toast.makeText(this, "No dialogue lines to generate", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerateFullDialogue.setEnabled(false);
        Toast.makeText(this, "Synthesizing dialogue lines in sequence…", Toast.LENGTH_LONG).show();
        generateLineSequential(0, lines, new ArrayList<>());
    }

    private void generateLineSequential(int index, List<DialogueLine> lines, List<String> audioPaths) {
        if (index >= lines.size()) {
            // All lines generated! Merge them
            mergeDialogueAudio(audioPaths);
            return;
        }

        DialogueLine line = lines.get(index);
        if (line.getAudioPath() != null && new File(line.getAudioPath()).exists()) {
            audioPaths.add(line.getAudioPath());
            generateLineSequential(index + 1, lines, audioPaths);
            return;
        }

        speechRepository.generateSpeech(
                line.getText(),
                line.getVoiceId(),
                line.getSpeakerName(),
                prefs.getSelectedModelId(),
                prefs.getVoiceSettings(),
                "mp3_44100_128",
                new SpeechRepository.SpeechCallback() {
                    @Override
                    public void onSuccess(ProjectItem project) {
                        line.setAudioPath(project.getAudioPath());
                        line.setGenerated(true);
                        audioPaths.add(project.getAudioPath());
                        generateLineSequential(index + 1, lines, audioPaths);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        btnGenerateFullDialogue.setEnabled(true);
                        Toast.makeText(DialogueActivity.this, "Error on line " + (index + 1) + ": " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void mergeDialogueAudio(List<String> audioPaths) {
        File outDir = new File(getFilesDir(), "audio_studio");
        if (!outDir.exists()) outDir.mkdirs();
        String mergedPath = new File(outDir, "dialogue_master_" + System.currentTimeMillis() + ".mp3").getAbsolutePath();

        boolean success = AudioProcessingUtils.mergeAudioFiles(audioPaths, mergedPath);
        btnGenerateFullDialogue.setEnabled(true);

        if (success) {
            ProjectItem mergedProject = new ProjectItem(
                    0,
                    "Dialogue Scene (" + linesSummary() + ")",
                    fullDialogueText(),
                    "multi_speaker",
                    "Multi-Speaker",
                    mergedPath,
                    new File(mergedPath).length() / 16,
                    new File(mergedPath).length(),
                    System.currentTimeMillis(),
                    false,
                    "dialogue"
            );
            dbHelper.insertProject(mergedProject);
            playerManager.play(mergedPath);
            Toast.makeText(this, "Full dialogue synthesized and merged successfully!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to merge dialogue audio tracks.", Toast.LENGTH_SHORT).show();
        }
    }

    private String linesSummary() {
        return adapter.getLines().size() + " lines";
    }

    private String fullDialogueText() {
        StringBuilder sb = new StringBuilder();
        for (DialogueLine l : adapter.getLines()) {
            sb.append(l.getSpeakerName()).append(": ").append(l.getText()).append("\n\n");
        }
        return sb.toString().trim();
    }
}
