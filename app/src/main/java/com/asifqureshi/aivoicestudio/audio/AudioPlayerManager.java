package com.asifqureshi.aivoicestudio.audio;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import java.io.File;

public class AudioPlayerManager {
    public interface PlayerListener {
        void onPlaybackStateChanged(boolean isPlaying);
        void onProgressUpdate(long currentPositionMs, long durationMs);
        void onCompleted();
        void onError(String error);
    }

    private static AudioPlayerManager instance;
    private final Context context;
    private ExoPlayer exoPlayer;
    private PlayerListener listener;
    private final Handler progressHandler;
    private final Runnable progressRunnable;
    private String currentDataSource;

    public AudioPlayerManager(Context context) {
        this.context = context.getApplicationContext();
        this.progressHandler = new Handler(Looper.getMainLooper());
        this.progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null && exoPlayer.isPlaying()) {
                    long current = exoPlayer.getCurrentPosition();
                    long duration = Math.max(0, exoPlayer.getDuration());
                    if (listener != null) {
                        listener.onProgressUpdate(current, duration);
                    }
                    progressHandler.postDelayed(this, 60);
                }
            }
        };
        initPlayer();
    }

    public static synchronized AudioPlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new AudioPlayerManager(context);
        }
        return instance;
    }

    private void initPlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        exoPlayer = new ExoPlayer.Builder(context).build();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (listener != null) {
                    listener.onPlaybackStateChanged(isPlaying);
                }
                if (isPlaying) {
                    progressHandler.post(progressRunnable);
                } else {
                    progressHandler.removeCallbacks(progressRunnable);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    if (listener != null) {
                        listener.onCompleted();
                    }
                    progressHandler.removeCallbacks(progressRunnable);
                }
            }
        });
    }

    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void play(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            if (listener != null) listener.onError("Audio source is empty");
            return;
        }

        if (exoPlayer == null) {
            initPlayer();
        }

        // If playing the same file and paused, resume
        if (pathOrUrl.equals(currentDataSource) && exoPlayer.getPlaybackState() != Player.STATE_IDLE && exoPlayer.getPlaybackState() != Player.STATE_ENDED) {
            exoPlayer.play();
            return;
        }

        currentDataSource = pathOrUrl;
        Uri uri;
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://") || pathOrUrl.startsWith("content://")) {
            uri = Uri.parse(pathOrUrl);
        } else {
            uri = Uri.fromFile(new File(pathOrUrl));
        }

        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    public void pause() {
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.pause();
        }
    }

    public void resume() {
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    public void togglePlayPause(String pathOrUrl) {
        if (isPlaying() && pathOrUrl.equals(currentDataSource)) {
            pause();
        } else {
            play(pathOrUrl);
        }
    }

    public void seekTo(long positionMs) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(positionMs);
        }
    }

    public void setSpeed(float speed) {
        if (exoPlayer != null) {
            exoPlayer.setPlaybackParameters(new PlaybackParameters(speed));
        }
    }

    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    public long getCurrentPosition() {
        return exoPlayer != null ? exoPlayer.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return exoPlayer != null ? Math.max(0, exoPlayer.getDuration()) : 0;
    }

    public String getCurrentDataSource() {
        return currentDataSource;
    }

    public void stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
        }
        progressHandler.removeCallbacks(progressRunnable);
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        progressHandler.removeCallbacks(progressRunnable);
    }
}
