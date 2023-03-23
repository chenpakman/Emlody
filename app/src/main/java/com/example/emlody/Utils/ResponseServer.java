package com.example.emlody.Utils;

public class ResponseServer {
    String playlistUrl;
    String error;
    String emotion;

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
}