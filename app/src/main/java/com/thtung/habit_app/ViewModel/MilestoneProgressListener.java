package com.thtung.habit_app.ViewModel;


import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.RewardMilestone;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface MilestoneProgressListener {
    // Báo trạng thái loading dữ liệu ban đầu
    void onLoadingStateChanged(boolean isLoading);

    // Cập nhật UI với dữ liệu mới nhất
    void onProgressUpdated(
            int currentPerfectDayStreak,
            @Nullable RewardMilestone nextUnachievedMilestone,
            boolean isAnyMilestoneAchievedRecently,
            Set<String> completedHabitIdsToday, // Các habit ID đã hoàn thành hôm nay
            List<Habit> requiredHabits // Danh sách habit cần cho perfect day
    );

    // Thông báo khi mốc mục tiêu VỪA được đạt
    void onTargetMilestoneAchieved(RewardMilestone milestone);

    // Thông báo khi cần hoàn thành thêm habit trong ngày
    void onCompletionNeeded(int remainingCount);

    // Thông báo lỗi (ví dụ: lỗi mạng, không đọc/ghi được Firestore)
    void onError(String errorMessage);

    // Cung cấp danh sách habit ban đầu để tạo UI
    void onRequiredHabitsLoaded(List<Habit> habits);
}
