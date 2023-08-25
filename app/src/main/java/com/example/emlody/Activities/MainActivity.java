package com.example.emlody.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.emlody.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.BLACK);

    }
    public void generatePlaylistButtonClicked(View view){
        Intent intent=new Intent(MainActivity.this, AnalyzeEmotionActivity.class);
        startActivity(intent);
    }
}