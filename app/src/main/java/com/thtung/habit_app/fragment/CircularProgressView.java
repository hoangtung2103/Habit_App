package com.thtung.habit_app.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;

import com.thtung.habit_app.R;

import java.lang.Math; // Cho Math.max/min

public class CircularProgressView extends View {

    private int percentage = 0;
    private int progressColor;
    private int viewBackgroundColor; // Màu vòng tròn nền mờ
    private float strokeWidthValue;
    private int textColor;
    private float textSizeValue;

    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private RectF rectF = new RectF();
    private Shader shader = null; // Cho gradient

    public CircularProgressView(Context context) {
        super(context);
        init(null, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        // Giá trị mặc định
        progressColor = ContextCompat.getColor(getContext(), R.color.yellow); // Thay R.color...
        viewBackgroundColor = ContextCompat.getColor(getContext(), R.color.green);
        strokeWidthValue = 20f; // Giá trị pixel, có thể chuyển thành dp nếu muốn linh hoạt hơn
        textColor = Color.WHITE;
        textSizeValue = 60f; // Giá trị pixel, sp tốt hơn cho text

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.CircularProgressView, defStyleAttr, 0);

            percentage = a.getInt(R.styleable.CircularProgressView_percentage, percentage);
            progressColor = a.getColor(R.styleable.CircularProgressView_progressColor, progressColor);
            viewBackgroundColor = a.getColor(R.styleable.CircularProgressView_backgroundColor, viewBackgroundColor);
            strokeWidthValue = a.getDimension(R.styleable.CircularProgressView_strokeWidth, strokeWidthValue);
            textColor = a.getColor(R.styleable.CircularProgressView_textColor, textColor);
            textSizeValue = a.getDimension(R.styleable.CircularProgressView_textSize, textSizeValue);
            // TODO: Lấy thêm thuộc tính nếu bạn định nghĩa

            a.recycle();
        }

        setupPaints();
    }

    private void setupPaints() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(viewBackgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidthValue);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthValue);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        // progressPaint.setColor(progressColor); // Sẽ bị ghi đè bởi shader nếu dùng gradient

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSizeValue);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    private void createGradientShader(int width, int height) {
        // Điều chỉnh màu sắc gradient theo ý muốn
        int startColor = ContextCompat.getColor(getContext(), R.color.yellow); // Ví dụ: #A5D6A7
        int endColor = ContextCompat.getColor(getContext(), R.color.green);     // Ví dụ: #4CAF50

        shader = new SweepGradient(width / 2f, height / 2f,
                new int[]{startColor, endColor, startColor}, // Lặp lại màu đầu để liền mạch
                new float[]{0f, 0.75f, 1.0f} // Vị trí màu (điều chỉnh để khớp ảnh)
        );

        // Xoay gradient để bắt đầu từ đỉnh (12h)
        Matrix matrix = new Matrix();
        matrix.preRotate(-90f, width / 2f, height / 2f);
        if (shader != null) {
            shader.setLocalMatrix(matrix);
        }
        progressPaint.setShader(shader);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Tính toán lại vùng vẽ và shader khi kích thước thay đổi
        float padding = strokeWidthValue / 2f;
        rectF.set(padding, padding, w - padding, h - padding);
        createGradientShader(w, h); // Tạo lại shader với kích thước mới
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Vẽ vòng tròn nền (mờ hơn)
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // 2. Vẽ vòng cung tiến trình
        float sweepAngle = 360f * percentage / 100f;
        // Bắt đầu vẽ từ -90 độ (12 giờ)
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);

        // 3. Vẽ chữ phần trăm ở giữa
        String text = percentage + "%";
        // Tính toán vị trí Y để căn giữa text theo chiều dọc
        float textHeight = textPaint.descent() - textPaint.ascent();
        float textBaseline = (getHeight() / 2f) - (textHeight / 2f) - textPaint.ascent();
        canvas.drawText(text, getWidth() / 2f, textBaseline, textPaint);
    }

    // Hàm để cập nhật phần trăm từ code
    public void setPercentage(int newPercentage) {
        this.percentage = Math.max(0, Math.min(100, newPercentage)); // Đảm bảo trong khoảng 0-100
        invalidate(); // Yêu cầu View vẽ lại chính nó
    }

    // (Tùy chọn) Các hàm setter khác nếu cần
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color); // Cập nhật màu
        progressPaint.setShader(null); // Bỏ shader nếu set màu đơn
        invalidate();
    }

    public void setGradientColors(int startColorRes, int endColorRes) {
        int startColor = ContextCompat.getColor(getContext(), startColorRes);
        int endColor = ContextCompat.getColor(getContext(), endColorRes);
        shader = new SweepGradient(getWidth() / 2f, getHeight() / 2f,
                new int[]{startColor, endColor, startColor},
                new float[]{0f, 0.75f, 1.0f});
        Matrix matrix = new Matrix();
        matrix.preRotate(-90f, getWidth() / 2f, getHeight() / 2f);
        if (shader != null) {
            shader.setLocalMatrix(matrix);
        }
        progressPaint.setShader(shader);
        invalidate();
    }

    // ... các hàm setter khác cho strokeWidth, textColor, textSize...
}