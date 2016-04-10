package mlevy94.robiny.gettingwarmer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;


import java.util.jar.Manifest;

import mlevy94.robiny.gettingwarmer.MainActivity;

/**
 * Created by robin yang on 4/3/2016.
 */
public class GPSManager implements LocationListener {
    MainActivity mainActivity;
    LocationManager locationManager;
    String LOCATIONPROVIDER;
    private static final int REQUEST_LOCATION = 0;

    public GPSManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        locationManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        LOCATIONPROVIDER = locationManager.getBestProvider(criteria, false);
    }

    public void register() {
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Explain to the user why we need to read the contacts
                Toast.makeText(mainActivity.getApplicationContext(), "Location permission required for functionality", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(mainActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);

            return;
        }
        locationManager.requestLocationUpdates(LOCATIONPROVIDER, 100, 0, this);
        mainActivity.updateGPSLocation(locationManager.getLastKnownLocation(LOCATIONPROVIDER));
    }

    public void unregister() {
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mainActivity.updateGPSLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}

