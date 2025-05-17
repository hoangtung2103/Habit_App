package com.thtung.habit_app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thtung.habit_app.R;
import com.thtung.habit_app.databinding.ActivityFeedbackBinding;
import com.thtung.habit_app.firebase.FirestoreManager;

public class FeedbackActivity extends AppCompatActivity {
    private ActivityFeedbackBinding binding;
    private FirebaseUser firebaseUser;
    private FirestoreManager firestoreManager = new FirestoreManager();
    private ImageView[] stars;
    private int rating = 0;
    private String existingFeedbackId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        stars = new ImageView[]{
                binding.star1,
                binding.star2,
                binding.star3,
                binding.star4,
                binding.star5
        };

        for (int i = 0; i < stars.length; i++) {
            final int index = i + 1;
            stars[i].setOnClickListener(v -> {
                rating = index;
                updateStarDisplay();
            });
        }

        // Load Feedback
        firestoreManager.getFeedback(firebaseUser.getUid(), feedback -> {
            existingFeedbackId = feedback.getId();
            binding.reviewText.setText(feedback.getContent());
            rating = feedback.getRating();
            updateStarDisplay();
        }, () ->{});

        binding.cancelButton.setOnClickListener(v -> {
            finish();
        });

        binding.saveButton.setOnClickListener(v -> {
            String review = binding.reviewText.getText().toString().trim();
            if(rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao.", Toast.LENGTH_SHORT).show();
                return;
            }

            firestoreManager.saveOrUpdateFeedback(firebaseUser.getUid(), review, rating, existingFeedbackId, this::finish);
            Toast.makeText(this, "Cảm ơn bạn đã góp ý!", Toast.LENGTH_SHORT).show();
        });

    }

    private void updateStarDisplay() {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_border);
            }
        }
    }
}