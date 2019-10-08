package com.atlasClientLocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
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
        }

    }

    /**
     * Get react context
     */
    private ReactApplicationContext getContext() {
        return getReactApplicationContext();
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    public void sendData(){
        WritableMap iData = Arguments.createMap();
        iData.putDouble("data", 1.732);
        sendEvent(reactContext, "location", iData);
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
