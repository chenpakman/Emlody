package com.example.moodio.Utils;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moodio.R;

import java.nio.ByteBuffer;

public class Test extends AppCompatActivity implements ImageReader.OnImageAvailableListener {

    private ImageReader imageReader;
    private HandlerThread imageProcessingThread;
    private Handler imageProcessingHandler;
    private boolean isProcessing = false;

    // Adjust the desired capture rate here (in milliseconds)
    private static final long CAPTURE_RATE = 1000; // 1 image per second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int imageReaderWidth = 640; // Set the desired width of the images
        int imageReaderHeight = 480; // Set the desired height of the images

        // Create the ImageReader to capture frames from the camera
        imageReader = ImageReader.newInstance(imageReaderWidth, imageReaderHeight, ImageFormat.YUV_420_888, 2);

        // Start the background thread for image processing
        imageProcessingThread = new HandlerThread("ImageProcessingThread");
        imageProcessingThread.start();
        imageProcessingHandler = new Handler(imageProcessingThread.getLooper());

        // Set the OnImageAvailableListener to receive images
        imageReader.setOnImageAvailableListener(this, imageProcessingHandler);

        // Schedule the capture of images at the desired rate
        imageProcessingHandler.postDelayed(this::captureImage, CAPTURE_RATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up resources
        imageProcessingThread.quitSafely();
        imageReader.close();
    }

    private void captureImage() {
        // Check if image processing is ongoing, and wait for next capture if it's still processing
        if (isProcessing) {
            imageProcessingHandler.postDelayed(this::captureImage, CAPTURE_RATE);
            return;
        }

        // Request a new image from the ImageReader
        Image image = imageReader.acquireLatestImage();
        if (image != null) {
            // Process the image here
            processImage(image);

            // Close the image to release resources
            image.close();
        }

        // Schedule the next capture after the desired rate
        imageProcessingHandler.postDelayed(this::captureImage, CAPTURE_RATE);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        // The onImageAvailable callback will not be used in this implementation,
        // as we handle image capture using the captureImage method.
    }

    private void processImage(Image image) {
        // Image processing logic goes here
        // For example, you can convert the image to a Bitmap and perform face detection.
    }
}

