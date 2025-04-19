package com.thtung.habit_app.ViewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.HabitLog;
import com.thtung.habit_app.model.RewardMilestone;
import com.thtung.habit_app.model.UserMilestone;
import com.thtung.habit_app.model.UserStreak;
import com.thtung.habit_app.utils.DateTimeUtils;
import com.thtung.habit_app.utils.HabitUtils;
//import com.google.type.Date;
import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StreakViewModel extends ViewModel {
    private DateTimeUtils dateTimeUtils;
    private static final String TAG = "StreakViewModel";
    private static final String PERFECT_DAY_STREAK_TYPE = "PERFECT_DAY";
    private String userId;

    private FirestoreManager firestoreManager;

    @Nullable
    private MilestoneProgressListener listener;

    // --- State Variables ---
    private List<Habit> requiredHabits = new ArrayList<>(); // Danh sách các habit cần hoàn thành
    private UserStreak perfectDayStreak = null; // Streak hiện tại của user cho PERFECT_DAY
    private List<RewardMilestone> allMilestones = new ArrayList<>();
    private String targetMilestoneId;
    private Set<String> achievedMilestoneIds = new HashSet<>(); // Đã đạt được milestone này chưa
    private Set<String> completedHabitIdsToday = new HashSet<>();
    private boolean isInitialized = false;

    public StreakViewModel() {
        // Constructor này phải tồn tại để ViewModelProvider mặc định hoạt động
        Log.d(TAG, "StreakViewModel no-arg constructor called.");
    }

    // Hàm Init
    public void init(String userId, FirestoreManager manager) {
        if (isInitialized) return;
        if (userId == null || userId.isEmpty() || manager == null) {
            Log.e(TAG, "Cannot initialize ViewModel with null args.");
            return;
        }
        this.userId = userId;
        this.firestoreManager = manager;
        this.isInitialized = true;
        this.dateTimeUtils = new DateTimeUtils();
        Log.d(TAG, "ViewModel initialized with userId: " + userId);
        loadInitialData();
    }

    // 1. Tải dữ liệu ban đầu
    public void loadInitialData() {
        if (listener != null) listener.onLoadingStateChanged(true);

        Task<QuerySnapshot> habitsTask = firestoreManager.getHabitsTask(userId);
        Task<QuerySnapshot> allMilestonesTask = firestoreManager.getAllMilestonesTask();
        Task<QuerySnapshot> streakTask = firestoreManager.getUserStreakTask(userId, PERFECT_DAY_STREAK_TYPE);
        Task<QuerySnapshot> achievedTask = firestoreManager.getAchievedMilestonesTask(userId);
        String todayDateStr = dateTimeUtils.getTodayDateString();
        Task<QuerySnapshot> completionsTodayTask = firestoreManager.getTodaysCompletionsTask(userId, todayDateStr);

        // Gộp các Task lại
        Task<List<Object>> allTasks = Tasks.whenAllSuccess(
                habitsTask, allMilestonesTask, streakTask, achievedTask, completionsTodayTask
        );

        allTasks.addOnSuccessListener(results -> {
            try {
                // Habits (index 0)
                QuerySnapshot habitsSnapshot = (QuerySnapshot) results.get(0);
                List<Habit> loadedHabits = habitsSnapshot.toObjects(Habit.class);
                if (loadedHabits  != null) {

                    List<DocumentSnapshot> habitDocuments = habitsSnapshot.getDocuments();
                    if(loadedHabits.size() == habitDocuments.size()){
                        for (int i = 0; i < loadedHabits.size(); i++) {
                            Habit habit = loadedHabits.get(i);
                            DocumentSnapshot doc = habitDocuments.get(i);
                            if (habit != null && doc != null) {
                                habit.setId(doc.getId()); // Gán ID document vào đối tượng Habit
                                if (HabitUtils.shouldDisplayToday(habit)) {
                                    requiredHabits.add(habit);
                                }
                                Log.d(TAG, "loadInitialData - Added habit with ID: " + habit.getId() + ", Name: " + habit.getName());
                            } else {
                                Log.e(TAG, "loadInitialData - Null habit or document encountered during ID assignment at index " + i);
                            }
                        }
                    } else {
                        Log.e(TAG, "loadInitialData - Mismatch between loadedHabits size and document size. Cannot assign IDs correctly.");
                    }
                    Log.d(TAG, "loadInitialData - Final requiredHabits list size = " + requiredHabits.size());
                    if (listener != null) listener.onRequiredHabitsLoaded(new ArrayList<>(requiredHabits));
                } else {
                    requiredHabits.clear();
                }


                // All Milestones (index 1)
                QuerySnapshot milestonesSnapshot = (QuerySnapshot) results.get(1);
                if (milestonesSnapshot != null) {
                    allMilestones = milestonesSnapshot.toObjects(RewardMilestone.class);
                } else {
                    allMilestones.clear();
                }
                Log.d(TAG, "Loaded " + allMilestones.size() + " total milestones.");

                // Streak (index 2)
                QuerySnapshot streakSnapshot = (QuerySnapshot) results.get(2);
                if (streakSnapshot != null && !streakSnapshot.isEmpty()) { // Thêm kiểm tra null
                    perfectDayStreak = streakSnapshot.getDocuments().get(0).toObject(UserStreak.class);
                    if (perfectDayStreak != null) { // Kiểm tra null sau toObject
                        perfectDayStreak.setId(streakSnapshot.getDocuments().get(0).getId()); // Gán ID
                        checkAndResetStreakIfNeeded(perfectDayStreak);
                    } else {
                        Log.e(TAG, "Failed to deserialize UserStreak document.");
                        // Khởi tạo mặc định nếu deserialize lỗi
                        perfectDayStreak = new UserStreak(userId, PERFECT_DAY_STREAK_TYPE, null, 0, null);
                    }
                } else {
                    perfectDayStreak = new UserStreak(userId, PERFECT_DAY_STREAK_TYPE, null, 0, null);
                    Log.i(TAG, "No existing PERFECT_DAY streak found. Initialized new one locally with null date.");
                }

                // Achieved Milestones (index 3)
                QuerySnapshot achievedSnapshot = (QuerySnapshot) results.get(3);
                achievedMilestoneIds.clear();
                if (achievedSnapshot != null) {
                    for (DocumentSnapshot doc : achievedSnapshot.getDocuments()) {
                        UserMilestone um = doc.toObject(UserMilestone.class);
                        if (um != null && um.getMilestone_id() != null) {
                            achievedMilestoneIds.add(um.getMilestone_id());
                        }
                    }
                }
                Log.d(TAG, "Loaded " + achievedMilestoneIds.size() + " achieved milestone IDs.");


                // Completions Today (index 4)
                QuerySnapshot completionsSnapshot = (QuerySnapshot) results.get(4);
                completedHabitIdsToday.clear();
                if(completionsSnapshot != null) {
                    for (DocumentSnapshot doc : completionsSnapshot.getDocuments()) {
                        HabitLog completion = doc.toObject(HabitLog.class);
                        if (completion != null && completion.getHabit_id() != null && todayDateStr.equals(completion.getDate())) {
                            completedHabitIdsToday.add(completion.getHabit_id());
                        }
                    }
                }
                Log.d(TAG, "Loaded " + completedHabitIdsToday.size() + " completions for today (" + todayDateStr + ")");

                Log.d(TAG, "Initial data loaded successfully.");
                if (listener != null) {
                    listener.onLoadingStateChanged(false);
                    notifyProgressUpdate();
                }

            } catch (Exception e) {
                handleError("Error processing initial data", e);
            }

        }).addOnFailureListener(e -> handleError("Failed to load initial data", e));
    }

    private void checkAndResetStreakIfNeeded(UserStreak streak) {
        if (streak == null || streak.getLastCompletionDate() == null || streak.getCurrentStreak() == 0) {
            return;
        }

        LocalDate lastCompletionLocalDate = dateTimeUtils.timestampToLocalDate(streak.getLastCompletionDate());
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        if (!lastCompletionLocalDate.isEqual(today) && !lastCompletionLocalDate.isEqual(yesterday)) {
            Log.i(TAG, "Streak outdated (" + lastCompletionLocalDate + "). Resetting.");
            streak.setCurrentStreak(0);
            streak.setLastCompletionDate(null);
            firestoreManager.saveOrUpdateUserStreak(streak, new FirestoreManager.FirestoreWriteCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Streak successfully reset in Firestore via Manager Callback.");
                }
                @Override
                public void onError(String message) {
                    Log.e(TAG, "Failed to reset streak in Firestore via Manager Callback: " + message);
                    // Rollback hoặc báo lỗi
                }
            });
        }
    }


    // 2. Đánh dấu Habit hoàn thành
    public void handleHabitLogged(String habitId, String date) {
        if (!isInitialized) {
            handleError("ViewModel not initialized, cannot handle habit log.");
            return;
        }
        String todayDateStr = dateTimeUtils.getTodayDateString();
        if (!todayDateStr.equals(date)) {
            Log.w(TAG, "handleHabitLogged received log for a different date (" + date + "). Ignoring check.");
            return;
        }

        Log.d(TAG, "Handling successful log for habit: " + habitId + " on date " + date);
        // 1. Cập nhật cache local
        completedHabitIdsToday.add(habitId);

        // 2. Gọi kiểm tra hoàn thành tất cả
        checkIfAllHabitsCompletedToday();


        // 4. (Tùy chọn) Gọi cập nhật Statistic từ đây thay vì từ Adapter
        // Lấy thông tin repeat của habit từ requiredHabits nếu cần
        // Habit habitInfo = findHabitInfoById(habitId);
        // if (habitInfo != null && habitInfo.getRepeat() != null) {
        //     firestoreManager.updateStatisticAfterLog(habitId, userId, date, habitInfo.getRepeat());
        // } else {
        //     Log.w(TAG, "Could not find habit info or repeat type to update statistic for " + habitId);
        // }
    }

    // 4. Kiểm tra xem tất cả habit cần thiết đã hoàn thành hôm nay chưa
    private void checkIfAllHabitsCompletedToday() {
        if (requiredHabits.isEmpty()) {
            Log.w(TAG,"check" +
                    "IfAllHabitsCompletedToday: requiredHabits list is empty.");
            return;
        }

        Set<String> requiredIds = requiredHabits.stream().map(Habit::getId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        boolean allCompleted = completedHabitIdsToday.containsAll(requiredIds);

        if (allCompleted) {
            Log.d(TAG, "All required habits completed for today!");
            updatePerfectDayStreak(); // Gọi hàm cập nhật streak
        } else {
            int remaining = requiredHabits.size() - completedHabitIdsToday.size();
            Log.d(TAG, "Not all habits completed today. Remaining: " + remaining);
            if (listener != null) {
                listener.onCompletionNeeded(remaining);
            }
        }
    }

    private void processStreakUpdate(boolean isNewPerfectDay) {
        if (perfectDayStreak == null) {
            // Nên tạo hoặc load lại trước khi gọi hàm này
            handleError("Cannot process streak update, streak object is null.");
            return;
        }

        if (!isNewPerfectDay) {
            Log.d(TAG, "processStreakUpdate: Today was already marked perfect, no streak change.");
            notifyProgressUpdate();
            return;
        }

        int previousStreak = perfectDayStreak.getCurrentStreak(); // Lưu lại streak cũ để so sánh

        Timestamp todayTimestamp = Timestamp.now();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastCompletionDateForRollback = dateTimeUtils.timestampToLocalDate(perfectDayStreak.getLastCompletionDate());
        LocalDate lastCompletionDate = (perfectDayStreak.getLastCompletionDate() != null)
                ? dateTimeUtils.timestampToLocalDate(perfectDayStreak.getLastCompletionDate())
                : null;

        if (lastCompletionDate != null && lastCompletionDate.isEqual(yesterday)) {
            perfectDayStreak.setCurrentStreak(perfectDayStreak.getCurrentStreak() + 1);
        } else {
            perfectDayStreak.setCurrentStreak(1);
        }
        perfectDayStreak.setLastCompletionDate(dateTimeUtils.getStartOfDayTimestamp(todayTimestamp));
        Log.i(TAG, "processStreakUpdate: Streak updated to " + perfectDayStreak.getCurrentStreak());

        //--- Kiểm tra TẤT CẢ milestones MỚI đạt được ---
        List<RewardMilestone> newlyAchievedMilestones = new ArrayList<>();
        int currentStreakValue = perfectDayStreak.getCurrentStreak();

        for (RewardMilestone milestone : allMilestones) { // Duyệt qua tất cả milestones đã load
            if (currentStreakValue >= milestone.getRequired_streak_days() && // Đủ streak
                    !achievedMilestoneIds.contains(milestone.getId())) // Chưa đạt trước đó
            {
                newlyAchievedMilestones.add(milestone);
                achievedMilestoneIds.add(milestone.getId()); // Cập nhật cache local ngay lập tức
                Log.i(TAG, "processStreakUpdate: Newly achieved milestone: " + milestone.getReward_name());
            }
        }
        //----------------------------------------------------

        //--- Thực hiện ghi vào Firestore ---
        if (!newlyAchievedMilestones.isEmpty()) {
            List<UserMilestone> achievementsToSave = new ArrayList<>();
            for(RewardMilestone achieved : newlyAchievedMilestones) {
                achievementsToSave.add(new UserMilestone(userId, achieved.getId()));
            }
            // *** Gọi hàm batch callback của FirestoreManager ***
            firestoreManager.saveStreakAndAchievementsBatch(perfectDayStreak, achievementsToSave, new FirestoreManager.FirestoreWriteCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Batch Success via Manager Callback: Streak updated and " + newlyAchievedMilestones.size() + " new milestones achieved.");
                    updateLocalCompletionsCache();
                    notifyProgressUpdate();
                    if (listener != null) {
                        for (RewardMilestone achieved : newlyAchievedMilestones) {
                            listener.onTargetMilestoneAchieved(achieved);
                        }
                    }
                }
                @Override
                public void onError(String message) {
                    Log.e(TAG, "Batch Failed via Manager Callback for new milestones: " + message);
                    // Rollback cache local
                    for (RewardMilestone achieved : newlyAchievedMilestones) {
                        achievedMilestoneIds.remove(achieved.getId());
                    }
                    perfectDayStreak.setCurrentStreak(previousStreak);
                    perfectDayStreak.setLastCompletionDate(dateTimeUtils.localDateToStartOfDayTimestamp(lastCompletionDateForRollback));
                    handleError("Lỗi khi lưu streak và các phần thưởng mới.", new Exception(message));
                    notifyProgressUpdate();
                }
            });
        } else {
            // *** Gọi hàm save/update đơn lẻ callback của FirestoreManager ***
            firestoreManager.saveOrUpdateUserStreak(perfectDayStreak, new FirestoreManager.FirestoreWriteCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Streak updated successfully via Manager Callback (no new milestones).");
                    updateLocalCompletionsCache();
                    notifyProgressUpdate();
                }
                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error updating streak via Manager Callback: " + message);
                    perfectDayStreak.setCurrentStreak(previousStreak);
                    perfectDayStreak.setLastCompletionDate(dateTimeUtils.localDateToStartOfDayTimestamp(lastCompletionDateForRollback));
                    handleError("Lỗi khi cập nhật streak.", new Exception(message));
                    notifyProgressUpdate();
                }
            });
        }
    }


    // 5. Cập nhật (hoặc tạo mới) UserStreak cho PERFECT_DAY
    private void updatePerfectDayStreak() {
        if (perfectDayStreak == null) return;
        Timestamp todayTimestamp = Timestamp.now();
        boolean isAlreadyMarkedToday = perfectDayStreak.getLastCompletionDate() != null && dateTimeUtils.isSameDay(perfectDayStreak.getLastCompletionDate(), todayTimestamp);
        processStreakUpdate(!isAlreadyMarkedToday);
    }

    private void updateLocalCompletionsCache() {
        if(!requiredHabits.isEmpty()){
            requiredHabits.forEach(h -> completedHabitIdsToday.add(h.getId()));
        }
    }

    @Nullable
    private RewardMilestone findMilestoneById(String milestoneId) {
        if (milestoneId == null) return null;
        for (RewardMilestone m : allMilestones) {
            if (milestoneId.equals(m.getId())) {
                return m;
            }
        }
        return null;
    }



    // 7. Thông báo cập nhật cho Listener
    public void notifyProgressUpdate() {
        if (listener != null) {
            List<Habit> habitsCopy = new ArrayList<>(requiredHabits);
            Set<String> completedTodayCopy = new HashSet<>(completedHabitIdsToday);
            int currentStreakValue = (perfectDayStreak != null) ? perfectDayStreak.getCurrentStreak() : 0;

            // Tìm mốc tiếp theo CHƯA ĐẠT ĐƯỢC
            RewardMilestone nextMilestone = findNextUnachievedMilestone(currentStreakValue);

            // Cờ báo hiệu có mốc nào mới đạt gần đây không (logic này có thể phức tạp)
            // Tạm thời để false, vì thông báo từng mốc qua onTargetMilestoneAchieved
            boolean achievedRecently = false;

            listener.onProgressUpdated(
                    currentStreakValue,
                    nextMilestone, // Mốc tiếp theo chưa đạt
                    achievedRecently,
                    completedTodayCopy,
                    habitsCopy
            );
        } else {
            Log.w(TAG, "Cannot notify progress update. Listener is null.");
        }
    }

    @Nullable
    private RewardMilestone findNextUnachievedMilestone(int currentStreak) {
        // Giả định allMilestones đã được sắp xếp theo requiredStreakDays tăng dần
        for (RewardMilestone milestone : allMilestones) {
            if (!achievedMilestoneIds.contains(milestone.getId())) {
                // Đây là mốc đầu tiên chưa đạt
                return milestone;
            }
        }
        return null; // Đã đạt tất cả các mốc
    }

    public void markDayAsPerfect() {
        Log.d(TAG, "Attempting to mark today as a Perfect Day via explicit action.");
        if (perfectDayStreak == null) {
            Log.w(TAG, "PerfectDayStreak was null when markDayAsPerfect called. Initializing a new one locally.");
            perfectDayStreak = new UserStreak(userId, PERFECT_DAY_STREAK_TYPE, null, 0, Timestamp.now());
        }
        Timestamp todayTimestamp = Timestamp.now();
        boolean isAlreadyMarkedToday = perfectDayStreak.getLastCompletionDate() != null && dateTimeUtils.isSameDay(perfectDayStreak.getLastCompletionDate(), todayTimestamp);
        Log.d(TAG,"markDayAsPerfect - Calculated isAlreadyMarkedToday = " + isAlreadyMarkedToday);
        processStreakUpdate(!isAlreadyMarkedToday);
    }



    private void handleError(String message, @Nullable Exception e) {
        Log.e(TAG, message, e);
        if (listener != null) {
            listener.onLoadingStateChanged(false); // Tắt loading nếu đang bật
            listener.onError(message + (e != null ? ": " + e.getMessage() : ""));
        }
    }
    private void handleError(String message) {
        handleError(message, null);
    }

}
