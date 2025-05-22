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
import com.thtung.habit_app.databinding.ActivityAddNoteBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String noteId = null;  // nếu null là thêm mới, ngược lại là chỉnh sửa
    private String habitId;  // Thêm biến để lưu habitId
    private String habitName; // Thêm biến để lưu habitName

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
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
            habitName = "Thói quen không xác định"; // Giá trị mặc định nếu không có habitName
            Log.w("AddNoteActivity", "habitName is null or empty, using default value");
        }

        // Hiển thị tên thói quen trong textHabitTitle
        binding.textHabitTitle.setText(habitName);

        // Hiển thị thời gian hiện tại nếu là ghi chú mới
        if (firebaseAuth.getUid() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            binding.textTime.setText(currentTime);
        }

        noteId = getIntent().getStringExtra("noteId");
        if (noteId != null) {
            loadNoteFromFirestore(noteId);
        }

        binding.btnCancel.setOnClickListener(view -> finish());

        binding.btnSave.setOnClickListener(view -> saveNoteToFirestore());
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

        if (noteId != null) {
            Map<String, Object> noteData = new HashMap<>();
            noteData.put("content", noteContent);
            noteData.put("createAt", Timestamp.now());

            firestore.collection("HabitNote")
                    .document(noteId)
                    .update(noteData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã cập nhật ghi chú!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Map<String, Object> noteData = new HashMap<>();
            noteData.put("content", noteContent);
            noteData.put("createAt", Timestamp.now());
            noteData.put("habitId", habitId);
            noteData.put("userId", userId);

            firestore.collection("HabitNote")
                    .add(noteData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AddNoteActivity", "New note added with ID: " + documentReference.getId());
                        Toast.makeText(this, "Đã thêm ghi chú mới!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Thêm ghi chú thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}