package com.example.emlody.Activities;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.emlody.LoadingAlert;
import com.example.emlody.R;
import com.example.emlody.Utils.RealPathUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private static final int CAMERA_PERMISSION_CODE=1;
    private static final int GALLERY_REQ_CODE=1000;
    ActivityResultLauncher<String> imageLauncher;
    ActivityResultLauncher<Uri> cameraImageLauncher;
    Uri imageUri;
    ImageView imageView;
    File imageFile;
    FloatingActionButton galleryFloatingActionButton;
    FloatingActionButton cameraFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_emotion);
        cameraFloatingActionButton=findViewById(R.id.cameraFloatingActionButton);
        galleryFloatingActionButton=findViewById(R.id.floatingActionButton);
        imageView=findViewById(R.id.choosenImageView);
        imageUri=createUri();
        registerPictureCameraLauncher();
        registerPictureGalleryLauncher();
       cameraFloatingActionButton.setOnClickListener(v -> checkCameraPermissionsAndOpenCamera());
        galleryFloatingActionButton.setOnClickListener(v -> {
            Intent iGallery = new Intent(Intent.ACTION_PICK);
            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imageLauncher.launch("image/*");
        });


    }
private Uri createUri(){
        imageFile=new File(getApplicationContext().getFilesDir(),"camera_photo.jpg");
        return FileProvider.getUriForFile(
                getApplicationContext(),
                "com.example.emlody.fileProvider",
                imageFile
        );
}

private void registerPictureCameraLauncher(){
    cameraImageLauncher=registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                try{
                    if(result){
                        selectFromGallery();
                        imageView.setImageURI(imageUri);
                    }
                } catch (Exception e){
                    System.out.println("Error:"+e);
                }
            }
    );

}
private void registerPictureGalleryLauncher(){
    imageLauncher =registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        String path=RealPathUtil.getRealPath(AnalyzeEmotionActivity.this,result);
        imageView.setImageURI(result);
        imageFile=new File(path);
        selectFromGallery();
    });
}

private void checkCameraPermissionsAndOpenCamera(){
    if(ActivityCompat.checkSelfPermission(AnalyzeEmotionActivity.this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this,
                new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
    }
    else {
        cameraImageLauncher.launch(imageUri);
    }
 }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CAMERA_PERMISSION_CODE){
            if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                cameraImageLauncher.launch(imageUri);
            }
            else{
                Toast.makeText(this,"Camera permission denied,please allow permission to take picture",Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void selectFromGallery() {
        LoadingAlert loadingAlert =new LoadingAlert(AnalyzeEmotionActivity.this);
        runOnUiThread(() -> loadingAlert.startAlertDialog());
        uploadImageToServer(imageFile, loadingAlert);
     }

    private void uploadImageToServer( File file ,LoadingAlert loadingAlert){
    System.out.println("here3");
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
            .url("http://192.168.1.44:9000/app")
            .post(requestBody)
            .build();
    okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        loadingAlert.closeAlertDialog();
                        Toast.makeText(AnalyzeEmotionActivity.this, "Something went wrong, please try again." + e.getMessage(), Toast.LENGTH_LONG).show();

                    });
                }

                @Override
                public void onResponse(@NonNull Call call, final Response response) {
                    runOnUiThread(() -> {
                        loadingAlert.closeAlertDialog();
                        try {
                            Toast.makeText(AnalyzeEmotionActivity.this, response.body().string(), Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });
                }
            });
}
}




