package com.thtung.habit_app.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.thtung.habit_app.model.RewardMilestone;

public class RewardMilestoneViewModel extends ViewModel {
    private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void createRewardMilestone(RewardMilestone reward, FirebaseFirestore firestoredb) {
        firestoredb.collection("RewardMilestone")
                .add(reward)
                .addOnSuccessListener(documentReference -> {
                    reward.setId(documentReference.getId());
                    documentReference.update("id", reward.getId());
                    createSuccess.postValue(true);
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue(e.getMessage());
                    createSuccess.postValue(false);
                });
    }
}
