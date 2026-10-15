package com.asifqureshi.aivoicestudio.data.models;

import java.io.Serializable;

public class DialogueSpeaker implements Serializable {
    private String id;
    private String name;
    private String voiceId;
    private String voiceName;
    private String colorHex;

    public DialogueSpeaker() {
    }

    public DialogueSpeaker(String id, String name, String voiceId, String voiceName, String colorHex) {
        this.id = id;
        this.name = name;
        this.voiceId = voiceId;
        this.voiceName = voiceName;
        this.colorHex = colorHex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "Speaker";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getVoiceName() {
        return voiceName != null ? voiceName : "Selected Voice";
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getColorHex() {
        return colorHex != null ? colorHex : "#00E5FF";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
