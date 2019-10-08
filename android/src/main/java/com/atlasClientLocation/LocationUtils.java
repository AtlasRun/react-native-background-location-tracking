package com.atlasClientLocation;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import androidx.core.app.ActivityCompat;

public class LocationUtils {

    private static final int LOCATION_REQUEST_CODE = 100;

    /**
     * Check if location permissions are granted.
     */
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Build error response for error callback.
     */
    public static WritableMap buildError(int code, String message) {
        WritableMap error = Arguments.createMap();
        error.putInt("code", code);

        if (message != null) {
            error.putString("message", message);
        }

        return error;
    }

    /**
     * Build location response object
     */
    public static WritableMap locationDataMap(Location location){
        WritableMap locationData = Arguments.createMap();
        WritableMap coords = Arguments.createMap();

        coords.putDouble("latitude", location.getLatitude());
        coords.putDouble("longitude", location.getLongitude());
        coords.putDouble("altitude", location.getAltitude());
        coords.putDouble("accuracy", location.getAccuracy());
        coords.putDouble("heading", location.getBearing());
        coords.putDouble("speed", location.getSpeed());
        locationData.putMap("coords", coords);
        locationData.putDouble("timestamp", location.getTime());


        return locationData;
    }

}
