package com.thtung.habit_app.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.thtung.habit_app.R;
import com.thtung.habit_app.model.BarChartData;
import java.util.List;
import java.util.Locale;

public class BarChartAdapter extends RecyclerView.Adapter<BarChartAdapter.BarChartViewHolder> {
    private static final String TAG = "BarChartAdapter";
    private final List<BarChartData> barChartDataList;

    public BarChartAdapter(List<BarChartData> barChartDataList) {
        this.barChartDataList = barChartDataList;
    }

    @NonNull
    @Override
    public BarChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bar_chart, parent, false);
        return new BarChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarChartViewHolder holder, int position) {
        BarChartData data = position < barChartDataList.size()
                ? barChartDataList.get(position)
                : new BarChartData("", "No Data", 0.0);

        // Hiển thị phần trăm
        holder.tvBarPercent.setText(String.format(Locale.getDefault(), "%.0f%%", data.getSuccessPercent()));
        holder.tvBarPercent.setVisibility(View.VISIBLE);

        // Hiển thị tên thói quen
        holder.tvHabitName.setText(data.getHabitName() == null || data.getHabitName().isEmpty()
                ? "No Habit" : data.getHabitName());
        holder.tvHabitName.setVisibility(View.VISIBLE);

        // Điều chỉnh chiều cao và tạo gradient cho cột
        ViewGroup.LayoutParams params = holder.barView.getLayoutParams();
        Context context = holder.itemView.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        int maxHeight = (int) (250 * density); // Chiều cao tối đa 250dp
        params.height = data.getSuccessPercent() <= 0
                ? (int) (15 * density) // Chiều cao tối thiểu 15dp
                : (int) ((data.getSuccessPercent() / 100.0) * maxHeight);
        holder.barView.setLayoutParams(params);
        holder.barView.setVisibility(View.VISIBLE);

        // Tạo gradient và bo tròn góc cho cột
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(10 * density);
        int startColor, endColor;

        if (data.getSuccessPercent() < 30) {
            startColor = ContextCompat.getColor(context, R.color.red_light);
            endColor = ContextCompat.getColor(context, R.color.red_dark);
        } else if (data.getSuccessPercent() < 70) {
            startColor = ContextCompat.getColor(context, R.color.yellow_light);
            endColor = ContextCompat.getColor(context, R.color.yellow_dark);
        } else {
            startColor = ContextCompat.getColor(context, R.color.green_light);
            endColor = ContextCompat.getColor(context, R.color.green_dark);
        }
        gradientDrawable.setColors(new int[] {startColor, endColor});
        gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        holder.barView.setBackground(gradientDrawable);
    }

    @Override
    public int getItemCount() {
        return 5; // Luôn hiển thị 5 cột
    }

    public static class BarChartViewHolder extends RecyclerView.ViewHolder {
        TextView tvBarPercent, tvHabitName;
        View barView;

        public BarChartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarPercent = itemView.findViewById(R.id.tv_bar_percent);
            tvHabitName = itemView.findViewById(R.id.tv_habit_name);
            barView = itemView.findViewById(R.id.bar_view);
        }
    }
}