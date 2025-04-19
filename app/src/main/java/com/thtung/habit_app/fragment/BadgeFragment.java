package com.thtung.habit_app.fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.RewardMilestoneAdapter;
import com.thtung.habit_app.adapters.UserMilestoneAdapter;
import com.thtung.habit_app.model.RewardMilestone;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.model.UserStreak;
import com.thtung.habit_app.utils.DateTimeUtils;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class BadgeFragment extends Fragment {
    private static final String TAG = "BadgeFragment";
    private static final String PERFECT_DAY_STREAK_TYPE = "PERFECT_DAY";
    private RecyclerView recyclerViewBadges;
    private UserMilestoneAdapter userMilestoneAdapter;
    private RewardMilestoneAdapter rewardMilestoneAdapter;
    private TextView textBadgeTitle; // Optional: Nếu muốn thay đổi tiêu đề
    private ProgressBar loadingIndicator; // Thêm ProgressBar
    private FirebaseFirestore db;
    private User user;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    private DateTimeUtils dateTimeUtils;

    public BadgeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
          currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
       }
        dateTimeUtils = new DateTimeUtils();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_badge, container, false); // Sử dụng layout mới

        recyclerViewBadges = view.findViewById(R.id.recyclerViewBadges);
        textBadgeTitle = view.findViewById(R.id.textBadgeTitle); // Optional
        loadingIndicator = view.findViewById(R.id.badgeLoadingIndicator);
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Tải dữ liệu từ Firestore khi view đã sẵn sàng
        loadBadgeDataFromFirestore();
    }


    private void setupRecyclerView() {
        // Khởi tạo adapter mới với context
        // Danh sách ban đầu rỗng, streak ban đầu là 0, sẽ được cập nhật sau
        rewardMilestoneAdapter = new RewardMilestoneAdapter(getContext());
        recyclerViewBadges.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBadges.setAdapter(rewardMilestoneAdapter);
        // Adapter sẽ tự hiển thị 0% cho đến khi có dữ liệu mới
    }

    private void loadBadgeDataFromFirestore() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Current User ID is missing. Cannot load badge data.");
            Toast.makeText(getContext(), "Lỗi: Không xác định được người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE); // Hiển thị loading

        // Task 1: Lấy tất cả RewardMilestones
        Task<QuerySnapshot> milestonesTask = db.collection("RewardMilestone")
                .orderBy("required_streak_days", Query.Direction.ASCENDING)
                .get();

        // Task 2: Lấy UserStreak PERFECT_DAY của user
        Task<QuerySnapshot> streakTask = db.collection("UserStreak")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("streakType", PERFECT_DAY_STREAK_TYPE)
                .limit(1)
                .get();

        // Gộp các task
        Task<List<Object>> allTasks = Tasks.whenAllSuccess(milestonesTask, streakTask);

        allTasks.addOnSuccessListener(results -> {
            if (!isAdded()) return; // Kiểm tra Fragment còn được gắn vào Activity không

            try {
                // Xử lý kết quả milestones
                List<RewardMilestone> milestones = new ArrayList<>();
                QuerySnapshot milestonesSnapshot = (QuerySnapshot) results.get(0);
                if (milestonesSnapshot != null) {
                    milestones = milestonesSnapshot.toObjects(RewardMilestone.class);
                }

                // Xử lý kết quả streak
                UserStreak currentStreakObj = null;
                QuerySnapshot streakSnapshot = (QuerySnapshot) results.get(1);
                if (streakSnapshot != null && !streakSnapshot.isEmpty()) {
                    currentStreakObj = streakSnapshot.getDocuments().get(0).toObject(UserStreak.class);
                }

                // Kiểm tra và lấy giá trị streak hợp lệ
                int validStreak = checkAndGetValidStreak(currentStreakObj);

                // Cập nhật Adapter với dữ liệu mới
                if (rewardMilestoneAdapter != null) {
                    rewardMilestoneAdapter.updateData(milestones, validStreak);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error processing badge data results", e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi xử lý dữ liệu huy hiệu.", Toast.LENGTH_SHORT).show();
                }
            } finally {
                loadingIndicator.setVisibility(View.GONE); // Ẩn loading dù thành công hay lỗi
            }

        }).addOnFailureListener(e -> {
            if (!isAdded()) return; // Kiểm tra Fragment còn được gắn vào Activity không
            Log.e(TAG, "Failed to load badge data", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Không thể tải dữ liệu huy hiệu.", Toast.LENGTH_SHORT).show();
            }
            loadingIndicator.setVisibility(View.GONE); // Ẩn loading khi có lỗi
        });
    }

    // kiểm tra streak
    private int checkAndGetValidStreak(@Nullable UserStreak streak) {
        if (streak == null || streak.getLastCompletionDate() == null || streak.getCurrentStreak() == 0) {
            return 0;
        }
        LocalDate lastCompletionLocalDate = dateTimeUtils.timestampToLocalDate(streak.getLastCompletionDate());
        if (lastCompletionLocalDate == null) return 0;

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate yesterday = today.minusDays(1);

        if (!lastCompletionLocalDate.isEqual(today) && !lastCompletionLocalDate.isEqual(yesterday)) {
            return 0; // Streak đã bị reset
        }
        return streak.getCurrentStreak(); // Streak hợp lệ
    }

}
