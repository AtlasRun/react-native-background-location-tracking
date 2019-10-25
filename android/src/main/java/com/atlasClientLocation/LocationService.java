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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;


public class LocationService extends Service  {

    public static final String NOTIFICATON_CHANNEL_ID = "LOCATION_SERVICE_CHANNEL";
    public static final String LOG_TAG = "TESTLOCATIONTRACKING";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private final IBinder serviceBinder = new LocalBinder();
    StateMachine stateMachine = new StateMachine(this);


    ArrayList<Location> points = new ArrayList<>();


    @Override
    public void onCreate() {
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        createNotificationChannel();
//        buildLocationRequest();
//        buildLocationCallback();
    }

    public void _startTracking(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        buildLocationRequest();
        buildLocationCallback();
    }

    public void _stopTracking(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
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
        Log.d(LOG_TAG, "onBind: IS CALLED");
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


    public void startTracking(){
        stateMachine.setState(TrackingState.WAITING_FOR_SIGNAL.getValue());
    }

    public void stopTracking(){
        stateMachine.setState(TrackingState.NOT_TRACKING.getValue());
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

                    if(points.size()>0){
                        stateMachine.setState(TrackingState.TRACKING_IN_PROGRESS.getValue());
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        };
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        stopSelf();
    }

}
