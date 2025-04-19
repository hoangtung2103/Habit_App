package com.thtung.habit_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.thtung.habit_app.R;
import com.thtung.habit_app.model.RewardMilestone;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RewardMilestoneAdapter extends RecyclerView.Adapter<RewardMilestoneAdapter.RewardMilestoneViewHolder>{
    private List<RewardMilestone> milestoneList = new ArrayList<>();
    private int currentPerfectDayStreak = 0; // Lưu streak hiện tại
    private Context context;

    public RewardMilestoneAdapter(Context context) {
        this.context = context;
    }

    // Phương thức cập nhật dữ liệu mới
    public void updateData(List<RewardMilestone> newMilestoneList, int currentStreak) {
        this.milestoneList = newMilestoneList != null ? newMilestoneList : new ArrayList<>();
        this.currentPerfectDayStreak = currentStreak;
        notifyDataSetChanged(); // Thông báo thay đổi toàn bộ danh sách
        // Nếu muốn hiệu ứng tốt hơn, dùng DiffUtil
    }

    @NonNull
    @Override
    public RewardMilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false); // Sử dụng layout item của bạn
        return new RewardMilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardMilestoneViewHolder holder, int position) {
        RewardMilestone milestone = milestoneList.get(position);
        holder.bind(milestone, currentPerfectDayStreak); // Truyền cả milestone và streak
    }

    @Override
    public int getItemCount() {
        return milestoneList.size();
    }

    // --- ViewHolder ---
    public class RewardMilestoneViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView text;
        private ProgressBar progressBar;
        private TextView progressText;
        private ImageView checkmarkIcon; // Optional: Icon đánh dấu đã hoàn thành

        public RewardMilestoneViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.badge_icon);
            text = itemView.findViewById(R.id.badge_text);
            progressBar = itemView.findViewById(R.id.badge_progress_bar);
            progressText = itemView.findViewById(R.id.badge_progress_text);
//            checkmarkIcon = itemView.findViewById(R.id.badge_checkmark); // Optional: Tìm ID checkmark
        }

        public void bind(RewardMilestone milestone, int currentStreak) {
            text.setText(milestone.getReward_name()); // Hiển thị tên milestone

            int requiredDays = milestone.getRequired_streak_days();
            int progressPercent = 0;

            boolean isAchieved = requiredDays > 0 && currentStreak >= requiredDays;

            if (requiredDays > 0) {
                progressPercent = Math.min(100, (int) (((double) currentStreak / requiredDays) * 100));
            } else {
                // Xử lý trường hợp requiredDays = 0 hoặc âm (nếu có)
                progressPercent = isAchieved ? 100 : 0;
            }

            progressBar.setProgress(progressPercent);

            // Hiển thị text progress hoặc trạng thái "Đã đạt"
            if (isAchieved) {
                progressBar.setProgress(100); // Đảm bảo đầy thanh
                progressText.setText("Đã đạt!");
                progressText.setTextColor(ContextCompat.getColor(context, R.color.white)); // Ví dụ đổi màu
                if (checkmarkIcon != null) {
                    checkmarkIcon.setVisibility(View.VISIBLE);
                }
                itemView.setAlpha(1.0f);
            } else {
                progressText.setText(String.format(Locale.getDefault(), "%d%%", progressPercent));
                progressText.setTextColor(ContextCompat.getColor(context, R.color.white)); // Màu mặc định
                if (checkmarkIcon != null) {
                    checkmarkIcon.setVisibility(View.GONE);
                }
                itemView.setAlpha(0.8f); // Làm mờ nhẹ mục chưa đạt
            }


            // Hiển thị icon (dùng Glide/Picasso)
            if (milestone.getIconUrl() != null && !milestone.getIconUrl().isEmpty()) {
                Glide.with(context)
                        .load(milestone.getIconUrl())
//                        .placeholder(R.drawable.ic_placeholder_badge) // Ảnh placeholder
//                        .error(R.drawable.ic_error_badge)       // Ảnh khi lỗi
                        .into(icon);
            } else {
                // Nếu không có URL, hiển thị icon mặc định
                icon.setImageResource(R.drawable.bronze_medal);
            }
        }
    }
}
