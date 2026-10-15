package com.asifqureshi.aivoicestudio.data.repository;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.asifqureshi.aivoicestudio.data.models.GenerationRequest;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.models.UserProfile;
import com.asifqureshi.aivoicestudio.data.models.VoiceSettings;
import com.asifqureshi.aivoicestudio.data.network.ApiClient;
import com.asifqureshi.aivoicestudio.data.network.ElevenLabsApiService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class SpeechRepository {
    private static final String TAG = "SpeechRepository";
    private final Context context;
    private final PreferencesManager prefs;
    private final ProjectDatabaseHelper dbHelper;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface SpeechCallback {
        void onSuccess(ProjectItem project);
        void onError(String errorMessage);
    }

    public SpeechRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PreferencesManager(context);
        this.dbHelper = new ProjectDatabaseHelper(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void generateSpeech(String text, String voiceId, String voiceName, String modelId, VoiceSettings settings, String outputFormat, SpeechCallback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onError("Please enter script text to generate speech.");
            return;
        }

        String apiKey = prefs.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("ElevenLabs API Key is not configured. Please enter your API key in Settings or connect your backend server.");
            return;
        }

        executorService.execute(() -> {
            try {
                ElevenLabsApiService api = ApiClient.getService(context);
                GenerationRequest request = new GenerationRequest(text, modelId != null ? modelId : "eleven_multilingual_v2", settings != null ? settings : new VoiceSettings());

                Call<ResponseBody> call = api.generateSpeech(voiceId, outputFormat != null ? outputFormat : "mp3_44100_128", apiKey, request);
                Response<ResponseBody> response = call.execute();

                if (!response.isSuccessful() || response.body() == null) {
                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown API error";
                    int code = response.code();
                    String userError;
                    if (code == 401) {
                        userError = "Unauthorized: Invalid ElevenLabs API Key. Please verify in Settings.";
                    } else if (code == 429) {
                        userError = "ElevenLabs quota exceeded or rate limit reached for this billing tier.";
                    } else {
                        userError = "Generation failed (HTTP " + code + "): " + errorBody;
                    }
                    postError(callback, userError);
                    return;
                }

                // Stream response into audio file
                File audioDir = new File(context.getFilesDir(), "audio_studio");
                if (!audioDir.exists()) {
                    audioDir.mkdirs();
                }

                String fileName = "tts_" + System.currentTimeMillis() + ".mp3";
                File outputFile = new File(audioDir, fileName);

                try (InputStream is = response.body().byteStream();
                     OutputStream os = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                }

                long fileSizeBytes = outputFile.length();
                long durationMs = 0;
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(outputFile.getAbsolutePath());
                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    if (durationStr != null) {
                        durationMs = Long.parseLong(durationStr);
                    }
                    mmr.release();
                } catch (Exception e) {
                    Log.w(TAG, "Could not extract duration metadata", e);
                    // Approximate duration based on MP3 bitrate (~16 KB per sec for 128kbps)
                    durationMs = (fileSizeBytes / 16000) * 1000;
                }

                // Generate title from first few words
                String title = text.length() > 32 ? text.substring(0, 32).trim() + "…" : text;
                ProjectItem project = new ProjectItem(
                        0,
                        title,
                        text,
                        voiceId,
                        voiceName,
                        outputFile.getAbsolutePath(),
                        durationMs,
                        fileSizeBytes,
                        System.currentTimeMillis(),
                        false,
                        "tts"
                );

                long id = dbHelper.insertProject(project);
                project.setId(id);

                // Update character quota in user profile
                UserProfile profile = prefs.getUserProfile();
                profile.setCharactersUsed(profile.getCharactersUsed() + text.length());
                prefs.setUserProfile(profile);

                postSuccess(callback, project);

            } catch (Exception e) {
                Log.e(TAG, "Speech synthesis exception", e);
                postError(callback, "Generation error: " + e.getMessage());
            }
        });
    }

    private void postSuccess(SpeechCallback callback, ProjectItem project) {
        mainHandler.post(() -> callback.onSuccess(project));
    }

    private void postError(SpeechCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
