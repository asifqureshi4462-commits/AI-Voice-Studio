package com.asifqureshi.aivoicestudio.ui.dialogue;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.asifqureshi.aivoicestudio.audio.AudioPlayerManager;
import com.asifqureshi.aivoicestudio.data.models.DialogueLine;
import java.util.ArrayList;
import java.util.List;

public class DialogueLineAdapter extends RecyclerView.Adapter<DialogueLineAdapter.LineViewHolder> {
    public interface OnLineActionListener {
        void onPlayLine(DialogueLine line);
        void onDeleteLine(int position);
    }

    private final Context context;
    private final List<DialogueLine> lineList = new ArrayList<>();
    private final OnLineActionListener actionListener;
    private final AudioPlayerManager playerManager;

    public DialogueLineAdapter(Context context, OnLineActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
        this.playerManager = AudioPlayerManager.getInstance(context);
    }

    public void setLines(List<DialogueLine> lines) {
        this.lineList.clear();
        if (lines != null) {
            this.lineList.addAll(lines);
        }
        notifyDataSetChanged();
    }

    public void addLine(DialogueLine line) {
        this.lineList.add(line);
        notifyItemInserted(this.lineList.size() - 1);
    }

    public List<DialogueLine> getLines() {
        return lineList;
    }

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dialogue_line, parent, false);
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        DialogueLine line = lineList.get(position);
        holder.tvSpeakerTag.setText(line.getSpeakerName());

        holder.etLineText.setText(line.getText());
        holder.etLineText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                line.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.btnPlayLine.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onPlayLine(line);
            }
        });

        holder.btnDeleteLine.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos >= 0 && pos < lineList.size()) {
                lineList.remove(pos);
                notifyItemRemoved(pos);
                if (actionListener != null) {
                    actionListener.onDeleteLine(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lineList.size();
    }

    static class LineViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpeakerTag;
        EditText etLineText;
        ImageButton btnPlayLine, btnDeleteLine;

        public LineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpeakerTag = itemView.findViewById(R.id.tvSpeakerTag);
            etLineText = itemView.findViewById(R.id.etLineText);
            btnPlayLine = itemView.findViewById(R.id.btnPlayLine);
            btnDeleteLine = itemView.findViewById(R.id.btnDeleteLine);
        }
    }
}
