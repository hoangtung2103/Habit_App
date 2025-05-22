package com.thtung.habit_app.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.databinding.ActivityFixNoteBinding;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FixNoteActivity extends AppCompatActivity {

    private ActivityFixNoteBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String noteId;
    private String habitId;
    private String habitName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFixNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Lấy habitId và habitName từ Intent
        habitId = getIntent().getStringExtra("habitId");
        habitName = getIntent().getStringExtra("habitName");
        if (habitId == null || habitId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID thói quen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (habitName == null || habitName.isEmpty()) {
            habitName = "Thói quen không xác định";
        }

        // Hiển thị tên thói quen
        binding.textHabitTitle.setText(habitName);

        // Lấy noteId từ Intent
        noteId = getIntent().getStringExtra("noteId");
        if (noteId == null || noteId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID ghi chú", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải dữ liệu ghi chú hiện có
        loadNoteFromFirestore(noteId);

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnSave.setOnClickListener(view -> updateNoteInFirestore());
    }

    private void loadNoteFromFirestore(String noteId) {
        String userId = firebaseAuth.getUid();
        if (userId == null) {
            Toast.makeText(this, "Không thể lấy UID người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("HabitNote")
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String content = documentSnapshot.getString("content");
                        binding.editNoteContent.setText(content);

                        String noteUserId = documentSnapshot.getString("userId");
                        if (!userId.equals(noteUserId)) {
                            Toast.makeText(this, "Bạn không có quyền chỉnh sửa ghi chú này", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        Timestamp createAt = documentSnapshot.getTimestamp("createAt");
                        if (createAt != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                            String createTime = sdf.format(createAt.toDate());
                            binding.textTime.setText(createTime);
                        } else {
                            binding.textTime.setText("Không có thời gian tạo");
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy ghi chú.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateNoteInFirestore() {
        String noteContent = binding.editNoteContent.getText().toString().trim();
        if (noteContent.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung ghi chú!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getUid();
        if (userId == null) {
            Toast.makeText(this, "Không thể lấy UID người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chỉ cập nhật content, không cập nhật createAt
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("content", noteContent);

        firestore.collection("HabitNote")
                .document(noteId)
                .update(noteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật nội dung ghi chú!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}