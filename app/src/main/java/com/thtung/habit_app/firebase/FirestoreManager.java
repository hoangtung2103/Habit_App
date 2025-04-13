package com.thtung.habit_app.firebase;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.thtung.habit_app.activities.AddHabitActivity;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.HabitLog;
import com.thtung.habit_app.model.Statistic;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.utils.CalculateLastCompleted;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirestoreManager {
    private FirebaseFirestore db;

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }


    public void getUser(String userId, UserCallback callback) {
        db.collection("User").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        user.setId(documentSnapshot.getId());
                        callback.onUserLoaded(user);
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void saveHabit(Context context, String habitID, String userID, String habitName, String iconUrl, String lapLai, String mucTieu, String timeNhacNho, String ghiChu, Timestamp ngayBD) {
        Map <String, Object> habitMap = new HashMap<>();
        habitMap.put("user_id", userID);
        habitMap.put("name", habitName);
        habitMap.put("url_icon", iconUrl);
        habitMap.put("repeat", lapLai);
        habitMap.put("target", mucTieu);
        habitMap.put("remind_time", timeNhacNho);
        habitMap.put("description", ghiChu);
        habitMap.put("start_at", ngayBD);

        db.collection("Habit")
                .add(habitMap)
                .addOnSuccessListener(aVoid -> {
                    // Tạo bảng thống kê tương ứng với Habit vừa tạo
                    Map<String, Object> statisticMap = new HashMap<>();
                    statisticMap.put("habit_id", aVoid.getId());
                    statisticMap.put("user_id", userID);
                    statisticMap.put("total_completed", 0);
                    statisticMap.put("last_completed", 0);
                    statisticMap.put("streak", 0);
                    statisticMap.put("max_streak", 0);
                    db.collection("Statistic").add(statisticMap);

                    Toast.makeText(context, "Thêm thói quen thành công!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Thêm thói quen thất bại!", Toast.LENGTH_SHORT).show();
                });
    }

    public void updateHabit(Context context, String habitID, String habitName, String iconUrl, String lapLai, String mucTieu, String timeNhacNho, String ghiChu) {
        Map <String, Object> habitMap = new HashMap<>();
        habitMap.put("name", habitName);
        habitMap.put("url_icon", iconUrl);
        habitMap.put("repeat", lapLai);
        habitMap.put("target", mucTieu);
        habitMap.put("remind_time", timeNhacNho);
        habitMap.put("description", ghiChu);

        db.collection("Habit")
                .document(habitID)
                .update(habitMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cập nhật thói quen thành công!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Cập nhật thói quen thất bại!", Toast.LENGTH_SHORT).show();
                });
    }
    public void getHabits(String userId, HabitListCallback callback) {
        db.collection("Habit")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Habit> habitList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Habit habit = doc.toObject(Habit.class);
                        habit.setId(doc.getId());
                        habitList.add(habit);
                    }
                    callback.onHabitListLoaded(habitList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getOneHabit(String habitId, HabitCallback callback) {
        db.collection("Habit")
                .document(habitId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Habit habit = documentSnapshot.toObject(Habit.class);
                    habit.setId(documentSnapshot.getId());
                    callback.onHabitLoaded(habit);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteHabit(String habitId, DeleteHabitCallback callback) {
        WriteBatch batch = db.batch();
        db.collection("Habit")
                .document(habitId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("HabitLog")
                            .whereEqualTo("habit_id", habitId)
                            .get()
                            .addOnSuccessListener(logSnapshot -> {
                                for (DocumentSnapshot logDoc : logSnapshot.getDocuments()) {
                                    batch.delete(logDoc.getReference());
                                }

                                db.collection("Statistic")
                                        .whereEqualTo("habit_id", habitId)
                                        .get()
                                        .addOnSuccessListener(statSnapshot -> {
                                            for (DocumentSnapshot statDoc : statSnapshot.getDocuments()) {
                                                batch.delete(statDoc.getReference());
                                            }
                                            batch.commit().addOnSuccessListener(aVoid1 -> {
                                                callback.onDeleteCompleted();
                                            }).addOnFailureListener(e -> {
                                                callback.onError(e.getMessage());
                                            });
                                        }).addOnFailureListener(e -> {
                                            callback.onError(e.getMessage());
                                        });
                            }).addOnFailureListener(e -> {
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void saveHabitLog(Context context, String habitId, String userId, String date, boolean completed) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("habit_id", habitId);
        logMap.put("user_id", userId);
        logMap.put("date", date);  // format yyyyMMdd
        logMap.put("completed", completed);

        db.collection("HabitLog")
                .add(logMap)
                .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Cập nhật trạng thái thất bại!", Toast.LENGTH_SHORT).show();
                });
    }

    public void getDSHabitLog(String habitId, GetHabitLogCallback callback) {
        db.collection("HabitLog")
                .whereEqualTo("habit_id", habitId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshot -> {
                    ArrayList<HabitLog> habitLogList = new ArrayList<>();
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshot) {
                        HabitLog log = doc.toObject(HabitLog.class);
                        log.setId(doc.getId());
                        habitLogList.add(log);
                    }
                    callback.onHabitLogLoaded(habitLogList);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void checkHabitLog(String habitId, String userId, String date, CheckHabitLogCallback callback) {
        db.collection("HabitLog")
                .whereEqualTo("habit_id", habitId)
                .whereEqualTo("user_id", userId)
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

    //Cập nhật bảng Statistic khi người dùng check habit log
    public void updateStatisticAfterLog(Context context, String habitId, String userId, String todayStr, String repeatType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        // Tính ngày "trước đó" theo kiểu lặp lại
        Calendar cal = Calendar.getInstance();
        try {
            Date today = sdf.parse(todayStr);
            cal.setTime(today);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Xác định ngày cần kiểm tra dựa vào repeatType
        switch (repeatType) {
            case "Hàng tuần":
                cal.add(Calendar.WEEK_OF_YEAR, -1); // Trừ 1 tuần
                break;
            case "Hàng tháng":
                cal.add(Calendar.MONTH, -1); // Trừ 1 tháng
                break;
            case "Hàng ngày":
            default:
                cal.add(Calendar.DAY_OF_YEAR, -1); // Trừ 1 ngày
                break;
        }

        String prevDateStr = sdf.format(cal.getTime());

        // Kiểm tra log của ngày trước đó
        db.collection("HabitLog")
                .whereEqualTo("habit_id", habitId)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("date", prevDateStr)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hadLogBefore = !querySnapshot.isEmpty();

                    // Lấy document thống kê
                    db.collection("Statistic")
                            .whereEqualTo("habit_id", habitId)
                            .whereEqualTo("user_id", userId)
                            .get()
                            .addOnSuccessListener(statSnap -> {
                                if (!statSnap.isEmpty()) {
                                    DocumentSnapshot doc = statSnap.getDocuments().get(0);
                                    DocumentReference statRef = doc.getReference();

                                    long total = doc.getLong("total_completed") != null ? doc.getLong("total_completed") : 0;
                                    long streak = doc.getLong("streak") != null ? doc.getLong("streak") : 0;
                                    long maxStreak = doc.getLong("max_streak") != null ? doc.getLong("max_streak") : 0;
                                    long lastCompleted = doc.getLong("last_completed") != null ? doc.getLong("last_completed") : 0;
                                    long newStreak = hadLogBefore ? streak + 1 : 1;
                                    long newMaxStreak = Math.max(maxStreak, newStreak);
                                    if(lastCompleted == 0) {
                                        statRef.update("last_completed", 1);
                                    }
                                    statRef.update(
                                            "total_completed", total + 1,
                                            "streak", newStreak,
                                            "max_streak", newMaxStreak
                                    );
                                }
                            });
                });
    }

    //Cập nhật last_completed và streak hàng ngày
    public void updateLastCompletedForAllHabits(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference habitsRef = db.collection("Habit");
        CollectionReference logsRef = db.collection("HabitLog");
        CollectionReference statsRef = db.collection("Statistic");

        Calendar todayCal = Calendar.getInstance();
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(yesterdayCal.getTime());

        habitsRef.whereEqualTo("user_id", userId).get()
                .addOnSuccessListener(habitsSnapshot -> {
                    for (QueryDocumentSnapshot habitDoc : habitsSnapshot) {
                        String habitId = habitDoc.getId();
                        String repeat = habitDoc.getString("repeat");
                        Timestamp startAtTs = habitDoc.getTimestamp("start_at");

                        if (repeat == null || startAtTs == null) continue;

                        Calendar startCal = Calendar.getInstance();
                        startCal.setTime(startAtTs.toDate());

                        long lastCompleted = CalculateLastCompleted.calculateLastCompleted(startCal, todayCal, repeat);

                        statsRef.whereEqualTo("user_id", userId).whereEqualTo("habit_id", habitId).get()
                                .addOnSuccessListener(statSnapshot -> {
                                    if (statSnapshot.isEmpty()) return;

                                    DocumentSnapshot statDoc = statSnapshot.getDocuments().get(0);
                                    DocumentReference statRef = statDoc.getReference();

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("last_completed", lastCompleted);

                                    // Check log hôm qua
                                    logsRef.whereEqualTo("user_id", userId)
                                            .whereEqualTo("habit_id", habitId)
                                            .whereEqualTo("date", yesterdayStr)
                                            .get()
                                            .addOnSuccessListener(logSnapshot -> {
                                                if (logSnapshot.isEmpty()) {
                                                    updates.put("streak", 0); // reset streak nếu hôm qua không hoàn thành
                                                }
                                                statRef.update(updates);
                                            });
                                });
                    }
                });
    }



    public void getStatistic(String habitId, GetStatisticCallback callback) {
        db.collection("Statistic")
                .whereEqualTo("habit_id", habitId)
                .get()
                .addOnSuccessListener(doc -> {
                    Statistic statistic = doc.getDocuments().get(0).toObject(Statistic.class);
                    statistic.setId(doc.getDocuments().get(0).getId());
                    callback.onStatisticLoaded(statistic);
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

    public interface HabitCallback {
        void onHabitLoaded(Habit habit);
        void onError(String errorMessage);
    }

    public interface GetHabitLogCallback {
        void onHabitLogLoaded(ArrayList<HabitLog> habitLog);
        void onError(String errorMessage);
    }

    public interface DeleteHabitCallback {
        void onDeleteCompleted();
        void onError(String errorMessage);
    }

    public interface CheckHabitLogCallback {
        void onCheckCompleted(boolean completed);
        void onError(String errorMessage);
    }

    public interface GetStatisticCallback {
        void onStatisticLoaded(Statistic statistic);
        void onError(String errorMessage);
    }

}
