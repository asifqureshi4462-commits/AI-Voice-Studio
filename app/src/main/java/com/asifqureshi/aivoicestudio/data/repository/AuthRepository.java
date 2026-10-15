package com.asifqureshi.aivoicestudio.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.asifqureshi.aivoicestudio.data.models.UserProfile;

public class AuthRepository {
    private final Context context;
    private final PreferencesManager prefs;
    private final Handler handler;

    public interface AuthCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
    }

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PreferencesManager(context);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public boolean isLoggedIn() {
        return prefs.isLoggedIn();
    }

    public UserProfile getCurrentUser() {
        return prefs.getUserProfile();
    }

    public void login(String email, String password, boolean rememberMe, AuthCallback callback) {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            callback.onError("Please enter a valid email address.");
            return;
        }
        if (password == null || password.length() < 6) {
            callback.onError("Password must be at least 6 characters.");
            return;
        }

        // Simulate network verification or connect to backend
        handler.postDelayed(() -> {
            UserProfile profile = prefs.getUserProfile();
            profile.setEmail(email.trim());
            if (profile.getName() == null || profile.getName().isEmpty() || profile.getName().equals("Studio Creator")) {
                String inferredName = email.split("@")[0];
                inferredName = Character.toUpperCase(inferredName.charAt(0)) + inferredName.substring(1);
                profile.setName(inferredName);
            }
            prefs.setUserProfile(profile);
            prefs.setLoggedIn(true);
            prefs.setAuthToken("token_" + System.currentTimeMillis());
            callback.onSuccess(profile);
        }, 600);
    }

    public void signup(String fullName, String email, String password, boolean termsAccepted, AuthCallback callback) {
        if (fullName == null || fullName.trim().isEmpty()) {
            callback.onError("Please enter your full name.");
            return;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            callback.onError("Please enter a valid email address.");
            return;
        }
        if (password == null || password.length() < 6) {
            callback.onError("Password must be at least 6 characters.");
            return;
        }
        if (!termsAccepted) {
            callback.onError("You must agree to the Terms of Service and Privacy Policy.");
            return;
        }

        handler.postDelayed(() -> {
            UserProfile profile = new UserProfile(
                    "usr_" + System.currentTimeMillis(),
                    fullName.trim(),
                    email.trim(),
                    "Creator Starter Plan",
                    0,
                    25000
            );
            prefs.setUserProfile(profile);
            prefs.setLoggedIn(true);
            prefs.setAuthToken("token_" + System.currentTimeMillis());
            callback.onSuccess(profile);
        }, 700);
    }

    public void loginAsGuest(AuthCallback callback) {
        handler.postDelayed(() -> {
            UserProfile guestProfile = new UserProfile(
                    "usr_guest",
                    "Studio Guest",
                    "guest@aivoicestudio.io",
                    "Pro Creator (Trial)",
                    4200,
                    50000
            );
            prefs.setUserProfile(guestProfile);
            prefs.setLoggedIn(true);
            prefs.setAuthToken("guest_token_" + System.currentTimeMillis());
            callback.onSuccess(guestProfile);
        }, 300);
    }

    public void logout() {
        prefs.clearSession();
    }
}
