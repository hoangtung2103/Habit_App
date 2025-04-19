package com.thtung.habit_app.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.utils.DateTimeUtils;

import java.util.HashMap;
import java.util.Map;

public class PointHistoryRepository {
    private DateTimeUtils dateTimeUtils;
    private static volatile PointHistoryRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    public PointHistoryRepository(){
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dateTimeUtils = new DateTimeUtils();
    }

    public static PointHistoryRepository getInstance(){
        if (instance == null) {
            synchronized (PointHistoryRepository.class) {
                if (instance == null) {
                    instance = new PointHistoryRepository();
                }
            }
        }
        return instance;
    }

    public void createPointHistory(String userId, int point, String reason, Timestamp create_at, @Nullable UserPointCallback callback){
        if (userId == null || userId.isEmpty()) {
            Log.e("FirestoreManager", "User ID is required to create point history.");
            if (callback != null) callback.onError("User ID is required.");
            return;
        }

        CollectionReference pointHistoryCollection = db.collection("PointHistory");
        DocumentReference newHistoryRef = pointHistoryCollection.document();

        String newId = newHistoryRef.getId();

        Map<String, Object> historyData = new HashMap<>();
        historyData.put("id", newId);
        historyData.put("user_id", userId);
        historyData.put("point", point);
        historyData.put("reason", reason);
        historyData.put("create_at", FieldValue.serverTimestamp());

        newHistoryRef
                .set(historyData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreManager", "PointHistory created successfully with ID: " + newHistoryRef.getId() + " for user: " + userId);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Error creating PointHistory for user: " + userId, e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }
}
