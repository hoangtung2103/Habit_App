package com.thtung.habit_app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class ProgressCircleView extends View {

    private float percent = 0f;

    private Paint bgPaint;
    private Paint progressPaint;
    private Paint textPaint;

    public ProgressCircleView(Context context) {
        super(context);
        init();
    }

    public ProgressCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Màu nền - hồng nhạt
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#F8BBD0"));
        bgPaint.setStrokeWidth(30f);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        // Màu phần hoàn thành - hồng đậm
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.parseColor("#E91E63"));
        progressPaint.setStrokeWidth(30f);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Vẽ text ở giữa
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#E91E63"));
        textPaint.setTextSize(60f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setPercent(float percent) {
        this.percent = percent;
        invalidate(); // Vẽ lại khi cập nhật %
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2 - 30;

        RectF oval = new RectF(
                width / 2 - radius,
                height / 2 - radius,
                width / 2 + radius,
                height / 2 + radius
        );

        // Vẽ vòng tròn nền
        canvas.drawArc(oval, 0, 360, false, bgPaint);

        // Vẽ phần hoàn thành
        float sweepAngle = 360 * (percent / 100f);
        canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);

        // Vẽ số % ở giữa
        String percentText = String.format(Locale.getDefault(), "%.0f%%", percent);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float centerY = height / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2;
        canvas.drawText(percentText, width / 2, centerY, textPaint);
    }
}


