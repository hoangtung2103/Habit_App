package com.thtung.habit_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.thtung.habit_app.R;
import com.thtung.habit_app.model.HabitNote;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HabitNoteAdapter extends RecyclerView.Adapter<HabitNoteAdapter.NoteViewHolder> {

    private Context context;
    private List<HabitNote> noteList;
    private OnNoteActionListener listener;

    public HabitNoteAdapter(Context context, List<HabitNote> noteList, OnNoteActionListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        HabitNote note = noteList.get(position);

        // Nội dung ghi chú
        holder.noteContent.setText(note.getContent());

        // Thời gian
        Timestamp timestamp = note.getCreateAt();
        if (timestamp != null) {
            String formatted = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
                    .format(timestamp.toDate());
            holder.noteTime.setText(formatted);
        } else {
            holder.noteTime.setText("Không rõ thời gian");
        }

        // Xử lý sự kiện
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(note);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(note);
        });
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteContent, noteTime;
        ImageView editButton, deleteButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteContent = itemView.findViewById(R.id.noteContent);
            noteTime = itemView.findViewById(R.id.noteTime);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // Callback interface
    public interface OnNoteActionListener {
        void onEdit(HabitNote note);
        void onDelete(HabitNote note);
    }
}
