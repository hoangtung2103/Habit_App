package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.thtung.habit_app.R;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout accountRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ dòng "Tài khoản"
        accountRow = findViewById(R.id.accountRow);

        // Bắt sự kiện khi nhấn vào dòng "Tài khoản"
        accountRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // TODO: thêm các dòng điều hướng khác sau
    }
}
