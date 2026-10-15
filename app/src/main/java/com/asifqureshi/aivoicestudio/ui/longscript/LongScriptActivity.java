package com.asifqureshi.aivoicestudio.ui.longscript;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.models.ScriptChunk;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import com.asifqureshi.aivoicestudio.data.repository.SpeechRepository;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LongScriptActivity extends AppCompatActivity {
    private static final int TARGET_CHUNK_SIZE = 600;

    private EditText etLongScriptInput;
    private TextView tvChunksHeader;
    private RecyclerView rvScriptChunks;
    private LinearLayout layoutLongScriptBottom;
    private MaterialButton btnGenerateAllChunks;

    private ScriptChunkAdapter adapter;
    private final List<ScriptChunk> chunkList = new ArrayList<>();
    private SpeechRepository speechRepository;
    private ProjectDatabaseHelper dbHelper;
    private AudioPlayerManager playerManager;
    private PreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_script);

        speechRepository = new SpeechRepository(this);
        dbHelper = new ProjectDatabaseHelper(this);
        playerManager = AudioPlayerManager.getInstance(this);
        prefs = new PreferencesManager(this);

        etLongScriptInput = findViewById(R.id.etLongScriptInput);
        tvChunksHeader = findViewById(R.id.tvChunksHeader);
        rvScriptChunks = findViewById(R.id.rvScriptChunks);
        layoutLongScriptBottom = findViewById(R.id.layoutLongScriptBottom);
        btnGenerateAllChunks = findViewById(R.id.btnGenerateAllChunks);

        findViewById(R.id.btnBackLongScript).setOnClickListener(v -> finish());

        rvScriptChunks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScriptChunkAdapter(this, chunk -> {
            if (chunk.getAudioPath() != null && new File(chunk.getAudioPath()).exists()) {
                playerManager.play(chunk.getAudioPath());
            }
        });
        rvScriptChunks.setAdapter(adapter);

        findViewById(R.id.btnSegmentScript).setOnClickListener(v -> segmentScript());

        btnGenerateAllChunks.setOnClickListener(v -> generateAllChunks());
    }

    private void segmentScript() {
        String fullText = etLongScriptInput.getText() != null ? etLongScriptInput.getText().toString().trim() : "";
        if (fullText.isEmpty()) {
            Toast.makeText(this, "Please enter script text to segment", Toast.LENGTH_SHORT).show();
            return;
        }

        chunkList.clear();
        // Intelligent sentence and paragraph splitting
        String[] paragraphs = fullText.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String p : paragraphs) {
            String pTrimmed = p.trim();
            if (pTrimmed.isEmpty()) continue;

            if (currentChunk.length() + pTrimmed.length() <= TARGET_CHUNK_SIZE) {
                if (currentChunk.length() > 0) currentChunk.append("\n\n");
                currentChunk.append(pTrimmed);
            } else {
                if (currentChunk.length() > 0) {
                    chunkList.add(new ScriptChunk(chunkList.size(), currentChunk.toString()));
                    currentChunk = new StringBuilder();
                }

                // If paragraph itself is longer than target size, split by sentences
                if (pTrimmed.length() > TARGET_CHUNK_SIZE) {
                    String[] sentences = pTrimmed.split("(?<=[.!?])\\s+");
                    for (String sentence : sentences) {
                        if (currentChunk.length() + sentence.length() <= TARGET_CHUNK_SIZE) {
                            if (currentChunk.length() > 0) currentChunk.append(" ");
                            currentChunk.append(sentence);
                        } else {
                            if (currentChunk.length() > 0) {
                                chunkList.add(new ScriptChunk(chunkList.size(), currentChunk.toString()));
                                currentChunk = new StringBuilder();
                            }
                            currentChunk.append(sentence);
                        }
                    }
                } else {
                    currentChunk.append(pTrimmed);
                }
            }
        }

        if (currentChunk.length() > 0) {
            chunkList.add(new ScriptChunk(chunkList.size(), currentChunk.toString()));
        }

        adapter.setChunks(chunkList);
        tvChunksHeader.setVisibility(View.VISIBLE);
        layoutLongScriptBottom.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Script prepared into " + chunkList.size() + " narration chunks", Toast.LENGTH_SHORT).show();
    }

    private void generateAllChunks() {
        if (chunkList.isEmpty()) {
            Toast.makeText(this, "No chunks to synthesize", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerateAllChunks.setEnabled(false);
        Toast.makeText(this, "Batch synthesizing narration chunks…", Toast.LENGTH_LONG).show();
        generateSequentialChunk(0, new ArrayList<>());
    }

    private void generateSequentialChunk(int index, List<String> audioPaths) {
        if (index >= chunkList.size()) {
            // Stitch all chunks
            stitchAllAudio(audioPaths);
            return;
        }

        ScriptChunk chunk = chunkList.get(index);
        chunk.setStatus("generating");
        adapter.notifyItemChanged(index);

        speechRepository.generateSpeech(
                chunk.getText(),
                prefs.getSelectedVoiceId(),
                prefs.getSelectedVoiceName(),
                prefs.getSelectedModelId(),
                prefs.getVoiceSettings(),
                "mp3_44100_128",
                new SpeechRepository.SpeechCallback() {
                    @Override
                    public void onSuccess(ProjectItem project) {
                        chunk.setAudioPath(project.getAudioPath());
                        chunk.setStatus("completed");
                        adapter.notifyItemChanged(index);
                        audioPaths.add(project.getAudioPath());
                        generateSequentialChunk(index + 1, audioPaths);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        chunk.setStatus("error");
                        chunk.setErrorMessage(errorMessage);
                        adapter.notifyItemChanged(index);
                        btnGenerateAllChunks.setEnabled(true);
                        Toast.makeText(LongScriptActivity.this, "Error in chunk " + (index + 1) + ": " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void stitchAllAudio(List<String> audioPaths) {
        File outDir = new File(getFilesDir(), "audio_studio");
        if (!outDir.exists()) outDir.mkdirs();
        String mergedPath = new File(outDir, "narration_master_" + System.currentTimeMillis() + ".mp3").getAbsolutePath();

        boolean success = AudioProcessingUtils.mergeAudioFiles(audioPaths, mergedPath);
        btnGenerateAllChunks.setEnabled(true);

        if (success) {
            String fullScript = etLongScriptInput.getText().toString();
            String title = "Narration (" + chunkList.size() + " segments)";
            ProjectItem mergedProject = new ProjectItem(
                    0,
                    title,
                    fullScript,
                    prefs.getSelectedVoiceId(),
                    prefs.getSelectedVoiceName(),
                    mergedPath,
                    new File(mergedPath).length() / 16,
                    new File(mergedPath).length(),
                    System.currentTimeMillis(),
                    false,
                    "long_script"
            );
            dbHelper.insertProject(mergedProject);
            playerManager.play(mergedPath);
            Toast.makeText(this, "Complete narration stitched into master audio!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to stitch audio files", Toast.LENGTH_SHORT).show();
        }
    }
}
