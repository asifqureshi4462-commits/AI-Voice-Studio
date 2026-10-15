package com.asifqureshi.aivoicestudio.ui.longscript;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.data.models.ScriptChunk;
import java.util.ArrayList;
import java.util.List;

public class ScriptChunkAdapter extends RecyclerView.Adapter<ScriptChunkAdapter.ChunkViewHolder> {
    public interface OnChunkPlayListener {
        void onPlayChunk(ScriptChunk chunk);
    }

    private final Context context;
    private final List<ScriptChunk> chunkList = new ArrayList<>();
    private final OnChunkPlayListener playListener;
    private final AudioPlayerManager playerManager;

    public ScriptChunkAdapter(Context context, OnChunkPlayListener playListener) {
        this.context = context;
        this.playListener = playListener;
        this.playerManager = AudioPlayerManager.getInstance(context);
    }

    public void setChunks(List<ScriptChunk> chunks) {
        this.chunkList.clear();
        if (chunks != null) {
            this.chunkList.addAll(chunks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChunkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_script_chunk, parent, false);
        return new ChunkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChunkViewHolder holder, int position) {
        ScriptChunk chunk = chunkList.get(position);
        holder.tvIndex.setText(String.valueOf(chunk.getIndex() + 1));
        holder.tvStats.setText(String.format("%d characters", chunk.getCharCount()));
        holder.tvText.setText(chunk.getText());

        String status = chunk.getStatus();
        holder.tvStatus.setText(status.toUpperCase());
        if ("completed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            holder.btnPlay.setVisibility(View.VISIBLE);
        } else if ("generating".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#00E5FF"));
            holder.btnPlay.setVisibility(View.GONE);
        } else if ("error".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
            holder.btnPlay.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#94A3B8"));
            holder.btnPlay.setVisibility(View.GONE);
        }

        holder.btnPlay.setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onPlayChunk(chunk);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chunkList.size();
    }

    static class ChunkViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvStats, tvStatus, tvText;
        ImageButton btnPlay;

        public ChunkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvChunkIndex);
            tvStats = itemView.findViewById(R.id.tvChunkStats);
            tvStatus = itemView.findViewById(R.id.tvChunkStatus);
            tvText = itemView.findViewById(R.id.tvChunkText);
            btnPlay = itemView.findViewById(R.id.btnPlayChunk);
        }
    }
}
