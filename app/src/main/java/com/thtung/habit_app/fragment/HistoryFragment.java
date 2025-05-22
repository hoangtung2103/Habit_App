package com.thtung.habit_app.fragment;

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


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.UserPointAdapter;
import com.thtung.habit_app.model.PointHistory;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";
    private RecyclerView recyclerViewHistory;
    private TextView textHistoryTitle;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String currentUserId;
    private UserPointAdapter adapter;

    public HistoryFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_history, container, false); // Sử dụng layout mới

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        textHistoryTitle = view.findViewById(R.id.textHistory); // Optional
        progressBar = view.findViewById(R.id.progressBarHistory);
        setupRecyclerView(); // Thiết lập RecyclerView

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Tải dữ liệu từ Firestore khi view đã sẵn sàng
        loadHistoryDataFromFirestore();
    }

    private void setupRecyclerView() {
        // Thiết lập LayoutManager cho RecyclerView
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter
        adapter = new UserPointAdapter(getContext(), new ArrayList<>());
        recyclerViewHistory.setAdapter(adapter); // Gán adapter cho RecyclerView
    }

    private void loadHistoryDataFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewHistory.setVisibility(View.GONE);

        db.collection("PointHistory")
                .whereEqualTo("user_id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PointHistory> pointHistoryList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        PointHistory history = doc.toObject(PointHistory.class);
                        pointHistoryList.add(history);
                    }

                    // Sắp xếp danh sách theo ngày giảm dần
                    pointHistoryList.sort((a, b) -> b.getCreate_at().compareTo(a.getCreate_at()));

                    // Cập nhật adapter
                    adapter = new UserPointAdapter(getContext(), pointHistoryList);
                    recyclerViewHistory.setAdapter(adapter);

                    progressBar.setVisibility(View.GONE);
                    recyclerViewHistory.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

}
