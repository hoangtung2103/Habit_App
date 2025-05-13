package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.BarChartAdapter;
import com.thtung.habit_app.model.BarChartData;
import com.thtung.habit_app.model.Statistic;
import com.thtung.habit_app.utils.ProgressCircleView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class StatisticActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private static final String TAG = "StatisticActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView tvTotalTasks, tvTotalCompleted, tvTotalUncompleted;
    private TextView tvCompleted, tvUncompletedDetail; // Thêm TextView cho "Hoàn thành" và "Thất bại"
    private RecyclerView rvBarChart;
    private ProgressCircleView pieChart;
    private int progressPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        tvTotalTasks = findViewById(R.id.tv_total_tasks);
        tvTotalCompleted = findViewById(R.id.tv_total_completed);
        tvTotalUncompleted = findViewById(R.id.tv_total_uncompleted);
        tvCompleted = findViewById(R.id.txt_hoanthanh); // TextView "Hoàn thành"
        tvUncompletedDetail = findViewById(R.id.txt_khonghoanthanh); // TextView "Thất bại"
        rvBarChart = findViewById(R.id.rv_bar_chart);
        pieChart = findViewById(R.id.pieChart);

        // Thiết lập RecyclerView cho biểu đồ cột
        rvBarChart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Khởi tạo các nút trên thanh điều hướng
        findViewById(R.id.img_ic_home).setOnClickListener(v -> {
            startActivity(new Intent(StatisticActivity.this, MainActivity.class));
            finish();
        });

        findViewById(R.id.img_ic_rank).setOnClickListener(v -> {
            startActivity(new Intent(StatisticActivity.this, BadgeActivity.class));
            finish();
        });

        findViewById(R.id.btn_caidat).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(StatisticActivity.this, SettingsActivity.class));
            finish();
        });

        FloatingActionButton fabAdd = findViewById(R.id.ic_add);
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(StatisticActivity.this, AddHabitActivity.class));
        });

        if (user != null) {
            loadStatistics(user.getUid());
            loadBarChartData(user.getUid());
        } else {
            Toast.makeText(this, "Người dùng chưa được xác thực", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadStatistics(String userId) {
        // Truy vấn tất cả Statistic có cùng user_id
        db.collection("Statistic")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(statisticSnapshots -> {
                    long totalTasks = 0;
                    long completedTasks = 0;

                    // Tính tổng last_completed và total_completed
                    for (QueryDocumentSnapshot document : statisticSnapshots) {
                        Statistic stat = document.toObject(Statistic.class);
                        Long lastCompleted = stat.getLast_completed();
                        Long totalCompleted = stat.getTotal_completed();

                        if (lastCompleted != null) {
                            totalTasks += lastCompleted;
                        }
                        if (totalCompleted != null) {
                            completedTasks += totalCompleted;
                        }
                    }

                    Log.d(TAG, "Total Tasks: " + totalTasks);
                    Log.d(TAG, "Completed Tasks: " + completedTasks);

                    long uncompletedTasks = Math.max(0, totalTasks - completedTasks); // Đảm bảo không âm

                    // Tính phần trăm tiến độ tổng
                    int progressPercent = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;

                    // Cập nhật giao diện
                    tvTotalTasks.setText(String.valueOf(totalTasks));
                    tvTotalCompleted.setText(String.valueOf(completedTasks));
                    tvTotalUncompleted.setText(String.valueOf(uncompletedTasks));
                    tvCompleted.setText(String.valueOf(completedTasks));
                    tvUncompletedDetail.setText(String.valueOf(uncompletedTasks));

                    // Cập nhật ProgressCircleView
                    pieChart.setPercent(progressPercent); // Thay đổi nếu cần
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Statistic: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải Statistic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private long calculateDaysDifference(QuerySnapshot habitSnapshots) {
        long totalDays = 0;
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC+7"));
        currentDate.setTime(new Date()); // Hiện tại: May 13, 2025, 06:12 PM +07

        for (QueryDocumentSnapshot document : habitSnapshots) {
            try {
                Timestamp startAt = document.getTimestamp("start_at");
                if (startAt != null) {
                    Date startDate = startAt.toDate();
                    long diffInMillies = currentDate.getTimeInMillis() - startDate.getTime();
                    long diffDays = TimeUnit.MILLISECONDS.toDays(diffInMillies) + 1; // +1 để bao gồm ngày đầu tiên
                    totalDays += Math.max(diffDays, 0); // Đảm bảo không âm
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return totalDays;
    }

    private void loadBarChartData(String userId) {
        db.collection("Statistic")
                .whereEqualTo("user_id", userId)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BarChartData> barChartDataList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Statistic stat = document.toObject(Statistic.class);
                        String habitId = stat.getHabit_id(); // Điều chỉnh getter
                        Long totalCompleted = stat.getTotal_completed(); // Điều chỉnh getter
                        Long lastCompleted = stat.getLast_completed(); // Điều chỉnh getter

                        double successPercent = (lastCompleted != null && lastCompleted > 0)
                                ? (totalCompleted != null ? (totalCompleted * 100.0 / lastCompleted) : 0) : 0;
                        barChartDataList.add(new BarChartData(habitId, successPercent));
                    }

                    BarChartAdapter barChartAdapter = new BarChartAdapter(barChartDataList);
                    rvBarChart.setAdapter(barChartAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu biểu đồ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}