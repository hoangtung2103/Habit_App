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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.BarChartAdapter;
import com.thtung.habit_app.model.BarChartData;
import com.thtung.habit_app.model.Statistic;
import com.thtung.habit_app.utils.ProgressCircleView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatisticActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private static final String TAG = "StatisticActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView tvTotalTasks, tvTotalCompleted, tvTotalUncompleted;
    private TextView tvCompleted, tvUncompletedDetail, tvProgressMessage;
    private RecyclerView rvBarChart;
    private ProgressCircleView pieChart;

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
        tvCompleted = findViewById(R.id.txt_hoanthanh);
        tvUncompletedDetail = findViewById(R.id.txt_khonghoanthanh);
        tvProgressMessage = findViewById(R.id.tv_progress_message);
        rvBarChart = findViewById(R.id.rv_bar_chart);
        pieChart = findViewById(R.id.pieChart);

        rvBarChart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBarChart.setNestedScrollingEnabled(false);

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
        db.collection("Statistic")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(statisticSnapshots -> {
                    long totalTasks = 0;
                    long completedTasks = 0;

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

                    long uncompletedTasks = Math.max(0, totalTasks - completedTasks);
                    int progressPercent = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;

                    tvTotalTasks.setText(String.valueOf(totalTasks));
                    tvTotalCompleted.setText(String.valueOf(completedTasks));
                    tvTotalUncompleted.setText(String.valueOf(uncompletedTasks));
                    tvCompleted.setText(String.valueOf(completedTasks));
                    tvUncompletedDetail.setText(String.valueOf(uncompletedTasks));
                    tvProgressMessage.setText("Bạn đã thực hiện " + progressPercent + "% thói quen của mình!");

                    pieChart.setPercent(progressPercent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải Statistic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBarChartData(String userId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        String endDate = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        String startDate = dateFormat.format(calendar.getTime());

        db.collection("Habit")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(habitSnapshots -> {
                    List<BarChartData> barChartDataList = new ArrayList<>();
                    Map<String, String> habitNames = new HashMap<>();
                    Set<String> habitIds = new HashSet<>();

                    for (QueryDocumentSnapshot document : habitSnapshots) {
                        String habitId = document.getId();
                        String name = document.getString("name");
                        habitIds.add(habitId);
                        habitNames.put(habitId, name != null ? name : "No Data");
                        Log.d(TAG, "Habit - ID: " + habitId + ", Name: " + name);
                    }

                    // Truy vấn đơn giản hơn, chỉ lọc user_id và completed
                    db.collection("HabitLog")
                            .whereEqualTo("user_id", userId)
                            .whereEqualTo("completed", true)
                            .get()
                            .addOnSuccessListener(logSnapshots -> {
                                Map<String, Set<String>> completedDaysMap = new HashMap<>();

                                // Lọc thủ công các tài liệu có date trong phạm vi
                                for (QueryDocumentSnapshot logDoc : logSnapshots) {
                                    String habitId = logDoc.getString("habit_id");
                                    String dateStr = logDoc.getString("date");

                                    if (habitId != null && habitIds.contains(habitId) && dateStr != null && dateStr.matches("\\d{8}")) {
                                        // So sánh thủ công với startDate và endDate
                                        if (dateStr.compareTo(startDate) >= 0 && dateStr.compareTo(endDate) <= 0) {
                                            completedDaysMap.putIfAbsent(habitId, new HashSet<>());
                                            completedDaysMap.get(habitId).add(dateStr);
                                        }
                                    } else {
                                        Log.w(TAG, "Invalid data - habitId: " + habitId + ", date: " + dateStr);
                                    }
                                }

                                for (String habitId : habitIds) {
                                    int completedDays = completedDaysMap.getOrDefault(habitId, new HashSet<>()).size();
                                    double successPercent = (completedDays * 100.0) / 5; // Chia cho 5 ngày
                                    String habitName = habitNames.getOrDefault(habitId, "No Habit");
                                    barChartDataList.add(new BarChartData(habitId, habitName, successPercent));
                                    Log.d(TAG, "BarChartData - ID: " + habitId +
                                            ", Name: " + habitName +
                                            ", CompletedDays: " + completedDays +
                                            ", SuccessPercent: " + successPercent);
                                }

                                while (barChartDataList.size() < 5) {
                                    barChartDataList.add(new BarChartData("", "No Habit", 0.0));
                                    Log.d(TAG, "Added empty column, size: " + barChartDataList.size());
                                }

                                BarChartAdapter barChartAdapter = new BarChartAdapter(barChartDataList);
                                rvBarChart.setAdapter(barChartAdapter);
                                rvBarChart.post(() -> barChartAdapter.notifyDataSetChanged());
                                Log.d(TAG, "Adapter set with " + barChartDataList.size() + " items");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading HabitLog: " + e.getMessage());
                                Toast.makeText(this, "Lỗi tải dữ liệu HabitLog: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                List<BarChartData> fallbackList = new ArrayList<>();
                                for (int i = 0; i < 5; i++) {
                                    fallbackList.add(new BarChartData("", "No Habit", 0.0));
                                }
                                BarChartAdapter barChartAdapter = new BarChartAdapter(fallbackList);
                                rvBarChart.setAdapter(barChartAdapter);
                                rvBarChart.post(() -> barChartAdapter.notifyDataSetChanged());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Habits: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải danh sách thói quen: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    List<BarChartData> fallbackList = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        fallbackList.add(new BarChartData("", "No Habit", 0.0));
                    }
                    BarChartAdapter barChartAdapter = new BarChartAdapter(fallbackList);
                    rvBarChart.setAdapter(barChartAdapter);
                    rvBarChart.post(() -> barChartAdapter.notifyDataSetChanged());
                });
    }
}