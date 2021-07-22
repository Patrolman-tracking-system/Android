package com.example.indianrailways;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.indianrailways.LocTracking.Constants;
import com.example.indianrailways.LocTracking.LoctionService;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.indianrailways.Tracking.REQUEST_CODE_LOCATION_PERMISSION;

public class MainPage extends AppCompatActivity {


    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private int seconds = 0;

    private boolean running;

    private boolean wasRunning;
    Button startTracking, stopTracking;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

            Toolbar toolbar = findViewById(R.id.toolbar);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                setSupportActionBar(toolbar);
            }


//        ActionBar actionBar;
//
//        actionBar = getActionBar();
//        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#FFFFFF"));
//        actionBar.setBackgroundDrawable(colorDrawable);

//        report = findViewById(R.id.reportIncident);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }


        if (savedInstanceState != null) {
            seconds
                    = savedInstanceState
                    .getInt("seconds");
            running
                    = savedInstanceState
                    .getBoolean("running");
            wasRunning
                    = savedInstanceState
                    .getBoolean("wasRunning");
        }
//        runTimer();


//        track=findViewById(R.id.track);
//        track.setOnClickListener(new Track());
        startTracking = findViewById(R.id.startTrackingButton);
        stopTracking = findViewById(R.id.stopTrackingButton);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        stopTracking.setEnabled(true);

                    }
                }, new IntentFilter(LoctionService.ACTION_LOCATION_BROADCAST)
        );

        startTracking.setOnClickListener(v -> {
            locService();
        });

        stopTracking.setOnClickListener(v -> {
            Toast.makeText(this, "Patrolling Stopped", Toast.LENGTH_SHORT).show();
            running = false;
            stopLocationService();
        });

    }

    private void locService() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && !enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        stopTracking.setEnabled(false);
        Toast.makeText(this, "Patrolling Started", Toast.LENGTH_SHORT).show();
        running = true;
        startLocationService();
    }
//
//    @Override
////    public boolean onCreateOptionsMenu(Menu menu){
////        getMenuInflater().inflate(R.menu.appbar_menu, menu);
////        return true;
////
////    }
//        @Override
//        public void onSaveInstanceState(
//                @NonNull Bundle savedInstanceState) {
//            super.onSaveInstanceState(savedInstanceState);
//            savedInstanceState
//                    .putInt("seconds", seconds);
//            savedInstanceState
//                    .putBoolean("running", running);
//            savedInstanceState
//                    .putBoolean("wasRunning", wasRunning);
//        }
//        private void runTimer()
//        {
//            final TextView timeView
//                    = findViewById(
//                    R.id.tv);
//            final Handler handler
//                    = new Handler();
//
//            handler.post(new Runnable() {
//                @Override
//
//                public void run()
//                {
//                    int hours = seconds / 3600;
//                    int minutes = (seconds % 3600) / 60;
//                    int secs = seconds % 60;
//                    String time
//                            = String
//                            .format(Locale.getDefault(),
//                                    "%d:%02d:%02d", hours,
//                                    minutes, secs);
//                    timeView.setText(time);
//                    if (running) {
//                        seconds++;
//                    }
//                    handler.postDelayed(this, 1000);
//                }
//            });
//        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startLocationService();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }


//        report.setOnClickListener(new Report());


//    private class Report implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            startActivity(new Intent(MainPage.this, com.example.indianrailways.ReportIncident.class));
//        }
//    }
//
//    private class Track implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            startActivity(new Intent(MainPage.this, com.example.indianrailways.Tracking.class));
////        }
//    }



    private boolean isLocationServiceRunning() {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                for (ActivityManager.RunningServiceInfo service :
                        activityManager.getRunningServices(Integer.MAX_VALUE)) {
                    if (LoctionService.class.getName().equals(service.service.getClassName())) {
                        if (service.foreground) {
                            return true;
                        }
                    }
                }
                return false;
            }
            return false;
        }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire(10*60*1000L /*10 minutes*/);

            Intent intent = new Intent(getApplicationContext(), LoctionService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("TAG", "startLocationService: Foreground");
                startForegroundService(intent);
            }
            startService(intent);

//                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                WakeLock cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gps_service");
//                cpuWakeLock.acquire();



            Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LoctionService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);


            if(wakeLock.isHeld())
                wakeLock.release();


            startService(intent);
            Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
        }
    }
}