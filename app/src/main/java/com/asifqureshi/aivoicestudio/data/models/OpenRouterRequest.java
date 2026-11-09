package com.asifqureshi.aivoicestudio.data.models;

import com.google.gson.annotations.SerializedName;

public class OpenRouterRequest {
    @SerializedName("model")
    private String model;

    @SerializedName("input")
    private String input;

    @SerializedName("voice")
    private String voice;

    @SerializedName("response_format")
    private String responseFormat;

    public OpenRouterRequest() {
    }

    public OpenRouterRequest(String model, String input, String voice, String responseFormat) {
        this.model = model != null ? model : "deepgram/flux-tts:free";
        this.input = input;
        this.voice = voice != null ? voice : "aura-asteria-en";
        this.responseFormat = responseFormat != null ? responseFormat : "mp3";
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }
}
