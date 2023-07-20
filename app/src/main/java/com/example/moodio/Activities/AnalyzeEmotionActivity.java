package com.example.moodio.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.example.moodio.EmotionNotFoundDialog;
import com.example.moodio.LoadingAlert;
import com.example.moodio.R;
import com.example.moodio.Utils.RealPathUtil;
import com.example.moodio.Utils.ResponseServer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


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
    GoogleSignInAccount account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_emotion);
        getWindow().setStatusBarColor(Color.BLACK);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        checkForPermission();
        cameraFloatingActionButton=findViewById(R.id.cameraFloatingActionButton);
        galleryFloatingActionButton=findViewById(R.id.floatingActionButton);
        title=findViewById(R.id.vibesTextView);
        buttonList.add(cameraFloatingActionButton);
        buttonList.add(galleryFloatingActionButton);
        imageView=findViewById(R.id.choosenImageView);
        imageUri=createUri();
        buttonAnimation();
        registerPictureCameraLauncher();
        registerPictureGalleryLauncher();
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_LOCATION_BOUNDING_BOX,FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    1, // e.g. 1
                    account,
                    fitnessOptions);
        } else {
          //accessGoogleFit(fitnessOptions,account);
            //measureHeartRate(account);
        }


        cameraFloatingActionButton.setOnClickListener(v -> checkCameraPermissionsAndOpenCamera());
        galleryFloatingActionButton.setOnClickListener(v -> {
            Intent iGallery = new Intent(Intent.ACTION_PICK);
            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imageLauncher.launch("image/*");
        });

    }


    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Process heart rate data
            float heartRate = event.values[0];
            System.out.println("measured! "+heartRate);
            // Handle heart rate value
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle accuracy changes if needed
        }
    };
private void checkForSensors()
{
    FitnessOptions fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_DELTA).build();

// Note: Fitness.SensorsApi.findDataSources() requires the
// ACCESS_FINE_LOCATION permission.
    Fitness.getSensorsClient(getApplicationContext(), GoogleSignIn.getAccountForExtension(getApplicationContext(), fitnessOptions))
            .findDataSources(
                    new DataSourcesRequest.Builder()
                            .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                            .setDataSourceTypes(DataSource.TYPE_RAW)
                            .build())
            .addOnSuccessListener(dataSources -> {
                dataSources.forEach(dataSource -> {
                    System.out.println("Data source found: ${it.streamIdentifier}");
                    System.out.println("Data Source type: ${it.dataType.name}");

                    if (dataSource.getDataType() == DataType.TYPE_STEP_COUNT_DELTA) {
                        System.out.println( "Data source for STEP_COUNT_DELTA found!");

                    }
                });})
            .addOnFailureListener(e ->
                    System.out.println("Find data sources request failed"+ e.getMessage()));
}

    private void accessGoogleFit(FitnessOptions fitnessOptions, GoogleSignInAccount account) {


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start= end.minusYears(1);
           /* long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();
            long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();*/
            long endTime = System.currentTimeMillis();  // Current time
            long startTime = endTime - TimeUnit.DAYS.toMillis(20);
            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_HEART_RATE_BPM)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .enableServerQueries()
                    .build();
            Fitness.getHistoryClient(this, account)
                    .readData(readRequest)
                    .addOnSuccessListener(res->
                            getHeartHistory(res.getBuckets())
                    )
                    .addOnFailureListener( e -> System.out.println("error!"+e));
                 }
    }



private void getHeartHistory(List<Bucket> buckets){
    for (Bucket bucket : buckets) {
        List<DataSet> dataSets = bucket.getDataSets();
        for (DataSet dataSet : dataSets) {
            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                float heartRate = dataPoint.getValue(Field.FIELD_AVERAGE).asFloat();
                System.out.println("heartRate"+heartRate);
            }
        }
    }

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
        String path=RealPathUtil.getRealPath(AnalyzeEmotionActivity.this,result);
        reorderUi(result);
        imageFile=new File(path);
        selectFromGallery();
    });
}
private void reorderUi(Uri imageUri){
  imageView.setImageURI(imageUri);

    title.animate().alpha(1f).y(100);
    cameraFloatingActionButton.animate().alpha(1f).y(1960);
    galleryFloatingActionButton.animate().alpha(1f).y(1710);
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
        runOnUiThread(() -> loadingAlert.startAlertDialog());
        uploadImageToServer(imageFile, loadingAlert);
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
            //.url("http://192.168.1.218:9000/app")
            .url("http://192.168.1.34:9000/app")
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



private void buttonAnimation(){
    for (Button b:buttonList) {
        b.setAlpha(0f);
        b.setTranslationY(1000);
        b.animate().alpha(1f).translationYBy(-1000).setDuration(1500);
    }
}
    public void requestPlayLists(String emotions) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
               // .url("http://192.168.1.218:9000/app?emotions=" + emotions)
                .url("http://192.168.1.34:9000/app?emotions=" + emotions)
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

}




