package com.thtung.habit_app.repository;


import com.google.firebase.auth.FirebaseUser;
import com.thtung.habit_app.model.User;

public interface UserDataCallback {
    void onUserLoaded(User user);
    void onUserNotFound();
    void onError(String errorMessage);
    void onLoadingStateChanged(boolean isLoading);
    void onAuthStateChanged(FirebaseUser firebaseUser);
}
