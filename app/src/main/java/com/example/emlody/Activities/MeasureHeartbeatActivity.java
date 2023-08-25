package com.example.emlody.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.emlody.R;
import com.example.emlody.SharedViewModel;
import com.example.emlody.SharedViewModelFactory;

import java.util.Objects;


public class MeasureHeartbeatActivity extends AppCompatActivity {
    SharedViewModel sharedViewModel;
    String serverResponseString;
    Handler handler = new Handler();
    ImageView heartImg;
    ImageView fingerprint;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_heartbeat);
        getWindow().setStatusBarColor(Color.BLACK);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        sharedViewModel =  SharedViewModelFactory.getInstance();
        setResponseLiveDataObserve();
        heartImg=findViewById(R.id.heart);
        fingerprint= findViewById(R.id.finger);
        setAnimations();

    }
    private void setResponseLiveDataObserve(){
        LiveData<String> serverResponseLiveData = sharedViewModel.getServerResponseLiveData();
        serverResponseLiveData.observe(this, serverResponse -> {
            System.out.println("serverResponse"+serverResponse);
            serverResponseString=serverResponse;
            if(serverResponseString==null){
             //TODO:START DIALOG
            }
        });
    }
    private void setAnimations(){
        final Animation zoomAnimation= AnimationUtils.loadAnimation(this,R.anim.zoom);
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
        fingerprint.startAnimation(shake);
        setFingerprintListener(shake,zoomAnimation);

    }
    @SuppressLint("ClickableViewAccessibility")
    private void setFingerprintListener(Animation shake, Animation zoomAnimation){
        fingerprint.setOnTouchListener((v, event) -> {
            System.out.println("in on touch");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    heartImg.startAnimation(zoomAnimation);
                    fingerprint.clearAnimation();
                    startTimer();
                    return true;

                case MotionEvent.ACTION_UP:
                    fingerprint.startAnimation(shake);
                    heartImg.clearAnimation();
                    handler.removeCallbacksAndMessages(null);
                    return true;
            }

            return false;});
    }
    public void startTimer(){
        handler.postDelayed(() -> {
            if(serverResponseString!=null){
                System.out.println("serverResponseString"+serverResponseString);
                Intent intent = new Intent(MeasureHeartbeatActivity.this, PlaylistsActivity.class);
                intent.putExtra("EXTRA_MESSAGE", serverResponseString);
                startActivity(intent);}
        }, 7000);
    }

}