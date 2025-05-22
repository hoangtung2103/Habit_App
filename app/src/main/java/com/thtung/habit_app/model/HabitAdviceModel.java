package com.thtung.habit_app.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HabitAdviceModel {
    private JSONObject knowledgeBase;
    private Context context;

    public HabitAdviceModel(Context context) {
        this.context = context;
        loadKnowledgeBase();
    }

    private void loadKnowledgeBase() {
        try {
            InputStream is = context.getAssets().open("HabitAdviceKnowledgeBase.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            knowledgeBase = new JSONObject(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAdvice(Statistic stat) {
        try {
            double completionRate = (double) stat.getTotal_completed() / stat.getLast_completed();
            JSONArray rules = knowledgeBase.getJSONArray("rules");

            for (int i = 0; i < rules.length(); i++) {
                JSONObject rule = rules.getJSONObject(i);
                JSONObject condition = rule.getJSONObject("condition");

                boolean conditionMet = true;

                // Kiểm tra streak
                if (condition.has("streak")) {
                    JSONObject streak = condition.getJSONObject("streak");
                    long currentStreak = stat.getStreak();
                    if (streak.has("min") && currentStreak < streak.getInt("min")) {
                        conditionMet = false;
                    }
                    if (streak.has("max") && currentStreak > streak.getInt("max")) {
                        conditionMet = false;
                    }
                }

                // Kiểm tra completion_rate
                if (condition.has("completion_rate")) {
                    JSONObject rate = condition.getJSONObject("completion_rate");
                    if (rate.has("min") && completionRate < rate.getDouble("min")) {
                        conditionMet = false;
                    }
                    if (rate.has("max") && completionRate > rate.getDouble("max")) {
                        conditionMet = false;
                    }
                }

                // Kiểm tra total_completed
                if (condition.has("total_completed")) {
                    JSONObject total = condition.getJSONObject("total_completed");
                    long totalCompleted = stat.getTotal_completed();
                    if (total.has("min") && totalCompleted < total.getInt("min")) {
                        conditionMet = false;
                    }
                    if (total.has("max") && totalCompleted > total.getInt("max")) {
                        conditionMet = false;
                    }
                }

                // Kiểm tra max_streak
                if (condition.has("max_streak")) {
                    JSONObject maxStreak = condition.getJSONObject("max_streak");
                    long maxStreakValue = stat.getMax_streak();
                    if (maxStreak.has("min") && maxStreakValue < maxStreak.getInt("min")) {
                        conditionMet = false;
                    }
                    if (maxStreak.has("max") && maxStreakValue > maxStreak.getInt("max")) {
                        conditionMet = false;
                    }
                }

                if (conditionMet) {
                    return rule.getString("advice");
                }
            }

            return "Hãy tiếp tục cố gắng! Đặt mục tiêu rõ ràng và thực hiện thói quen vào cùng một thời điểm mỗi ngày để tạo thói quen bền vững.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi tạo lời khuyên.";
        }
    }
}
