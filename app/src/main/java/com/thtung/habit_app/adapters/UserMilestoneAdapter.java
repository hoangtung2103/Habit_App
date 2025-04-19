package com.thtung.habit_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Để lấy màu từ resources
import androidx.recyclerview.widget.RecyclerView;


import com.thtung.habit_app.R;
import com.thtung.habit_app.model.UserMilestone;

import java.util.List;
import java.util.Locale; // Để format string "%"


public class UserMilestoneAdapter extends RecyclerView.Adapter<UserMilestoneAdapter.UserMilestoneViewHolder>{

    private List<UserMilestone> badgeList;
    private Context context;

    public UserMilestoneAdapter(Context context, List<UserMilestone> badgeList) {
        this.context = context;
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public UserMilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new UserMilestoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserMilestoneViewHolder holder, int position) {
        UserMilestone userMilestone = badgeList.get(position);
        holder.bind(userMilestone);
    }

    @Override
    public int getItemCount() {
        return badgeList != null ? badgeList.size() : 0;
    }

    public class UserMilestoneViewHolder extends RecyclerView.ViewHolder{
        private ImageView icon;
        private TextView text;
        private ProgressBar progressBar;
        private TextView progressText;

        public UserMilestoneViewHolder(@NonNull View itemView){
            super(itemView);
            icon = itemView.findViewById(R.id.badge_icon);
            text = itemView.findViewById(R.id.badge_text);
            progressBar = itemView.findViewById(R.id.badge_progress_bar);
            progressText = itemView.findViewById(R.id.badge_progress_text);
        }

        public void bind(UserMilestone userMilestone) {
//            icon.setImageResource(userMilestone.getIconResId()); // Hoặc dùng Glide/Picasso
//            text.setText(userMilestone.getText());
            progressBar.setProgress(userMilestone.getCurrent());
            progressText.setText(String.format(Locale.getDefault(), "%d%%", userMilestone.getCurrent()));

            // Optional: Đổi màu text progress dựa trên giá trị
            // int colorResId = (badge.getProgress() == 100) ? R.color.some_completed_color : R.color.progress_text_color;
            // progressText.setTextColor(ContextCompat.getColor(context, colorResId));
        }

    }

    public void updateData(List<UserMilestone> newBadgeList) {
        this.badgeList = newBadgeList;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật lại toàn bộ list
    }


}
