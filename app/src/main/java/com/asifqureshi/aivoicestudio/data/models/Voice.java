package com.asifqureshi.aivoicestudio.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Voice implements Serializable {
    @SerializedName("voice_id")
    private String voiceId;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("accent")
    private String accent;

    @SerializedName("gender")
    private String gender;

    @SerializedName("description")
    private String description;

    @SerializedName("preview_url")
    private String previewUrl;

    private String language;
    private boolean isFavorite;
    private boolean isCloned;

    public Voice() {
    }

    public Voice(String voiceId, String name, String category, String accent, String gender, String description, String previewUrl, String language) {
        this.voiceId = voiceId;
        this.name = name;
        this.category = category;
        this.accent = accent;
        this.gender = gender;
        this.description = description;
        this.previewUrl = previewUrl;
        this.language = language;
        this.isFavorite = false;
        this.isCloned = false;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccent() {
        return accent != null ? accent : "American";
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getGender() {
        return gender != null ? gender : "Neutral";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getLanguage() {
        return language != null ? language : "English (US)";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isCloned() {
        return isCloned;
    }

    public void setCloned(boolean cloned) {
        isCloned = cloned;
    }
}
