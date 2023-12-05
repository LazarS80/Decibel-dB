package com.aaappps.app7562;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1;
    private MediaRecorder mediaRecorder;
    private ImageView emojiImageView;

// Constants for decibel calculation
private static final double MAX_AMPLITUDE_THRESHOLD = 12230.0;
private static final double REFERENCE = 0.03;
private static final double BASE_SENSITIVITY = 12.04;

// New settings
private double jumpIntensity = 30.0; // Ugrás intenzitása

private void updateDecibelLevel() {
    runOnUiThread(() -> {
        if (mediaRecorder != null) {
            try {
                double amplitude = mediaRecorder.getMaxAmplitude();

                if (amplitude > 0) {
                    // Limit amplitude value to MAX_AMPLITUDE_THRESHOLD
                    double normalizedAmplitude = Math.min(amplitude, MAX_AMPLITUDE_THRESHOLD) / MAX_AMPLITUDE_THRESHOLD;

                    // Calculate decibel level
                    double decibelLevel = jumpIntensity * Math.log10(normalizedAmplitude / REFERENCE) * BASE_SENSITIVITY;

                    updateDecibelMeter(decibelLevel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error updating decibel level: " + e.getMessage());
            }
        }
    });
}

    private void updateDecibelMeter(double decibelLevel) {
        runOnUiThread(() -> {
            // Add logic here to make the emoji jump based on decibelLevel
            // Example: emojiImageView.animate().translationY(-decibelLevel).setDuration(200);
emojiImageView.animate().translationY((float) -decibelLevel).setDuration(200);

        });
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile("/dev/null");

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error setting up MediaRecorder: " + e.getMessage());
        }
    }

    private void stopMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emojiImageView = findViewById(R.id.emojiImageView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            setupMediaRecorder();
            startDecibelMonitoring();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMediaRecorder(); // Stop MediaRecorder when the activity is destroyed
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMediaRecorder();
                startDecibelMonitoring();
            } else {
                showToast("Permission denied. App may not function as expected.");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startDecibelMonitoring() {
        // Create a handler to periodically update decibel level and trigger emoji animation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDecibelLevel();
                handler.postDelayed(this, 100); // Update every 100 milliseconds
            }
        }, 100);
    }
}
