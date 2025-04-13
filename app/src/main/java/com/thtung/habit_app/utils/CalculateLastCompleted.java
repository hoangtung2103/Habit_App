package com.thtung.habit_app.utils;

import java.util.Calendar;

public class CalculateLastCompleted {

    public static long calculateLastCompleted(Calendar startCal, Calendar todayCal, String repeat) {
        // Clone để tránh làm thay đổi biến gốc
        Calendar start = (Calendar) startCal.clone();
        Calendar today = (Calendar) todayCal.clone();

        // Nếu start sau today thì không tính
        if (start.after(today)) return 0;

        // Reset time về 00:00:00 để tính ngày chính xác
        resetTimeToMidnight(start);
        resetTimeToMidnight(today);

        switch (repeat) {
            case "Hàng ngày":
                long diffMillis = today.getTimeInMillis() - start.getTimeInMillis();
                long diffDays = diffMillis / (1000L * 60 * 60 * 24);
                return diffDays + 1;

            case "Hàng tuần":
                int startDayOfWeek = start.get(Calendar.DAY_OF_WEEK);

                long totalDays = (today.getTimeInMillis() - start.getTimeInMillis()) / (1000L * 60 * 60 * 24);
                long fullWeeks = totalDays / 7;
                long remainingDays = totalDays % 7;

                Calendar temp = (Calendar) today.clone();
                temp.add(Calendar.DAY_OF_YEAR, -(int) remainingDays);

                boolean match = false;
                for (int i = 0; i <= remainingDays; i++) {
                    if (temp.get(Calendar.DAY_OF_WEEK) == startDayOfWeek) {
                        match = true;
                        break;
                    }
                    temp.add(Calendar.DAY_OF_YEAR, 1);
                }

                return fullWeeks + (match ? 1 : 0) + 1;

            case "Hàng tháng":
                int yearDiff = today.get(Calendar.YEAR) - start.get(Calendar.YEAR);
                int monthDiff = yearDiff * 12 + today.get(Calendar.MONTH) - start.get(Calendar.MONTH);

                int startDay = start.get(Calendar.DAY_OF_MONTH);
                int todayDay = today.get(Calendar.DAY_OF_MONTH);
                boolean includeThisMonth = todayDay >= startDay;

                return monthDiff + (includeThisMonth ? 1 : 0);

            default:
                return 0;
        }
    }

    private static void resetTimeToMidnight(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
