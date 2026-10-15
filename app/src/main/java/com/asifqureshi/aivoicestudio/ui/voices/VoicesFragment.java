package com.asifqureshi.aivoicestudio.ui.voices;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.repository.PreferencesManager;
import com.asifqureshi.aivoicestudio.data.repository.VoiceRepository;
import com.asifqureshi.aivoicestudio.ui.main.MainActivity;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoicesFragment extends Fragment {
    private RecyclerView rvVoices;
    private VoiceAdapter voiceAdapter;
    private VoiceRepository voiceRepository;
    private PreferencesManager prefs;
    private TextView tvVoiceCount;
    private EditText etSearchVoice;
    private ChipGroup chipGroupCategory;

    private List<Voice> allVoices = new ArrayList<>();
    private String currentCategory = "All";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voices, container, false);

        voiceRepository = new VoiceRepository(requireContext());
        prefs = new PreferencesManager(requireContext());

        rvVoices = view.findViewById(R.id.rvVoices);
        tvVoiceCount = view.findViewById(R.id.tvVoiceCount);
        etSearchVoice = view.findViewById(R.id.etSearchVoice);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);

        rvVoices.setLayoutManager(new LinearLayoutManager(requireContext()));
        voiceAdapter = new VoiceAdapter(requireContext(), voice -> {
            prefs.setSelectedVoiceId(voice.getVoiceId());
            prefs.setSelectedVoiceName(voice.getName());
            Toast.makeText(requireContext(), "Selected voice: " + voice.getName(), Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_create);
            }
        });
        rvVoices.setAdapter(voiceAdapter);

        setupFilters();
        loadVoices();

        return view;
    }

    private void setupFilters() {
        etSearchVoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase(Locale.getDefault()).trim();
                filterVoices();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) currentCategory = "All";
                else if (id == R.id.chipNarration) currentCategory = "Narration";
                else if (id == R.id.chipConversational) currentCategory = "Conversational";
                else if (id == R.id.chipNews) currentCategory = "News";
                else if (id == R.id.chipCharacters) currentCategory = "Characters";
            }
            filterVoices();
        });
    }

    private void loadVoices() {
        voiceRepository.fetchVoices(new VoiceRepository.VoiceCallback() {
            @Override
            public void onSuccess(List<Voice> voices) {
                if (!isAdded()) return;
                allVoices = voices;
                tvVoiceCount.setText(voices.size() + " Voices");
                filterVoices();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                allVoices = voiceRepository.getCachedVoices();
                filterVoices();
            }
        });
    }

    private void filterVoices() {
        List<Voice> filtered = new ArrayList<>();
        for (Voice v : allVoices) {
            boolean matchesCat = "All".equalsIgnoreCase(currentCategory) || v.getCategory().equalsIgnoreCase(currentCategory);
            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || v.getName().toLowerCase(Locale.getDefault()).contains(currentSearchQuery)
                    || v.getAccent().toLowerCase(Locale.getDefault()).contains(currentSearchQuery)
                    || v.getDescription().toLowerCase(Locale.getDefault()).contains(currentSearchQuery);

            if (matchesCat && matchesSearch) {
                filtered.add(v);
            }
        }
        voiceAdapter.setVoices(filtered);
    }
}
