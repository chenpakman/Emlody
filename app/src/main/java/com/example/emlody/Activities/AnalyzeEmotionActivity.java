package com.example.emlody.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.emlody.EmotionNotFoundDialog;
import com.example.emlody.LoadingAlert;
import com.example.emlody.R;
import com.example.emlody.Utils.RealPathUtil;
import com.example.emlody.Utils.ResponseServer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    Dialog playlistsDialog;

    Dialog emotionsDialog;

    TextView goBackText, tellText, doneText;

    String[] emotions = {"Happy", "Sad", "Angry", "Exited", "Nervous", "Fear"};
    ArrayAdapter<String> adapter;

    List<String> chosenEmotionsByUser;

    ListView emotionsListView;

    ArrayList emotionsArrayList;



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
            .url("http://192.168.1.218:9000/app")
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
                public void onResponse(@NonNull Call call, final Response response) throws IOException {
                    if(response.body()!=null){
                        String url = response.body().string();
                        if (response.code() == 200) {
                            runOnUiThread(() -> {
                                loadingAlert.closeAlertDialog();
                                Intent intent = new Intent(AnalyzeEmotionActivity.this, PlaylistsActivity.class);
                                intent.putExtra("EXTRA_MESSAGE", url);
                                startActivity(intent);
                            });
                        }
                        else if(response.code() == 204) {
                            runOnUiThread(() -> {
                                loadingAlert.closeAlertDialog();
                                EmotionNotFoundDialog dialog = new EmotionNotFoundDialog(AnalyzeEmotionActivity.this);
                                dialog.show();
                            });

                        }
                        else{
                            Gson gson = new Gson(); // Or use new GsonBuilder().create();
                            ResponseServer serverResponse = gson.fromJson(url, ResponseServer.class);
                            runOnUiThread(() -> {
                                loadingAlert.closeAlertDialog();
                                Toast.makeText(AnalyzeEmotionActivity.this, serverResponse.getError(), Toast.LENGTH_LONG).show();
                            });
                        }
                }
            }});
    }

    private void showPlaylists(ResponseServer serverRes) {
        PlaylistsActivity playlistsActivity = new PlaylistsActivity();
        Intent intent = new Intent(this, PlaylistsActivity.class);
        intent.putExtra("EXTRA_MESSAGE", serverRes.getPlaylistUrl());
        for (Map.Entry<String, String> playlist : serverRes.getPlaylistsUrls().entrySet()) {
            //playlistsActivity.addPlaylistIcon(playlist.getKey(), playlist.getValue());
        }
        startActivity(intent);
    }

    public void showPlayListsDialog() {

        playlistsDialog = new Dialog(AnalyzeEmotionActivity.this);
        playlistsDialog.setContentView(R.layout.playlists_dialog_layout);
        playlistsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playlistsDialog.setCancelable(false);
        playlistsDialog.getWindow().getAttributes().windowAnimations = R.style.animation;
        tellText = findViewById(R.id.okay_text);
        goBackText = findViewById(R.id.cancel_text);
        goBackText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistsDialog.dismiss();
            }
        });

        tellText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistsDialog.dismiss();
                showEmotionsDialog();
            }
        });
    }

    public void showEmotionsDialog() {
        chosenEmotionsByUser = new ArrayList<>();
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

        emotionsDialog.show();
    }
}




