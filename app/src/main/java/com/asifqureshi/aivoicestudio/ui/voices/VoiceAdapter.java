package com.asifqureshi.aivoicestudio.ui.voices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.data.models.Voice;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.VoiceViewHolder> {
    public interface OnVoiceSelectedListener {
        void onVoiceSelected(Voice voice);
    }

    private final Context context;
    private final List<Voice> voiceList = new ArrayList<>();
    private final OnVoiceSelectedListener selectListener;
    private final ProjectDatabaseHelper dbHelper;
    private final AudioPlayerManager playerManager;
    private String playingVoiceId = null;

    public VoiceAdapter(Context context, OnVoiceSelectedListener selectListener) {
        this.context = context;
        this.selectListener = selectListener;
        this.dbHelper = new ProjectDatabaseHelper(context);
        this.playerManager = AudioPlayerManager.getInstance(context);
    }

    public void setVoices(List<Voice> voices) {
        this.voiceList.clear();
        if (voices != null) {
            this.voiceList.addAll(voices);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voice, parent, false);
        return new VoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceViewHolder holder, int position) {
        Voice voice = voiceList.get(position);
        holder.tvName.setText(voice.getName());
        holder.tvDetails.setText(String.format("%s • %s • %s", voice.getAccent(), voice.getGender(), voice.getCategory()));
        holder.tvDesc.setText(voice.getDescription());

        boolean isFav = dbHelper.isVoiceFavorite(voice.getVoiceId());
        holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart);

        holder.btnFavorite.setOnClickListener(v -> {
            dbHelper.toggleVoiceFavorite(voice.getVoiceId());
            notifyItemChanged(holder.getAdapterPosition());
        });

        boolean isThisPlaying = voice.getVoiceId().equals(playingVoiceId) && playerManager.isPlaying();
        holder.btnPreview.setImageResource(isThisPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

        holder.btnPreview.setOnClickListener(v -> {
            if (voice.getPreviewUrl() != null && !voice.getPreviewUrl().isEmpty()) {
                if (isThisPlaying) {
                    playerManager.pause();
                    playingVoiceId = null;
                } else {
                    playingVoiceId = voice.getVoiceId();
                    playerManager.play(voice.getPreviewUrl());
                }
                notifyDataSetChanged();
            }
        });

        holder.btnSelectVoice.setOnClickListener(v -> {
            if (selectListener != null) {
                selectListener.onVoiceSelected(voice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voiceList.size();
    }

    static class VoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvDesc;
        ImageButton btnFavorite, btnPreview;
        MaterialButton btnSelectVoice;

        public VoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVoiceName);
            tvDetails = itemView.findViewById(R.id.tvVoiceDetails);
            tvDesc = itemView.findViewById(R.id.tvVoiceDesc);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnPreview = itemView.findViewById(R.id.btnPreview);
            btnSelectVoice = itemView.findViewById(R.id.btnSelectVoice);
        }
    }
}
