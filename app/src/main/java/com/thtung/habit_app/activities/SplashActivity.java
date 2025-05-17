package com.thtung.habit_app.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thtung.habit_app.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 1500;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserStatus();
        }, SPLASH_DELAY);

        //Xin quyền thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }


    }

    private void checkUserStatus() {
        // Lấy người dùng hiện tại từ FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent;
        if (currentUser != null) {
            // Người dùng đã đăng nhập
            Log.d(TAG, "User is signed in. Navigating to MainActivity.");
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // Người dùng chưa đăng nhập
            Log.d(TAG, "User is signed out. Navigating to LoginActivity.");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        // Thêm cờ để xóa SplashActivity khỏi stack và ngăn quay lại nó
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
