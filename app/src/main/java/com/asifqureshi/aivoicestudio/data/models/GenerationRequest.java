package com.asifqureshi.aivoicestudio.data.models;

import com.google.gson.annotations.SerializedName;

public class GenerationRequest {
    @SerializedName("text")
    private String text;

    @SerializedName("model_id")
    private String modelId;

    @SerializedName("voice_settings")
    private VoiceSettings voiceSettings;

    public GenerationRequest() {
    }

    public GenerationRequest(String text, String modelId, VoiceSettings voiceSettings) {
        this.text = text;
        this.modelId = modelId;
        this.voiceSettings = voiceSettings;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public VoiceSettings getVoiceSettings() {
        return voiceSettings;
    }

    public void setVoiceSettings(VoiceSettings voiceSettings) {
        this.voiceSettings = voiceSettings;
    }
}
