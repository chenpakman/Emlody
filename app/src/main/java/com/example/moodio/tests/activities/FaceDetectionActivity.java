package com.example.moodio.tests.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.moodio.Activities.PlaylistsActivity;
import com.example.moodio.R;
import com.example.moodio.Utils.ResponseServer;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;

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

public class FaceDetectionActivity extends AppCompatActivity {

    private SurfaceView cameraView;
    private CameraSource cameraSource;

    private static final int GALLERY_REQ_CODE=1000;
    private String currentDetectedMood = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_face_detection);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //cameraView = findViewById(R.id.camera_view);

        // Check camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        if (!detector.isOperational()) {
            Toast.makeText(this, "Face detector could not be set up on your device",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        cameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(1.0f)
                .setAutoFocusEnabled(true)
                .build();


        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(FaceDetectionActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        detector.setProcessor(new Detector.Processor<Face>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Face> detections) {

            }

            /*@Override
            public void receiveDetections(@NonNull Detector.Detections<Face> detections) {
                SparseArray<Face> detectedFaces = detections.getDetectedItems();

                // Ensure that at least one face is detected
                if (detectedFaces.size() > 0) {
                    Face face = detectedFaces.valueAt(0); // Get the first detected face

                    // Get the frame data as a byte array
                    //byte[] data = cameraSource.getPreviewFrameBytes();


                    // Get frame properties
                    int frameWidth = cameraSource.getPreviewSize().getWidth();
                    int frameHeight = cameraSource.getPreviewSize().getHeight();
                    // Calculate the face bounding box coordinates
                    float left = face.getPosition().x;
                    float top = face.getPosition().y;
                    float width = face.getWidth();
                    float height = face.getHeight();

                    // Ensure the bounding box is within the frame dimensions
                    left = Math.max(0, left);
                    top = Math.max(0, top);
                    width = Math.min(frameWidth - left, width);
                    height = Math.min(frameHeight - top, height);

                    // Create a Bitmap from the frame data
                    //Bitmap bitmapFrame = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Bitmap bitmapFrame = BitmapFactory.dec
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90); // Adjust the rotation angle as needed
                    bitmapFrame = Bitmap.createBitmap(bitmapFrame, 0, 0, frameWidth, frameHeight, matrix, true);

                    // Crop the Bitmap to the face region
                    Bitmap bitmapFace = Bitmap.createBitmap(bitmapFrame, (int) left, (int) top, (int) width, (int) height);

                    // Now you have a Bitmap image (bitmapFace) representing the detected face region.
                    // You can use it for further processing or save it to storage.
                    // For example, to save the image to storage:

                    // Note: Don't forget to handle storage permissions in your AndroidManifest.xml
                    // and request runtime permissions if targeting API 23 and above.

                    // Save the Bitmap as an image file
                    File outputDir = getOutputMediaDir();
                    if (outputDir != null) {
                        String fileName = "face_image.jpg";
                        File imageFile = new File(outputDir, fileName);

                        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                            bitmapFace.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            fos.close();
                            // Now the Bitmap image of the detected face region is saved to imageFile as a JPEG.
                            // You can access the image using imageFile.getAbsolutePath().
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }*/

            /*public void receiveDetections(@NonNull Detector.Detections<Face> detections) {
                SparseArray<Face> faces = detections.getDetectedItems();
                if (faces.size() > 0) {
                    Log.d("FaceDetection", "Face detected");
                    if (cameraSource != null) {
                        cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                            @Override
                            public void onPictureTaken(@NonNull byte[] bytes) {
                                try {
                                    File pictureFile = getOutputMediaFile();
                                    if (pictureFile == null) {
                                        Log.d("CameraSource", "Error creating media file, check storage permissions.");
                                        return;
                                    }

                                    FileOutputStream fos = new FileOutputStream(pictureFile);
                                    fos.write(bytes);
                                    fos.close();
                                    Toast.makeText(FaceDetectionActivity.this, "Picture saved: " + pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                    uploadImageToServer(pictureFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    *//*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FaceDetectionActivity.this, "Face detected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });*//*
                }
            }*/
        });
        
        
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getOutputMediaFile() {
        // Replace "MyApp" with your app's name or folder name
        File mediaStorageDir = new File(getExternalFilesDir(null), "Moodio");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void uploadImageToServer(File file ){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        ActivityCompat.requestPermissions(FaceDetectionActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQ_CODE);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.218:9000/app")
                //.url("http://192.168.1.34:9000/app")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull final Call call, @NonNull IOException e) {

                    }
                    @Override
                    public void onResponse(@NonNull Call call, final Response response) throws IOException {
                        if(response.body()!=null){
                            Gson gson = new Gson();
                            String url = response.body().string();
                            ResponseServer serverResponse = gson.fromJson(url, ResponseServer.class);
                            if (response.code() == 200) {
                                if(currentDetectedMood == null || !currentDetectedMood.equals(serverResponse.getEmotion())) {
                                    //cameraSource.stop();
                                    currentDetectedMood = serverResponse.getEmotion();

                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(FaceDetectionActivity.this, PlaylistsActivity.class);
                                        intent.putExtra("EXTRA_MESSAGE", url);
                                        startActivity(intent);
                                    });
                                }
                            }
                        }
                    }});
    }
}

