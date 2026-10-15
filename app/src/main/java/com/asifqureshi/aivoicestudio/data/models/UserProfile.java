package com.asifqureshi.aivoicestudio.data.models;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String planName;
    private long charactersUsed;
    private long charactersLimit;
    private boolean isEmailVerified;
    private long memberSince;

    public UserProfile() {
        this.userId = "usr_creator_01";
        this.name = "Studio Creator";
        this.email = "creator@aivoicestudio.io";
        this.planName = "Pro Studio Plan";
        this.charactersUsed = 14250;
        this.charactersLimit = 100000;
        this.isEmailVerified = true;
        this.memberSince = System.currentTimeMillis();
    }

    public UserProfile(String userId, String name, String email, String planName, long charactersUsed, long charactersLimit) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.planName = planName;
        this.charactersUsed = charactersUsed;
        this.charactersLimit = charactersLimit;
        this.isEmailVerified = true;
        this.memberSince = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name != null ? name : "Creator";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlanName() {
        return planName != null ? planName : "Pro Creator Plan";
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public long getCharactersUsed() {
        return charactersUsed;
    }

    public void setCharactersUsed(long charactersUsed) {
        this.charactersUsed = charactersUsed;
    }

    public long getCharactersLimit() {
        return charactersLimit;
    }

    public void setCharactersLimit(long charactersLimit) {
        this.charactersLimit = charactersLimit;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public long getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(long memberSince) {
        this.memberSince = memberSince;
    }
}
