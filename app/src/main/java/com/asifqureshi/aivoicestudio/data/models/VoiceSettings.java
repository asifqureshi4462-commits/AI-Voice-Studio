package com.asifqureshi.aivoicestudio.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class VoiceSettings implements Serializable {
    @SerializedName("stability")
    private float stability;

    @SerializedName("similarity_boost")
    private float similarityBoost;

    @SerializedName("style")
    private float style;

    @SerializedName("speed")
    private float speed;

    @SerializedName("use_speaker_boost")
    private boolean useSpeakerBoost;

    public VoiceSettings() {
        this.stability = 0.50f;
        this.similarityBoost = 0.75f;
        this.style = 0.0f;
        this.speed = 1.0f;
        this.useSpeakerBoost = true;
    }

    public VoiceSettings(float stability, float similarityBoost, float style, float speed, boolean useSpeakerBoost) {
        this.stability = stability;
        this.similarityBoost = similarityBoost;
        this.style = style;
        this.speed = speed;
        this.useSpeakerBoost = useSpeakerBoost;
    }

    public float getStability() {
        return stability;
    }

    public void setStability(float stability) {
        this.stability = Math.max(0.0f, Math.min(1.0f, stability));
    }

    public float getSimilarityBoost() {
        return similarityBoost;
    }

    public void setSimilarityBoost(float similarityBoost) {
        this.similarityBoost = Math.max(0.0f, Math.min(1.0f, similarityBoost));
    }

    public float getStyle() {
        return style;
    }

    public void setStyle(float style) {
        this.style = Math.max(0.0f, Math.min(1.0f, style));
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = Math.max(0.5f, Math.min(2.0f, speed));
    }

    public boolean isUseSpeakerBoost() {
        return useSpeakerBoost;
    }

    public void setUseSpeakerBoost(boolean useSpeakerBoost) {
        this.useSpeakerBoost = useSpeakerBoost;
    }
}
