package com.example.emlody;

public class Playlist {
    int image;
    String playlistName;
    String playlistUrl;
    public Playlist(int image,String playlistName,String playlistUrl){
        this.image=image;
        this.playlistName=playlistName;
        this.playlistUrl=playlistUrl;

    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public int getImage() {
        return image;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }
}
