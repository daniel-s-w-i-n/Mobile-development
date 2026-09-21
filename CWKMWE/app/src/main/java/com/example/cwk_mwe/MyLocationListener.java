package com.example.cwk_mwe;

/*
    //Skeleton code given by Lecturer
 */

import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;


public class MyLocationListener implements LocationListener {

    boolean firstDistance = true;
    Location oldLocation;
    float results;
    @Override
    public void onLocationChanged(Location location) {
        //Log.d("comp3018", location.getLatitude() + " " + location.getLongitude());

        //gets the difference between the previous call and the current call
        if (firstDistance) {
            firstDistance = false;
            oldLocation = location;
        }else{
            results = location.distanceTo(oldLocation);
            oldLocation = location;
        }

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
        Log.d("comp3018", "onStatusChanged: " + provider + " " + status);
    }
    @Override
    public void onProviderEnabled(String provider) {
        // the user enabled (for example) the GPS
        Log.d("comp3018", "onProviderEnabled: " + provider);
    }
    @Override
    public void onProviderDisabled(String provider) {
        // the user disabled (for example) the GPS
        Log.d("comp3018", "onProviderDisabled: " + provider);
    }

    //returns the change in distance since last check
    public int getDistance() {
        try{
            return (int) results;
        }catch(Exception e){
            return 0;
        }
    }
}

