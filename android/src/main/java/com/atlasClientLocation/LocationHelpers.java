package com.atlasClientLocation;

import android.location.Location;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationHelpers {

    public static Map convertToMap(Location location){
        Map<String, Number> locationMap = new HashMap<>();

        locationMap.put("latitude", location.getLatitude());
        locationMap.put("longitude", location.getLongitude());
        locationMap.put("timestamp", location.getTime()/1000.0);
        locationMap.put("accuracy", location.getAccuracy());
       return locationMap;
    }

}
