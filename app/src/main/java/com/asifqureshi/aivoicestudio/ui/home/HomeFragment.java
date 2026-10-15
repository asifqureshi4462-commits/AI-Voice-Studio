package com.asifqureshi.aivoicestudio.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.models.UserProfile;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import com.asifqureshi.aivoicestudio.ui.cloning.VoiceCloningActivity;
import com.asifqureshi.aivoicestudio.ui.dialogue.DialogueActivity;
import com.asifqureshi.aivoicestudio.ui.library.ProjectAdapter;
import com.asifqureshi.aivoicestudio.ui.longscript.LongScriptActivity;
import com.asifqureshi.aivoicestudio.ui.main.MainActivity;
import com.asifqureshi.aivoicestudio.ui.studio.AudioStudioActivity;
import java.util.List;

public class HomeFragment extends Fragment {
    private TextView tvGreeting, tvQuotaStat, tvEmptyRecent;
    private ProgressBar pbQuota;
    private RecyclerView rvRecentProjects;
    private ProjectAdapter projectAdapter;
    private PreferencesManager prefs;
    private ProjectDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = new PreferencesManager(requireContext());
        dbHelper = new ProjectDatabaseHelper(requireContext());

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvQuotaStat = view.findViewById(R.id.tvQuotaStat);
        pbQuota = view.findViewById(R.id.pbQuota);
        tvEmptyRecent = view.findViewById(R.id.tvEmptyRecent);
        rvRecentProjects = view.findViewById(R.id.rvRecentProjects);

        setupUserProfile();
        setupQuickActions(view);
        setupRecentProjects();

        return view;
    }

    private void setupUserProfile() {
        UserProfile user = prefs.getUserProfile();
        tvGreeting.setText(getString(R.string.home_greeting, user.getName()));

        long used = user.getCharactersUsed();
        long limit = Math.max(1, user.getCharactersLimit());
        int percent = (int) ((used * 100) / limit);

        pbQuota.setProgress(Math.min(100, percent));
        tvQuotaStat.setText(getString(R.string.home_quota_stat, String.format("%,d", used), String.format("%,d", limit)));
    }

    private void setupQuickActions(View view) {
        view.findViewById(R.id.btnHeaderProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_profile);
            }
        });

        view.findViewById(R.id.cardQuickTts).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_create);
            }
        });

        view.findViewById(R.id.cardQuickDialogue).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), DialogueActivity.class));
        });

        view.findViewById(R.id.cardQuickLongScript).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LongScriptActivity.class));
        });

        view.findViewById(R.id.cardQuickAudioEditor).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AudioStudioActivity.class));
        });

        view.findViewById(R.id.cardQuickCloning).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), VoiceCloningActivity.class));
        });

        view.findViewById(R.id.btnViewAllRecent).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_library);
            }
        });
    }

    private void setupRecentProjects() {
        rvRecentProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        projectAdapter = new ProjectAdapter(requireContext(), item -> {
            // Open in audio studio
            Intent intent = new Intent(requireContext(), AudioStudioActivity.class);
            intent.putExtra("AUDIO_PATH", item.getAudioPath());
            intent.putExtra("AUDIO_TITLE", item.getTitle());
            startActivity(intent);
        });
        rvRecentProjects.setAdapter(projectAdapter);

        loadRecentProjects();
    }

    private void loadRecentProjects() {
        List<ProjectItem> all = dbHelper.getAllProjects();
        if (all.isEmpty()) {
            tvEmptyRecent.setVisibility(View.VISIBLE);
            rvRecentProjects.setVisibility(View.GONE);
        } else {
            tvEmptyRecent.setVisibility(View.GONE);
            rvRecentProjects.setVisibility(View.VISIBLE);
            // Show at most 4 recent items on dashboard
            List<ProjectItem> subList = all.subList(0, Math.min(4, all.size()));
            projectAdapter.setProjects(subList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUserProfile();
        loadRecentProjects();
    }
}
