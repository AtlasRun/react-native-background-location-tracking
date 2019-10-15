package com.atlasClientLocation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class LocationService extends Service  {

    public static final String NOTIFICATON_CHANNEL_ID = "LOCATION_SERVICE_CHANNEL";
    public static final String LOG_TAG = "TESTLOCATIONTRACKING";
    private static final String FILE_NAME = "points.txt";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private final IBinder serviceBinder = new LocalBinder();

    ArrayList<Location> points = new ArrayList<>();


    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        buildLocationRequest();
        buildLocationCallback();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Intent notificationIntent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATON_CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Running...")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class LocalBinder extends Binder {
        LocationService getService(){
            return LocationService.this;
        }
    }

    public ArrayList getPoints(){
        return points;
    }

    public void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(LOG_TAG, "notification channel called");

            android.app.NotificationChannel notificationChannel = new android.app.NotificationChannel(
                    NOTIFICATON_CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationChannel.setDescription("Accessing device location");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

    }

    private void buildLocationCallback() {
        if (fusedLocationProviderClient !=null && locationRequest!=null){
            locationCallback = new LocationCallback(){

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.d(LOG_TAG, "LocationCallbacks entered");
                    if(locationResult == null) {
                        Log.d(LOG_TAG, "onLocationResult:"+ locationResult.getLocations());
                        return;

                    }
                    Location location = locationResult.getLastLocation();
                    points.add(location);

                    Log.d(LOG_TAG, "locationDataMap: "+ points);
//
//                    try {
//                        FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND);
//                        ObjectOutputStream ois = new ObjectOutputStream(fos);
//                        ois.writeObject(locationData);
//                        ois.close();
//                        fos.close();
//
//                    }catch (FileNotFoundException e) {
//                        System.out.println("File not found");
//                    } catch (IOException e) {
//                        System.out.println("Error initializing stream");
//                    }

                    // write the data to disk ()
                    // Send only important events through broadcast to the module
                    Intent intent = new Intent("location_data_action");
                    intent.putExtra("location_data", location);
                    sendBroadcast(intent);
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        };
    }

    @Override
    public void onDestroy(){
//        try{
//            Object locationDataFromFile;
//            FileInputStream fis = openFileInput(FILE_NAME);
//            ObjectInputStream ois = new ObjectInputStream(fis);
//            locationDataFromFile =  ois.readObject();
//            Log.d(LOG_TAG, "readingFromFile:"+ locationDataFromFile);
//        }catch(Exception ex) {
//            ex.printStackTrace();
//        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
        //stopSelf();
    }

}
