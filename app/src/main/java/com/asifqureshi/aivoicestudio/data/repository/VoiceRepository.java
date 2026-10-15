package com.asifqureshi.aivoicestudio.data.repository;

import android.content.Context;
import android.util.Log;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.network.ApiClient;
import com.asifqureshi.aivoicestudio.data.network.ElevenLabsApiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoiceRepository {
    private static final String TAG = "VoiceRepository";
    private final Context context;
    private final PreferencesManager prefs;
    private final ProjectDatabaseHelper dbHelper;
    private final List<Voice> cachedVoices = new ArrayList<>();

    public interface VoiceCallback {
        void onSuccess(List<Voice> voices);
        void onError(String message);
    }

    public VoiceRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PreferencesManager(context);
        this.dbHelper = new ProjectDatabaseHelper(context);
        initDefaultVoices();
    }

    private void initDefaultVoices() {
        cachedVoices.clear();
        addDefaultVoice("21m00Tcm4TlvDq8ikWAM", "Rachel", "Conversational", "American", "Female", "Calm, natural, and friendly. Great for casual conversation and explainers.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/21m00Tcm4TlvDq8ikWAM/preview.mp3", "English (US)");
        addDefaultVoice("pNInz6obpgDQGcFmaJgB", "Adam", "Narration", "American", "Male", "Deep, warm, and authoritative. Perfect for audiobooks and corporate narration.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/pNInz6obpgDQGcFmaJgB/preview.mp3", "English (US)");
        addDefaultVoice("ErXwobaYiN019PkySvjV", "Antoni", "Narration", "American", "Male", "Well-rounded, crisp, and articulate. Ideal for podcasts and commercial spots.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/ErXwobaYiN019PkySvjV/preview.mp3", "English (US)");
        addDefaultVoice("EXAVITQu4vr4xnSDxMaL", "Bella", "Characters", "American", "Female", "Soft, expressive, and emotional. Suited for storytelling and video games.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/EXAVITQu4vr4xnSDxMaL/preview.mp3", "English (US)");
        addDefaultVoice("2EiwWnXFnvU5JabPnv8n", "Clyde", "Characters", "American", "Male", "Grit, character, and war veteran tone. Perfect for action stories and video games.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/2EiwWnXFnvU5JabPnv8n/preview.mp3", "English (US)");
        addDefaultVoice("AZnzlk1XvdvUeBnXmlld", "Domi", "Social Media", "American", "Female", "Strong, emphatic, and engaging. Great for TikTok, Reels, and promos.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/AZnzlk1XvdvUeBnXmlld/preview.mp3", "English (US)");
        addDefaultVoice("MF3mGyEYCl7XYWbV9V6O", "Elli", "Conversational", "American", "Female", "Young, lively, and cheerful. Great for animated content and tutorials.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/MF3mGyEYCl7XYWbV9V6O/preview.mp3", "English (US)");
        addDefaultVoice("TxGEqnHWrfWFTfGW9XjX", "Josh", "Narration", "American", "Male", "Deep, resonant, and natural. Best for documentary and technical narration.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/TxGEqnHWrfWFTfGW9XjX/preview.mp3", "English (US)");
        addDefaultVoice("yoZ06aMxZJJ28mfd3POQ", "Sam", "Narration", "American", "Male", "Dynamic, clear, and energetic. Excellent for educational storytelling.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/yoZ06aMxZJJ28mfd3POQ/preview.mp3", "English (US)");
        addDefaultVoice("XB0fDUnXU5powFXDhCwa", "Charlotte", "Characters", "British", "Female", "Posh, refined, and captivating. Perfect for high-end luxury brands.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/XB0fDUnXU5powFXDhCwa/preview.mp3", "English (UK)");
        addDefaultVoice("JBFqnCBsd6RMkjVDRZzb", "George", "Narration", "British", "Male", "Warm, classic British narrator tone with expressive nuances.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/JBFqnCBsd6RMkjVDRZzb/preview.mp3", "English (UK)");
        addDefaultVoice("piTKgcLEGmPE4e6mEKli", "Nicole", "Conversational", "Australian", "Female", "Whispering, relaxed, and soothing. Excellent for meditation and ASMR.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/piTKgcLEGmPE4e6mEKli/preview.mp3", "English (AU)");
        addDefaultVoice("onwK4e9ZLuTAKqWW03F9", "Daniel", "News", "British", "Male", "Authoritative broadcast anchor style with deep articulation.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/onwK4e9ZLuTAKqWW03F9/preview.mp3", "English (UK)");
        addDefaultVoice("nPczCjzI2devNBz1zQrb", "Brian", "News", "American", "Male", "Classic TV broadcast news anchor. Professional, steady, and clear.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/nPczCjzI2devNBz1zQrb/preview.mp3", "English (US)");
        addDefaultVoice("pFZP5JQG7iQjIQuC4Bku", "Lily", "Narration", "British", "Female", "Warm, velvety, and charming. Great for audiobooks and bedtime stories.", "https://storage.googleapis.com/eleven-public-prod/previews/voices/pFZP5JQG7iQjIQuC4Bku/preview.mp3", "English (UK)");
    }

    private void addDefaultVoice(String id, String name, String cat, String accent, String gender, String desc, String preview, String lang) {
        Voice v = new Voice(id, name, cat, accent, gender, desc, preview, lang);
        v.setFavorite(dbHelper.isVoiceFavorite(id));
        cachedVoices.add(v);
    }

    public List<Voice> getCachedVoices() {
        // Refresh favorite states
        for (Voice v : cachedVoices) {
            v.setFavorite(dbHelper.isVoiceFavorite(v.getVoiceId()));
        }
        return new ArrayList<>(cachedVoices);
    }

    public void fetchVoices(VoiceCallback callback) {
        String apiKey = prefs.getApiKey();
        ElevenLabsApiService api = ApiClient.getService(context);

        api.getVoices(apiKey).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonArray voicesArray = response.body().getAsJsonArray("voices");
                        if (voicesArray != null && voicesArray.size() > 0) {
                            cachedVoices.clear();
                            for (JsonElement el : voicesArray) {
                                JsonObject obj = el.getAsJsonObject();
                                String vId = obj.get("voice_id").getAsString();
                                String vName = obj.has("name") ? obj.get("name").getAsString() : "Voice";
                                String cat = obj.has("category") ? obj.get("category").getAsString() : "General";
                                String preview = obj.has("preview_url") && !obj.get("preview_url").isJsonNull() ? obj.get("preview_url").getAsString() : null;
                                
                                String accent = "Neutral";
                                String gender = "Neutral";
                                String desc = "";
                                if (obj.has("labels") && obj.get("labels").isJsonObject()) {
                                    JsonObject labels = obj.getAsJsonObject("labels");
                                    if (labels.has("accent")) accent = labels.get("accent").getAsString();
                                    if (labels.has("gender")) gender = labels.get("gender").getAsString();
                                    if (labels.has("description")) desc = labels.get("description").getAsString();
                                }

                                Voice v = new Voice(vId, vName, cat, accent, gender, desc, preview, "Multilingual");
                                v.setFavorite(dbHelper.isVoiceFavorite(vId));
                                cachedVoices.add(v);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing voices", e);
                    }
                }
                callback.onSuccess(getCachedVoices());
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.w(TAG, "Failed to fetch voices from API: " + t.getMessage() + ". Using cached voices catalog.");
                callback.onSuccess(getCachedVoices());
            }
        });
    }

    public Voice findVoiceById(String voiceId) {
        for (Voice v : cachedVoices) {
            if (v.getVoiceId().equals(voiceId)) {
                v.setFavorite(dbHelper.isVoiceFavorite(voiceId));
                return v;
            }
        }
        return cachedVoices.isEmpty() ? null : cachedVoices.get(0);
    }
}
