package com.thewhitewings.shaky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnOn, btnOff;
    private MediaAndSensorViewModel mediaAndSensorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaAndSensorViewModel = new ViewModelProvider(this).get(MediaAndSensorViewModel.class);

        // Observe playback state and current track title for UI updates
        mediaAndSensorViewModel.getCurrentTrack().observe(this, trackTitle -> {
            // Update UI with track title
        });

        mediaAndSensorViewModel.getIsPlaying().observe(this, isPlaying -> {
            // Update UI with play/pause status
            btnOff.setText(isPlaying ? "Pause" : "Play");
        });

        btnOff = findViewById(R.id.btn_off);
        btnOff.setEnabled(true);
        btnOff.setOnClickListener(v -> {
            mediaAndSensorViewModel.play();
            Log.d(TAG, "onCreate: btnOff click");
        });

        findViewById(R.id.btn_more).setOnClickListener(v -> {
            mediaAndSensorViewModel.stop();
            Log.d(TAG, "onCreate: btnMore click");
        });


        // Request POST_NOTIFICATIONS permission for the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaAndSensorViewModel.stop();
        Log.d(TAG, "onDestroy: ");
    }
}
