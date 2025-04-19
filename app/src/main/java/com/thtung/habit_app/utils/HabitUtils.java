package com.thtung.habit_app.utils;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.thtung.habit_app.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HabitUtils {

    private static final String TAG = "HabitUtils"; // Tag cho logging

    /**
     * Kiểm tra xem một thói quen có nên được hiển thị/tính vào ngày hôm nay không,
     * dựa trên ngày bắt đầu và kiểu lặp lại của nó.
     *
     * @param habit Thói quen cần kiểm tra.
     * @return true nếu thói quen cần thực hiện hôm nay, false nếu không.
     */
    public static boolean shouldDisplayToday(Habit habit) {
        // --- Điều kiện đầu vào ---
        if (habit == null) {
            Log.w(TAG, "shouldDisplayToday: Habit object is null.");
            return false;
        }
        // Lấy Timestamp, cần xử lý null cẩn thận
        Timestamp startTimestamp = habit.getStart_at();
        if (startTimestamp == null) {
            Log.w(TAG, "shouldDisplayToday: Habit '" + habit.getName() + "' has null start_at timestamp.");
            // Quyết định: Coi như không hiển thị hay mặc định là hiển thị nếu không có ngày bắt đầu?
            // Tạm thời coi như không hiển thị nếu không có ngày bắt đầu.
            return false;
        }
        Date startDate = startTimestamp.toDate(); // Chuyển đổi an toàn hơn nếu đã kiểm tra null
        Date todayDate = new Date(); // Ngày hiện tại

        // --- So sánh ngày (bỏ qua giờ) ---
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int startInt;
        int todayInt;
        try {
            startInt = Integer.parseInt(sdf.format(startDate));
            todayInt = Integer.parseInt(sdf.format(todayDate));
        } catch (NumberFormatException e) {
            Log.e(TAG, "shouldDisplayToday: Error parsing date for habit '" + habit.getName() + "'", e);
            return false; // Lỗi parse ngày, không hiển thị
        }

        // 1. Kiểm tra ngày bắt đầu có trong tương lai không
        if (startInt > todayInt) {
            Log.d(TAG, "shouldDisplayToday: Habit '" + habit.getName() + "' starts in the future (" + startInt + "). Not displaying today.");
            return false;
        }

        // --- Kiểm tra kiểu lặp lại ---
        String repeatType = habit.getRepeat();
        // Xử lý trường hợp repeatType là null hoặc rỗng -> Mặc định là "Hàng ngày"? (Tùy yêu cầu)
        String effectiveRepeatType = (repeatType != null && !repeatType.isEmpty()) ? repeatType : "Hàng ngày";
        Log.d(TAG, "shouldDisplayToday - Checking habit: '" + habit.getName() + "', Repeat: '" + effectiveRepeatType + "', Start: " + startInt);


        switch (effectiveRepeatType) {
            case "Hàng ngày":
                // Nếu đã qua ngày bắt đầu, hiển thị hàng ngày
                return true;

            case "Hàng tuần": {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                Calendar todayCal = Calendar.getInstance();
                // todayCal.setTime(todayDate); // Không cần thiết vì todayCal đã là ngày hôm nay
                int startDayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                int todayDayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK);
                boolean match = (startDayOfWeek == todayDayOfWeek);
                Log.d(TAG, "shouldDisplayToday (Weekly): '" + habit.getName() + "', StartDoW=" + startDayOfWeek + ", TodayDoW=" + todayDayOfWeek + ", Match=" + match);
                return match;
            }

            case "Hàng tháng": {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                Calendar todayCal = Calendar.getInstance();
                // todayCal.setTime(todayDate);
                int startDayOfMonth = startCal.get(Calendar.DAY_OF_MONTH);
                int todayDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH);
                boolean match = (startDayOfMonth == todayDayOfMonth);
                Log.d(TAG, "shouldDisplayToday (Monthly): '" + habit.getName() + "', StartDoM=" + startDayOfMonth + ", TodayDoM=" + todayDayOfMonth + ", Match=" + match);
                return match;
            }

            default:
                // Nếu repeatType không khớp các giá trị trên
                Log.w(TAG, "shouldDisplayToday: Unknown repeatType '" + effectiveRepeatType + "' for habit '" + habit.getName() + "'. Not displaying today.");
                return false;
        }
    }

    // Có thể thêm các hàm tiện ích khác liên quan đến Habit ở đây nếu cần
    // Ví dụ: hàm định dạng ngày tháng, hàm tính toán,...
}