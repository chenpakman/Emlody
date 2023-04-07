package com.example.emlody;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class SpotifyImageView extends AppCompatImageView {

    String spotifyUri = null;

    public SpotifyImageView(@NonNull Context context) {
        super(context);
    }

    public String getSpotifyUri() {
        return this.spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }
}
