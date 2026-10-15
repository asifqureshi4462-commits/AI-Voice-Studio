package com.asifqureshi.aivoicestudio.ui.library;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.audio.AudioProcessingUtils;
import com.asifqureshi.aivoicestudio.data.models.ProjectItem;
import com.asifqureshi.aivoicestudio.data.repository.ProjectDatabaseHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
    public interface OnProjectClickListener {
        void onProjectClick(ProjectItem item);
    }

    private final Context context;
    private final List<ProjectItem> projectList = new ArrayList<>();
    private final OnProjectClickListener clickListener;
    private final ProjectDatabaseHelper dbHelper;
    private final AudioPlayerManager playerManager;
    private long playingProjectId = -1;

    public ProjectAdapter(Context context, OnProjectClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.dbHelper = new ProjectDatabaseHelper(context);
        this.playerManager = AudioPlayerManager.getInstance(context);
    }

    public void setProjects(List<ProjectItem> projects) {
        this.projectList.clear();
        if (projects != null) {
            this.projectList.addAll(projects);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectItem item = projectList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvSnippet.setText(item.getText());
        holder.tvMeta.setText(String.format("%s • %s • %s",
                item.getVoiceName(),
                AudioProcessingUtils.formatDuration(item.getDurationMs()),
                AudioProcessingUtils.formatFileSize(item.getFileSizeBytes())));

        holder.btnFavorite.setImageResource(item.isFavorite() ? R.drawable.ic_heart_filled : R.drawable.ic_heart);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean newFav = !item.isFavorite();
            item.setFavorite(newFav);
            dbHelper.updateProjectFavorite(item.getId(), newFav);
            notifyItemChanged(holder.getAdapterPosition());
        });

        boolean isPlayingThis = (playingProjectId == item.getId()) && playerManager.isPlaying();
        holder.btnPlay.setImageResource(isPlayingThis ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

        holder.btnPlay.setOnClickListener(v -> {
            if (item.getAudioPath() != null && new File(item.getAudioPath()).exists()) {
                if (isPlayingThis) {
                    playerManager.pause();
                    playingProjectId = -1;
                } else {
                    playingProjectId = item.getId();
                    playerManager.play(item.getAudioPath());
                }
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Audio file not found on device", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            if (item.getAudioPath() != null) {
                File audioFile = new File(item.getAudioPath());
                if (audioFile.exists()) {
                    try {
                        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", audioFile);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("audio/mpeg");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(Intent.createChooser(shareIntent, "Share Audio via"));
                    } catch (Exception e) {
                        Toast.makeText(context, "Error sharing audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            dbHelper.deleteProject(item.getId());
            int pos = holder.getAdapterPosition();
            if (pos >= 0 && pos < projectList.size()) {
                projectList.remove(pos);
                notifyItemRemoved(pos);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProjectClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSnippet, tvMeta;
        ImageButton btnPlay, btnFavorite, btnShare, btnDelete;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvSnippet = itemView.findViewById(R.id.tvProjectSnippet);
            tvMeta = itemView.findViewById(R.id.tvProjectMeta);
            btnPlay = itemView.findViewById(R.id.btnPlayProject);
            btnFavorite = itemView.findViewById(R.id.btnProjectFavorite);
            btnShare = itemView.findViewById(R.id.btnProjectShare);
            btnDelete = itemView.findViewById(R.id.btnProjectDelete);
        }
    }
}
