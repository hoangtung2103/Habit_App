package com.thtung.habit_app.utils;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter DATE_FORMATTER_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    // --- Tiện ích xử lý Ngày/Giờ ---

    // Lấy Timestamp đầu ngày (00:00:00) theo múi giờ hệ thống
    public Timestamp getStartOfDayTimestamp(Timestamp timestamp) {
        LocalDate localDate = timestampToLocalDate(timestamp);
        return localDateToStartOfDayTimestamp(localDate);
    }

    // Lấy Timestamp cuối ngày (23:59:59.999) theo múi giờ hệ thống
    public Timestamp getEndOfDayTimestamp(Timestamp timestamp) {
        LocalDate localDate = timestampToLocalDate(timestamp);
        Instant instant = localDate.atTime(23, 59, 59, 999_999_999) // Cuối ngày
                .atZone(ZoneId.systemDefault())
                .toInstant();
        return new Timestamp(instant.getEpochSecond(), instant.getNano());
    }

    // Chuyển Timestamp (từ Firestore) sang LocalDate (theo múi giờ hệ thống)
    public LocalDate timestampToLocalDate(Timestamp timestamp) {
        if (timestamp == null) return null; // Hoặc trả về một giá trị mặc định an toàn
        return timestamp.toDate().toInstant() // Chuyển Timestamp -> java.util.Date -> Instant
                .atZone(ZoneId.systemDefault()) // Áp dụng múi giờ hệ thống
                .toLocalDate(); // Lấy phần ngày
    }

    // Chuyển LocalDate sang Timestamp đầu ngày (00:00:00) theo múi giờ hệ thống
    public Timestamp localDateToStartOfDayTimestamp(LocalDate localDate) {
        if (localDate == null) return null;
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return new Timestamp(instant.getEpochSecond(), instant.getNano());
    }

    // So sánh xem 2 Timestamp có cùng ngày (theo múi giờ hệ thống) không
    public boolean isSameDay(Timestamp ts1, Timestamp ts2) {
        if (ts1 == null || ts2 == null) {
            return false;
        }
        LocalDate date1 = timestampToLocalDate(ts1);
        LocalDate date2 = timestampToLocalDate(ts2);
        return date1.isEqual(date2);
    }

    public String getTodayDateString() {
        return LocalDate.now(ZoneId.systemDefault()).format(DATE_FORMATTER_YYYYMMDD);
    }

}
