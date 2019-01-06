package com.example.hxtruong.grabbikeapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

import core.customer.Customer;
import core.helper.MyHelper;

public class WaitingLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_location);

        setContentView(R.layout.splash_screen_waitting_location);  //your layout with the picture

        // Start timer and launch main activity
        IntentLauncher launcher = new IntentLauncher();
        launcher.start();
    }

    private class IntentLauncher extends Thread {
        @Override
        public void run() {
            while (true) {
                LatLng currentLocation = Customer.getInstance().mLastKnownLocation;
                if (currentLocation.latitude != 0 && currentLocation.longitude != 0)
                    break;
            }

            // Start main activity
            Intent intent = new Intent(WaitingLocationActivity.this, CustomerMapActivity.class);
            startActivity(intent);
            finish();
        }
    }


}
