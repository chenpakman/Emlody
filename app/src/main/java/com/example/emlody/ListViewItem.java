package com.example.emlody;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class ListViewItem extends View {

    private SpotifyImageView spotifyIcon;

    private TextView moodText;

    public ListViewItem(Context context) {
        super(context);

    }

    public void setSpotifyIcon(SpotifyImageView spotifyIcon) {
        this.spotifyIcon = spotifyIcon;
    }

    public void setMoodText(TextView moodText) {
        this.moodText = moodText;
    }

    public SpotifyImageView getSpotifyIcon() {
        return this.spotifyIcon;
    }
}
