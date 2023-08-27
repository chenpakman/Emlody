package com.example.moodio;

public class SharedViewModelFactory {
    static SharedViewModel sharedViewModelInstance;

    public static SharedViewModel getInstance() {

        if (sharedViewModelInstance == null) {
            sharedViewModelInstance = new SharedViewModel();
        }
        return sharedViewModelInstance;
    }
}
