package com.thtung.habit_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thtung.habit_app.R;
import com.thtung.habit_app.activities.AddNoteActivity;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.HabitNote;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class DailyNoteAdapter extends RecyclerView.Adapter<DailyNoteAdapter.ViewHolder> {

    private Context context;
    private ArrayList<HabitNote> noteList;
    private String userId;
    private String habitId;
    private LocalDate selectedDate;
    private FirestoreManager firestoreManager;

    public DailyNoteAdapter(Context context, ArrayList<HabitNote> noteList, String userId, String habitId, LocalDate selectedDate, FirestoreManager firestoreManager) {
        this.context = context;
        this.noteList = noteList;
        this.userId = userId;
        this.habitId = habitId;
        this.selectedDate = selectedDate;
        this.firestoreManager = firestoreManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitNote note = noteList.get(position);
        holder.tvContent.setText(note.getContent() != null ? note.getContent() : "Không có nội dung");

        // Định dạng và hiển thị thời gian tạo (nếu có)
        if (note.getCreateAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
            holder.tvTime.setText(sdf.format(note.getCreateAt().toDate()));
        } else {
            holder.tvTime.setText("Chưa có thời gian");
        }

        // Xử lý nút "Sửa"
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddNoteActivity.class);
            intent.putExtra("habitId", habitId);
            intent.putExtra("userId", userId);
            intent.putExtra("noteId", note.getId());
            intent.putExtra("selectedDate", selectedDate);
            context.startActivity(intent);
        });

        // Xử lý nút "Xóa"
        holder.deleteButton.setOnClickListener(v -> {
            if (note.getId() != null && !note.getId().isEmpty()) {
                firestoreManager.deleteHabitNote(userId, note.getId(), new FirestoreManager.NoteDeleteCallback() {
                    @Override
                    public void onNoteDeleted() {
                        noteList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, noteList.size());
                        Toast.makeText(context, "Đã xóa ghi chú", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(context, "Lỗi xóa: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Không thể xóa: ID không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvTime;
        ImageView editButton;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.noteContent);
            tvTime = itemView.findViewById(R.id.noteTime);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}