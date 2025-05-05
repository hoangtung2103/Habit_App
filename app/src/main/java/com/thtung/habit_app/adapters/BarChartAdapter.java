package com.thtung.habit_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thtung.habit_app.R;
import com.thtung.habit_app.model.BarChartData;

import java.util.List;

public class BarChartAdapter extends RecyclerView.Adapter<BarChartAdapter.BarChartViewHolder> {
    private List<BarChartData> barChartDataList;

    public BarChartAdapter(List<BarChartData> barChartDataList) {
        this.barChartDataList = barChartDataList;
        if (this.barChartDataList.size() > 5) {
            this.barChartDataList = this.barChartDataList.subList(0, 5);
        }
    }

    @NonNull
    @Override
    public BarChartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bar_chart, parent, false);
        return new BarChartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarChartViewHolder holder, int position) {
        BarChartData data = barChartDataList.get(position);
        double percent = data.getSuccessPercent();
        holder.tvBarPercent.setText(String.format("%.0f%%", percent));

        // Điều chỉnh chiều cao của cột dựa trên phần trăm (tối đa 100%)
        ViewGroup.LayoutParams params = holder.barView.getLayoutParams();
        params.height = (int) (percent * 1.2); // Tỷ lệ chiều cao cột (1.2dp mỗi 1%)
        holder.barView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return barChartDataList.size();
    }

    public static class BarChartViewHolder extends RecyclerView.ViewHolder {
        TextView tvBarPercent;
        View barView;

        public BarChartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarPercent = itemView.findViewById(R.id.tv_bar_percent);
            barView = itemView.findViewById(R.id.bar_view);
        }
    }
}