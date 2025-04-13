package com.thtung.habit_app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.thtung.habit_app.firebase.FirestoreManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatisticUtil {
    public static void checkAndUpdateStatisticsOncePerDay(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        //prefs.edit().putString("lastStatsUpdateDate", String.valueOf(20250411)).apply();
        String lastUpdated = prefs.getString("lastStatsUpdateDate", null);
        // Lấy ngày hôm nay dạng yyyyMMdd
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        Log.d("StatisticUtil", "today = " + today + ", lastUpdated = " + lastUpdated);
        if (!today.equals(lastUpdated)) {
            // Cập nhật thống kê nếu chưa cập nhật hôm nay
            FirestoreManager manager = new FirestoreManager();
            manager.updateLastCompletedForAllHabits(userId);// Lưu lại ngày cập nhật
            prefs.edit().putString("lastStatsUpdateDate", today).apply();
            Log.d("StatisticUtil", "Cập nhật thống kê thành công");
        }
    }
}
