package com.thtung.habit_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.thtung.habit_app.R;
import com.thtung.habit_app.model.PointHistory;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserPointAdapter extends RecyclerView.Adapter<UserPointAdapter.UserPointViewHolder> {
    private List<PointHistory> historyList;
    private Context context;

    public UserPointAdapter(Context context, List<PointHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public UserPointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_point, parent, false);
        return new UserPointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPointViewHolder holder, int position) {
        PointHistory historyPoint = historyList.get(position);
        holder.bind(historyPoint);
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public class UserPointViewHolder extends RecyclerView.ViewHolder{
        private ImageView coin;
        private TextView text;
        private TextView time;
        private TextView point;

        public UserPointViewHolder(@NonNull View itemView){
            super(itemView);
            coin = itemView.findViewById(R.id.icon_coin);
            text = itemView.findViewById(R.id.reason_coin);
            time = itemView.findViewById(R.id.time_coin);
            point = itemView.findViewById(R.id.item_point);
        }

        public void bind(PointHistory historyPoint) {
            text.setText(historyPoint.getReason());
            point.setText("+" + String.valueOf(historyPoint.getPoint()));

            if (historyPoint.getCreate_at() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String date = sdf.format(historyPoint.getCreate_at().toDate());
                time.setText("Ngày: " + date);
            }
        }

        public void updateData(List<PointHistory> newHistoryList) {
            historyList.clear();
            historyList.addAll(newHistoryList);
            notifyDataSetChanged();
        }
    }
}
