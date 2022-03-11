package com.example.newrailways.Tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.newrailways.APIdata.LocationPojo;
import com.example.newrailways.APIdata.PostLocationData;
import com.example.newrailways.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;


public class LocationService extends Service {
    LocationPojo data=new LocationPojo();
    public static final String MyPREFERENCES = "UserDetails";
    SharedPreferences sharedpreferences;
    String userID="";
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            locationResult.getLastLocation();
            double latitude = locationResult.getLastLocation().getLatitude();
            double longitude = locationResult.getLastLocation().getLongitude();
            double speed=locationResult.getLastLocation().getSpeed();
            data.setlatitude(String.valueOf(latitude));
            data.setlongitude(String.valueOf(longitude));
            data.setSpeed(String.valueOf(speed));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && data.getlatitude()!=null && data.getlongitude()!=null) {
                Date instant=  Timestamp.from(Instant.now());
//            Timestamp timestamp= Timestamp.valueOf(instant.toString());
                data.setTimeStamp(instant.toString());
            }
            if (locationResult.getLastLocation().hasAccuracy()){
                new PostLocationData(getApplicationContext(),data.getUserID(),data.getlatitude(),data.getlongitude(),data.getSpeed(),data.getTimeStamp());
            }
            else{
                new PostLocationData(getApplicationContext(),data.getUserID(),data.getlatitude(),data.getlongitude(),"NA",data.getTimeStamp());
            }
            Log.d("TAG", "onLocationResult: LAT "+latitude+" LONG "+longitude+" TIME "+ data.getTimeStamp());

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.warning);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);

            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(com.example.railwaygeolocation.tracking.Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        userID = sharedpreferences.getString("UserID", null);
        data.setUserID(userID);


        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(com.example.railwaygeolocation.tracking.Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(com.example.railwaygeolocation.tracking.Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }
}
