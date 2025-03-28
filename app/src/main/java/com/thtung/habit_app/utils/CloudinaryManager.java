package com.thtung.habit_app.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.policy.UploadPolicy;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";

    // Gọi hàm này 1 lần ở MainActivity hoặc Application để init
    private static boolean isInitialized = false;

    public static void initCloudinary(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "do1pouxi6");
            config.put("api_key", "646342459889886");
            config.put("api_secret", "xTy4qEK4uQW-mb-DfaWMYi29Yh0");

            MediaManager.init(context.getApplicationContext(), config);
            isInitialized = true;
        }
    }

    // Hàm upload image và trả về callback
    public static void uploadImage(Context context, Uri fileUri, UploadCallback callback) {
        initCloudinary(context);

        UploadPolicy uploadPolicy = new UploadPolicy.Builder()
                .networkPolicy(UploadPolicy.NetworkType.ANY)   // Có thể upload qua Wifi hoặc 4G
                .maxRetries(2)
                .build();

        MediaManager.get().upload(fileUri)
                .option("folder", "habit_app") // Tự tạo folder trên Cloudinary
                .option("resource_type", "image")
                .policy(uploadPolicy)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started");
                        if (callback != null) callback.onStart(requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (callback != null) callback.onProgress(requestId, bytes, totalBytes);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Upload success: " + resultData.get("secure_url"));
                        if (callback != null) callback.onSuccess(requestId, resultData);
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        if (callback != null) callback.onError(requestId, error);
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        if (callback != null) callback.onReschedule(requestId, error);
                    }
                })
                .dispatch();
    }
}