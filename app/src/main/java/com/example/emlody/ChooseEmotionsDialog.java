package com.example.emlody;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ChooseEmotionsDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private Dialog dialog;
    private TextView tellText, goBackText, doneText, informationText;

    String[] emotions = {"Happy", "Sad", "Angry", "Exited", "Nervous", "Fear"};
    ArrayAdapter<String> adapter;

    List<String> chosenEmotionsByUser;

    ListView emotionsListView;

    ArrayList emotionsArrayList;

    public ChooseEmotionsDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showEmotionsDialog() {
        /*chosenEmotionsByUser = new ArrayList<>();
        emotionsDialog = new Dialog(AnalyzeEmotionActivity.this);
        emotionsDialog.setContentView(R.layout.emotions_dialog_layout);
        emotionsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emotionsDialog.setCancelable(false);
        emotionsDialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        emotionsListView = findViewById(R.id.moods_listview);
        emotionsArrayList = new ArrayList();
        adapter = new ArrayAdapter<>
                (AnalyzeEmotionActivity.this,
                        android.R.layout.select_dialog_multichoice,
                        emotionsArrayList
                );
        emotionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenEmotionsByUser.add(adapter.getItem(position));
            }
        });
        this.emotionsListView.setAdapter(adapter);
        doneText = findViewById(R.id.done_text);
        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionsDialog.dismiss();
                //todo call the server with emotions chosen by user
            }
        });

        emotionsDialog.show();*/
    }
}
