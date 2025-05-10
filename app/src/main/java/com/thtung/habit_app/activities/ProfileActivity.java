package com.thtung.habit_app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.R;
import com.thtung.habit_app.databinding.ActivityProfileBinding;
import com.thtung.habit_app.databinding.DialogResetPasswordBinding;
import com.thtung.habit_app.databinding.DialogRevisePersonalInfoBinding;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.User;
import com.thtung.habit_app.repository.UserDataCallback;
import com.thtung.habit_app.repository.UserRepository;
import com.thtung.habit_app.utils.CloudinaryManager;

import java.util.Calendar;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements UserDataCallback {

    private UserRepository userRepository;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView profileImageView;
    private ActivityResultLauncher<Intent> mGetContent;
    private FirebaseFirestore firestoredb;
    private ActivityProfileBinding binding;
    private DialogRevisePersonalInfoBinding dialogRevisePersonalInfoBinding;
    private FirebaseUser firebaseUser;
    private User user;

    private FirestoreManager firestoreManager;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firestoreManager = new FirestoreManager();
        userRepository = UserRepository.getInstance();

        showLoading(true);
        String imageBannerCloudinary = "https://res.cloudinary.com/do1pouxi6/image/upload/v1743782798/ee447cdc11e0dc89b7284733937ff4fa_thlgtf.jpg";

        Glide.with(this).load(imageBannerCloudinary).into(binding.imageBanner);

        profileImageView = binding.avatarImage;

        binding.backButtonProfileMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            }
        });

        binding.linkRevisePersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        mGetContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        profileImageView.setImageURI(imageUri);
                        uploadImageToCloudinary(imageUri);
                    }
                }
        );


        binding.buttonUploadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        binding.changePasswordLayout.setOnClickListener(v -> {
            showBottomSheetDialogChangePassword();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Đăng ký callback khi Activity bắt đầu hoặc resume
        userRepository.setUserDataCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy đăng ký callback khi Activity dừng
        userRepository.removeUserDataCallback();
    }

    @Override
    public void onUserLoaded(User user) {
        runOnUiThread(() -> {

            this.user = user;
            if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(ProfileActivity.this)
                        .load(user.getAvatar())
                        .apply(RequestOptions.circleCropTransform())
//                        .placeholder(R.drawable.default_avatar_placeholder)
//                        .error(R.drawable.default_avatar_placeholder)
                        .into(binding.avatarImage);
                this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                this.firestoredb = FirebaseFirestore.getInstance();
                firestoreManager.getUser(firebaseUser.getUid(), new FirestoreManager.UserCallback() {
                    @Override
                    public void onUserLoaded(User nguoiDung) {
                        binding.username2Display.setText(nguoiDung.getName());
                        binding.dobDisplay.setText(nguoiDung.getBirthdate());
                        binding.emailDisplay.setText(nguoiDung.getEmail());
                    }

                    public void onError(String errorMessage) {
                        Toast.makeText(ProfileActivity.this, "Lỗi :" + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
                binding.setUser(user);
            }
        });
    }

    @Override
    public void onUserNotFound() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            // Có thể reset binding hoặc hiển thị trạng thái "không tìm thấy"
            binding.setUser(null); // Hoặc new User(...) rỗng
//            binding.avatarImage.setImageResource(R.drawable.default_avatar_placeholder);
            // binding.contentLayout.setVisibility(View.GONE); // Hoặc ẩn nội dung
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            // Có thể reset binding hoặc hiển thị trạng thái lỗi
            binding.setUser(null);
//            binding.avatarImage.setImageResource(R.drawable.default_avatar_placeholder);
            // binding.contentLayout.setVisibility(View.GONE); // Hoặc ẩn nội dung
        });
    }

    @Override
    public void onLoadingStateChanged(boolean isLoading) {
        runOnUiThread(() -> {
            showLoading(isLoading);
        });
    }

    @Override
    public void onAuthStateChanged(FirebaseUser firebaseUser) {
        // Được gọi bởi UserRepository khi trạng thái đăng nhập thay đổi
        runOnUiThread(() -> {
            if (firebaseUser == null) {
                // Người dùng đã đăng xuất hoặc chưa đăng nhập
                // Đảm bảo không chuyển màn hình nếu activity đang bị hủy
                if (!isFinishing() && !isDestroyed()) {
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }
            }
            // Không cần làm gì khi firebaseUser != null, vì Repository sẽ tự động fetch data
            // và gọi các callback khác (onUserLoaded, onLoadingStateChanged...).
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.profileScreenContent.setVisibility(View.INVISIBLE); // Dùng INVISIBLE để không làm thay đổi layout quá nhiều khi ẩn/hiện
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.profileScreenContent.setVisibility(View.VISIBLE);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        mGetContent.launch(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToCloudinary(imageUri);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        CloudinaryManager.uploadImage(this, imageUri, new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d("ProfileActivity", "Upload started");
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                // Hiển thị tiến trình nếu cần
                double progress = ((double) bytes / totalBytes) * 100;
                Log.d("ProfileActivity", "Upload progress: " + progress + "%");
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String imageUrl = (String) resultData.get("secure_url");
                Log.d("ProfileActivity", "Upload success: " + imageUrl);
                updateProfileImageUrl(imageUrl);
            }

            @Override
            public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                Log.e("ProfileActivity", "Upload error: " + error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                Log.d("ProfileActivity", "Upload rescheduled: " + error.getDescription());
            }
        });
    }

    private void updateProfileImageUrl(String imageUrl){
        user = new User(firebaseUser.getUid(), user.getName(), user.getEmail(), imageUrl, user.getBirthdate(), user.getGender(), user.getDescription());
        firestoredb.collection("User")
                .document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Avatar đã được cập nhật!", Toast.LENGTH_SHORT).show();
                    Glide.with(this)
                            .load(imageUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.avatarImage);
                })
                .addOnFailureListener(e -> {
                    // Lỗi
                    Log.e("Firestore", "Error saving user data", e);
                    Toast.makeText(ProfileActivity.this, "Có lỗi xảy ra khi lưu thông tin.", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);

        DialogRevisePersonalInfoBinding dialogRevisePersonalInfoBinding = DialogRevisePersonalInfoBinding.inflate(getLayoutInflater());

        bottomSheetDialog.setContentView(dialogRevisePersonalInfoBinding.getRoot());

        EditText editTextName = dialogRevisePersonalInfoBinding.usernameEdit;
        EditText editTextEmail = dialogRevisePersonalInfoBinding.emailEdit;
        Button buttonSubmit = dialogRevisePersonalInfoBinding.buttonSubmitRevisePersonal;
        EditText dobEditText = getEditText(dialogRevisePersonalInfoBinding);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String dob = dobEditText.getText().toString();

                if(username == null || username.isEmpty() || email == null || email.isEmpty() || dob == null || dob.isEmpty() || !isValidEmail(email)){
                    Toast.makeText(ProfileActivity.this, "Cần điền đúng các trường", Toast.LENGTH_SHORT).show();
                    return;
                }
                user = new User(firebaseUser.getUid(), username, email, user.getAvatar(), dob, user.getGender(), user.getDescription());

                firestoredb.collection("User")
                        .document(firebaseUser.getUid())
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Thông tin đã được cập nhật!", Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                            TextView usernameTextview = binding.usernameDisplay;
                            TextView dobTextview = binding.dobDisplay;
                            TextView emailTextview = binding.emailDisplay;

                            usernameTextview.setText(username);
                            dobTextview.setText(dob);
                            emailTextview.setText(email);
                        })
                        .addOnFailureListener(e -> {
                            // Lỗi
                            Log.e("Firestore", "Error saving user data", e);
                            Toast.makeText(ProfileActivity.this, "Có lỗi xảy ra khi lưu thông tin.", Toast.LENGTH_SHORT).show();
                        });

            }
        });
        bottomSheetDialog.show();
    }

    public void showBottomSheetDialogChangePassword(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);

        DialogResetPasswordBinding dialogResetPasswordBinding = DialogResetPasswordBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(dialogResetPasswordBinding.getRoot());

        EditText inputOldPassword = dialogResetPasswordBinding.inputOldPassword;
        EditText inputNewPassword = dialogResetPasswordBinding.inputNewPassword;
        EditText inputConfirmNewPassword = dialogResetPasswordBinding.inputConfirmNewPassword;

        Button buttonSubmit = dialogResetPasswordBinding.buttonSubmitChangePassword;

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = inputOldPassword.getText().toString().trim();
                String newPassword = inputNewPassword.getText().toString().trim();
                String confirmNewPassword = inputConfirmNewPassword.getText().toString().trim();

                buttonSubmit.setEnabled(false);

                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                    Toast.makeText(ProfileActivity.this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.length() < 6) { // Kiểm tra độ dài mật khẩu mới
                    Toast.makeText(ProfileActivity.this, "Mật khẩu mới phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(ProfileActivity.this, "Mật khẩu mới không khớp.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (oldPassword.equals(newPassword)) {
                    Toast.makeText(ProfileActivity.this, "Mật khẩu mới phải khác mật khẩu cũ.", Toast.LENGTH_SHORT).show();
                    return;
                }

                userRepository.reauthenticateUser(oldPassword, new UserRepository.ReauthenticationCallback() {
                    @Override
                    public void onReauthenticationSuccess() {
                        Log.d("ProfileActivity", "Re-authentication successful via Repository.");
                        userRepository.updatePassword(newPassword, new UserRepository.PasswordUpdateCallback() {
                            @Override
                            public void onPasswordUpdateSuccess() {
                                Log.d("ProfileActivity", "Password updated successfully via Repository.");
                                Toast.makeText(ProfileActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }

                            @Override
                            public void onPasswordUpdateFailure(String errorMessage) {
                                buttonSubmit.setEnabled(true);

                                Log.e("ProfileActivity", "Password update failed: " + errorMessage);
                                Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onReauthenticationFailure(String errorMessage, boolean isWrongPassword) {
                        buttonSubmit.setEnabled(true);

                        Log.e("ProfileActivity", "Re-authentication failed: " + errorMessage);
                        Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        if (isWrongPassword) {
                            inputOldPassword.setError("Mật khẩu không đúng");
                            inputOldPassword.requestFocus();
                        }
                    }
                });
            }
        });

        bottomSheetDialog.show();

    }

    @NonNull
    private EditText getEditText(DialogRevisePersonalInfoBinding dialogRevisePersonalInfoBinding) {
        EditText dobEditText = dialogRevisePersonalInfoBinding.dobEdit;

        dobEditText.setOnClickListener(v -> {
            // Lấy ngày hiện tại
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Tạo DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String dateOfBirth = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        dobEditText.setText(dateOfBirth);
                    }, year, month, day);

            // Hiển thị DatePickerDialog
            datePickerDialog.show();
        });
        return dobEditText;
    }
}

