package com.asifqureshi.aivoicestudio.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import java.util.ArrayList;
import java.util.List;

public class ProjectDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ai_voice_studio.db";
    private static final int DATABASE_VERSION = 1;

    // Table Projects
    private static final String TABLE_PROJECTS = "projects";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_TEXT = "text";
    private static final String COL_VOICE_ID = "voice_id";
    private static final String COL_VOICE_NAME = "voice_name";
    private static final String COL_AUDIO_PATH = "audio_path";
    private static final String COL_DURATION_MS = "duration_ms";
    private static final String COL_FILE_SIZE = "file_size";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_IS_FAVORITE = "is_favorite";
    private static final String COL_PROJECT_TYPE = "project_type";

    // Table Favorite Voices
    private static final String TABLE_FAV_VOICES = "favorite_voices";
    private static final String COL_FAV_VOICE_ID = "voice_id";

    public ProjectDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProjectsTable = "CREATE TABLE " + TABLE_PROJECTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT, "
                + COL_TEXT + " TEXT, "
                + COL_VOICE_ID + " TEXT, "
                + COL_VOICE_NAME + " TEXT, "
                + COL_AUDIO_PATH + " TEXT, "
                + COL_DURATION_MS + " INTEGER, "
                + COL_FILE_SIZE + " INTEGER, "
                + COL_CREATED_AT + " INTEGER, "
                + COL_IS_FAVORITE + " INTEGER, "
                + COL_PROJECT_TYPE + " TEXT)";
        db.execSQL(createProjectsTable);

        String createFavVoicesTable = "CREATE TABLE " + TABLE_FAV_VOICES + " ("
                + COL_FAV_VOICE_ID + " TEXT PRIMARY KEY)";
        db.execSQL(createFavVoicesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAV_VOICES);
        onCreate(db);
    }

    public long insertProject(ProjectItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, item.getTitle());
        cv.put(COL_TEXT, item.getText());
        cv.put(COL_VOICE_ID, item.getVoiceId());
        cv.put(COL_VOICE_NAME, item.getVoiceName());
        cv.put(COL_AUDIO_PATH, item.getAudioPath());
        cv.put(COL_DURATION_MS, item.getDurationMs());
        cv.put(COL_FILE_SIZE, item.getFileSizeBytes());
        cv.put(COL_CREATED_AT, item.getCreatedAt() > 0 ? item.getCreatedAt() : System.currentTimeMillis());
        cv.put(COL_IS_FAVORITE, item.isFavorite() ? 1 : 0);
        cv.put(COL_PROJECT_TYPE, item.getProjectType());
        long id = db.insert(TABLE_PROJECTS, null, cv);
        item.setId(id);
        return id;
    }

    public List<ProjectItem> getAllProjects() {
        List<ProjectItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_PROJECTS, null, null, null, null, null, COL_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do {
                list.add(cursorToProject(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public List<ProjectItem> getFavoriteProjects() {
        List<ProjectItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_PROJECTS, null, COL_IS_FAVORITE + " = 1", null, null, null, COL_CREATED_AT + " DESC");
        if (c != null && c.moveToFirst()) {
            do {
                list.add(cursorToProject(c));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public void updateProjectFavorite(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_PROJECTS, cv, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteProject(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROJECTS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public boolean isVoiceFavorite(String voiceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_FAV_VOICES, null, COL_FAV_VOICE_ID + " = ?", new String[]{voiceId}, null, null, null);
        boolean exists = (c != null && c.getCount() > 0);
        if (c != null) c.close();
        return exists;
    }

    public void toggleVoiceFavorite(String voiceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (isVoiceFavorite(voiceId)) {
            db.delete(TABLE_FAV_VOICES, COL_FAV_VOICE_ID + " = ?", new String[]{voiceId});
        } else {
            ContentValues cv = new ContentValues();
            cv.put(COL_FAV_VOICE_ID, voiceId);
            db.insert(TABLE_FAV_VOICES, null, cv);
        }
    }

    private ProjectItem cursorToProject(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
        String title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
        String text = c.getString(c.getColumnIndexOrThrow(COL_TEXT));
        String voiceId = c.getString(c.getColumnIndexOrThrow(COL_VOICE_ID));
        String voiceName = c.getString(c.getColumnIndexOrThrow(COL_VOICE_NAME));
        String audioPath = c.getString(c.getColumnIndexOrThrow(COL_AUDIO_PATH));
        long duration = c.getLong(c.getColumnIndexOrThrow(COL_DURATION_MS));
        long fileSize = c.getLong(c.getColumnIndexOrThrow(COL_FILE_SIZE));
        long createdAt = c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT));
        boolean isFav = c.getInt(c.getColumnIndexOrThrow(COL_IS_FAVORITE)) == 1;
        String type = c.getString(c.getColumnIndexOrThrow(COL_PROJECT_TYPE));
        return new ProjectItem(id, title, text, voiceId, voiceName, audioPath, duration, fileSize, createdAt, isFav, type);
    }
}
