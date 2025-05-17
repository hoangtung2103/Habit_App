package com.thtung.habit_app.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thtung.habit_app.R;
import com.thtung.habit_app.activities.MainActivity;

import java.util.HashMap;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "habit_reminder_channel";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);
        // Lưu token lên Firestore
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        HashMap<String, Object> data = new HashMap<>();
        data.put("fcm_token", token);

        FirebaseFirestore.getInstance()
                .collection("User")
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
                .addOnFailureListener(e -> Log.e("FCM", "Error updating token: ", e));
    }


    // Gửi thông báo khi đang trong app
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("FCM", "Message received");

        // Lấy nội dung thông báo
        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Nhắc nhở";
        String body = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "";

        // Tạo channel nếu cần (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở thói quen",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // Tạo intent mở app
        Intent intent = new Intent(this, MainActivity.class); // chỉnh nếu muốn mở màn khác
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_svg)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
        } else {
            Log.w("FCM", "Notification permission not granted");
        }

    }
}
