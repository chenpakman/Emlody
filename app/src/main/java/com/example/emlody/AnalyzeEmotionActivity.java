package com.example.emlody;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.emlody.Utils.RealPathUtil;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalyzeEmotionActivity extends AppCompatActivity {
    private final int GALLERY_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_emotion);
    }

    public void choseImageFromGallery(View view) {
        Intent iGallery = new Intent(Intent.ACTION_PICK);
        iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(iGallery, GALLERY_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean permission= ContextCompat.checkSelfPermission(AnalyzeEmotionActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED;
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE) {
                if(permission) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AnalyzeEmotionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(AnalyzeEmotionActivity.this, "please accept for required permission", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }
                else {
                    LoadingAlert loadingAlert =new LoadingAlert(AnalyzeEmotionActivity.this);
                    loadingAlert.startAlertDialog();
                    Context context = AnalyzeEmotionActivity.this;
                    String path = RealPathUtil.getRealPath(context,data.getData());
                    File file = new File(path);
                    uploadImageToServer(file, loadingAlert);
                }
            }
        }
    }
private void uploadImageToServer( File file ,LoadingAlert loadingAlert){
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQ_CODE);
    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
            .build();
    Request request = new Request.Builder()
            .url("http://10.0.2.2:9000/app")
            .post(requestBody)
            .build();
    okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingAlert.closeAlertDialog();
                            Toast.makeText(AnalyzeEmotionActivity.this,"The image that you choose is not valid.\n Please try again.",Toast.LENGTH_LONG).show();

                        }
                    });
                }
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    loadingAlert.closeAlertDialog();
                    String res = response.body().string();
                    System.out.println(res);

                }
            });
}
}
