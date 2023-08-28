package com.example.moodio.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import com.example.moodio.LoadingAlert;
import com.example.moodio.R;
import com.example.moodio.utils.RealPathUtil;
import com.example.moodio.utils.ResponseServer;
import com.example.moodio.SharedViewModel;
import com.example.moodio.SharedViewModelFactory;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private static final int CAMERA_PERMISSION_CODE=111;
    private static final int GALLERY_REQ_CODE=1000;
    private ActivityResultLauncher<String> imageLauncher;
    private ActivityResultLauncher<Uri> cameraImageLauncher;
    private List<Button> buttonList=new ArrayList<>();
    private Uri imageUri;
    private ImageView imageView;
    private TextView title;

    private File imageFile;
    private Button galleryFloatingActionButton;
    private Button cameraFloatingActionButton;

    private Button liveStreamFloatingActionButton;
    private SharedViewModel sharedViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_emotion);
        getWindow().setStatusBarColor(Color.BLACK);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        checkForPermission();
        sharedViewModel = SharedViewModelFactory.getInstance();
        cameraFloatingActionButton=findViewById(R.id.cameraFloatingActionButton);
        galleryFloatingActionButton=findViewById(R.id.floatingActionButton);
        liveStreamFloatingActionButton = findViewById(R.id.liveCameraActionButton);
        title=findViewById(R.id.vibesTextView);
        liveStreamFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AnalyzeEmotionActivity.this, LiveCameraActivity.class);
                startActivity(intent);
            }
        });
        buttonList.add(cameraFloatingActionButton);
        buttonList.add(galleryFloatingActionButton);
        buttonList.add(liveStreamFloatingActionButton);
        imageView=findViewById(R.id.chosenImageView);
        imageUri=createUri();
        buttonAnimation();
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
                "com.example.moodio.fileProvider", imageFile
        );
}

private void registerPictureCameraLauncher(){
    cameraImageLauncher=registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                try{
                    if(result){
                        selectFromGallery();
                        reorderUi(imageUri);
                    }
                } catch (Exception e){
                    System.out.println("Error:"+e);
                }
            }
    );

}
private void registerPictureGalleryLauncher(){
    imageLauncher =registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        if(null != result){
            String path=RealPathUtil.getRealPath(AnalyzeEmotionActivity.this,result);
            reorderUi(result);
            imageFile=new File(path);
            selectFromGallery();
        }
    });
}
private void reorderUi(Uri imageUri){
  imageView.setImageURI(imageUri);

    title.animate().alpha(1f).y(100);
    cameraFloatingActionButton.animate().alpha(1f).y(1560);
    galleryFloatingActionButton.animate().alpha(1f).y(1310);
}
private void checkForPermission(){
    if (ContextCompat.checkSelfPermission(AnalyzeEmotionActivity.this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this,
                new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                8);
    } if (ContextCompat.checkSelfPermission(AnalyzeEmotionActivity.this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this,
                new String[]{Manifest.permission.BODY_SENSORS},
                344);
    }if (ContextCompat.checkSelfPermission(AnalyzeEmotionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                10);
    }

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
        //runOnUiThread(() -> loadingAlert.startAlertDialog());
        uploadImageToServer(imageFile, loadingAlert);
     }

    private void uploadImageToServer( File file ,LoadingAlert loadingAlert){
        Intent intent=new Intent(AnalyzeEmotionActivity.this, MeasureHeartbeatActivity.class);
        sharedViewModel.setServerResponse(null);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    ActivityCompat.requestPermissions(AnalyzeEmotionActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQ_CODE);
    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream")))
            .build();
    Request request = new Request.Builder()
            //.url("http://3.70.133.202:8080/app")
            .url("http://192.168.1.218:9000/app")
            .post(requestBody)
            .build();
    okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    sharedViewModel.setPostServerResponse("e");
                    runOnUiThread(() -> {
                        Toast.makeText(AnalyzeEmotionActivity.this, "Something went wrong, please try again." + e.getMessage(), Toast.LENGTH_LONG).show();

                    });
                }
                @Override
                public void onResponse(@NonNull Call call, final Response response) throws IOException {
                    if(response.body()!=null){
                        String url = response.body().string();

                        if (response.code() == 200) {
                            sharedViewModel.setPostServerResponse(url);
                            System.out.println("setServerResponse!");

                        }
                        else if(response.code() == 204) {
                            sharedViewModel.setPostServerResponse("e");


                        }
                        else{

                            Gson gson = new Gson(); // Or use new GsonBuilder().create();
                            ResponseServer serverResponse = gson.fromJson(url, ResponseServer.class);
                            runOnUiThread(() -> {
                               // loadingAlert.closeAlertDialog();
                                Toast.makeText(AnalyzeEmotionActivity.this, serverResponse.getError(), Toast.LENGTH_LONG).show();
                            });
                        }
                }
            }});
        runOnUiThread(() -> {
            startActivity(intent);
        });
    }



private void buttonAnimation(){
    for (Button b:buttonList) {
        b.setAlpha(0f);
        b.setTranslationY(1000);
        b.animate().alpha(1f).translationYBy(-1000).setDuration(1500);
    }
}
    /*public void requestPlayLists(String emotions) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                //.url("http://3.70.133.202:8080/app?emotions=" + emotions)
                .url("http://192.168.1.218:9000/app?emotions=" + emotions)
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
                            Toast.makeText(AnalyzeEmotionActivity.this, "Something went wrong, please try again." + e.getMessage(), Toast.LENGTH_LONG).show();

                        });
                    }
                    @Override
                    public void onResponse(@NonNull Call call, final Response response) throws IOException {
                        if(response.body()!=null){
                            String url = response.body().string();
                            if (response.code() == 200) {
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(AnalyzeEmotionActivity.this, PlaylistsActivity.class);
                                    intent.putExtra("EXTRA_MESSAGE", url);
                                    startActivity(intent);
                                });
                            }
                            else{
                                Gson gson = new Gson(); // Or use new GsonBuilder().create();
                                ResponseServer serverResponse = gson.fromJson(url, ResponseServer.class);
                                runOnUiThread(() -> {
                                    Toast.makeText(AnalyzeEmotionActivity.this, serverResponse.getError(), Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                }}  );
    }
*/
    }




