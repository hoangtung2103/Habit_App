package com.thtung.habit_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.thtung.habit_app.R;
import com.thtung.habit_app.databinding.ActivitySettingsBinding;


public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private LinearLayout accountRow;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.imgIcHome.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        });

        binding.imgIcThongke.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, StatisticActivity.class));
            finish();
        });

        binding.imgIcRank.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, BadgeActivity.class));
            finish();
        });

        binding.icAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, AddHabitActivity.class));
            }
        });


        binding.feedBack.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, FeedbackActivity.class));
        });

        binding.dangxuat.setOnClickListener(v -> {
            mAuth.signOut();

            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });

        });
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
