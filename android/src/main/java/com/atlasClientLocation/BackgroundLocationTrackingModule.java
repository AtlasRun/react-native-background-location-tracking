package com.atlasClientLocation;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

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
    private volatile boolean called = false;
    ArrayList<Map> persistedPoints = new ArrayList<>();


    public BackgroundLocationTrackingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
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
    public void requestLocation(ReadableMap options) {
        if(called) return;
        called = true;
        Log.d(LOG_TAG, "request location called");
        ReactApplicationContext context = getContext();

        if(!LocationUtils.hasLocationPermission(context)){

            invokeError(
                    LocationError.SETTINGS_NOT_SATISFIED.getValue(),
                    "Location permission not granted."
            );
        }else {
            Intent locationServiceIntent = new Intent(getContext(), LocationService.class);
            getContext().startService(locationServiceIntent);
            getContext().bindService(locationServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE );
        }

    }

    @ReactMethod
    public void stopLocationTracking(){
        Intent locationServiceIntent = new Intent(getContext(), LocationService.class);
        myService.stopTracking();
        getContext().unbindService(serviceConnection);
        getContext().stopService(locationServiceIntent);
        called = false;
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
        if(myService!=null){
            myService.resetPersistedPoints();
        }
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


}
