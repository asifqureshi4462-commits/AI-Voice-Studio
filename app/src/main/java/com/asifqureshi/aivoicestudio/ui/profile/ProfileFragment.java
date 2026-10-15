package com.asifqureshi.aivoicestudio.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.UserProfile;
import com.asifqureshi.aivoicestudio.data.repository.AuthRepository;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.ui.admin.AdminActivity;
import com.asifqureshi.aivoicestudio.ui.auth.LoginActivity;
import com.asifqureshi.aivoicestudio.ui.settings.SettingsActivity;

public class ProfileFragment extends Fragment {
    private TextView tvProfileName, tvProfileEmail, tvProfilePlanBadge, tvProfileQuotaPercent, tvProfileQuotaText;
    private ProgressBar pbProfileQuota;
    private PreferencesManager prefs;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        prefs = new PreferencesManager(requireContext());
        authRepository = new AuthRepository(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfilePlanBadge = view.findViewById(R.id.tvProfilePlanBadge);
        tvProfileQuotaPercent = view.findViewById(R.id.tvProfileQuotaPercent);
        tvProfileQuotaText = view.findViewById(R.id.tvProfileQuotaText);
        pbProfileQuota = view.findViewById(R.id.pbProfileQuota);

        setupProfile();

        view.findViewById(R.id.btnNavSettings).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        view.findViewById(R.id.btnNavAdmin).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AdminActivity.class));
        });

        view.findViewById(R.id.btnSignOut).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), R.style.CustomBottomSheetDialogTheme)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out of AI Voice Studio?")
                    .setPositiveButton("Sign Out", (dialog, which) -> {
                        authRepository.logout();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void setupProfile() {
        UserProfile user = prefs.getUserProfile();
        tvProfileName.setText(user.getName());
        tvProfileEmail.setText(user.getEmail());
        tvProfilePlanBadge.setText(user.getPlanName().toUpperCase());

        long used = user.getCharactersUsed();
        long limit = Math.max(1, user.getCharactersLimit());
        int percent = (int) ((used * 100) / limit);

        pbProfileQuota.setProgress(Math.min(100, percent));
        tvProfileQuotaPercent.setText(percent + "%");
        tvProfileQuotaText.setText(String.format("%,d / %,d characters used this cycle", used, limit));
    }

    @Override
    public void onResume() {
        super.onResume();
        setupProfile();
    }
}
