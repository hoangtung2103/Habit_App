package com.thtung.habit_app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private TextView tvDateToday, tvEmptyNote;
    private ImageView btnBack;
    private FirestoreManager firestoreManager;
    private String userId;
    private String habitId;
    private String habitName;
    private LocalDate selectedDate;
    private DailyNoteAdapter adapter;
    private ArrayList<HabitNote> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_note);

        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        tvDateToday = findViewById(R.id.tvDateToday);
        tvEmptyNote = findViewById(R.id.tvEmptyNote);

        btnBack = findViewById(R.id.btnBack);
        firestoreManager = new FirestoreManager();

        // Lấy dữ liệu từ Intent
        habitId = getIntent().getStringExtra("habitId");
        habitName = getIntent().getStringExtra("habitName");
        selectedDate = (LocalDate) getIntent().getSerializableExtra("selectedDate");
        userId = getIntent().getStringExtra("userId");

        if (habitId == null || userId == null || selectedDate == null) {
            Toast.makeText(this, "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            noteList = new ArrayList<>();
            noteList.add(new HabitNote("", "", "", "Lỗi: Dữ liệu không hợp lệ", null));
            setupRecyclerView();
            return;
        }

        if (habitName == null || habitName.isEmpty()) {
            habitName = "Thói quen không xác định";
        }

        // Cập nhật tvDateToday theo ngày được chọn
        Date selectedJavaDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", new Locale("vi", "VN"));
        tvDateToday.setText(sdf.format(selectedJavaDate));

        setupRecyclerView();

        btnBack.setOnClickListener(v -> finish());

        // Tải danh sách ghi chú cho ngày được chọn
        loadNotesForDate();
    }

    private void setupRecyclerView() {
        noteList = new ArrayList<>();
        // Truyền habitName vào DailyNoteAdapter
        adapter = new DailyNoteAdapter(this, noteList, userId, habitId, habitName, selectedDate, firestoreManager);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotes.setAdapter(adapter);
    }

    private void loadNotesForDate() {
        String dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

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
                    tvEmptyNote.setVisibility(View.VISIBLE);
                    recyclerViewNotes.setVisibility(View.GONE);
                } else {
                    tvEmptyNote.setVisibility(View.GONE);
                    recyclerViewNotes.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(DailyNoteActivity.this, "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesForDate(); // Cập nhật lại danh sách khi quay lại activity
    }
}