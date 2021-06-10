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

import com.example.indianrailways.R;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoctionService extends Service {
    double lat = 0, long1 = 0;
    DatabaseReference reff;
    FirebaseFirestore fStore;
    com.example.indianrailways.LocTracking.Track track;
    double initialDist1 = 11000;
    int maxAllowedDeviation = 3;
    double curDistance=0,prevDistance=0,calcDistance=0;
    int objectCount = 0;
    double[] arrayLat = new double[5]; //to be fetched from db
    double[] arrayLong = new double[5];
    boolean[] status = new boolean[]{false, false, false, false, false};
    double threshold = 300;
    int curDevCount = 0;
    WindowManager windowManager2;
    WindowManager.LayoutParams params;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d("TAG", "onLocationResult:sd ");
            if(arrayLat[0]!=0.0 && arrayLong[0]!=0.0) {
                locationResult.getLastLocation();
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                double speed = locationResult.getLastLocation().getSpeed();
//            if (lat!=latitude && long1!=longitude) {
                track.setLat(String.valueOf(latitude));
                track.setLong1(String.valueOf(longitude));
                track.setSped(String.valueOf(speed));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    track.setSpeed2(String.valueOf(locationResult.getLastLocation().getSpeedAccuracyMetersPerSecond()));
                }
                WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                track.setId(ip);
                reff.push().setValue(track);
                Log.d("TAG", "onLocationResult: " + latitude + " " + longitude);

                Toast.makeText(LoctionService.this, "speed" + locationResult.getLastLocation().getSpeed(), Toast.LENGTH_SHORT).show();
                lat = latitude;
                long1 = longitude;
                Log.d("TAG", "onLocationResult: " + speed);
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
                    if (objectCount < arrayLat.length) {
//                for (double v : arrayLat) {
//                    Log.d("TAG", "onLocationResult lat: " + v);
//                }
//                for (double v : arrayLong) {
//                    Log.d("TAG", "onLocationResult long: " + v);
//                }
                        Log.d("TAG", "onLocationResult: lat " + Arrays.toString(arrayLat));
                        Log.d("TAG", "onLocationResult: long " + Arrays.toString(arrayLong));

                        float[] result = new float[1];
                        prevDistance = curDistance;
                        Location.distanceBetween(latitude, longitude, arrayLat[objectCount], arrayLong[objectCount], result);
                        curDistance = result[0];
                        calcDistance = curDistance - prevDistance;
                        Log.d("TAG", "onLocationResult: calc Distance " + calcDistance);
                        Log.d("TAG", "onLocationResult: result " + result[0]);
                        Log.d("TAG", "Prev distance = " + prevDistance + " current distance = " + curDistance);
                        if (result[0] < initialDist1 && curDevCount < maxAllowedDeviation) {
                            if (result[0] <= threshold) {
                                initialDist1 = result[0];
                                status[objectCount] = true;
                                Log.d("TAG", "onLocationResult: TRUE");
                                statusUpdate(true, objectCount, latitude, longitude, speed);
                                objectCount++;
                            }
                        } else if (curDevCount >= maxAllowedDeviation) {
                            initialDist1 = result[0];
                            status[objectCount] = false;
                            Log.d("TAG", "onLocationResult: FALSE");
                            statusUpdate(false, objectCount, latitude, longitude, speed);
                            objectCount++;
                            curDevCount = 0;

                        } else if (result[0] > initialDist1) {
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

    @Override
    public void onCreate() {
        super.onCreate();
        track = new com.example.indianrailways.LocTracking.Track();
        reff = FirebaseDatabase.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();
//        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
//        CollectionReference applicationsRef = rootRef.collection("Tracking");
//        DocumentReference applicationIdRef = applicationsRef.document("Duty1");
//        applicationIdRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                HashMap<Object,ArrayList> hm= (HashMap<Object, ArrayList>) document.get("Patrolman1.Trips");
//                assert hm != null;
////                or(Object al: Objects.requireNonNull(hm.get("Trip1"))){
//                int i=0;
//                for(Object al: Objects.requireNonNull(hm.get("Trip1"))){
////                    for (int al=0;al<=hm.get("Trip1").size();al++){
//                    Log.d("TAG", "LoctionService: hii");
//                    HashMap<Object,Double> al1= (HashMap<Object, Double>) al;
//                    Log.d("TAG", "LoctionService: al = "+al1.get("Latitude"));
//
//                    arrayLat[i]= al1.get("Latitude");
//                    i++;
//                }
//            }
//        });


        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference applicationsRef = rootRef.collection("Original Coordinates");
        DocumentReference applicationIdRef = applicationsRef.document("Station pair1");
        applicationIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Double>> users = (List<Map<String, Double>>) document.get("Aurangabad");
                    int i = 0;
                    assert users != null;
                    for (Map<String, Double> al : users) {
                        Log.d("TAG", "LoctionService1: lat " + al.get("Latitude"));
                        Log.d("TAG", "LoctionService1: long" + al.get("Longitude"));
                        arrayLat[i] = al.get("Latitude");
                        arrayLong[i] = al.get("Longitude");
                        i++;
                    }
                }
            }
        });
    }

    void statusUpdate(boolean status, int ind, double lat, double long1, double speed) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        CollectionReference applicationsRef = rootRef.collection("Tracking");
        DocumentReference applicationIdRef = applicationsRef.document("Duty1");
        Log.d("TAG", "statusUpdate: index = " + ind);

        applicationIdRef.update("Patrolman1.Trips.Trip1." + ind + ".Status", status,
                "Patrolman1.Trips.Trip1." + ind + ".Latitude", lat,
                "Patrolman1.Trips.Trip1." + ind + ".Longitude", long1,
                "Patrolman1.Trips.Trip1." + ind + ".Speed", speed).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("TAG", "onComplete: SUCCESS");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: " + e);
            }
        });
