package com.asifqureshi.aivoicestudio.ui.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import com.asifqureshi.aivoicestudio.ui.studio.AudioStudioActivity;
import com.google.android.material.tabs.TabLayout;
import java.util.List;

public class LibraryFragment extends Fragment {
    private RecyclerView rvProjects;
    private LinearLayout layoutEmptyLibrary;
    private TabLayout tabLayout;
    private ProjectAdapter adapter;
    private ProjectDatabaseHelper dbHelper;
    private int currentTab = 0; // 0: All, 1: Favorites

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        dbHelper = new ProjectDatabaseHelper(requireContext());

        rvProjects = view.findViewById(R.id.rvProjects);
        layoutEmptyLibrary = view.findViewById(R.id.layoutEmptyLibrary);
        tabLayout = view.findViewById(R.id.tabLayoutLibrary);

        rvProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProjectAdapter(requireContext(), item -> {
            Intent intent = new Intent(requireContext(), AudioStudioActivity.class);
            intent.putExtra("AUDIO_PATH", item.getAudioPath());
            intent.putExtra("AUDIO_TITLE", item.getTitle());
            startActivity(intent);
        });
        rvProjects.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadProjects();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadProjects();

        return view;
    }

    private void loadProjects() {
        List<ProjectItem> list;
        if (currentTab == 1) {
            list = dbHelper.getFavoriteProjects();
        } else {
            list = dbHelper.getAllProjects();
        }

        if (list.isEmpty()) {
            layoutEmptyLibrary.setVisibility(View.VISIBLE);
            rvProjects.setVisibility(View.GONE);
        } else {
            layoutEmptyLibrary.setVisibility(View.GONE);
            rvProjects.setVisibility(View.VISIBLE);
            adapter.setProjects(list);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }
}
