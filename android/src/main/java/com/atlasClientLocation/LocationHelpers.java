package com.atlasClientLocation;

import android.content.Context;
import android.location.Location;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationHelpers {
   static ArrayList<Map<String,Number>> fromFile = new ArrayList<>();


    public static Map convertToMap(Location location){
        Map<String, Number> locationMap = new HashMap<>();

        locationMap.put("latitude", location.getLatitude());
        locationMap.put("longitude", location.getLongitude());
        locationMap.put("timestamp", location.getTime()/1000.0);
        locationMap.put("accuracy", location.getAccuracy());
       return locationMap;
    }

    public static ArrayList readPersistedPoints(Context context){

        try{
            String path = context.getFilesDir().getPath().toString()+"/locationData.txt";
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            fromFile = (ArrayList<Map<String, Number>>) ois.readObject();
            ois.close();
            fis.close();
            return fromFile;
        }catch (IOException ioe){
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<Map<String, Number>>();
    }

    public static void resetPersistedPoints(Context context){
            try{
                String path = context.getFilesDir().getPath().toString()+"/locationData.txt";
                new FileOutputStream(path).close();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
    }

    public static WritableArray convertToWritableArray(ArrayList<Map> mapArrayList){
        WritableArray out = Arguments.createArray();

        for (int i = 0; i < mapArrayList.size(); i++) {
            WritableMap map = Arguments.createMap();

            // {lat: 4, long: 5}
            Map point = mapArrayList.get(i);

            // Putting {lat: 4, long: 5} => WritableMap
            map.putDouble("latitude", (double) point.get("latitude"));
            map.putDouble("longitude", (double) point.get("longitude"));
            map.putDouble("timestamp", (double) point.get("timestamp"));
            map.putDouble("accuracy", (float) point.get("accuracy"));

            // Appending map to array [{lat: 4, long: 5}, ...]
             out.pushMap(map);
        }
        return out;
    }

}
