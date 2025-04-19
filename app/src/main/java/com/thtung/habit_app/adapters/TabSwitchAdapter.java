package com.thtung.habit_app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.thtung.habit_app.fragment.BadgeFragment;
import com.thtung.habit_app.fragment.HistoryFragment;


public class TabSwitchAdapter extends FragmentStateAdapter {
    public TabSwitchAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new BadgeFragment();
        else return new HistoryFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
