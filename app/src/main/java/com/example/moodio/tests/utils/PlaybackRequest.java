package com.example.moodio.tests.utils;
import com.google.gson.annotations.SerializedName;


import java.util.List;

public class PlaybackRequest {
    private List<String> uris;

    public PlaybackRequest(List<String> uris) {
        this.uris = uris;
    }

    public List<String> getUris() {
        return uris;
    }

    public void setUris(List<String> uris) {
        this.uris = uris;
    }
}


