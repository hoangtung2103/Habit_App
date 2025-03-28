package com.thtung.habit_app.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thtung.habit_app.databinding.PopupMuctieuBinding;

public class PopupMucTieu extends BottomSheetDialogFragment {
    private PopupMuctieuBinding binding;
    private final TargetSelectedListener listener;

    public interface TargetSelectedListener {
        void onTargetSelected(String target);
    }

    public PopupMucTieu(TargetSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PopupMuctieuBinding.inflate(inflater, container, false);
        setupPickers();
        binding.btnDone.setOnClickListener(v -> {
            int value = binding.numberPickerValue.getValue();
            String unit = binding.numberPickerUnit.getDisplayedValues()[binding.numberPickerUnit.getValue()];
            String period = binding.numberPickerPeriod.getDisplayedValues()[binding.numberPickerPeriod.getValue()];
            String result = value + " " + unit + " " + period;
            listener.onTargetSelected(result);
            dismiss();
        });
        return binding.getRoot();
    }

    private void setupPickers() {
        binding.numberPickerValue.setMinValue(1);
        binding.numberPickerValue.setMaxValue(10);
        binding.numberPickerValue.setValue(1);

        String[] unitOptions = {"lần", "phút"};
        binding.numberPickerUnit.setMinValue(0);
        binding.numberPickerUnit.setMaxValue(unitOptions.length - 1);
        binding.numberPickerUnit.setDisplayedValues(unitOptions);

        String[] periodOptions = {"mỗi ngày", "mỗi tuần", "mỗi tháng"};
        binding.numberPickerPeriod.setMinValue(0);
        binding.numberPickerPeriod.setMaxValue(periodOptions.length - 1);
        binding.numberPickerPeriod.setDisplayedValues(periodOptions);
    }
}
