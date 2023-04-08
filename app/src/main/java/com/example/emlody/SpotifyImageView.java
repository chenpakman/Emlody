package com.example.emlody;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class SpotifyImageView extends AppCompatImageView {

    String spotifyUri = null;

    String mood = null;

    public SpotifyImageView(@NonNull Context context) {

        super(context);
        this.setImageResource(R.drawable.spotify);
        this.setClickable(true);
        this.setMinimumHeight(50);
        this.setMinimumWidth(50);
        this.setMaxWidth(50);
        this.setMaxHeight(50);
    }

    public String getSpotifyUri() {
        return this.spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getMood() {
        return this.mood;
    }
}
