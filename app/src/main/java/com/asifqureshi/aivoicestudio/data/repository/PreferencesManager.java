package com.asifqureshi.aivoicestudio.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.asifqureshi.aivoicestudio.data.models.UserProfile;
import com.asifqureshi.aivoicestudio.data.models.VoiceSettings;
import com.google.gson.Gson;

public class PreferencesManager {
    private static final String PREF_NAME = "ai_voice_studio_prefs";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_PROFILE = "user_profile";
    private static final String KEY_API_KEY = "elevenlabs_api_key";
    private static final String KEY_BACKEND_URL = "backend_url";
    private static final String KEY_SELECTED_VOICE_ID = "selected_voice_id";
    private static final String KEY_SELECTED_VOICE_NAME = "selected_voice_name";
    private static final String KEY_SELECTED_MODEL_ID = "selected_model_id";
    private static final String KEY_OUTPUT_FORMAT = "output_format";
    private static final String KEY_VOICE_SETTINGS = "voice_settings";
    private static final String KEY_DARK_THEME = "dark_theme";

    private final SharedPreferences prefs;
    private final Gson gson;

    public PreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    public void setAuthToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public UserProfile getUserProfile() {
        String json = prefs.getString(KEY_USER_PROFILE, null);
        if (json != null) {
            try {
                return gson.fromJson(json, UserProfile.class);
            } catch (Exception ignored) {
            }
        }
        return new UserProfile();
    }

    public void setUserProfile(UserProfile profile) {
        prefs.edit().putString(KEY_USER_PROFILE, gson.toJson(profile)).apply();
    }

    public String getApiKey() {
        return prefs.getString(KEY_API_KEY, "");
    }

    public void setApiKey(String apiKey) {
        prefs.edit().putString(KEY_API_KEY, apiKey != null ? apiKey.trim() : "").apply();
    }

    public String getBackendUrl() {
        return prefs.getString(KEY_BACKEND_URL, "");
    }

    public void setBackendUrl(String url) {
        prefs.edit().putString(KEY_BACKEND_URL, url != null ? url.trim() : "").apply();
    }

    public String getSelectedVoiceId() {
        return prefs.getString(KEY_SELECTED_VOICE_ID, "21m00Tcm4TlvDq8ikWAM"); // Rachel default
    }

    public void setSelectedVoiceId(String voiceId) {
        prefs.edit().putString(KEY_SELECTED_VOICE_ID, voiceId).apply();
    }

    public String getSelectedVoiceName() {
        return prefs.getString(KEY_SELECTED_VOICE_NAME, "Rachel");
    }

    public void setSelectedVoiceName(String name) {
        prefs.edit().putString(KEY_SELECTED_VOICE_NAME, name).apply();
    }

    public String getSelectedModelId() {
        return prefs.getString(KEY_SELECTED_MODEL_ID, "eleven_multilingual_v2");
    }

    public void setSelectedModelId(String modelId) {
        prefs.edit().putString(KEY_SELECTED_MODEL_ID, modelId).apply();
    }

    public String getOutputFormat() {
        return prefs.getString(KEY_OUTPUT_FORMAT, "mp3_44100_128");
    }

    public void setOutputFormat(String format) {
        prefs.edit().putString(KEY_OUTPUT_FORMAT, format).apply();
    }

    public VoiceSettings getVoiceSettings() {
        String json = prefs.getString(KEY_VOICE_SETTINGS, null);
        if (json != null) {
            try {
                return gson.fromJson(json, VoiceSettings.class);
            } catch (Exception ignored) {
            }
        }
        return new VoiceSettings();
    }

    public void setVoiceSettings(VoiceSettings settings) {
        prefs.edit().putString(KEY_VOICE_SETTINGS, gson.toJson(settings)).apply();
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean(KEY_DARK_THEME, true);
    }

    public void setDarkTheme(boolean dark) {
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply();
    }

    public void clearSession() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_AUTH_TOKEN)
                .apply();
    }
}
