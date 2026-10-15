package com.asifqureshi.aivoicestudio.audio;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class AudioProcessingUtils {
    private static final String TAG = "AudioProcessingUtils";

    public static String formatDuration(long millis) {
        if (millis <= 0) return "00:00";
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static boolean mergeAudioFiles(List<String> inputPaths, String outputPath) {
        if (inputPaths == null || inputPaths.isEmpty() || outputPath == null) {
            return false;
        }

        File outFile = new File(outputPath);
        if (outFile.getParentFile() != null && !outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            for (String inputPath : inputPaths) {
                File inFile = new File(inputPath);
                if (!inFile.exists() || inFile.length() == 0) continue;

                try (FileInputStream fis = new FileInputStream(inFile)) {
                    int read;
                    while ((read = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                }
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error merging audio files", e);
            return false;
        }
    }

    public static Uri exportAudioToDownloads(Context context, File sourceFile, String displayName) {
        if (sourceFile == null || !sourceFile.exists()) return null;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, displayName.endsWith(".mp3") ? displayName : displayName + ".mp3");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AIVoiceStudio");
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }

        if (uri != null) {
            try {
                try (InputStream is = new FileInputStream(sourceFile);
                     OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0);
                    context.getContentResolver().update(uri, values, null, null);
                }
                return uri;
            } catch (Exception e) {
                Log.e(TAG, "Error exporting audio", e);
            }
        }
        return null;
    }
}
