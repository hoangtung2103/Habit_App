package com.thtung.habit_app.repository;

import android.util.Log;

import androidx.annotation.NonNull;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.model.User;

public class UserRepository {
    private static volatile UserRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore firestoredb;
    private UserDataCallback userDataCallback; // Tham chiếu đến callback của Activity/Fragment
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirestoreManager firestoreManager = new FirestoreManager();

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        firestoredb = FirebaseFirestore.getInstance();
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    public void setUserDataCallback(UserDataCallback callback) {
        this.userDataCallback = callback;
        setupAndAddAuthStateListener(); // Thiết lập và add listener khi có callback
        // Ngay lập tức thông báo trạng thái auth hiện tại cho callback mới
        if (this.userDataCallback != null) {
            this.userDataCallback.onAuthStateChanged(mAuth.getCurrentUser());
            // Nếu đang đăng nhập, có thể trigger fetch data ngay
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null) {
                fetchUserData(currentUser.getUid());
            }
        }
    }

    // Phương thức để Activity/Fragment hủy đăng ký callback
    public void removeUserDataCallback() {
        this.userDataCallback = null;
        removeAuthStateListener(); // Remove listener khi không còn callback
    }

    // Thiết lập và thêm AuthStateListener
    private void setupAndAddAuthStateListener() {
        if (authStateListener == null) { // Chỉ tạo listener một lần
            authStateListener = firebaseAuth -> {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                // Thông báo cho callback (nếu còn tồn tại)
                if (userDataCallback != null) {
                    userDataCallback.onAuthStateChanged(currentUser);
                }
                // Tự động fetch data nếu đăng nhập và có callback
                if (currentUser != null && userDataCallback != null) {
                    fetchUserData(currentUser.getUid());
                } else if (currentUser == null && userDataCallback != null) {
                    // User đăng xuất, đảm bảo trạng thái loading là false
                    userDataCallback.onLoadingStateChanged(false);
                }
            };
        }
        // Luôn remove listener cũ trước khi add (đề phòng) và add listener mới
        mAuth.removeAuthStateListener(authStateListener);
        mAuth.addAuthStateListener(authStateListener);
    }

    // Gỡ bỏ AuthStateListener
    private void removeAuthStateListener() {
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
            // Không nên set authStateListener = null ở đây vì có thể được add lại
        }
    }


    // Hàm fetch dữ liệu, sử dụng callback
    public void fetchUserData(String uid) {
        if (uid == null) {
            if(userDataCallback != null) userDataCallback.onError("User ID is null");
            return;
        }
        if (userDataCallback == null) {
            // Không có ai lắng nghe, không cần fetch
            return;
        }

        // Thông báo bắt đầu tải
        userDataCallback.onLoadingStateChanged(true);

        firestoredb.collection("User")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (userDataCallback == null) return; // Kiểm tra lại callback phòng trường hợp bị hủy giữa chừng

                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String dob = documentSnapshot.getString("birthdate");
                        String avatar = documentSnapshot.getString("avatar");
                        String gender = documentSnapshot.getString("gender");
                        String description = documentSnapshot.getString("description");

                        User user = new User(uid, username, email, avatar, dob, gender, description);
                        userDataCallback.onUserLoaded(user); // Gọi callback thành công
                    } else {
                        userDataCallback.onUserNotFound(); // Gọi callback không tìm thấy
                    }
                    userDataCallback.onLoadingStateChanged(false); // Thông báo kết thúc tải
                })
                .addOnFailureListener(e -> {
                    if (userDataCallback == null) return; // Kiểm tra lại callback

                    userDataCallback.onError("Lỗi khi lấy thông tin: " + e.getMessage()); // Gọi callback lỗi
                    userDataCallback.onLoadingStateChanged(false); // Thông báo kết thúc tải
                });
    }

    public void reauthenticateUser(String currentPassword, @NonNull ReauthenticationCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            callback.onReauthenticationFailure("Người dùng chưa đăng nhập hoặc thiếu email.", false);
            return;
        }
        if (currentPassword == null || currentPassword.isEmpty()) {
            callback.onReauthenticationFailure("Vui lòng nhập mật khẩu hiện tại.", false);
            return;
        }


        String email = firebaseUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        Log.d("UserRepository", "Attempting to re-authenticate user: " + email);
        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("UserRepository", "Re-authentication successful.");
                        callback.onReauthenticationSuccess();
                    } else {
                        Log.e("UserRepository", "Re-authentication failed.", task.getException());
                        boolean isWrongPassword = task.getException() instanceof FirebaseAuthInvalidCredentialsException;
                        String errorMessage = isWrongPassword ? "Mật khẩu hiện tại không đúng." : "Lỗi xác thực lại: " + task.getException().getMessage();
                        callback.onReauthenticationFailure(errorMessage, isWrongPassword);
                    }
                });
    }

    public void updatePassword(String newPassword, @NonNull PasswordUpdateCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            callback.onPasswordUpdateFailure("Người dùng chưa đăng nhập.");
            return;
        }
        if (newPassword == null || newPassword.length() < 6) { // Kiểm tra cơ bản
            callback.onPasswordUpdateFailure("Mật khẩu mới không hợp lệ (ít nhất 6 ký tự).");
            return;
        }

        Log.d("UserRepository", "Attempting to update password.");
        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("UserRepository", "Password updated successfully.");
                        callback.onPasswordUpdateSuccess();
                    } else {
                        Log.e("UserRepository", "Failed to update password.", task.getException());
                        callback.onPasswordUpdateFailure("Lỗi cập nhật mật khẩu: " + task.getException().getMessage());
                    }
                });
    }

    public interface PasswordUpdateCallback {
        void onPasswordUpdateSuccess();
        void onPasswordUpdateFailure(String errorMessage);
    }

    public interface ReauthenticationCallback {
        void onReauthenticationSuccess();
        void onReauthenticationFailure(String errorMessage, boolean isWrongPassword); // Thêm cờ báo sai mật khẩu
    }
    public void logout() {
        mAuth.signOut();
        // AuthStateListener sẽ tự động được kích hoạt và gọi onAuthStateChanged callback
    }
}
