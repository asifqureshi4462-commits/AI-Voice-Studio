package com.asifqureshi.aivoicestudio.data.models;

import java.io.Serializable;

public class ScriptChunk implements Serializable {
    private int index;
    private String text;
    private int charCount;
    private String status; // "pending", "generating", "completed", "error"
    private String audioPath;
    private String errorMessage;

    public ScriptChunk() {
    }

    public ScriptChunk(int index, String text) {
        this.index = index;
        this.text = text;
        this.charCount = text != null ? text.length() : 0;
        this.status = "pending";
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.charCount = text != null ? text.length() : 0;
    }

    public int getCharCount() {
        return charCount;
    }

    public String getStatus() {
        return status != null ? status : "pending";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
