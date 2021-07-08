package com.example.indianrailways.LocTracking;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.indianrailways.R;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoctionService extends Service {
    double lat = 0, long1 = 0;
    DatabaseReference reff;
    FirebaseFirestore fStore;
    com.example.indianrailways.LocTracking.Track track;
    double initialDist1 = Double.MAX_VALUE;
    int maxAllowedDeviation = 3;
    public static final String ACTION_LOCATION_BROADCAST = LoctionService.class.getName() + "LocationBroadcast";
    long tripId = 0;
    int objectCount = 0;
    LocalTime dt;
    double[] arrayLat = new double[5];
    double[] arrayLong = new double[5];
    boolean[] status = new boolean[]{false, false, false, false, false};
    double threshold = 20;
    int curDevCount = 0;
    WindowManager windowManager2;
    WindowManager.LayoutParams params;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(arrayLat[0]!=0.0 && arrayLong[0]!=0.0) {
                locationResult.getLastLocation();
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                double speed = locationResult.getLastLocation().getSpeed();
//            if (lat!=latitude && long1!=longitude) {


//                Toast.makeText(LoctionService.this, "Lat = " + locationResult.getLastLocation().getLatitude() + " Long: " + locationResult.getLastLocation().getLongitude(), Toast.LENGTH_LONG).show();
                lat = latitude;
                long1 = longitude;
                if (speed >= 2.5) {
                    Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/quite_impressed.mp3");
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(LoctionService.this, default_notification_channel_id)
                            .setSmallIcon(R.drawable.warning)
                            .setContentTitle("Alert")
                            .setSound(sound)
                            .setContentText("You are going too fast");
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build();
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel notificationChannel = new
                                NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                        notificationChannel.setSound(sound, audioAttributes);
                        mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                        assert mNotificationManager != null;
                        mNotificationManager.createNotificationChannel(notificationChannel);
                    }
                    assert mNotificationManager != null;
                    mNotificationManager.notify((int) System.currentTimeMillis(),
                            mBuilder.build());
                } else {
//                    Log.d("TAG", "onLocationResult: true");

                    if (objectCount == 5) {
                        tripId++;
                        updateTID(tripId);
                        objectCount = 6;
                        sendBroadcastMessage();
                    }
                    if (objectCount < arrayLat.length && tripId <= 4) {
//                for (double v : arrayLat) {
//                    Log.d("TAG", "onLocationResult lat: " + v);
//                }
//                for (double v : arrayLong) {
//                    Log.d("TAG", "onLocationResult long: " + v);
//                }
//                        Log.d("TAG", "onLocationResult: lat " + Arrays.toString(arrayLat));
//                        Log.d("TAG", "onLocationResult: long " + Arrays.toString(arrayLong));

                        float[] result = new float[1];
//                        prevDistance = curDistance;
                        Location.distanceBetween(latitude, longitude, arrayLat[objectCount], arrayLong[objectCount], result);
                        track.setLat(String.valueOf(latitude));
                        track.setLong1(String.valueOf(longitude));
                        track.setSped(String.valueOf(speed));
                        track.setObjCount(objectCount);
                        track.setCurrent(String.valueOf(result[0]));
                        track.setInit(String.valueOf(initialDist1));
                        track.setdevCount(String.valueOf(curDevCount));
                        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                        track.setId(ip);
                        reff.push().setValue(track);
//                        curDistance = result[0];
//                        calcDistance = curDistance - prevDistance;
//                        Log.d("TAG", "onLocationResult: calc Distance " + calcDistance);
                        Log.d("TAG", "onLocationResult: result " + result[0]);
//                        Log.d("TAG", "Prev distance = " + prevDistance + " current distance = " + curDistance);
                        Log.d("TAG", "onLocationResult: current dev = " + curDevCount);
//                        Log.d("TAG", "onLocationResult: current distance  = " + result[0]);
//                        Log.d("TAG", "onLocationResult: initial distance  = " + initialDist1);
                        if (result[0] < initialDist1 && curDevCount < maxAllowedDeviation) {
                            if (result[0] <= threshold) {
                                initialDist1 = Double.MAX_VALUE;
                                status[objectCount] = true;
                                curDevCount = 0;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    dt = LocalTime.now();
                                }
//                                Log.d("TAG", "onLocationResult: TRUE");
                                statusUpdate(true, objectCount, latitude, longitude, speed, dt, tripId);
                                objectCount++;
                            } else {
                                initialDist1 = result[0];
                            }
                        } else if (curDevCount >= maxAllowedDeviation) {
                            initialDist1 = Double.MAX_VALUE;
                            status[objectCount] = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                dt = LocalTime.now();
                            }
//                            Log.d("TAG", "onLocationResult: FALSE");
                            statusUpdate(false, objectCount, latitude, longitude, speed, dt, tripId);
                            objectCount++;
                            curDevCount = 0;
                        } else if (result[0] > initialDist1 && (result[0] - initialDist1) > 20) {
                            Log.d("TAG", "onLocationResult: result " + result[0]);
                            Log.d("TAG", "onLocationResult: init dist " + initialDist1);
//                            Toast.makeText(LoctionService.this, "Current distance: " + result[0] + " Init Dist: " + initialDist1, Toast.LENGTH_SHORT).show();
                            initialDist1 = result[0];
                            curDevCount += 1;
                        }

                        Log.d("TAG", "onLocationResult: Obj count " + objectCount);
                    }
                }
