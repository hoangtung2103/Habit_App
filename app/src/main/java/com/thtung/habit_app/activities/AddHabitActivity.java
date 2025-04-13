package com.thtung.habit_app.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.thtung.habit_app.R;
import com.thtung.habit_app.databinding.ActivityAddHabitBinding;
import com.thtung.habit_app.databinding.PopupGhichuBinding;
import com.thtung.habit_app.dialog.IconBottomSheet;
import com.thtung.habit_app.dialog.PopupLapLai;
import com.thtung.habit_app.dialog.PopupMucTieu;
import com.thtung.habit_app.firebase.FirestoreManager;
import com.thtung.habit_app.utils.CloudinaryManager;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class AddHabitActivity extends AppCompatActivity {
    private ActivityAddHabitBinding binding;
    private FirestoreManager firestoreManager;

    private String userID = "";
    private String iconHabitUrl = "";
    private String tenhabit = "";
    private String lapLai = "Hàng ngày";
    private String muctieu = "1 lần mỗi ngày";
    private String timeNhacNho = "07:00";
    private String ghiChu = "";

    private Timestamp datebd = new Timestamp(Calendar.getInstance().getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddHabitBinding.inflate(getLayoutInflater());
        firestoreManager = new FirestoreManager();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        setContentView(binding.getRoot());

        //Chọn icon và load lên Cloudinary và lấy link
        binding.icon.setOnClickListener(v -> {
            IconBottomSheet iconBottomSheet = new IconBottomSheet(assetPath -> {
                try {
                    InputStream is = getAssets().open(assetPath);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    binding.icon.setImageBitmap(bitmap);
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                uploadAssetToCloudinary(assetPath);
            });
            iconBottomSheet.show(getSupportFragmentManager(), "IconPicker");
        });


        // Lấy tên thói quen
        binding.tenHabitEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tenhabit = s.toString();
                Log.d("TenHabitDebug", "Ten Habit: " + tenhabit);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        //Popup Lap Lai va lay gia tri
        binding.laplai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupLapLai popupLapLai = new PopupLapLai(new PopupLapLai.OnRepeatOptionSelectedListener(){
                    @Override
                    public void onRepeatSelected(String repeatOption) {
                        lapLai = repeatOption;
                        Log.d("LapLaiDebug", "Lap Lai: " + lapLai);
                        binding.laplaiTxt.setText(lapLai);
                    }
                });
                popupLapLai.show(getSupportFragmentManager(), popupLapLai.getTag());
            }

        });

        //Popup chọn mục tiêu
        binding.muctieu.setOnClickListener(v -> {
            PopupMucTieu popupMucTieu = new PopupMucTieu(selected -> {
                binding.muctieuTxt.setText(selected);
                muctieu = selected;
                Log.d("MucTieuDebug", "Muc Tieu: " + muctieu);
            });
            popupMucTieu.show(getSupportFragmentManager(), popupMucTieu.getTag());
        });

        //Chọn giờ nhắc nhở
        binding.nhacnho.setOnClickListener(v -> {
            int currentHour = 7;
            int currentMinute = 0;
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,(view, hourOfDay, minute) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute);
                binding.nhacnhoTxt.setText(time);
                timeNhacNho = time;
                Log.d("TimeDebug", "Time: " + timeNhacNho);

            }, currentHour, currentMinute, true);
            timePickerDialog.show();
        });

//        binding.nhacnho.setOnClickListener(v -> {
//            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
//                    .setTimeFormat(TimeFormat.CLOCK_24H)
//                    .setHour(7)
//                    .setMinute(0)
//                    .setTitleText("Chọn giờ nhắc nhở")
//                    .build();
//
//            timePicker.addOnPositiveButtonClickListener(v1 -> {
//                int hour = timePicker.getHour();
//                int minute = timePicker.getMinute();
//                String time = String.format("%02d:%02d", hour, minute);
//                binding.nhacnhoTxt.setText(time);
//                timeNhacNho = time;
//                Log.d("TimeDebug", "Time: " + timeNhacNho);
//            });
//            timePicker.show(getSupportFragmentManager(), "time_picker");
//        });


        //Thêm ghi chú
        binding.ghichu.setOnClickListener(v -> {
            PopupGhichuBinding bindinggc = PopupGhichuBinding.inflate(LayoutInflater.from(this));
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Nhập ghi chú")
                    .setView(bindinggc.getRoot())
                    .setPositiveButton("Lưu", (dialog1, which) -> {
                        String noteTinput = bindinggc.edtNote.getText().toString().trim();
                        if (!noteTinput.isEmpty()) {
                            ghiChu = noteTinput;
                            bindinggc.edtNote.setText(ghiChu);
                        }
                        Log.d("GhiChuDebug", "Ghi Chu: " + ghiChu);
                    })
                    .setNegativeButton("Hủy", null)
                    .create();
            dialog.show();
        });


        //Tạo lịch và lấy ngày bắt đầu
        binding.ngaybd.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        datebd = new Timestamp(calendar.getTime());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Log.d("DateDebug", "Date: " + dateFormat.format(calendar.getTime()));
                        binding.ngaybdTxt.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });


        //// Xử lý button HUỶ và LƯU
        binding.huyBtn.setOnClickListener(v -> {
            startActivity(new Intent(AddHabitActivity.this, MainActivity.class));
        });

        binding.luuBtn.setOnClickListener(v -> {
            if (tenhabit.isEmpty() || iconHabitUrl.isEmpty() || lapLai.isEmpty() || muctieu.isEmpty() || timeNhacNho.isEmpty()  || datebd == null) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            firestoreManager.saveHabit(this, "", userID, tenhabit, iconHabitUrl, lapLai, muctieu, timeNhacNho, ghiChu, datebd);
            startActivity(new Intent(AddHabitActivity.this, MainActivity.class));
        });

    }


    private void uploadAssetToCloudinary(String assetPath) {
        try {
            File tempFile = new File(getCacheDir(), "temp_image.png");
            InputStream istream = getAssets().open(assetPath);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = istream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            istream.close();

            CloudinaryManager.uploadImage(this, Uri.fromFile(tempFile), new UploadCallback() {
                @Override
                public void onStart(String requestId) {}

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {}

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String cloudUrl = resultData.get("secure_url").toString();
                    iconHabitUrl = cloudUrl;
                    Log.d("IconDebug", "Icon: " + iconHabitUrl);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    Log.e("CLOUDINARY_ERROR", "Upload Fail: " + error.getDescription());
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {}
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}