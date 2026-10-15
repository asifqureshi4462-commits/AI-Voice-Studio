package com.asifqureshi.aivoicestudio;

import android.app.Application;
import android.util.Log;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;

public class AIVoiceStudioApp extends Application {
    private static final String TAG = "AIVoiceStudioApp";
    private static AIVoiceStudioApp instance;
    private PreferencesManager preferencesManager;
    private ProjectDatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferencesManager = new PreferencesManager(this);
        databaseHelper = new ProjectDatabaseHelper(this);
        Log.i(TAG, "AI Voice Studio initialized. Developer: Asif Qureshi");
    }

    public static AIVoiceStudioApp getInstance() {
        return instance;
    }

    public PreferencesManager getPreferencesManager() {
        return preferencesManager;
    }

    public ProjectDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
