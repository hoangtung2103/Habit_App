package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
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
import com.thtung.habit_app.adapters.StatisticAdapter;
import com.thtung.habit_app.model.BarChartData;
import com.thtung.habit_app.model.Statistic;

import java.util.ArrayList;
import java.util.List;

public class StatisticActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView tvTotalCompleted, tvCurrentStreak, tvUncompleted;
    private TextView tvProgressPercent, tvProgressMessage;
    private TextView tvEnergy, tvHeart;
    private RecyclerView rvHabitStats, rvBarChart;
    private ProgressBar progressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        tvTotalCompleted = findViewById(R.id.tv_total_completed);
        tvCurrentStreak = findViewById(R.id.tv_current_streak);
        tvUncompleted = findViewById(R.id.tv_uncompleted);
        rvHabitStats = findViewById(R.id.rv_habit_stats);
        rvBarChart = findViewById(R.id.rv_bar_chart);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        tvProgressMessage = findViewById(R.id.tv_progress_message);
        tvEnergy = findViewById(R.id.tv_energy);
        tvHeart = findViewById(R.id.tv_heart);
        progressCircle = findViewById(R.id.progress_circle);

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
        // Truy vấn tổng số nhiệm vụ từ Statistic
        db.collection("Statistic")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(statisticSnapshots -> {
                    List<Statistic> stats = new ArrayList<>();
                    long totalTasks = statisticSnapshots.size(); // Tổng số nhiệm vụ

                    for (QueryDocumentSnapshot document : statisticSnapshots) {
                        Statistic stat = document.toObject(Statistic.class);
                        stats.add(stat);
                    }

                    // Truy vấn số nhiệm vụ đã hoàn thành từ habit_log
                    db.collection("habit_log")
                            .whereEqualTo("user_id", userId)
                            .whereEqualTo("completed", true)
                            .get()
                            .addOnSuccessListener(habitLogSnapshots -> {
                                long completedTasks = habitLogSnapshots.size(); // Số nhiệm vụ đã hoàn thành
                                long uncompletedTasks = totalTasks - completedTasks; // Số chưa hoàn thành

                                // Tính phần trăm tiến độ tổng
                                int progressPercent = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;

                                // Cập nhật giao diện
                                tvTotalCompleted.setText(String.valueOf(totalTasks));
                                tvCurrentStreak.setText(String.valueOf(completedTasks));
                                tvUncompleted.setText(String.valueOf(uncompletedTasks));
                                tvProgressPercent.setText(progressPercent + "%");
                                tvProgressMessage.setText("Bạn đã hoàn thành " + progressPercent + " %");
                                progressCircle.setProgress(progressPercent);

                                // Truy vấn giá trị streak từ UserStreak
                                db.collection("UserStreak")
                                        .whereEqualTo("user_id", userId)
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener(userStreakSnapshots -> {
                                            long streak = 0;
                                            if (!userStreakSnapshots.isEmpty()) {
                                                Long streakValue = userStreakSnapshots.getDocuments().get(0).getLong("streak");
                                                streak = streakValue != null ? streakValue : 0;
                                            }
                                            tvEnergy.setText(String.valueOf(streak));
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Lỗi tải streak: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            tvEnergy.setText("0");
                                        });

                                // Giả lập giá trị tv_heart
                                tvHeart.setText("40");

                                StatisticAdapter adapter = new StatisticAdapter(stats);
                                rvHabitStats.setLayoutManager(new LinearLayoutManager(this));
                                rvHabitStats.setAdapter(adapter);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi tải habit_log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
                        String habitId = stat.getHabit_id();
                        long totalCompleted = stat.getTotal_completed() != null ? stat.getTotal_completed() : 0;
                        long lastCompleted = stat.getLast_completed() != null ? stat.getLast_completed() : 1;

                        double successPercent = (lastCompleted > 0) ? (totalCompleted * 100.0 / lastCompleted) : 0;
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