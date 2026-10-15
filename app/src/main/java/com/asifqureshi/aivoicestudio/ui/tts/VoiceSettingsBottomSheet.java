package com.asifqureshi.aivoicestudio.ui.tts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.VoiceSettings;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class VoiceSettingsBottomSheet extends BottomSheetDialogFragment {
    public interface OnSettingsSavedListener {
        void onSettingsSaved(VoiceSettings settings);
    }

    private OnSettingsSavedListener listener;
    private PreferencesManager prefs;
    private VoiceSettings currentSettings;

    public static VoiceSettingsBottomSheet newInstance() {
        return new VoiceSettingsBottomSheet();
    }

    public void setListener(OnSettingsSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_voice_settings_sheet, container, false);
        prefs = new PreferencesManager(requireContext());
        currentSettings = prefs.getVoiceSettings();

        TextView tvStabilityValue = view.findViewById(R.id.tvStabilityValue);
        SeekBar sbStability = view.findViewById(R.id.sbStability);

        TextView tvSimilarityValue = view.findViewById(R.id.tvSimilarityValue);
        SeekBar sbSimilarity = view.findViewById(R.id.sbSimilarity);

        TextView tvStyleValue = view.findViewById(R.id.tvStyleValue);
        SeekBar sbStyle = view.findViewById(R.id.sbStyle);

        TextView tvSpeedValue = view.findViewById(R.id.tvSpeedValue);
        SeekBar sbSpeed = view.findViewById(R.id.sbSpeed);

        SwitchMaterial switchSpeakerBoost = view.findViewById(R.id.switchSpeakerBoost);
        MaterialButton btnApply = view.findViewById(R.id.btnApplySettings);

        // Initial setup
        int initialStability = Math.round(currentSettings.getStability() * 100);
        sbStability.setProgress(initialStability);
        tvStabilityValue.setText(initialStability + "%");

        int initialSim = Math.round(currentSettings.getSimilarityBoost() * 100);
        sbSimilarity.setProgress(initialSim);
        tvSimilarityValue.setText(initialSim + "%");

        int initialStyle = Math.round(currentSettings.getStyle() * 100);
        sbStyle.setProgress(initialStyle);
        tvStyleValue.setText(initialStyle + "%");

        // Speed: 0.5 to 2.0 (progress 0 to 150 -> 50 + progress / 100)
        int initialSpeedProgress = Math.round((currentSettings.getSpeed() - 0.5f) * 100);
        sbSpeed.setProgress(Math.max(0, Math.min(150, initialSpeedProgress)));
        tvSpeedValue.setText(String.format("%.1fx", currentSettings.getSpeed()));

        switchSpeakerBoost.setChecked(currentSettings.isUseSpeakerBoost());

        // Listeners
        sbStability.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvStabilityValue.setText(progress + "%");
                currentSettings.setStability(progress / 100.0f);
            }
        });

        sbSimilarity.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSimilarityValue.setText(progress + "%");
                currentSettings.setSimilarityBoost(progress / 100.0f);
            }
        });

        sbStyle.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvStyleValue.setText(progress + "%");
                currentSettings.setStyle(progress / 100.0f);
            }
        });

        sbSpeed.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = 0.5f + (progress / 100.0f);
                tvSpeedValue.setText(String.format("%.1fx", speed));
                currentSettings.setSpeed(speed);
            }
        });

        switchSpeakerBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentSettings.setUseSpeakerBoost(isChecked);
        });

        btnApply.setOnClickListener(v -> {
            prefs.setVoiceSettings(currentSettings);
            if (listener != null) {
                listener.onSettingsSaved(currentSettings);
            }
            dismiss();
        });

        return view;
    }

    private abstract static class SimpleSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
