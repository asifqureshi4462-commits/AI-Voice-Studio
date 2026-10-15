package com.asifqureshi.aivoicestudio.data.models;

import java.io.Serializable;

public class ProjectItem implements Serializable {
    private long id;
    private String title;
    private String text;
    private String voiceId;
    private String voiceName;
    private String audioPath;
    private long durationMs;
    private long fileSizeBytes;
    private long createdAt;
    private boolean isFavorite;
    private String projectType; // "tts", "dialogue", "long_script"

    public ProjectItem() {
    }

    public ProjectItem(long id, String title, String text, String voiceId, String voiceName, String audioPath, long durationMs, long fileSizeBytes, long createdAt, boolean isFavorite, String projectType) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.voiceId = voiceId;
        this.voiceName = voiceName;
        this.audioPath = audioPath;
        this.durationMs = durationMs;
        this.fileSizeBytes = fileSizeBytes;
        this.createdAt = createdAt;
        this.isFavorite = isFavorite;
        this.projectType = projectType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "Untitled Recording";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getVoiceName() {
        return voiceName != null ? voiceName : "Default Voice";
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getProjectType() {
        return projectType != null ? projectType : "tts";
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
}
