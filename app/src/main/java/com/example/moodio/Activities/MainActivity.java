package com.example.moodio.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.moodio.R;
import com.example.moodio.tests.activities.LiveStreamActivity;
import com.example.moodio.tests.activities.TestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.BLACK);

    }
    public void generatePlaylistButtonClicked(View view){
        Intent intent=new Intent(MainActivity.this, TestActivity.class);
        startActivity(intent);
    }
}