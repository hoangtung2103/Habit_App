package com.thtung.habit_app.repository;

import android.util.Log;

import androidx.annotation.Nullable;


import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.UserPoint;
import com.thtung.habit_app.utils.DateTimeUtils;

import java.util.HashMap;
import java.util.Map;

public class UserPointRepository {
    private static final int POINTS_PER_LOGIN = 1;
    private static final int POINTS_PER_COMPLETED_HABIT = 5;
    private DateTimeUtils dateTimeUtils;
    private static volatile UserPointRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private PointHistoryRepository pointHistoryRepository;

    public UserPointRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dateTimeUtils = new DateTimeUtils();
        pointHistoryRepository = pointHistoryRepository.getInstance();
    }

    public static UserPointRepository getInstance() {
        if (instance == null) {
            synchronized (UserPointRepository.class) {
                if (instance == null) {
                    instance = new UserPointRepository();
                }
            }
        }
        return instance;
    }

    public void getUserPoint(String userId, FirestoreManager.UserPointCallback callback){
        db.collection("UserPoint").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        UserPoint userPoint = documentSnapshot.toObject(UserPoint.class);
                        callback.onUserPointLoaded(userPoint);
                    } else{
                        callback.onError("UserPoint not found!");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void incrementLoginPoints(String userId, @Nullable UserPointCallback callback){
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("Invalid userId for incrementing points.");
            return;
        }

        DocumentReference userPointDocRef = db.collection("UserPoint").document(userId);
        final Timestamp now = Timestamp.now();
        final Timestamp startOfToday = dateTimeUtils.getStartOfDayTimestamp(now);

        userPointDocRef.update(
                        "total_point", FieldValue.increment(POINTS_PER_LOGIN),
                        "last_update", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreManager", "Incremented login points successfully for userId: " + userId);
                    pointHistoryRepository.createPointHistory(userId, POINTS_PER_LOGIN, "Daily login Bonus", now, callback);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Error incrementing login points for userId: " + userId, e);
                    if (callback != null) callback.onError("Failed to update points: " + e.getMessage());
                });
    }

    public void incrementPointPerCompletedHabitInDay(String userId, String habitId, String habitName, @Nullable UserPointCallback callback){
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("Invalid userId for incrementing points.");
            return;
        }

        DocumentReference userPointDocRef = db.collection("UserPoint").document(userId);
        final Timestamp now = Timestamp.now();
        final Timestamp startOfToday = dateTimeUtils.getStartOfDayTimestamp(now);

        userPointDocRef.update(
                        "total_point", FieldValue.increment(POINTS_PER_COMPLETED_HABIT),
                        "last_update", FieldValue.serverTimestamp()
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreManager", "Incremented points successfully per completed habit for userId: " + userId + "with habitId: " + habitId);
                    pointHistoryRepository.createPointHistory(userId, POINTS_PER_COMPLETED_HABIT, "Hoàn thành thói quen: " + habitName, now, callback);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Error incrementing login points for userId: " + userId, e);
                    if (callback != null) callback.onError("Failed to update points: " + e.getMessage());
                });
    }

    public void checkCreateAndIncreaseUserPoint(String userId, @Nullable UserPointCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onError("Invalid userId provided.");
            return;
        }

        final DocumentReference userPointDocRef = db.collection("UserPoint").document(userId);
        final Timestamp now = Timestamp.now();
        final Timestamp startOfToday = dateTimeUtils.getStartOfDayTimestamp(now);

        if (startOfToday == null) {
            if (callback != null) callback.onError("Could not determine start of today.");
            return;
        }

        userPointDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && !document.exists()) {
                    Log.d("FirestoreManager", "UserPoint not found for " + userId + ". Creating...");
                    Map<String, Object> defaultPointData = new HashMap<>();
                    defaultPointData.put("user_id", userId);
                    defaultPointData.put("total_point", 0);
                    defaultPointData.put("last_update", FieldValue.serverTimestamp()); // Chỉ set khi tạo mới
                    defaultPointData.put("lastLoginPointAwardDate", startOfToday);

                    userPointDocRef.set(defaultPointData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirestoreManager", "UserPoint created successfully.");
                                incrementLoginPoints(userId, callback);
//                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreManager", "Error creating UserPoint.", e);
                                if (callback != null) callback.onError(e.getMessage());
                            });
                } else if (document != null && document.exists()) {
                    Log.d("FirestoreManager", "UserPoint already exists for " + userId);

                    UserPoint userPoint = document.toObject(UserPoint.class);

                    if (userPoint == null) {
                        Log.e("UserPointRepository", "Failed to deserialize existing UserPoint for " + userId);
                        if (callback != null) callback.onError("Error reading user point data.");
                        return;
                    }

                    Timestamp lastAwardTimestamp = userPoint.getLastLoginPointAwardDate();

                    if (lastAwardTimestamp != null && dateTimeUtils.isSameDay(lastAwardTimestamp, startOfToday)) {
                        // Đã cộng điểm hôm nay rồi
                        Log.d("UserPointRepository", "Login points already awarded today for " + userId);
                        if (callback != null) {
                            callback.onAlreadyAwardedToday();
                            callback.onSuccess();
                        }
                    } else {
                        Log.d("UserPointRepository", "Awarding login points for existing user " + userId);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("total_point", FieldValue.increment(POINTS_PER_LOGIN)); // Tăng điểm
                        updates.put("last_update", FieldValue.serverTimestamp());
                        updates.put("lastLoginPointAwardDate", startOfToday);

                        userPointDocRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("UserPointRepository", "Points incremented successfully for " + userId);
                                    pointHistoryRepository.createPointHistory(userId, POINTS_PER_LOGIN, "Daily Login Bonus", now, null);
                                    if (callback != null) {
                                        callback.onPointsAwarded();
                                        callback.onSuccess();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("UserPointRepository", "Error updating points for " + userId, e);
                                    if (callback != null) callback.onError("Failed to update points: " + e.getMessage());
                                });
                    }
                } else {
                    Log.e("FirestoreManager", "Error checking UserPoint existence (snapshot null).");
                    if (callback != null) callback.onError("Error checking UserPoint existence.");
                }
            } else {
                Log.e("FirestoreManager", "Error getting UserPoint document", task.getException());
                if (callback != null) callback.onError(task.getException().getMessage());
            }
        });
    }
    public void createUserPointRecord(String userId,  @Nullable FirestoreManager.FirestoreWriteCallback callback){
        DocumentReference userPointDocRef = db.collection("UserPoint").document(userId);

        Map<String, Object> pointMap = new HashMap<>();
        pointMap.put("user_id", userId);
        pointMap.put("total_point", 0);
        pointMap.put("last_update", FieldValue.serverTimestamp());

        userPointDocRef.set(pointMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignupActivity", "UserPoint record created successfully for userId: " + userId);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("SignupActivity", "Error creating UserPoint record for userId: " + userId, e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }


}
