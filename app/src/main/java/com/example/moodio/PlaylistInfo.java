package com.example.moodio;

public class PlaylistInfo {
    private String image;
    private String playlistName;
    private String playlistUrl;
    public PlaylistInfo(String image, String playlistName, String playlistUrl){
        this.image=image;
        this.playlistName=playlistName + " Mix";
        this.playlistUrl=playlistUrl;

    }

    public String getPlaylistUrl() {
        return playlistUrl;
    }

    public String  getImage() {
        return image;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }
}
