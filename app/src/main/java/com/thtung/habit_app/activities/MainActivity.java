package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.HabitAdapter;
import com.thtung.habit_app.adapters.WeekAdapter;
import com.thtung.habit_app.databinding.ActivityMainBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.DayModel;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.utils.CloudinaryManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private WeekAdapter adapterWeek;
    private List<DayModel> dayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestoreManager = new FirestoreManager();
        CloudinaryManager.initCloudinary(this);
        if(user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            setContentView(binding.getRoot());
        }

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

        // Chuyển sang trang AddHabit
        binding.icAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddHabitActivity.class));
            }
        });

//        binding.button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                finish();
//            }
//        });
        //Hiển thị lịch 1 tuần
        binding.weekRecyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // 7 cột cố định
        initWeekData();
        adapterWeek = new WeekAdapter(dayList);
        binding.weekRecyclerView.setAdapter(adapterWeek);


        // Hiển thị danh sách thói quen
        firestoreManager.getHabits(user.getUid(), new FirestoreManager.HabitListCallback() {
            @Override
            public void onHabitListLoaded(ArrayList<Habit> habitList) {
                binding.recyclerViewHaBit.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                HabitAdapter habitAdapter = new HabitAdapter(MainActivity.this, habitList, user.getUid());
                binding.recyclerViewHaBit.setAdapter(habitAdapter);
                binding.progressBarHabit.setVisibility(View.GONE);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi :" + errorMessage, Toast.LENGTH_SHORT).show();
            }


        });


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


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

}