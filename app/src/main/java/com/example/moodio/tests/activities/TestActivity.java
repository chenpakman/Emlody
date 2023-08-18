package com.example.moodio.tests.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.moodio.R;
import com.example.moodio.tests.utils.SpotifyManager;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.spotify.protocol.types.Track;

import java.io.IOException;

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
        mSpotifyManager.stopMusic();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        mSpotifyManager.resumeMusic();
    }*/
}

