package com.example.emlody.Utils;

import java.util.HashMap;
import java.util.Map;

public class ResponseServer {
   private Map<String, String> playlistsUrls;
   private String playlistUrl;
   private String error;
   private String emotion;

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }

    public String getEmotion() {
        return emotion;
    }

    public String getError() {
        return error;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public Map<String, String> getPlaylistsUrls() {
        return playlistsUrls;
    }

    public void addPlaylistUrl(String emotion, String playlistUrl) {
        if(null == this.playlistsUrls){
            this.playlistsUrls = new HashMap<>();
        }

        this.playlistsUrls.put(emotion, playlistUrl);
    }
}