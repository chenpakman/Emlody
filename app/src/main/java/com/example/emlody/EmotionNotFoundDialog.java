package com.example.emlody;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class EmotionNotFoundDialog extends Dialog implements View.OnClickListener {

    private Context context;

    private Dialog dialog;
    private TextView tellText, goBackText;

    private ChooseEmotionsDialog emotionsDialog;

    public EmotionNotFoundDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_text:
                this.dismiss();
                break;
            case R.id.okay_text:
                this.dismiss();
                this.emotionsDialog = new ChooseEmotionsDialog(this.context);
                emotionsDialog.show();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.playlists_dialog_layout);
        tellText = findViewById(R.id.okay_text);
        goBackText = findViewById(R.id.cancel_text);
        tellText.setOnClickListener(this);
        goBackText.setOnClickListener(this);
    }

}
