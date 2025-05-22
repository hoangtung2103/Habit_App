package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.HabitNoteAdapter;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.HabitNote;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitNoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitNoteAdapter adapter;
    private List<HabitNote> noteList = new ArrayList<>();
    private FirestoreManager firestoreManager;
    private String userId;
    private String habitId;
    private ImageButton addButton;
    private ImageButton btnBack;
    private Spinner dropdownSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_note);

        recyclerView = findViewById(R.id.recyclerViewNotes);
        addButton = findViewById(R.id.addButton);
        btnBack = findViewById(R.id.btnBack);
        dropdownSpinner = findViewById(R.id.dropdown);

        firestoreManager = new FirestoreManager();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        habitId = getIntent().getStringExtra("habitId");
        String habitName = getIntent().getStringExtra("habitName");

        if (habitId == null || habitId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID thói quen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView noteTitle = findViewById(R.id.note_title);
        noteTitle.setText(habitName);

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this, R.array.note_filter_options, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownSpinner.setAdapter(adapterSpinner);

        dropdownSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadNotesFromFirestore();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        adapter = new HabitNoteAdapter(this, noteList, new HabitNoteAdapter.OnNoteActionListener() {
            @Override
            public void onEdit(HabitNote note) {
                Intent intent = new Intent(HabitNoteActivity.this, FixNoteActivity.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("habitId", habitId);
                intent.putExtra("habitName", habitName);
                startActivity(intent);
            }

            @Override
            public void onDelete(HabitNote note) {
                if (note == null || note.getId() == null || note.getId().isEmpty()) {
                    Toast.makeText(HabitNoteActivity.this, "Lỗi: Ghi chú không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(HabitNoteActivity.this, "Lỗi: Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                    return;
                }

                firestoreManager.deleteHabitNote(userId, note.getId(), new FirestoreManager.NoteDeleteCallback() {
                    @Override
                    public void onNoteDeleted() {
                        Toast.makeText(HabitNoteActivity.this, "Đã xoá ghi chú", Toast.LENGTH_SHORT).show();
                        loadNotesFromFirestore();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(HabitNoteActivity.this, "Lỗi xoá: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotesFromFirestore();

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(HabitNoteActivity.this, AddNoteActivity.class);
            intent.putExtra("habitId", habitId);
            intent.putExtra("habitName", habitName);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(HabitNoteActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadNotesFromFirestore() {
        firestoreManager.getHabitNotesByHabitId(userId, habitId, new FirestoreManager.HabitNoteListCallback() {
            @Override
            public void onHabitNoteListLoaded(ArrayList<HabitNote> notes) {
                //Sort
                notes.sort((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()));
                noteList.clear();

                // Kiểm tra và log từng note
                for (HabitNote note : notes) {
                    if (note.getId() == null || note.getId().isEmpty()) {
                        continue;
                    }
                }

                String selectedFilter = dropdownSpinner.getSelectedItem().toString();

                Calendar currentDate = Calendar.getInstance();
                currentDate.setTime(new Date()); // Ngày hiện tại: 05:27 PM +07, Thursday, May 22, 2025

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

                for (HabitNote note : notes) {
                    if (note.getId() == null || note.getId().isEmpty()) {
                        continue; // Bỏ qua note không hợp lệ
                    }

                    Timestamp createAt = note.getCreateAt();
                    if (createAt == null) {
                        Log.w("HabitNoteActivity", "Note ID " + note.getId() + " has null createAt, skipping.");
                        continue;
                    }

                    Calendar noteDate = Calendar.getInstance();
                    noteDate.setTime(createAt.toDate());

                    long diffDays = daysBetween(noteDate, currentDate);

                    if ("Tất cả".equals(selectedFilter) ||
                            ("7 ngày".equals(selectedFilter) && diffDays <= 7) ||
                            ("15 ngày".equals(selectedFilter) && diffDays <= 15)) {
                        noteList.add(note);
                    }
                }

                adapter.notifyDataSetChanged();
                TextView emptyText = findViewById(R.id.emptyNotesText);
                emptyText.setVisibility(noteList.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(noteList.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HabitNoteActivity.this, "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long daysBetween(Calendar startDate, Calendar endDate) {
        Calendar start = (Calendar) startDate.clone();
        Calendar end = (Calendar) endDate.clone();

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        long diffMs = end.getTimeInMillis() - start.getTimeInMillis();
        long diffDays = diffMs / (1000 * 60 * 60 * 24);
        return Math.abs(diffDays);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromFirestore();
    }
}