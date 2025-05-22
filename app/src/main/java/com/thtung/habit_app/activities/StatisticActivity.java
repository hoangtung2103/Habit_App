package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.BarChartAdapter;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.BarChartData;
import com.thtung.habit_app.model.HabitLog;
import com.thtung.habit_app.model.Statistic;
import com.thtung.habit_app.utils.ProgressCircleView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirestoreManager firestoreManager;
    private static final String TAG = "StatisticActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView tvTotalTasks, tvTotalCompleted, tvTotalUncompleted;
    private TextView tvCurStreak, tvPoint;
    private RecyclerView rvBarChart;
    private ProgressCircleView pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        firestoreManager = new FirestoreManager();

        tvTotalTasks = findViewById(R.id.tv_total_tasks);
        tvTotalCompleted = findViewById(R.id.tv_total_completed);
        tvTotalUncompleted = findViewById(R.id.tv_total_uncompleted);

        tvCurStreak = findViewById(R.id.txt_curstreak);
        tvPoint = findViewById(R.id.txt_point);

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
        // Truy vấn từ Statistic
        Task<QuerySnapshot> statisticTask = db.collection("Statistic")
                .whereEqualTo("user_id", userId)
                .get();

        // Truy vấn từ UserStreak
        Task<QuerySnapshot> userStreakTask = db.collection("UserStreak")
                .whereEqualTo("user_id", userId)
                .limit(1)
                .get();

        // Truy vấn từ UserPoint
        Task<QuerySnapshot> userPointTask = db.collection("UserPoint")
                .whereEqualTo("user_id", userId)
                .limit(1)
                .get();

        // Chờ tất cả truy vấn hoàn thành
        Tasks.whenAllSuccess(statisticTask, userStreakTask, userPointTask)
                .addOnSuccessListener(results -> {
                    // Xử lý Statistic
                    QuerySnapshot statisticSnapshots = (QuerySnapshot) results.get(0);
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

                    // Xử lý UserStreak
                    QuerySnapshot userStreakSnapshots = (QuerySnapshot) results.get(1);
                    long currentStreak = 0;
                    if (!userStreakSnapshots.isEmpty()) {
                        QueryDocumentSnapshot streakDoc = (QueryDocumentSnapshot) userStreakSnapshots.getDocuments().get(0);
                        Object streakValue = streakDoc.get("currentStreak");
                        if (streakValue instanceof Long) {
                            currentStreak = (Long) streakValue;
                        } else if (streakValue instanceof Integer) {
                            currentStreak = ((Integer) streakValue).longValue();
                        } else {
                            Log.w(TAG, "currentStreak has unexpected type: " + (streakValue != null ? streakValue.getClass().getSimpleName() : "null"));
                        }
                    } else {
                        Log.d(TAG, "No UserStreak document found for user_id: " + userId);
                    }

                    // Xử lý UserPoint
                    QuerySnapshot userPointSnapshots = (QuerySnapshot) results.get(2);
                    long totalPoint = 0;
                    if (!userPointSnapshots.isEmpty()) {
                        QueryDocumentSnapshot pointDoc = (QueryDocumentSnapshot) userPointSnapshots.getDocuments().get(0);
                        Object pointValue = pointDoc.get("total_point");
                        if (pointValue instanceof Long) {
                            totalPoint = (Long) pointValue;
                        } else if (pointValue instanceof Integer) {
                            totalPoint = ((Integer) pointValue).longValue();
                        } else {
                            Log.w(TAG, "total_point has unexpected type: " + (pointValue != null ? pointValue.getClass().getSimpleName() : "null"));
                        }
                    } else {
                        Log.d(TAG, "No UserPoint document found for user_id: " + userId);
                    }

                    // Cập nhật giao diện
                    tvTotalTasks.setText(String.valueOf(totalTasks));
                    tvTotalCompleted.setText(String.valueOf(completedTasks));
                    tvTotalUncompleted.setText(String.valueOf(uncompletedTasks));

                    tvCurStreak.setText(String.valueOf(currentStreak));
                    tvPoint.setText(String.valueOf(totalPoint));

                    pieChart.setPercent(progressPercent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải Statistic: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setDefaultStatistics();
                });
    }

    private void setDefaultStatistics() {
        tvTotalTasks.setText("0");
        tvTotalCompleted.setText("0");
        tvTotalUncompleted.setText("0");

        tvCurStreak.setText("0");
        tvPoint.setText("0");

        pieChart.setPercent(0);
    }

    private void loadBarChartData(String userId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM");
        Calendar calendar = Calendar.getInstance();
        List<String> dayLabels = new ArrayList<>();
        Map<String, Integer> dailyCompletedCount = new HashMap<>();

        // Lấy 5 ngày gần nhất
        for (int i = 4; i >= 0; i--) {
            Calendar dayCalendar = (Calendar) calendar.clone();
            dayCalendar.add(Calendar.DAY_OF_MONTH, -i);
            String day = dateFormat.format(dayCalendar.getTime());
            dayLabels.add(day);
            dailyCompletedCount.put(day, 0);
        }

        firestoreManager.getHabitsTask(userId).addOnSuccessListener(habitSnapshots -> {
            final int totalHabits = habitSnapshots.size();
            Log.d(TAG, "Total Habits: " + totalHabits);

            if (totalHabits == 0) {
                setDefaultBarChart();
                return;
            }

            List<Task<QuerySnapshot>> logTasks = new ArrayList<>();
            for (String day : dayLabels) {
                logTasks.add(firestoreManager.getTodaysCompletionsTask(userId, day));
            }

            Tasks.whenAllSuccess(logTasks).addOnSuccessListener(results -> {
                List<BarChartData> barChartDataList = new ArrayList<>();

                for (int i = 0; i < results.size(); i++) {
                    QuerySnapshot logSnapshot = (QuerySnapshot) results.get(i);
                    String day = dayLabels.get(i);
                    int completedCount = 0;

                    for (QueryDocumentSnapshot doc : logSnapshot) {
                        HabitLog log = doc.toObject(HabitLog.class);
                        if (log != null && log.isCompleted()) {
                            completedCount++;
                        }
                    }
                    dailyCompletedCount.put(day, completedCount);

                    double successPercent = (totalHabits > 0) ? (completedCount * 100.0) / totalHabits : 0.0;
                    String dayLabel;
                    try {
                        dayLabel = displayFormat.format(dateFormat.parse(day));
                    } catch (ParseException e) {
                        Log.e(TAG, "ParseException for day " + day + ": " + e.getMessage());
                        dayLabel = day;
                    }
                    barChartDataList.add(new BarChartData(day, dayLabel, successPercent));
                }

                // Đảm bảo luôn có 5 cột
                while (barChartDataList.size() < 5) {
                    String day = dayLabels.get(barChartDataList.size());
                    String dayLabel;
                    try {
                        dayLabel = displayFormat.format(dateFormat.parse(day));
                    } catch (ParseException e) {
                        Log.e(TAG, "ParseException for fallback day " + day + ": " + e.getMessage());
                        dayLabel = day;
                    }
                    barChartDataList.add(new BarChartData(day, dayLabel, 0.0));
                }

                BarChartAdapter barChartAdapter = new BarChartAdapter(barChartDataList);
                rvBarChart.setAdapter(barChartAdapter);
                rvBarChart.post(() -> barChartAdapter.notifyDataSetChanged());
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải dữ liệu HabitLog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                setDefaultBarChart();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải danh sách thói quen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            setDefaultBarChart();
        });
    }

    private void setDefaultBarChart() {
        List<BarChartData> fallbackList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM");
        Calendar calendar = Calendar.getInstance();
        for (int i = 4; i >= 0; i--) {
            Calendar dayCalendar = (Calendar) calendar.clone();
            dayCalendar.add(Calendar.DAY_OF_MONTH, -i);
            String day = dateFormat.format(dayCalendar.getTime());
            String dayLabel;
            try {
                dayLabel = displayFormat.format(dateFormat.parse(day));
            } catch (ParseException e) {
                Log.e(TAG, "ParseException for default day " + day + ": " + e.getMessage());
                dayLabel = day;
            }
            fallbackList.add(new BarChartData(day, dayLabel, 0.0));
        }
        BarChartAdapter barChartAdapter = new BarChartAdapter(fallbackList);
        rvBarChart.setAdapter(barChartAdapter);
        rvBarChart.post(() -> barChartAdapter.notifyDataSetChanged());
    }
}