//        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
//        DocumentReference ref = rootRef.collection("Tracking").document("Duty1");
//        Map<String, Object> availableProducts = new HashMap<>();
//        Map<String, Object> trip = new HashMap<>();
////        Map<String, Object> trip3 = new HashMap<>();
//        ArrayList<Integer> trip3=new ArrayList<Integer>();
//        Map<String, Object> values = new HashMap<>();
//        Map<String, Object> zeroMap = new HashMap<>();
//        Map<String, Object> product = new HashMap<>();
//        product.put("Status", true);
//        product.put("Speed", 2.3);
//        zeroMap.put("0", product);
//        availableProducts.put("Patrolman1", trip);
//        trip.put("Trips", trip3);
//        trip3.add(0);
////        values.put("Status","true");
//        Log.d("TAG", "statusUpdate: "+availableProducts.toString()+" "+trip.toString()+" "+trip3.toString()+" "+zeroMap+" "+product);
//        ref.set(availableProducts, SetOptions.merge());
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
        locationRequest.setInterval(20000); //1:6 60000
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

    private String convertLatitude(double latitude) {
        StringBuilder builder = new StringBuilder();

        if (latitude < 0) {
            builder.append("S ");
        } else {
            builder.append("N ");
        }

        String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        String[] latitudeSplit = latitudeDegrees.split(":");
        builder.append(latitudeSplit[0]);
        builder.append("°");
        builder.append(latitudeSplit[1]);
        builder.append("'");
        builder.append(latitudeSplit[2]);
        builder.append("\"");
        return builder.toString();
    }

    private String convertLongitude(double longitude) {
        StringBuilder builder = new StringBuilder();

        if (longitude < 0) {
            builder.append("W ");
        } else {
            builder.append("E ");
        }

        String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        String[] longitudeSplit = longitudeDegrees.split(":");
        builder.append(longitudeSplit[0]);
        builder.append("°");
        builder.append(longitudeSplit[1]);
        builder.append("'");
        builder.append(longitudeSplit[2]);
        builder.append("\"");
        return builder.toString();
    }

}