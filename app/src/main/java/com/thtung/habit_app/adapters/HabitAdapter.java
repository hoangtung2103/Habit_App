package com.thtung.habit_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thtung.habit_app.activities.HabitDetailActivity;
import com.thtung.habit_app.activities.HabitNoteActivity;
import com.thtung.habit_app.databinding.ItemHabitBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.Habit;
import com.thtung.habit_app.model.UserPoint;
import com.thtung.habit_app.repository.UserPointCallback;
import com.thtung.habit_app.repository.UserPointRepository;

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

    private OnHabitInteractionListener habitInteractionListener;
    private UserPointRepository userPointRepository;

    public interface OnHabitInteractionListener {
        void onHabitCheckedAndLogged(String habitId, String date);
        void onHabitCheckFailed(String habitId, String errorMessage);
    }

    public HabitAdapter(Context context, ArrayList<Habit> habitList, String userId, FirestoreManager manager, OnHabitInteractionListener listener) {
        this.context = context;
        this.originalHabitList = habitList;
        this.filteredHabitList = new ArrayList<>();
        this.firestoreManager = manager;
        this.userId = userId;
        this.habitInteractionListener = listener;
        this.userPointRepository = UserPointRepository.getInstance();
        filterHabits();
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

        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        firestoreManager.checkHabitLog(habit.getId(), userId, today, new FirestoreManager.CheckHabitLogCallback() {
            @Override
            public void onCheckCompleted(boolean completed) {
                holder.binding.checkBox.setOnCheckedChangeListener(null);
                holder.binding.checkBox.setChecked(completed);
                holder.binding.checkBox.setEnabled(!completed);

                holder.binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked && holder.binding.checkBox.isEnabled()) {
                        firestoreManager.saveHabitLog(habit.getId(), userId, today, true, new FirestoreManager.FirestoreWriteCallback() {
                            @Override
                            public void onSuccess() {
                                if (habitInteractionListener != null) {
                                    habitInteractionListener.onHabitCheckedAndLogged(habit.getId(), today);
                                }
                                Log.d("HabitAdapter", "saveHabitLog onSuccess for " + habit.getId());

                                userPointRepository.incrementPointPerCompletedHabitInDay(userId, habit.getId(), habit.getName(), new UserPointCallback() {
                                    @Override
                                    public void onUserPointLoaded(UserPoint userPoint) {}

                                    @Override
                                    public void onPointsAwarded() {}

                                    @Override
                                    public void onAlreadyAwardedToday() {}

                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(context, "Hoàn thành thói quen thành công, + 5 điểm!", Toast.LENGTH_SHORT).show();
                                        Log.d("HabitAdapter", "Points incremented successfully for " + habit.getName());
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e("HabitAdapter", "Failed to increment points for " + habit.getName() + ": " + message);
                                        Toast.makeText(context, "Lỗi cộng điểm!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {}
                        });
                        firestoreManager.updateStatisticAfterLog(context, habit.getId(), userId, today, habit.getRepeat());
                        holder.binding.checkBox.setEnabled(false);
                    } else if (!isChecked) {
                        holder.binding.checkBox.setChecked(true);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {}
        });

        holder.binding.getRoot().setOnClickListener(v -> {
            Log.d("HabitAdapter", "Opening HabitDetailActivity - habitId: " + habit.getId() + ", habitName: " + habit.getName() + ", userId: " + userId);
            if (userId == null || userId.isEmpty()) {
                Log.e("HabitAdapter", "Cannot open HabitDetailActivity: userId is null or empty");
                Toast.makeText(context, "Lỗi: Người dùng chưa được xác thực", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, HabitDetailActivity.class);
            intent.putExtra("habitId", habit.getId());
            intent.putExtra("habitName", habit.getName());
            intent.putExtra("userId", userId); // Thêm userId vào Intent
            context.startActivity(intent);
        });

        holder.binding.noteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, HabitNoteActivity.class);
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int startInt = Integer.parseInt(sdf.format(startDate));
        int todayInt = Integer.parseInt(sdf.format(todayDate));

        if (startInt > todayInt) return false;

        switch (repeatType) {
            case "Hàng ngày":
                return true;
            case "Hàng tuần":
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                Calendar todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                return startCal.get(Calendar.DAY_OF_WEEK) == todayCal.get(Calendar.DAY_OF_WEEK);
            case "Hàng tháng":
                startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                todayCal = Calendar.getInstance();
                todayCal.setTime(todayDate);
                return startCal.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH);
            default:
                return false;
        }
    }
}