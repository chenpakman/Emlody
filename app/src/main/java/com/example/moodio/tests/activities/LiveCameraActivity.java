package com.example.moodio.tests.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moodio.R;
import com.example.moodio.Utils.ResponseServer;
import com.example.moodio.tests.utils.CameraConnectionFragment;
import com.example.moodio.tests.utils.ImageUtils;
import com.example.moodio.tests.utils.SpotifyManager;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LiveCameraActivity extends AppCompatActivity implements ImageReader.OnImageAvailableListener {
    Handler handler;
    private int sensorOrientation;

    private SpotifyManager mSpotifyManager;
    private static final String GIF_URL = "file:///android_res/drawable/sound.gif";
    private WebView boomboxWebView;

    private String TITLE = "Currently playing the ";

    private TextView title;

    @Override
    protected void onStart() {
        super.onStart();
        mSpotifyManager.initializeSpotify();
    }



    @Override
    protected void onStop() {
        mSpotifyManager.stopMusic();
        super.onStop();
        mSpotifyManager.disconnect();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        mSpotifyManager.resumeMusic();
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera);
        handler = new Handler();

        title = findViewById(R.id.titleTextView);
        mSpotifyManager = new SpotifyManager(this);

        boomboxWebView = findViewById(R.id.boomboxWebView);
        WebSettings webSettings = boomboxWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        boomboxWebView.loadUrl(GIF_URL);

        //TODO ask for camera permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.CAMERA}, 121);
            }else{
                //TODO show live camera footage
                setFragment();
            }
        } else {
            //TODO show live camera footage
            setFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO show live camera footage
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setFragment();
        } else {

        }
    }

    //TODO fragment which show llive footage from camera
    int previewHeight = 0, previewWidth = 0;

    protected void setFragment(){
        String cameraId = null;

        /*CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraId = cameraManager.getCameraIdList()[0];*/
        cameraId = findFrontCameraId();


        Fragment fragment;
        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        new CameraConnectionFragment.ConnectionCallback() {
                            @Override
                            public void onPreviewSizeChosen(final Size size, final int rotation) {
                                previewHeight = size.getHeight();
                                previewWidth = size.getWidth();
                                sensorOrientation = rotation - getScreenOrientation();
                            }
                        },
                        this,
                        R.layout.camera_fragment,
                        new Size(640, 480));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    private String findFrontCameraId() {
        String frontCameraId = null;
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            frontCameraId = cameraManager.getCameraIdList()[0];

            /*for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId;
                    break;
                }
            }*/


            // If no front camera is found, use the default back camera
            /*if(frontCameraId == null){
                frontCameraId = cameraManager.getCameraIdList()[0];
            }*/
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        return frontCameraId ;
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }


    //TODO getting frames of live camera footage and passing them to model
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Bitmap rgbFrameBitmap;

    private FaceDetector detector;

    private boolean isUploadingImage = false;

    private static final int GALLERY_REQ_CODE=1000;

    @Override
    public void onImageAvailable(ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close(); //todo: might need to delete for image rotating
                            isProcessingFrame = false;
                        }
                    };

            processImage(image);

        } catch (final Exception e) {
            Log.d("tryError",e.getMessage());
            return;
        }

    }

    //TODO: add detector code here
    private void processImage(Image image) {

        detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.NO_LANDMARKS)
                .setMode(com.google.android.gms.vision.face.FaceDetector.FAST_MODE)
                .build();

        if (!detector.isOperational()) {
            Toast.makeText(this, "Face detector could not be set up on your device",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            imageConverter.run();
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
            rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
            //Do your work here
            Frame frame = new Frame.Builder().setBitmap(rgbFrameBitmap).build();
            SparseArray<Face> faces = detector.detect(frame);
            if(faces.size() > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LiveCameraActivity.this, "Hey There :)",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                // Before compressing the image
                //Bitmap originalBitmap = BitmapFactory.decodeFile("path_to_original_image");
                Matrix matrix = new Matrix();
                //matrix.postRotate(getImageOrientation("path_to_original_image"));
                matrix.postRotate(sensorOrientation);

                // Rotate the image if needed
                Bitmap rotatedBitmap = Bitmap.createBitmap(rgbFrameBitmap, 0, 0, rgbFrameBitmap.getWidth(), rgbFrameBitmap.getHeight(), matrix, true);

                // Compress and save the rotated image
                try {
                    FileOutputStream outputStream = new FileOutputStream("output_path.jpg");
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                    outputStream.close();
                }catch (IOException e){

                }

                Log.d("LiveCameraActivity", "FACE DETECTED ");

                File imageFile = saveBitmapToFile(rotatedBitmap);
                Log.d("LiveCameraActivity", "created file from bitmap " + imageFile.getPath());

                uploadImageToServer(imageFile);
                Log.d("LiveCameraActivity", "uploaded file to server");

            }
        }

        postInferenceCallback.run();

    }

    public int getImageOrientation(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return getRotationAngleForOrientation(orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getRotationAngleForOrientation(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }


    private File saveBitmapToFile(Bitmap bitmap) {
        File outputDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outputFile = createImageFile(outputDir);

        try {
            FileOutputStream outStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile;
    }




    private File createImageFile(File outputDir) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        try {
            return File.createTempFile(imageFileName, ".jpg", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void uploadImageToServer(File file){
        if(!isUploadingImage) {
            isUploadingImage = true;
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
            ActivityCompat.requestPermissions(LiveCameraActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQ_CODE);
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
                            Log.e("LiveCameraActivity", "couldnt connect to server " + e.getMessage());

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                            if (response.body() != null) {
                                String responseBody = response.body().string();
                                Gson gson = new Gson(); // Or use new GsonBuilder().create();
                                ResponseServer serverResponse = gson.fromJson(responseBody, ResponseServer.class);
                                if (response.code() == 200) {
                                    mSpotifyManager.playPlaylist(serverResponse.getDefaultPlaylistUrl(), serverResponse.getEmotion());
                                    Log.d("LiveCameraActivity", "Retrieved playlist");

                                } else if (response.code() == 204) {
                                    Log.e("LiveCameraActivity", "didn't get a playlist, response code: " + response.code());

                                } else {
                                    Log.e("LiveCameraActivity", "didn't get a playlist, response code:" + response.code());

                                }
                            }
                            Log.e("LiveCameraActivity", "didn't get a playlist response was null " + response.code());
                            response.close();
                        }
                    });

        }

        isUploadingImage = false;
    }




    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }
}