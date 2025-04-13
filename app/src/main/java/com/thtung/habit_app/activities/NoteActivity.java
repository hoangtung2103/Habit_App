package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.HabitNoteAdapter;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.HabitNote;

import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitNoteAdapter adapter;
    private List<HabitNote> noteList = new ArrayList<>();
    private FirestoreManager firestoreManager;
    private String userId;
    private String habitId;
    private ImageButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_habit_note); // Đảm bảo layout này chứa recyclerViewNotes, addButton, note_title

        recyclerView = findViewById(R.id.recyclerViewNotes);
        addButton = findViewById(R.id.addButton);

        firestoreManager = new FirestoreManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        habitId = getIntent().getStringExtra("habitId");
        String habitName = getIntent().getStringExtra("habitName");

        TextView noteTitle = findViewById(R.id.note_title);
        noteTitle.setText(habitName);

        adapter = new HabitNoteAdapter(this, noteList, new HabitNoteAdapter.OnNoteActionListener() {
            @Override
            public void onEdit(HabitNote note) {
                Intent intent = new Intent(NoteActivity.this, AddNoteActivity.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("habitId", habitId);
                startActivity(intent);
            }

            @Override
            public void onDelete(HabitNote note) {
                firestoreManager.deleteHabitNote(userId, note.getId(), new FirestoreManager.NoteDeleteCallback() {
                    @Override
                    public void onNoteDeleted() {
                        Toast.makeText(NoteActivity.this, "Đã xoá ghi chú", Toast.LENGTH_SHORT).show();
                        loadNotesFromFirestore();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(NoteActivity.this, "Lỗi xoá: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotesFromFirestore();

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(NoteActivity.this, AddNoteActivity.class);
            intent.putExtra("habitId", habitId);
            startActivity(intent);
        });
    }

    private void loadNotesFromFirestore() {
        firestoreManager.getHabitNotes(userId, new FirestoreManager.HabitNoteListCallback() {
            @Override
            public void onHabitNoteListLoaded(ArrayList<HabitNote> notes) {
                List<HabitNote> filtered = new ArrayList<>();
                for (HabitNote note : notes) {
                    if (note.getHabitId().equals(habitId)) {
                        filtered.add(note);
                    }
                }
                noteList.clear();
                noteList.addAll(filtered);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(NoteActivity.this, "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromFirestore();
    }
}
