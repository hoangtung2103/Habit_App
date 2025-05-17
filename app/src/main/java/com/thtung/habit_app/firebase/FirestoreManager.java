package com.thtung.habit_app.firebase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.thtung.habit_app.activities.AddHabitActivity;
import com.thtung.habit_app.model.Feedback;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.HabitLog;
import com.thtung.habit_app.model.HabitNote;
import com.thtung.habit_app.model.RewardMilestone;
import com.thtung.habit_app.model.Statistic;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.model.UserMilestone;
import com.thtung.habit_app.model.UserPoint;
import com.thtung.habit_app.model.UserStreak;
import com.thtung.habit_app.utils.CalculateLastCompleted;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class FirestoreManager {
    private FirebaseFirestore db;
    private static final String TAG = "FirestoreManager";
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

    public void saveHabitLog(String habitId, String userId, String date, boolean completed, FirestoreWriteCallback callback) {
        if (userId == null || userId.isEmpty() || habitId == null || habitId.isEmpty() || date == null || date.isEmpty()) {
            Log.e("FirestoreManager", "Invalid parameters for saveHabitLog.");
            if (callback != null) {
                callback.onError("Dữ liệu không hợp lệ để lưu log.");
            }
            return;
        }

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("habit_id", habitId);
        logMap.put("user_id", userId);
        logMap.put("date", date);  // format yyyyMMdd
        logMap.put("completed", completed);

        db.collection("HabitLog")
                .add(logMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreManager", "HabitLog added/updated successfully. Doc ID: " + documentReference.getId());
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Error saving HabitLog", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
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

    public void getHabitNotesByHabitId(String userId, String habitId, HabitNoteListCallback callback) {
        db.collection("HabitNote")
                .whereEqualTo("userId", userId)
                .whereEqualTo("habitId", habitId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<HabitNote> notes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        HabitNote note = doc.toObject(HabitNote.class);
                        notes.add(note);
                    }
                    callback.onHabitNoteListLoaded(notes);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    public void deleteHabitNote(String userId, String noteId, NoteDeleteCallback callback) {
        Log.d(TAG, "Attempting to delete note with noteId: " + noteId + " for userId: " + userId);

        if (noteId == null || noteId.isEmpty() || userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid parameters: noteId or userId is null or empty");
            if (callback != null) {
                callback.onError("Invalid noteId or userId");
            }
            return;
        }

        DocumentReference noteRef = db.collection("HabitNote").document(noteId);

        // Kiểm tra sự tồn tại và quyền truy cập trước khi xóa
        noteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Log.w(TAG, "Note not found: " + noteId);
                if (callback != null) {
                    callback.onError("Ghi chú không tồn tại");
                }
                return;
            }

            String noteUserId = documentSnapshot.getString("userId");
            if (!userId.equals(noteUserId)) {
                Log.w(TAG, "User " + userId + " does not have permission to delete note: " + noteId);
                if (callback != null) {
                    callback.onError("Bạn không có quyền xóa ghi chú này");
                }
                return;
            }

            // Tiến hành xóa
            noteRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Note deleted successfully: " + noteId);
                        if (callback != null) {
                            callback.onNoteDeleted();
                        }
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = "Lỗi khi xóa ghi chú: " + e.getMessage();
                        Log.e(TAG, errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    });
        }).addOnFailureListener(e -> {
            String errorMessage = "Lỗi khi kiểm tra ghi chú: " + e.getMessage();
            Log.e(TAG, errorMessage);
            if (callback != null) {
                callback.onError(errorMessage);
            }
        });
    }

    public Task<QuerySnapshot> getHabitsTask(String userId) {
        return db.collection("Habit").whereEqualTo("user_id", userId).get();
    }

    public Task<QuerySnapshot> getAllMilestonesTask() {
        return db.collection("RewardMilestone")
                .orderBy("required_streak_days", Query.Direction.ASCENDING)
                .get();
    }

    public Task<QuerySnapshot> getUserStreakTask(String userId, String streakType) {
        return db.collection("UserStreak")
                .whereEqualTo("userId", userId)
                .whereEqualTo("streakType", streakType)
                .limit(1)
                .get();
    }

    public Task<QuerySnapshot> getTodaysCompletionsTask(String userId, String dateStr) {
        return db.collection("HabitLog")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("date", dateStr)
                // .whereEqualTo("completed", true) // Có thể thêm nếu chỉ muốn lấy log hoàn thành
                .get();
    }

    public Task<QuerySnapshot> getAchievedMilestonesTask(String userId) {
        return db.collection("UserMilestone")
                .whereEqualTo("userId", userId)
                .get();
    }

    // Lấy UserStreak (cần xử lý cả trường hợp không tìm thấy)
    public void getUserStreak(String userId, String streakType, UserStreakCallback callback) {
        db.collection("UserStreak")
                .whereEqualTo("userId", userId)
                .whereEqualTo("streakType", streakType)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        UserStreak streak = querySnapshot.getDocuments().get(0).toObject(UserStreak.class);
                        if (streak != null) {
                            streak.setId(querySnapshot.getDocuments().get(0).getId()); // Gán ID nếu cần
                            callback.onUserStreakLoaded(streak);
                        } else {
                            // Lỗi deserialize dù document tồn tại
                            callback.onError("Error deserializing UserStreak document.");
                        }
                    } else {
                        // Không tìm thấy streak cho user và type này
                        callback.onUserStreakNotFound();
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Cập nhật hoặc Tạo mới UserStreak
    public void saveOrUpdateUserStreak(UserStreak streak, FirestoreWriteCallback callback) {
        if (streak == null || streak.getUserId() == null || streak.getUserId().isEmpty()) {
            callback.onError("Invalid UserStreak object provided.");
            return;
        }
        DocumentReference streakRef;
        if (streak.getId() != null && !streak.getId().isEmpty()) {
            streakRef = db.collection("UserStreak").document(streak.getId());
        } else {
            // Nếu ID null, tạo document mới và gán ID lại cho object (tùy chọn)
            streakRef = db.collection("UserStreak").document();
            // streak.setId(streakRef.getId()); // Gán ID vào object nếu cần dùng ngay sau đó
        }
        // Dùng set để ghi đè hoặc tạo mới
        streakRef.set(streak)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Kiểm tra xem UserMilestone đã tồn tại chưa (hữu ích cho ViewModel)
    public void checkUserMilestoneExists(String userId, String milestoneId, UserMilestoneCheckCallback callback) {
        db.collection("UserMilestone")
                .whereEqualTo("userId", userId)
                .whereEqualTo("milestone_id", milestoneId) // Giả sử tên trường là milestone_id
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onResult(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Xóa UserMilestone
    public void deleteUserMilestone(String userId, String milestoneIdToDelete, FirestoreWriteCallback callback) {
        // Query để tìm document ID cần xóa
        db.collection("UserMilestone")
                .whereEqualTo("userId", userId)
                .whereEqualTo("milestone_id", milestoneIdToDelete) // Giả sử tên trường là milestone_id
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference docRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        docRef.delete()
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        Log.w("FirestoreManager", "UserMilestone record not found to delete: " + milestoneIdToDelete);
                        callback.onSuccess(); // Coi như thành công nếu không tìm thấy để xóa
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Lưu Streak và nhiều UserMilestones cùng lúc
    public void saveStreakAndAchievementsBatch(UserStreak streakToSave, List<UserMilestone> achievementsToSave, FirestoreWriteCallback callback) {
        if (streakToSave == null || streakToSave.getUserId() == null) {
            callback.onError("Invalid UserStreak for batch save.");
            return;
        }
        if (achievementsToSave == null) {
            achievementsToSave = new ArrayList<>(); // Tránh lỗi null
        }

        WriteBatch batch = db.batch();

        // 1. Set UserStreak
        DocumentReference streakRef = getStreakDocRef(streakToSave); // Dùng lại helper nếu có, hoặc logic tương tự
        batch.set(streakRef, streakToSave);

        // 2. Set các UserMilestone mới
        for (UserMilestone achievement : achievementsToSave) {
            // Đảm bảo userId khớp hoặc set lại nếu cần
            achievement.setUser_id(streakToSave.getUserId());
            DocumentReference achievementRef = db.collection("UserMilestone").document(); // ID mới
            batch.set(achievementRef, achievement);
        }

        // 3. Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Helper lấy DocRef cho Streak
    private DocumentReference getStreakDocRef(UserStreak streak) {
        if (streak.getId() != null && !streak.getId().isEmpty()) {
            return db.collection("UserStreak").document(streak.getId());
        } else {
            return db.collection("UserStreak").document(); // Trả về ref mới
        }
    }

    // Lấy Feedback
    public void getFeedback(String userId, Consumer<Feedback> onFeedbackLoaded, Runnable onNoFeedback) {
        db.collection("Feedback")
                .whereEqualTo("user_id", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Feedback feedback = querySnapshot.getDocuments().get(0).toObject(Feedback.class);
                        feedback.setId(querySnapshot.getDocuments().get(0).getId());
                        onFeedbackLoaded.accept(feedback);
                    } else {
                        onNoFeedback.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Error getting feedback: " + e.getMessage());
                });
    }

    // Lưu hoặc cập nhật Feedback
    public void saveOrUpdateFeedback(String userId, String content, int rating, String existingFeedbackId, Runnable onSuccess) {
        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("user_id", userId);
        feedbackData.put("content", content);
        feedbackData.put("rating", rating);
        feedbackData.put("create_at", Timestamp.now());
        if(existingFeedbackId != null) { // UPDATE
            db.collection("Feedback")
                    .document(existingFeedbackId)
                    .set(feedbackData)
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreManager", "Error updating feedback: " + e.getMessage());
                    });
        } else { // CREATE
            db.collection("Feedback")
                    .add(feedbackData)
                    .addOnSuccessListener(documentReference -> onSuccess.run())
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreManager", "Error creating feedback: " + e.getMessage());
                    });
        }
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
    public interface NoteDeleteCallback {
        void onNoteDeleted();
        void onError(String errorMessage);
    }
    public interface HabitNoteListCallback {
        void onHabitNoteListLoaded(ArrayList<HabitNote> noteList);
        void onError(String errorMessage);
    }

    public interface FirestoreWriteCallback { // Callback chung cho các thao tác ghi/cập nhật/xóa
        void onSuccess();
        void onError(String message);
    }

    public interface MilestoneListCallback {
        void onMilestoneListLoaded(List<RewardMilestone> milestones);
        void onError(String message);
    }

    public interface UserMilestoneListCallback {
        void onUserMilestoneListLoaded(List<UserMilestone> userMilestones);
        void onError(String message);
    }

    public interface UserStreakCallback {
        void onUserStreakLoaded(UserStreak userStreak);
        void onUserStreakNotFound(); // Thêm callback này
        void onError(String message);
    }

    public interface UserMilestoneCheckCallback {
        void onResult(boolean exists); // Trả về true nếu tồn tại
        void onError(String message);
    }

    public interface  UserPointCallback{
        void onUserPointLoaded(UserPoint userPoint);
        void onError(String errorMessage);
    }


}
