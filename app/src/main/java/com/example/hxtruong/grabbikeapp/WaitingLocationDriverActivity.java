package com.example.hxtruong.grabbikeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import core.customer.Customer;

public class WaitingLocationDriverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_location_driver);
        setContentView(R.layout.splash_screen_waitting_location);  //your layout with the picture

        // Start timer and launch main activity
        WaitingLocationDriverActivity.IntentLauncher launcher = new WaitingLocationDriverActivity.IntentLauncher();
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

            Intent intent = new Intent(WaitingLocationDriverActivity.this, DriverMapActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
