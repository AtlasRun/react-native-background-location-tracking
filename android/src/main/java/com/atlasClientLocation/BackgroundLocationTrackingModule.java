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
import java.util.Map;
import java.util.ArrayList;
import android.widget.Toast;
import com.facebook.react.bridge.Promise;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
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
    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_SETTINGS_CONTINUOUS_UPDATE = 11404;
    LocationService myService;
    boolean isBound = false;


    public BackgroundLocationTrackingModule(ReactApplicationContext reactContext) {
        super(reactContext);
        //
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
    public void showToast(String message, int duration) {
        ReactApplicationContext context = getContext();
        // TODO: Implement some actually useful functionality
        Toast.makeText(getReactApplicationContext(), message, duration).show();
    }

    @ReactMethod
    public void requestLocation(ReadableMap options) {
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
        Log.d(LOG_TAG, "ALL POINTS: "+myService.getPoints());
        getContext().unbindService(serviceConnection);
        getContext().stopService(locationServiceIntent);
    }

    @ReactMethod
    public void getPoints(
      Promise promise) {

      // Get points list is an array of map [{lat: 4, long: 5}, {lat: 5, long: 6}]
      ArrayList<Map> list = myService.getPoints();

      WritableArray out = Arguments.createArray();

      for (int i = 0; i < list.size(); i++) {
        WritableMap map = Arguments.createMap();

        // {lat: 4, long: 5}
        Map<String,Double> point = list.get(i);

        // Putting {lat: 4, long: 5} => WritableMap
        map.putDouble("latitude", point.get("latitude"));
        map.putDouble("longitude", point.get("longitude"));

        // Appending map to array [{lat: 4, long: 5}, ...]
        out.pushMap(map);
      }

      promise.resolve(out);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(LOG_TAG, "onServiceConnected called ");
            LocationService.LocalBinder localBinder = (LocationService.LocalBinder) iBinder;
            myService = localBinder.getService();
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
