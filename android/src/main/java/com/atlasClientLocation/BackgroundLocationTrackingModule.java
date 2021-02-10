package com.atlasClientLocation;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.provider.Settings;
import android.app.ActivityManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import com.facebook.react.bridge.Promise;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.Nullable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BackgroundLocationTrackingModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    public final String LOG_TAG = "TESTLOCATIONTRACKING";
    private LocationService myService;
    boolean isBound = false;
    private boolean called = false;
    ArrayList<Map> persistedPoints = new ArrayList<>();
    private boolean returnValueFromBoundService;

    public static final String LOGTAG = "PERMISSIONS";
    Intent locationServiceIntent = new Intent(getContext(), LocationService.class);



    public BackgroundLocationTrackingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        android.util.Log.d(LOG_TAG, "BackgroundLocationTrackingModule: constructor invoked");
//        Boolean serviceRunning = isServiceRunning("com.atlasClientLocation.LocationService");
//        if(serviceRunning){
//            android.util.Log.d(LOG_TAG, "isServiceRunning constructor "+ serviceRunning);
////            Boolean res = this.reactContext.bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE );
//            android.util.Log.d(LOG_TAG, "boundService inside constructor: "+ res);
//            Boolean serviceRunningAfterBind = isServiceRunning("com.atlasClientLocation.LocationService");
//            Log.d(LOG_TAG, "serviceRunning After Bind: "+ serviceRunningAfterBind);
//        }

        BroadcastReceiver locationUpdatesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Location locationData = intent.getParcelableExtra("location_data");

                invokeSuccess(LocationUtils.locationDataMap(locationData));
            }
        };
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getReactApplicationContext());
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationUpdatesReceiver, new IntentFilter("location_data_action"));
    }

    @Override
    public String getName() {
        return "BackgroundLocationTracking";
    }

    @ReactMethod
    public void checkPowerOptimizationSettings( Promise promise) {
        ReactApplicationContext context = getContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Boolean result = pm.isIgnoringBatteryOptimizations(context.getPackageName());
        promise.resolve(result);
    }

    @ReactMethod
    public void showPowerOptimizationSettings() {
        ReactApplicationContext context = getContext();
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (settingsIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
            context.startActivity(settingsIntent);
        }

    }

    @ReactMethod
    public void checkSystemLocationAccuracySettings(Promise promise) {
        ReactApplicationContext context = getContext();
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // API Level >= 28 Google Location Accuracy ON/OFF
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            promise.resolve(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        } else {
            try{
                // API Level < 28 Location accuracy has 3 modes - Device only, Battery Saver, High Accuracy
                int locationAccuracy = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                if (locationAccuracy == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY){
                    promise.resolve(true);
                }
                else {
                    promise.resolve(false);
                }
            }catch (Settings.SettingNotFoundException e) {
                promise.resolve(false);
                e.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void showSystemLocationAccuracySettings() {
        ReactApplicationContext context = getContext();
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (settingsIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
            context.startActivity(settingsIntent);
        }
    }

    @ReactMethod
    public void requestLocation(ReadableMap options) {
        if(called) return;
        called = true;
        //Log.d(LOG_TAG, "request location called");
        ReactApplicationContext context = getContext();

        if(!LocationUtils.hasLocationPermission(context)){


            invokeError(
                    LocationError.SETTINGS_NOT_SATISFIED.getValue(),
                    "Location permission not granted."
            );
        }else {
//            Log.d(LOGTAG, "requestLocation: "+ LocationUtils.hasLocationPermission(context));
//            android.util.Log.d(LOG_TAG, "locationServiceIntent: "+locationServiceIntent);
            Boolean isServiceRunning = isServiceRunning("com.atlasClientLocation.LocationService");
            if(!isServiceRunning){
                getContext().startService(locationServiceIntent);

                Boolean serviceRunning = isServiceRunning("com.atlasClientLocation.LocationService");
                android.util.Log.d(LOG_TAG, "isServiceRunning startservice: "+ serviceRunning);
            }
            returnValueFromBoundService = getContext().bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE );
            Boolean isServiceRunningRequestLocation = isServiceRunning("com.atlasClientLocation.LocationService");
            android.util.Log.d(LOG_TAG, "isServiceRunning requestLocation "+ isServiceRunningRequestLocation);
        }

    }

    @ReactMethod
    public void stopLocationTracking(){
        try {
//        Intent locationServiceIntent = new Intent(getContext(), LocationService.class);

            myService.stopTracking();

            if (isBound == true) {
//                getContext().bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE );
                android.util.Log.d(LOG_TAG, "stopLocationTracking: called");
                getContext().stopService(locationServiceIntent);
                getContext().unbindService(serviceConnection);
                android.util.Log.d(LOG_TAG, "stopLocationTracking: "+returnValueFromBoundService);
//                myService.stopService();
                called = false;
                android.util.Log.d(LOG_TAG, "locationservicestopped: "+returnValueFromBoundService);
                Boolean serviceRunning = isServiceRunning("com.atlasClientLocation.LocationService");
                android.util.Log.d(LOG_TAG, "isServiceRunning stopservice "+ serviceRunning);
            }
        } catch (IllegalArgumentException exception) {
        } catch (Exception exception) {
        }
    }

    @ReactMethod
    public void getPoints(
            Promise promise) {

        if(myService == null) {
            persistedPoints = LocationHelpers.readPersistedPoints(getContext());
        } else {
            persistedPoints = myService.getPoints();
        }
        WritableArray out = LocationHelpers.convertToWritableArray(persistedPoints);
        promise.resolve(out);

    }

    @ReactMethod
    public void readPersistedPoints(Promise promise){
        if(myService !=null) {
            ArrayList<Map> persistedPoints = LocationHelpers.readPersistedPoints(getContext());

            WritableArray out = Arguments.createArray();

            for (int i = 0; i < persistedPoints.size(); i++) {
                WritableMap map = Arguments.createMap();

                // {lat: 4, long: 5}
                Map point = persistedPoints.get(i);

                // Putting {lat: 4, long: 5} => WritableMap
                map.putDouble("latitude", (double) point.get("latitude"));
                map.putDouble("longitude", (double) point.get("longitude"));
                map.putDouble("timestamp", (double) point.get("timestamp"));
                map.putDouble("accuracy", (float) point.get("accuracy"));

                // Appending map to array [{lat: 4, long: 5}, ...]
                out.pushMap(map);
            }

            promise.resolve(out);
        }
    }

    @ReactMethod
    public void resetPersistedPoints(){
        LocationHelpers.resetPersistedPoints(getContext());
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "onServiceConnected called ");
            LocationService.LocalBinder localBinder = (LocationService.LocalBinder) iBinder;
            myService = localBinder.getService();
            myService.startTracking();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            android.util.Log.d(LOG_TAG, "onServiceDisconnected: called");
            isBound = false;
        }

        @Override
        public void onBindingDied(ComponentName componentName) {
            android.util.Log.d(LOG_TAG, "onServiceDisconnected: onBindingDied");
            isBound = false;
        }

        @Override
        public void onNullBinding(ComponentName componentName) {
            android.util.Log.d(LOG_TAG, "onServiceDisconnected: onNullBinding");

            isBound = false;
        }


    };

    /**
     * Get react context
     */
    private ReactApplicationContext getContext() {
        return getReactApplicationContext();
    }


    private void invokeSuccess(WritableMap data) {
        getContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("geolocationDidChange", data);
    }


    private void invokeError(int code,  String message) {
        getContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("locationError", LocationUtils.buildError(code, message));

    }


    private boolean isServiceRunning(String serviceName) {
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);

        Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = i
                    .next();
//            android.util.Log.d(LOG_TAG, "isServiceRunning: " + runningServiceInfo.service.getClassName());
            if (runningServiceInfo.service.getClassName().equals(serviceName)) {
                serviceRunning = true;

                if (runningServiceInfo.foreground) {
                    //service run in foreground
                }
            }
        }
        return serviceRunning;
    }
}
