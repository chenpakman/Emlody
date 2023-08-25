package com.example.emlody;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;

public class SharedViewModel extends ViewModel  {
    private MutableLiveData<String> serverResponseLiveData = new MutableLiveData<>();

    public LiveData<String> getServerResponseLiveData() {
        return serverResponseLiveData;
    }

    public void setServerResponse(String serverResponse) {
        serverResponseLiveData.postValue(serverResponse);
    }
    public void clearServerResponse(){
        serverResponseLiveData.setValue(null);
    }
}
