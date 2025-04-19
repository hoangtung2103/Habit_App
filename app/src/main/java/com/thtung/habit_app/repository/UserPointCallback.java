package com.thtung.habit_app.repository;


import com.thtung.habit_app.model.UserPoint;

public interface UserPointCallback{
    void onUserPointLoaded(UserPoint userPoint);
    void onPointsAwarded();
    void onAlreadyAwardedToday();
    void onSuccess();
    void onError(String errorMessage);
}