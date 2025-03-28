package com.thtung.habit_app.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thtung.habit_app.databinding.PopupLaplaiBinding;

public class PopupLapLai extends BottomSheetDialogFragment {
    private PopupLaplaiBinding binding;
    private OnRepeatOptionSelectedListener listener;

    public interface OnRepeatOptionSelectedListener {
        void onRepeatSelected(String repeatOption);
    }

    public PopupLapLai(OnRepeatOptionSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = PopupLaplaiBinding.inflate(inflater, container, false);

        binding.optionDaily.setOnClickListener(view -> {
            listener.onRepeatSelected("Hàng ngày");
            dismiss();
        });

        binding.optionWeekly.setOnClickListener(view -> {
            listener.onRepeatSelected("Hàng tuần");
            dismiss();
        });

        binding.optionMonthly.setOnClickListener(view -> {
            listener.onRepeatSelected("Hàng tháng");
            dismiss();
        });

        return binding.getRoot();
    }
}
