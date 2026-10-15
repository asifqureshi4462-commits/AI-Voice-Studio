package com.asifqureshi.aivoicestudio.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.ui.home.HomeFragment;
import com.asifqureshi.aivoicestudio.ui.library.LibraryFragment;
import com.asifqureshi.aivoicestudio.ui.profile.ProfileFragment;
import com.asifqureshi.aivoicestudio.ui.tts.TtsFragment;
import com.asifqureshi.aivoicestudio.ui.voices.VoicesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private MaterialCardView miniPlayerContainer;
    private ImageButton btnMiniPlayPause, btnMiniClose;
    private TextView tvMiniTitle, tvMiniTime;
    private AudioPlayerManager playerManager;

    private final Fragment homeFragment = new HomeFragment();
    private final Fragment ttsFragment = new TtsFragment();
    private final Fragment voicesFragment = new VoicesFragment();
    private final Fragment libraryFragment = new LibraryFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerManager = AudioPlayerManager.getInstance(this);

        bottomNav = findViewById(R.id.bottomNavigation);
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
        btnMiniPlayPause = findViewById(R.id.btnMiniPlayPause);
        btnMiniClose = findViewById(R.id.btnMiniClose);
        tvMiniTitle = findViewById(R.id.tvMiniTitle);
        tvMiniTime = findViewById(R.id.tvMiniTime);

        // Setup fragments
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, profileFragment, "5").hide(profileFragment)
                .add(R.id.fragmentContainer, libraryFragment, "4").hide(libraryFragment)
                .add(R.id.fragmentContainer, voicesFragment, "3").hide(voicesFragment)
                .add(R.id.fragmentContainer, ttsFragment, "2").hide(ttsFragment)
                .add(R.id.fragmentContainer, homeFragment, "1")
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_create) {
                switchFragment(ttsFragment);
                return true;
            } else if (itemId == R.id.nav_voices) {
                switchFragment(voicesFragment);
                return true;
            } else if (itemId == R.id.nav_library) {
                switchFragment(libraryFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });

        setupMiniPlayer();
    }

    public void selectTab(int navItemId) {
        bottomNav.setSelectedItemId(navItemId);
    }

    private void switchFragment(Fragment target) {
        if (activeFragment != target) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
    }

    private void setupMiniPlayer() {
        btnMiniPlayPause.setOnClickListener(v -> {
            if (playerManager.isPlaying()) {
                playerManager.pause();
                btnMiniPlayPause.setImageResource(R.drawable.ic_play_arrow);
            } else {
                playerManager.resume();
                btnMiniPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });

        btnMiniClose.setOnClickListener(v -> {
            playerManager.stop();
            miniPlayerContainer.setVisibility(View.GONE);
        });

        playerManager.setListener(new AudioPlayerManager.PlayerListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                if (playerManager.getCurrentDataSource() != null && !playerManager.getCurrentDataSource().isEmpty()) {
                    miniPlayerContainer.setVisibility(View.VISIBLE);
                }
                btnMiniPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
            }

            @Override
            public void onProgressUpdate(long currentPositionMs, long durationMs) {
                if (miniPlayerContainer.getVisibility() == View.VISIBLE) {
                    tvMiniTime.setText(String.format("%s / %s",
                            AudioProcessingUtils.formatDuration(currentPositionMs),
                            AudioProcessingUtils.formatDuration(durationMs)));
                }
            }

            @Override
            public void onCompleted() {
                btnMiniPlayPause.setImageResource(R.drawable.ic_play_arrow);
            }

            @Override
            public void onError(String error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            playerManager.release();
        }
    }
}
