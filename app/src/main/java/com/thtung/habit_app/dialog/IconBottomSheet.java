package com.thtung.habit_app.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thtung.habit_app.R;
import com.thtung.habit_app.adapters.IconAdapter;

import java.io.IOException;

public class IconBottomSheet extends BottomSheetDialogFragment {

    private OnIconSelectedListener listener;
    private String[] iconList;

    public IconBottomSheet(OnIconSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.icon_bottom_sheet, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewIcons);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        try {
            iconList = requireContext().getAssets().list("icons");
        } catch (IOException e) {
            e.printStackTrace();
        }

        IconAdapter adapter = new IconAdapter(requireContext(), iconList, assetPath -> {
            listener.onIconSelected(assetPath);
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    public interface OnIconSelectedListener {
        void onIconSelected(String assetPath);
    }
}
