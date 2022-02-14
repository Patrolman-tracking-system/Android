package com.example.newrailways;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newrailways.Tracking.LocationService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainPage extends AppCompatActivity {
    TextView startText;
    final int Request_CODE_FOR_FINE_LOCATION = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
     int buttonCheck=0;
    public static final String MyPREFERENCES = "UserDetails";
    SharedPreferences sharedpreferences;
    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askLocationPermission();
        }
        displayLocationSettingsRequest(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        startText=findViewById(R.id.startText);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            setSupportActionBar(toolbar);
        }
        CardView start = findViewById(R.id.startTrackingButton);
//        Button stop = findViewById(R.id.stopTrackingButton);




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonCheck=1;
                if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    askLocationPermission();
                }
                displayLocationSettingsRequest(MainPage.this);
                if (ContextCompat.checkSelfPermission(MainPage.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    String flag = sharedpreferences.getString("flag", "1");
                    Log.d(TAG, "onClick: FLAG: "+flag);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
//                    if (flag == null) {
//                        editor.putString("flag", "1");
//                        flag="1";
//                    }

                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius( 17 );

                    if(flag.equals("1")){

                        shape.setColor(0xFFFE5352);
                        start.setBackground(shape);
                        startText.setText("STOP TRACKING");
                        editor.putString("flag", "0");
                        editor.apply();
                        startLocationService();

                    }
                    else{
                        shape.setColor(0xFF369B46);
                        start.setBackground(shape);
                        startText.setText("START TRACKING");
                        editor.putString("flag", "1");
                        editor.apply();
                        stopLocationService();
                    }
                }
            }
        });
    }
    synchronized private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(MainPage.this)
                        .setMessage("Need permission for location")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_CODE_FOR_FINE_LOCATION);

                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(MainPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_CODE_FOR_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Request_CODE_FOR_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MainPage.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(MainPage.this)
                            .setMessage("You have permanently denied this permission, goto settings to enable location")
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    gotoApplicationSettings();
                                }
                            }).setCancelable(false)
                            .show();
                }

            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(buttonCheck==1){
                    startLocationService();
                }
            }

        }
    }

    private void gotoApplicationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            status.startResolutionForResult(MainPage.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
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

            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(com.example.railwaygeolocation.tracking.Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(com.example.railwaygeolocation.tracking.Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
        }
    }
}