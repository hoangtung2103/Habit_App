package com.thtung.habit_app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.thtung.habit_app.R;
import com.thtung.habit_app.databinding.ActivityHabitDetailBinding;
import com.thtung.habit_app.databinding.CalendarDayViewBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.HabitAdviceModel;
import com.thtung.habit_app.model.HabitLog;
import com.thtung.habit_app.model.Statistic;
import com.thtung.habit_app.utils.ProgressCircleView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HabitDetailActivity extends AppCompatActivity {

    private ActivityHabitDetailBinding binding;
    private String habitId;
    private String habitName;
    private String userId;
    private Set<LocalDate> completedDates;
    private YearMonth currentViewedMonth;
    private HabitAdviceModel adviceModel;
    private int phanTram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        FirestoreManager firestoreManager = new FirestoreManager();
        habitId = intent.getStringExtra("habitId");
        habitName = intent.getStringExtra("habitName");
        userId = intent.getStringExtra("userId");

        Log.d("HabitDetailActivity", "Received data - habitId: " + habitId + ", habitName: " + habitName + ", userId: " + userId);

        if (habitId == null || userId == null) {
            Log.e("HabitDetailActivity", "Invalid parameters - habitId: " + habitId + ", userId: " + userId);
            Toast.makeText(this, "Lỗi: Dữ liệu không hợp lệ từ MainActivity", Toast.LENGTH_LONG).show();
            binding.habitname.setText("Lỗi: Không thể tải dữ liệu");
            return;
        }

        binding.habitname.setText(habitName);
        binding.back.setOnClickListener(v -> {
            Intent backIntent = new Intent(HabitDetailActivity.this, MainActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(backIntent);
            finish();
        });

        binding.editHabit.setOnClickListener(v -> {
            Intent intent1 = new Intent(HabitDetailActivity.this, EditHabitActivity.class);
            intent1.putExtra("habitId", habitId);
            startActivity(intent1);
        });

        completedDates = new HashSet<>();
        firestoreManager.getDSHabitLog(habitId, new FirestoreManager.GetHabitLogCallback() {
            @Override
            public void onHabitLogLoaded(ArrayList<HabitLog> habitLog) {
                for (HabitLog log : habitLog) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate date = LocalDate.parse(log.getDate(), formatter);
                    completedDates.add(date);
                    Log.d("HabitDetailActivity", "Ngày hoàn thành: " + date);
                }
                setupCalendar();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HabitDetailActivity", "Error loading habit logs: " + errorMessage);
            }
        });

        firestoreManager.getStatistic(habitId, new FirestoreManager.GetStatisticCallback() {
            @Override
            public void onStatisticLoaded(Statistic statistic) {
                phanTram = (int) (((float) statistic.getTotal_completed() / statistic.getLast_completed()) * 100);
                binding.txtHoanthanh.setText(Long.toString(statistic.getTotal_completed()));
                binding.txtKhonghoanthanh.setText(Long.toString(statistic.getLast_completed() - statistic.getTotal_completed()));
                binding.txtStreak.setText(Long.toString(statistic.getStreak()));
                binding.txtTong.setText(Long.toString(statistic.getLast_completed()));

                ProgressCircleView progressCircleView = binding.pieChart;
                Log.d("Phan Tram", String.valueOf(phanTram));
                progressCircleView.setPercent((float) phanTram);
                binding.goiY.setOnClickListener(v -> {
                    adviceModel = new HabitAdviceModel(HabitDetailActivity.this);
                    String advice = adviceModel.getAdvice(statistic);
                    showAdvicePopup(advice);
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HabitDetailActivity", "Error loading statistic: " + errorMessage);
            }
        });
    }





    private void showAdvicePopup(String advice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.popup_advice, null);
        builder.setView(dialogView);

        TextView tvAdvice = dialogView.findViewById(R.id.tvAdviceContent);
        tvAdvice.setText(advice);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }



    private void updateMonthYearText(YearMonth yearMonth) {
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        String year = String.valueOf(yearMonth.getYear());
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        binding.monthYearText.setText(monthName + ", " + year);
    }

    private void setupCalendar() {
        YearMonth currentMonth = YearMonth.now();
        currentViewedMonth = currentMonth;

        YearMonth startMonth = currentMonth.minusMonths(2);
        YearMonth endMonth = currentMonth.plusMonths(2);
        binding.calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY);
        binding.calendarView.scrollToMonth(currentMonth);

        updateMonthYearText(currentMonth);

        class DayViewContainer extends ViewContainer {
            private final CalendarDayViewBinding dayBinding;

            DayViewContainer(View view) {
                super(view);
                dayBinding = CalendarDayViewBinding.bind(view);
            }

            void bind(CalendarDay day) {
                LocalDate date = day.getDate();
                TextView dayText = dayBinding.dayText;
                dayText.setText(String.valueOf(date.getDayOfMonth()));
                dayText.setVisibility(View.VISIBLE);

                YearMonth monthOfDay = YearMonth.from(date);

                if (monthOfDay.equals(currentViewedMonth) && day.getPosition() == DayPosition.MonthDate) {
                    dayText.setAlpha(1f);
                    if (completedDates.contains(date)) {
                        dayText.setBackgroundResource(R.drawable.bg_day_completed);
                        dayText.setTextColor(Color.WHITE);
                    } else {
                        dayText.setBackgroundResource(R.drawable.bg_day_default);
                        dayText.setTextColor(Color.BLACK);
                    }
                } else {
                    dayText.setAlpha(0.3f);
                    dayText.setBackgroundResource(R.drawable.bg_day_default);
                    dayText.setTextColor(Color.BLACK);
                }

                dayBinding.getRoot().setOnClickListener(v -> {
                    Log.d("HabitDetailActivity", "Navigating to DailyNoteActivity - habitId: " + habitId + ", habitName: " + habitName + ", userId: " + userId + ", selectedDate: " + date);
                    if (habitId == null || userId == null) {
                        Log.e("HabitDetailActivity", "Cannot navigate: habitId or userId is null");
                        Toast.makeText(HabitDetailActivity.this, "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(HabitDetailActivity.this, DailyNoteActivity.class);
                    intent.putExtra("habitId", habitId);
                    intent.putExtra("habitName", habitName); // Thêm habitName vào Intent
                    intent.putExtra("userId", userId);
                    intent.putExtra("selectedDate", date);
                    startActivity(intent);
                });
            }
        }

        binding.calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.bind(day);
            }
        });

        binding.calendarView.setMonthScrollListener(month -> {
            currentViewedMonth = month.getYearMonth();
            updateMonthYearText(currentViewedMonth);
            binding.calendarView.notifyCalendarChanged();
            return null;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}