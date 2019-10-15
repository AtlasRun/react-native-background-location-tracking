package com.atlasClientLocation;

import android.location.Location;

import java.util.HashMap;
import java.util.Map;

public class LocationHelpers {

    public static Map convertToMap(Location location){
        Map<String, Double> locationMap = new HashMap<>();

        locationMap.put("latitude", location.getLatitude());
        locationMap.put("longitude", location.getLongitude());
       return locationMap;
    }
}
