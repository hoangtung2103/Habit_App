package com.thtung.habit_app.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thtung.habit_app.R;
import com.thtung.habit_app.ViewModel.StreakViewModel;
import com.thtung.habit_app.adapters.HabitAdapter;
import com.thtung.habit_app.adapters.WeekAdapter;
import com.thtung.habit_app.databinding.ActivityMainBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.DayModel;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.model.UserPoint;
import com.thtung.habit_app.repository.UserPointRepository;
import com.thtung.habit_app.utils.CloudinaryManager;
import com.thtung.habit_app.utils.StatisticUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements HabitAdapter.OnHabitInteractionListener{
    private ActivityMainBinding binding;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private WeekAdapter adapterWeek;
    private List<DayModel> dayList;
    private UserPointRepository userPointRepository;
    private String currentUserId;
    private StreakViewModel streakViewModel;

    private HabitAdapter habitAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreManager = new FirestoreManager();
        CloudinaryManager.initCloudinary(this);
        userPointRepository = userPointRepository.getInstance();
        if(user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            setContentView(binding.getRoot());
            currentUserId = user.getUid();
            getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("userId", user.getUid())
                    .apply();
        }


        if (currentUserId != null && !currentUserId.isEmpty()) {
            initializeViewModel(currentUserId);
        } else {
            Log.e(TAG, "Error: currentUserId is null or empty after auth check.");
            Toast.makeText(this, "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Cập nhật last_completed và streak
        StatisticUtil.checkAndUpdateStatisticsOncePerDay(MainActivity.this, user.getUid());


        binding.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        //Hiển thị Avatar và tên người dùng
        firestoreManager.getUser(user.getUid(), new FirestoreManager.UserCallback() {
            @Override
            public void onUserLoaded(User nguoiDung) {
                Glide.with(MainActivity.this)
                        .load(nguoiDung.getAvatar())
                        .placeholder(R.drawable.avt_macdinh)
                        .error(R.drawable.avt_macdinh)
                        .into(binding.avatar);
                binding.username.setText(nguoiDung.getName());
            }

            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi :" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        userPointRepository.getUserPoint(user.getUid(), new FirestoreManager.UserPointCallback() {
            @Override
            public void onUserPointLoaded(UserPoint userPoint) {
                String pointsString = String.valueOf(userPoint.getTotal_point());
                binding.point.setText(pointsString);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi : "  + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Chuyển sang trang AddHabit
        binding.icAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddHabitActivity.class));
            }
        });

        // Sang trang huy hiệu
        binding.imgIcRank.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BadgeActivity.class));
            finish();
        });

        //Test Đăng xuaats
        binding.btnCaidat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });


        //Hiển thị lịch 1 tuần
        binding.weekRecyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // 7 cột cố định
        initWeekData();
        adapterWeek = new WeekAdapter(dayList);
        binding.weekRecyclerView.setAdapter(adapterWeek);


        // Hiển thị danh sách thói quen
        setupHabitRecyclerView();
        loadHabits(currentUserId);


    }

    private void initWeekData() {
        dayList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("vi")); // Locale Việt Nam
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) { // Đảm bảo đủ 7 ngày
            String dayName = dayFormat.format(calendar.getTime());
            String dayNumber = dateFormat.format(calendar.getTime());
            dayList.add(new DayModel(dayName, dayNumber, i == 0));
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Di chuyển đúng từng ngày
        }
    }

    private void setupHabitRecyclerView() {
        // Tạo adapter với listener là Activity này, và truyền FirestoreManager
        // UserId ban đầu có thể là null, sẽ được cập nhật khi loadHabits
        habitAdapter = new HabitAdapter(this, new ArrayList<>(), currentUserId, firestoreManager, this);
        binding.recyclerViewHaBit.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHaBit.setAdapter(habitAdapter);
        binding.progressBarHabit.setVisibility(View.VISIBLE); // Hiển thị loading ban đầu
    }

    private void loadHabits(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot load habits, userId is invalid.");
            binding.progressBarHabit.setVisibility(View.GONE);
            Toast.makeText(this, "Không thể tải thói quen.", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.progressBarHabit.setVisibility(View.VISIBLE);
        firestoreManager.getHabits(userId, new FirestoreManager.HabitListCallback() {
            @Override
            public void onHabitListLoaded(ArrayList<Habit> habitList) {
                // Cập nhật adapter với dữ liệu mới
                if (habitAdapter != null) {
                    // Nếu Adapter có hàm updateData:
                    // habitAdapter.updateData(habitList, userId);
                    // Nếu không, tạo adapter mới:
                    habitAdapter = new HabitAdapter(MainActivity.this, habitList, userId, firestoreManager, MainActivity.this);
                    binding.recyclerViewHaBit.setAdapter(habitAdapter);
                }
                binding.progressBarHabit.setVisibility(View.GONE);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi tải Habits: " + errorMessage, Toast.LENGTH_SHORT).show();
                binding.progressBarHabit.setVisibility(View.GONE);
            }
        });
    }

    // Khởi tạo ViewModel
    private void initializeViewModel(String userId) {
        Log.d(TAG, "Initializing StreakViewModel for userId: " + userId);
        streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);
        streakViewModel.init(userId, firestoreManager);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onHabitCheckedAndLogged(String habitId, String date) {
        Log.d(TAG, "Listener: Habit checked and logged - " + habitId + " on " + date);
        // Gọi hàm xử lý trong ViewModel
        if (streakViewModel != null) {
            streakViewModel.handleHabitLogged(habitId, date);
        } else {
            Log.e(TAG, "StreakViewModel is null when trying to handle habit log.");
        }
    }

    @Override
    public void onHabitCheckFailed(String habitId, String errorMessage) {

    }
}