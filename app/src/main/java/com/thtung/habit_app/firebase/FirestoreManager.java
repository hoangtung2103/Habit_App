package com.thtung.habit_app.firebase;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thtung.habit_app.activities.AddHabitActivity;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.HabitLog;
import com.thtung.habit_app.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {
    private FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }


    public void getUser(String userId, UserCallback callback) {
        db.collection("user").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onUserLoaded(user);
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void saveHabit(Context context, String habitID, String userID, String habitName, String iconUrl, String lapLai, String mucTieu, String timeNhacNho, String ghiChu, Timestamp ngayBD) {
        Map <String, Object> habitMap = new HashMap<>();
        habitMap.put("userID", userID);
        habitMap.put("habitName", habitName);
        habitMap.put("iconUrl", iconUrl);
        habitMap.put("lapLai", lapLai);
        habitMap.put("mucTieu", mucTieu);
        habitMap.put("timeNhacNho", timeNhacNho);
        habitMap.put("ghiChu", ghiChu);
        habitMap.put("ngayBD", ngayBD);

        db.collection("habit")
                .add(habitMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Thêm thói quen thành công!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Thêm thói quen thất bại!", Toast.LENGTH_SHORT).show();
                });
    }


    public void getHabits(String userId, HabitListCallback callback) {
        db.collection("habit")
                .whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Habit> habitList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Habit habit = doc.toObject(Habit.class);
                        habit.setHabitID(doc.getId());
                        habitList.add(habit);
                    }
                    callback.onHabitListLoaded(habitList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void saveHabitLog(Context context, String habitId, String userId, String date, boolean completed) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("habitId", habitId);
        logMap.put("userId", userId);
        logMap.put("date", date);  // format yyyyMMdd
        logMap.put("completed", completed);

        db.collection("habit_logs")
                .add(logMap)
                .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Cập nhật trạng thái thất bại!", Toast.LENGTH_SHORT).show();
                });
    }

    public void checkHabitLog(String habitId, String userId, String date, CheckHabitLogCallback callback) {
        db.collection("habit_logs")
                .whereEqualTo("habitId", habitId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean completed = false;
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            HabitLog log = doc.toObject(HabitLog.class);
                            if (log.isCompleted()) completed = true;
                        }
                    }
                    callback.onCheckCompleted(completed);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    public interface UserCallback {
        void onUserLoaded(User user);
        void onError(String errorMessage);

    }



    public interface HabitListCallback {
        void onHabitListLoaded(ArrayList<Habit> habitList);
        void onError(String errorMessage);
    }

    public interface CheckHabitLogCallback {
        void onCheckCompleted(boolean completed);
        void onError(String errorMessage);
    }

}
