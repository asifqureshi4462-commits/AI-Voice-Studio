package com.asifqureshi.aivoicestudio.data.models;

import java.io.Serializable;

public class DialogueLine implements Serializable {
    private String id;
    private String speakerId;
    private String speakerName;
    private String voiceId;
    private String text;
    private String audioPath;
    private boolean isGenerating;
    private boolean isGenerated;

    public DialogueLine() {
    }

    public DialogueLine(String id, String speakerId, String speakerName, String voiceId, String text) {
        this.id = id;
        this.speakerId = speakerId;
        this.speakerName = speakerName;
        this.voiceId = voiceId;
        this.text = text;
        this.isGenerating = false;
        this.isGenerated = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(String speakerId) {
        this.speakerId = speakerId;
    }

    public String getSpeakerName() {
        return speakerName != null ? speakerName : "Speaker";
    }

    public void setSpeakerName(String speakerName) {
        this.speakerName = speakerName;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public boolean isGenerating() {
        return isGenerating;
    }

    public void setGenerating(boolean generating) {
        isGenerating = generating;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }
}
