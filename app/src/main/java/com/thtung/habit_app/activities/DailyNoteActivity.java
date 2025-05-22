package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.DailyNoteAdapter;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.HabitNote;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DailyNoteActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotes;
    private TextView tvDateToday;
    private ImageButton addButton;
    private ImageView btnBack;
    private FirestoreManager firestoreManager;
    private String userId;
    private String habitId;
    private String habitName; // Thêm habitName
    private LocalDate selectedDate;
    private DailyNoteAdapter adapter;
    private ArrayList<HabitNote> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_note);

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        tvDateToday = findViewById(R.id.tvDateToday);
        addButton = findViewById(R.id.addButton);
        btnBack = findViewById(R.id.btnBack);
        firestoreManager = new FirestoreManager();

        // Lấy dữ liệu từ Intent
        habitId = getIntent().getStringExtra("habitId");
        habitName = getIntent().getStringExtra("habitName"); // Lấy habitName
        selectedDate = (LocalDate) getIntent().getSerializableExtra("selectedDate");
        userId = getIntent().getStringExtra("userId");

        Log.d("DailyNoteActivity", "Received data - habitId: " + habitId + ", habitName: " + habitName + ", userId: " + userId + ", selectedDate: " + selectedDate);

        if (habitId == null || userId == null || selectedDate == null) {
            Log.e("DailyNoteActivity", "Invalid parameters - habitId: " + habitId + ", userId: " + userId + ", selectedDate: " + selectedDate);
            Toast.makeText(this, "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_LONG).show();
            noteList = new ArrayList<>();
            noteList.add(new HabitNote("", userId != null ? userId : "", habitId != null ? habitId : "", "Lỗi: Dữ liệu không hợp lệ", null));
            setupRecyclerView();
            return;
        }

        Date selectedJavaDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", new Locale("vi", "VN"));
        tvDateToday.setText(sdf.format(selectedJavaDate));

        setupRecyclerView();

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(DailyNoteActivity.this, AddNoteActivity.class);
            intent.putExtra("habitId", habitId);
            intent.putExtra("habitName", habitName); // Truyền habitName
            intent.putExtra("userId", userId);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());

        loadNotesForDate();
    }

    private void setupRecyclerView() {
        noteList = new ArrayList<>();
        adapter = new DailyNoteAdapter(this, noteList, userId, habitId, selectedDate, firestoreManager);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotes.setAdapter(adapter);
    }

    private void loadNotesForDate() {
        String dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Log.d("DailyNoteActivity", "Loading notes for date: " + dateStr + ", habitId: " + habitId + ", userId: " + userId);

        firestoreManager.getHabitNotesByHabitId(userId, habitId, new FirestoreManager.HabitNoteListCallback() {
            @Override
            public void onHabitNoteListLoaded(ArrayList<HabitNote> notes) {
                noteList.clear();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                Date selectedJavaDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                for (HabitNote note : notes) {
                    if (note.getCreateAt() != null) {
                        Date noteDate = note.getCreateAt().toDate();
                        if (sdf.format(noteDate).equals(dateStr)) {
                            noteList.add(note);
                        }
                    }
                }

                if (noteList.isEmpty()) {
                    noteList.add(new HabitNote("", userId, habitId, "Không có ghi chú nào trong ngày này.", null));
                }

                adapter.notifyDataSetChanged();
                Log.d("DailyNoteActivity", "Total notes loaded for date: " + noteList.size());
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DailyNoteActivity", "Error loading notes: " + errorMessage);
                Toast.makeText(DailyNoteActivity.this, "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesForDate();
    }
}