//            }
            }
            Log.d("TAG", "onLocationResult: status"+Arrays.toString(status));
        }
        private void showCustomPopupMenu()
        {
            windowManager2 = (WindowManager)getSystemService(WINDOW_SERVICE);
            LayoutInflater layoutInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view=layoutInflater.inflate(R.layout.activity_tracking, null);
            params=new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity= Gravity.CENTER;
            params.x = 0;
            params.y = 0;
            windowManager2.addView(view, params);
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    public LoctionService() {


    }

    void reverseArray(double[] ar) {
        double temp = 0;
        int h = ar.length - 1, l = 0;
        while (h > l) {
            temp = ar[l];
            ar[l] = ar[h];
            ar[h] = temp;
            l++;
            h--;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        track = new com.example.indianrailways.LocTracking.Track();
        reff = FirebaseDatabase.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();

        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference applicationsRef = rootRef.collection("Original Coordinates");
//        DocumentReference applicationIdRef = applicationsRef.document("Station pair1");
        DocumentReference applicationIdRef = applicationsRef.document("Station pair1");
        applicationIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Double>> users = (List<Map<String, Double>>) document.get("Aurangabad");
                    int i = 0;
                    assert users != null;
                    for (Map<String, Double> al : users) {
                        arrayLat[i] = al.get("Latitude");
                        arrayLong[i] = al.get("Longitude");
                        i++;
                    }
                }
            }
        });

        CollectionReference applicationsRef1 = rootRef.collection("Tracking");
            DocumentReference applicationIdRef1 = applicationsRef1.document("Duty1");
        applicationIdRef1.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Map<String, Object> data = document.getData();
                    assert data != null;
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        if (entry.getKey().equals("Patrolman1")) {
                            Map<String, Object> patrolman = (Map<String, Object>) entry.getValue();
                            for (Map.Entry<String, Object> e : patrolman.entrySet()) {
                                if (e.getKey().equals("Trips")) {
                                    Map<String, Object> tid = (Map<String, Object>) e.getValue();
                                    for (Map.Entry<String, Object> dataEntry : tid.entrySet()) {
                                        if (dataEntry.getKey().equals("TripCount")) {
                                            tripId = (Long) dataEntry.getValue();
//                                            Log.d("TAG", "TID : " + tripId);
                                            if (tripId % 2 != 0) {
                                                reverseArray(arrayLat);
                                                reverseArray(arrayLong);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

    }

    void statusUpdate(boolean status, int ind, double lat, double long1, double speed, LocalTime dt, double tID) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference applicationsRef = rootRef.collection("Tracking");
        DocumentReference applicationIdRef = applicationsRef.document("Duty1");
        int trip1 = (int) tID;
        String trip = Integer.toString(trip1);

        applicationIdRef.update("Patrolman1.Trips.Trip" + trip + "." + ind + ".Status", status,
                "Patrolman1.Trips.Trip" + trip + "." + ind + ".Latitude", lat,
                "Patrolman1.Trips.Trip" + trip + "." + ind + ".Longitude", long1,
                "Patrolman1.Trips.Trip" + trip + "." + ind + ".Speed", speed,
                "Patrolman1.Trips.Trip" + trip + "." + ind + ".Time", dt.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("TAG", "onComplete: SUCCESS");
            }
        });
    }

    void updateTID(long id) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference applicationsRef = rootRef.collection("Tracking");
        DocumentReference applicationIdRef = applicationsRef.document("Duty1");
//        Log.d("TAG", "statusUpdate: index = " + ind);
        applicationIdRef.update("Patrolman1.Trips.TripCount", id).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("TAG", "onComplete: SUCCESS ID");
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentTitle("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by Location Service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); //1:6 60000 13000 20
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }
    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            String action=intent.getAction();
            if (action!=null){
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }

    private void sendBroadcastMessage() {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}