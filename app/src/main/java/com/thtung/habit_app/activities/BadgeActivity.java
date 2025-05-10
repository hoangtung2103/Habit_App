package com.thtung.habit_app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.R;
import com.thtung.habit_app.ViewModel.RewardMilestoneViewModel;
import com.thtung.habit_app.ViewModel.StreakViewModel;
import com.thtung.habit_app.adapters.TabSwitchAdapter;
import com.thtung.habit_app.databinding.ActivityBadgeBinding;
import com.thtung.habit_app.databinding.DialogCreateMilestoneBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.RewardMilestone;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.repository.UserDataCallback;
import com.thtung.habit_app.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadgeActivity extends AppCompatActivity implements UserDataCallback {
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;
    private User user;
    private FirebaseFirestore firestoredb;
    private UserRepository userRepository;
    private ActivityBadgeBinding binding;
    public ViewPager2 viewPager;
    public TextView tabBadge, tabHistory;
    private StreakViewModel streakViewModel;
//    private ActivityResultLauncher<Intent> mGetContent;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean isViewModelInitialized = false;
    private String currentUserId;
    private FirestoreManager firestoreManager;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityBadgeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository = UserRepository.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        firestoreManager = new FirestoreManager();

        streakViewModel = new ViewModelProvider(this).get(StreakViewModel.class);
        if (!currentUserId.isEmpty()) {
            streakViewModel.init(currentUserId, firestoreManager); // Quan trọng!
//            streakViewModel.setProgressListener(this);
        } else {
            Log.e("BadgeActivity", "Cannot initialize ViewModel without userId after provider get");
            // Xử lý lỗi
            Toast.makeText(this, "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tabBadge = binding.tabBadge;
        tabHistory = binding.tabHistory;
        viewPager = binding.viewPager;

        viewPager.setAdapter(new TabSwitchAdapter((this)));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            public void onPageSelected(int position) {
            updateTabUI(position);
            }
        });

        tabBadge.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabHistory.setOnClickListener(v -> viewPager.setCurrentItem(1));

        binding.imgIcHome.setOnClickListener(v -> {
            startActivity(new Intent(BadgeActivity.this, MainActivity.class));
            finish();
        });

        binding.imgIcCaidat.setOnClickListener(v -> {
            startActivity(new Intent(BadgeActivity.this, SettingsActivity.class));
            finish();
        });

        binding.imgIcThongke.setOnClickListener(v -> {
            startActivity(new Intent(BadgeActivity.this, StatisticActivity.class));
            finish();
        });

        binding.icAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BadgeActivity.this, AddHabitActivity.class));
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        userRepository.setUserDataCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        userRepository.removeUserDataCallback();
    }

    @Override
    public void onUserLoaded(User user) {
        runOnUiThread(() -> {

            this.user = user;
            if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {

                this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                this.firestoredb = FirebaseFirestore.getInstance();
                binding.setUser(user);
            }
        });
    }

    @Override
    public void onUserNotFound() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            // Có thể reset binding hoặc hiển thị trạng thái "không tìm thấy"
            binding.setUser(null);
//            binding.avatarImage.setImageResource(R.drawable.default_avatar_placeholder);
            // binding.contentLayout.setVisibility(View.GONE);
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            binding.setUser(null);
//            binding.avatarImage.setImageResource(R.drawable.default_avatar_placeholder);
            // binding.contentLayout.setVisibility(View.GONE);
        });
    }

    @Override
    public void onLoadingStateChanged(boolean isLoading) {
        runOnUiThread(() -> {
//            showLoading(isLoading);
        });
    }

    @Override
    public void onAuthStateChanged(FirebaseUser firebaseUser) {
        runOnUiThread(() -> {
            if (firebaseUser == null) {
                if (!isFinishing() && !isDestroyed()) {
                    startActivity(new Intent(BadgeActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }

    private void updateTabUI(int position) {
        if (position == 0) {
            tabBadge.setBackgroundResource(R.drawable.tab_selected_background);
            tabBadge.setTextColor(Color.BLACK);
            tabBadge.setTypeface(null, Typeface.BOLD);

            tabHistory.setBackgroundResource(R.drawable.tab_unselected_background);
            tabHistory.setTextColor(Color.WHITE);
            tabHistory.setTypeface(null, Typeface.NORMAL);
        } else {
            tabHistory.setBackgroundResource(R.drawable.tab_selected_background);
            tabHistory.setTextColor(Color.BLACK);
            tabHistory.setTypeface(null, Typeface.BOLD);

            tabBadge.setBackgroundResource(R.drawable.tab_unselected_background);
            tabBadge.setTextColor(Color.WHITE);
            tabBadge.setTypeface(null, Typeface.NORMAL);
        }
    }

    // Hiển thị popup nhập mới Milestone
    private void showBottomSheetDialogCreateMilestone(){
        RewardMilestoneViewModel viewModel = new ViewModelProvider(this).get(RewardMilestoneViewModel.class);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(BadgeActivity.this);

        DialogCreateMilestoneBinding dialogCreateMilestoneBinding = DialogCreateMilestoneBinding.inflate(getLayoutInflater());

        bottomSheetDialog.setContentView(dialogCreateMilestoneBinding.getRoot());

        EditText milestoneName = dialogCreateMilestoneBinding.inputMilestoneName;
        EditText milestonePoint = dialogCreateMilestoneBinding.inputMilestonePoint;
        EditText milestoneDescription = dialogCreateMilestoneBinding.inputMilestoneDescription;
        EditText milestoneType = dialogCreateMilestoneBinding.inputMilestoneType;
        EditText milestoneRequiredStreaks = dialogCreateMilestoneBinding.inputRequiredStreakDay;

        Button buttonSubmit = dialogCreateMilestoneBinding.buttonSubmitCreateMilestone;

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String milestone_name = milestoneName.getText().toString();
                String milestone_point_text = milestonePoint.getText().toString();
                String milestone_description = milestoneDescription.getText().toString();
                String milestone_type = milestoneType.getText().toString();
                String milestone_requiredStreak_text = milestoneRequiredStreaks.getText().toString();

                if (milestone_name.isEmpty() || milestone_point_text.isEmpty()) {
                    Toast.makeText(BadgeActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                int milestone_point;
                int milestone_requiredStreak;
                try {
                    int point = Integer.parseInt(milestone_point_text);
                    int requiredStreak = Integer.parseInt(milestone_requiredStreak_text);
                    if(point <= 0 || requiredStreak <= 0){
                        Toast.makeText(BadgeActivity.this, "Điểm phải là số > 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    milestone_point = point;
                    milestone_requiredStreak = requiredStreak;
                } catch (NumberFormatException e) {
                    Toast.makeText(BadgeActivity.this, "Điểm phải là số", Toast.LENGTH_SHORT).show();
                    return;
                }

                RewardMilestone reward = new RewardMilestone();
                reward.setReward_name(milestone_name);
                reward.setDescription(milestone_description);
                reward.setPoint(milestone_point);
                reward.setType(milestone_type);
                reward.setRequired_streak_days(milestone_requiredStreak);

                viewModel.createRewardMilestone(reward, firestoredb);
                viewModel.getCreateSuccess().observe(BadgeActivity.this, isSuccess -> {
                    if (Boolean.TRUE.equals(isSuccess)) {
                        Toast.makeText(BadgeActivity.this, "Tạo mốc thưởng thành công", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                });

                viewModel.getErrorMessage().observe(BadgeActivity.this, error -> {
                    if (error != null) {
                        Toast.makeText(BadgeActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        bottomSheetDialog.show();
    }

}
