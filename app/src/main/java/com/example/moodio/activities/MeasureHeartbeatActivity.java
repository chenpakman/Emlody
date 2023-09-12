package com.example.moodio.activities;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.example.moodio.EmotionNotFoundDialog;
import com.example.moodio.LoadingAlert;
import com.example.moodio.R;
import com.example.moodio.SharedViewModel;
import com.example.moodio.SharedViewModelFactory;
import com.example.moodio.utils.ResponseServer;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


public class MeasureHeartbeatActivity extends AppCompatActivity {
    SharedViewModel sharedViewModel;
    String serverResponseString;
    LoadingAlert loadingAlert;
    Handler handler = new Handler();
    ImageView heartImg;
    ImageView fingerprint;
    private boolean time;
    private boolean isLoadingAlertActive = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_heartbeat);
        getWindow().setStatusBarColor(Color.BLACK);
        time=false;
        isLoadingAlertActive = false;
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
            serverResponseString=serverResponse;
            if(serverResponseString!=null&&serverResponseString.equals("e")){
                runOnUiThread(() -> {
                    EmotionNotFoundDialog dialog = new EmotionNotFoundDialog(MeasureHeartbeatActivity.this);
                    dialog.show();
                });
            }else if(time){
                startPlaylistsActivity();
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
            time=true;
            if(serverResponseString!=null){
                startPlaylistsActivity();

            }
            else{
                 loadingAlert =new LoadingAlert(MeasureHeartbeatActivity.this);
                runOnUiThread(() -> loadingAlert.startAlertDialog());
                isLoadingAlertActive=true;
            }
        }, 7000);
    }
    private void startPlaylistsActivity(){
        if(isLoadingAlertActive){
            loadingAlert.closeAlertDialog();
        }
        Intent intent = new Intent(MeasureHeartbeatActivity.this, PlaylistsActivity.class);
        intent.putExtra("EXTRA_MESSAGE", serverResponseString);
        startActivity(intent);
    }
    public void requestPlayLists(String emotions) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                //.url("http://3.70.133.202:8080/app?emotions=" + emotions)
                .url("http://3.70.133.202:8080/app?emotions=" + emotions)
                //.url("http://192.168.1.218:9000/app?emotions=" + emotions)

                .put(new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {

                    }
                })
                .build();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(MeasureHeartbeatActivity.this, "Something went wrong, please try again." + e.getMessage(), Toast.LENGTH_LONG).show();

                        });
                    }
                    @Override
                    public void onResponse(@NonNull Call call, final Response response) throws IOException {
                        if(response.body()!=null){
                            String url = response.body().string();
                            if (response.code() == 200) {
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(MeasureHeartbeatActivity.this, PlaylistsActivity.class);
                                    intent.putExtra("EXTRA_MESSAGE", url);
                                    startActivity(intent);
                                    finish();
                                });
                            }
                            else{
                                Gson gson = new Gson();
                                ResponseServer serverResponse = gson.fromJson(url, ResponseServer.class);
                                runOnUiThread(() -> {
                                    Toast.makeText(MeasureHeartbeatActivity.this, serverResponse.getError(), Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                    }}  );
    }

        @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AnalyzeEmotionActivity.class);
        startActivity(intent);
        finish();
    }
}
