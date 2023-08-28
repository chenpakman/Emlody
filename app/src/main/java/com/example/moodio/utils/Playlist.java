package com.example.moodio.utils;

public class Playlist {
    String playlistUrl;
    String imageUrl;
public Playlist(String playlistUrl,String imageUrl){
    this.playlistUrl=playlistUrl;
    this.imageUrl=imageUrl;
}
    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }

    public void setImageUrl(String imageUrl) {
        imageUrl = imageUrl;
    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}