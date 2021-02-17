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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;


public class LocationService extends Service  {

    public static final String NOTIFICATON_CHANNEL_ID = "LOCATION_SERVICE_CHANNEL";
    public static final String LOG_TAG = "TESTLOCATIONTRACKING";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private final IBinder serviceBinder = new LocalBinder();
    StateMachine stateMachine = new StateMachine(this);
    LocationHelpers locationHelpers;

    ArrayList<Location> points = new ArrayList<>();
    ArrayList<Map> serializedPoints = new ArrayList<>();
    private FileOutputStream fos;

    public LocationService() {
        fos = null;

    }

    @Override
    public void onCreate() {
//        Log.d(LOG_TAG, "LocationService onCreate: called");
        serializedPoints = LocationHelpers.readPersistedPoints(getApplicationContext());
//        Log.w(LOG_TAG, "onCreate: "+ serializedPoints );
    }

    public void _startTracking(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        buildLocationRequest();
        buildLocationCallback();

    }

    public void _stopTracking(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
//        Log.d(LOG_TAG, "LocationService onStartCommand: called");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
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
//        Log.d(LOG_TAG, "onBind: called");
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
//        android.util.Log.d(LOG_TAG, "onUnbind: called");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
//        android.util.Log.d(LOG_TAG, "onRebind: called");
        super.onRebind(intent);
    }

    public class LocalBinder extends Binder {
        LocationService getService(){
            return LocationService.this;
        }
    }

    public ArrayList getPoints(){
        return serializedPoints;
    }


    public void startTracking(){
//        Log.d(LOG_TAG, "LocationService startTracking: called");
        stateMachine.setState(TrackingState.WAITING_FOR_SIGNAL.getValue());
    }

    public void stopTracking(){
        stateMachine.setState(TrackingState.NOT_TRACKING.getValue());
//        Log.d(LOG_TAG,"LocationService stopTracking called");
        stopForeground(true);
    }


    public void stopService(){
//        Log.d(LOG_TAG,"stopSelf called");
        stopSelf();
    }




    public void createNotificationChannel() {
//        Log.d(LOG_TAG,"LocationService createNotificationChannel called");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Log.d(LOG_TAG, "notification channel called");

            android.app.NotificationChannel notificationChannel = new android.app.NotificationChannel(
                    getString(R.string.notification_channel_id),
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationChannel.setDescription("Accessing device location");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void buildLocationRequest() {
//        Log.d(LOG_TAG,"LocationService buildLocationRequest called");
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(6500);
        locationRequest.setFastestInterval(6500);
    }

    private void buildLocationCallback() {

        try{
            Log.d(LOG_TAG,"LocationService buildLocationRequest called");
            String path = getApplicationContext().getFilesDir().getPath().toString()+"/locationData.txt";
            File file = new File(path);
            file.createNewFile();
            if(file.createNewFile()){
//                Log.d(LOG_TAG, "File creation successful");

            } else {
//                Log.d(LOG_TAG, "File already exists"+ file.getAbsolutePath());
            }
            fos = new FileOutputStream(file);
            final ObjectOutputStream oos = new ObjectOutputStream(fos);
            final long initialPosition = fos.getChannel().position();
            //Log.w(LOG_TAG, "INITIAL POSITION: " + initialPosition );
            if (fusedLocationProviderClient !=null && locationRequest!=null){
                locationCallback = new LocationCallback(){

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // Log.d(LOG_TAG, "LocationCallbacks entered");
                        if(locationResult == null) {
                            return;

                        }
                        Location location = locationResult.getLastLocation();
                        points.add(location);
                        serializedPoints.add(locationHelpers.convertToMap(location));
                        Log.d(LOG_TAG, "serializedLocation:"+ serializedPoints.size());

                        try {
                            fos.getChannel().position(initialPosition);
                            oos.reset();
                            oos.writeObject(serializedPoints);
                            oos.flush();
                        }catch (IOException i){
                            i.printStackTrace();
                        }

                        if(points.size()>0){
                            stateMachine.setState(TrackingState.TRACKING_IN_PROGRESS.getValue());
                        }
                    }
                };

                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            };
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy(){
//        Log.d(LOG_TAG, "onDestroy called");
        super.onDestroy();
        stopSelf();
    }

}
