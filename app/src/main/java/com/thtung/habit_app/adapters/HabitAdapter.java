package com.thtung.habit_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thtung.habit_app.activities.HabitDetailActivity;
import com.thtung.habit_app.databinding.ItemHabitBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.Habit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private Context context;
    private ArrayList<Habit> originalHabitList;
    private ArrayList<Habit> filteredHabitList;
    private FirestoreManager firestoreManager;
    private String userId;

    public HabitAdapter(Context context, ArrayList<Habit> habitList, String userId) {
        this.context = context;
        this.originalHabitList = habitList;
        this.filteredHabitList = new ArrayList<>();
        this.firestoreManager = new FirestoreManager();
        this.userId = userId;
        filterHabits(); // Lọc ra danh sách đúng ngày
    }

    private void filterHabits() {
        filteredHabitList.clear();
        for (Habit habit : originalHabitList) {
            if (shouldDisplayToday(habit)) {
                filteredHabitList.add(habit);
            }
        }
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitBinding binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = filteredHabitList.get(position);
        holder.binding.tenHabit.setText(habit.getName());
        holder.binding.mucTieu.setText(habit.getTarget());
        Glide.with(context).load(habit.getUrl_icon()).into(holder.binding.iconHabit);

        // Lấy ngày hiện tại dạng yyyyMMdd
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Kiểm tra trạng thái hoàn thành hôm nay
        firestoreManager.checkHabitLog(habit.getId(), userId, today, new FirestoreManager.CheckHabitLogCallback() {
            @Override
            public void onCheckCompleted(boolean completed) {
                holder.binding.checkBox.setOnCheckedChangeListener(null); // Xóa listener cũ tránh callback khi setChecked
                holder.binding.checkBox.setChecked(completed);
                holder.binding.checkBox.setEnabled(!completed);  // Nếu hoàn thành thì khóa checkbox

                holder.binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked && holder.binding.checkBox.isEnabled()) {
                        firestoreManager.saveHabitLog(context, habit.getId(), userId, today, true);
                        firestoreManager.updateStatisticAfterLog(context, habit.getId(), userId, today, habit.getRepeat());
                        holder.binding.checkBox.setEnabled(false);
                    } else if (!isChecked) {
                        holder.binding.checkBox.setChecked(true); // Không cho uncheck
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Xử lý lỗi nếu cần
            }
        });

        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, HabitDetailActivity.class);
            intent.putExtra("habitId", habit.getId());
            intent.putExtra("habitName", habit.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredHabitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        ItemHabitBinding binding;

        public HabitViewHolder(@NonNull ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private boolean shouldDisplayToday(Habit habit) {
        String repeatType = habit.getRepeat();
        Date startDate = habit.getStart_at().toDate();
        Date todayDate = new Date();

        // Định dạng yyyyMMdd để so sánh ngày chính xác, bỏ qua giờ phút giây
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int startInt = Integer.parseInt(sdf.format(startDate));
        int todayInt = Integer.parseInt(sdf.format(todayDate));

        // Nếu ngày bắt đầu > hôm nay => không hiển thị
        if (startInt > todayInt) return false;

        switch (repeatType) {
            case "Hàng ngày":
                return true;  // Nếu đã bắt đầu, ngày nào cũng hiển thị
            case "Hàng tuần": {
                // Tính số tuần chênh lệch, kiểm tra đúng ngày trong tuần
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                return startCal.get(Calendar.DAY_OF_WEEK) == todayCal.get(Calendar.DAY_OF_WEEK);
            }
            case "Hàng tháng": {
                // Kiểm tra cùng ngày trong tháng
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                return startCal.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH);
            }
            default:
                return false;
        }
    }

}
