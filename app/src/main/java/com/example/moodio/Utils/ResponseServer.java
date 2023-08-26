package com.example.moodio.Utils;

import java.util.HashMap;
import java.util.Map;

public class ResponseServer {
   private Map<String, Playlist> playlistsUrls;
   private String defaultPlaylistUrl;
   private String error;
   private String emotion;
   private String imageUrl;

   private String defaultMixName;

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDefaultPlaylistUrl(String defaultPlaylistUrl) {
        this.defaultPlaylistUrl = defaultPlaylistUrl;
    }

    public String getEmotion() {
        return emotion;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getError() {
        return error;
    }

    public String getDefaultPlaylistUrl() {
        return defaultPlaylistUrl;
    }

    public Map<String, Playlist> getPlaylistsUrls() {
        return playlistsUrls;
    }

    public void addPlaylistUrl(String emotion, Playlist playlistUrl) {
        if(null == this.playlistsUrls){
            this.playlistsUrls = new HashMap<>();
        }

        this.playlistsUrls.put(emotion, playlistUrl);
    }

    public String getDefaultMixName() {
        return defaultMixName;
    }

    public void setDefaultMixName(String defaultMixName) {
        this.defaultMixName = defaultMixName;
    }
}