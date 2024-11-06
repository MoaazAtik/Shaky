package com.thewhitewings.shaky;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnOn, btnOff;
    private MediaAndSensorViewModel mediaAndSensorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaAndSensorViewModel = new ViewModelProvider(this).get(MediaAndSensorViewModel.class);

        mediaAndSensorViewModel.getIsActive().observe(this, isPlaying -> {
            btnOff.setText(isPlaying ? "Pause" : "Play");
        });

        btnOff = findViewById(R.id.btn_off);
        btnOff.setEnabled(true);
        btnOff.setOnClickListener(v -> {
            mediaAndSensorViewModel.activate();
        });

        findViewById(R.id.btn_more).setOnClickListener(v -> {
            mediaAndSensorViewModel.deactivate();
        });


        // Request POST_NOTIFICATIONS permission for the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaAndSensorViewModel.deactivate();
        Log.d(TAG, "onDestroy: ");
    }
}
