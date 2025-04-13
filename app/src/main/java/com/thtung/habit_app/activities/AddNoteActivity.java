package com.thtung.habit_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.databinding.ActivityAddNoteBinding;

import java.util.HashMap;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private String noteId = null;  // nếu null là thêm mới, ngược lại là chỉnh sửa

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        noteId = getIntent().getStringExtra("noteId");
        if (noteId != null) {
            loadNoteFromFirestore(noteId);
        }

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnSave.setOnClickListener(view -> saveNoteToFirestore());
    }

    private void loadNoteFromFirestore(String noteId) {
        String userId = firebaseAuth.getUid();
        if (userId == null) return;

        firestore.collection("HabitNote")
                .document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String content = documentSnapshot.getString("content");
                        binding.editNoteContent.setText(content);
                    } else {
                        Toast.makeText(this, "Không tìm thấy ghi chú.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveNoteToFirestore() {
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

        Map<String, Object> noteData = new HashMap<>();
        noteData.put("user_id", userId);
        noteData.put("content", noteContent);
        noteData.put("create_at", Timestamp.now());

        if (noteId != null) {
            // Cập nhật ghi chú hiện tại
            noteData.put("id", noteId);
            firestore.collection("HabitNote")
                    .document(noteId)
                    .set(noteData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã cập nhật ghi chú!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Thêm mới ghi chú
            String newNoteId = firestore.collection("HabitNote").document().getId();
            noteData.put("id", newNoteId);
            firestore.collection("HabitNote")
                    .document(newNoteId)
                    .set(noteData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Đã lưu ghi chú mới!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}