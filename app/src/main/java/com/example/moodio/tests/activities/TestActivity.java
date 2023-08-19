package com.example.moodio.tests.activities;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodio.R;
import com.example.moodio.tests.utils.SpotifyManager;

public class TestActivity extends AppCompatActivity {
    private SpotifyManager mSpotifyManager;
    private static final String GIF_URL = "file:///android_res/drawable/sound.gif";
    private WebView boomboxWebView;

    private String TITLE = "Currently playing the ";
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);
        title = findViewById(R.id.titleTextView);
        mSpotifyManager = new SpotifyManager(this);

        boomboxWebView = findViewById(R.id.boomboxWebView);
        WebSettings webSettings = boomboxWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        boomboxWebView.loadUrl(GIF_URL);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mSpotifyManager.initializeSpotify();
    }



    @Override
    protected void onStop() {
        mSpotifyManager.stopMusic();
        super.onStop();
        mSpotifyManager.disconnect();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        mSpotifyManager.resumeMusic();
    }*/
}

