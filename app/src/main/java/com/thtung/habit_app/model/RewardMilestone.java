package com.thtung.habit_app.model;

public class RewardMilestone {
    private String id;
    private int point;
    private String reward_name;
    private int required_streak_days;
    private String description;
    private String type;
    private String iconUrl;

    public RewardMilestone() {}

    public RewardMilestone(String id, int point, String reward_name, String description, String type) {
        this.id = id;
        this.point = point;
        this.reward_name = reward_name;
        this.description = description;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getRequired_streak_days() {
        return required_streak_days;
    }

    public void setRequired_streak_days(int required_streak_days) {
        this.required_streak_days = required_streak_days;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getReward_name() {
        return reward_name;
    }

    public void setReward_name(String reward_name) {
        this.reward_name = reward_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
