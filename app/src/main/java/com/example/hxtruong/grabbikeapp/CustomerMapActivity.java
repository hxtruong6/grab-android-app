package com.example.hxtruong.grabbikeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hxtruong.grabbikeapp.route.ShowRouteActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import core.customer.Customer;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int CODE_ORIGIN = 4000;
    private static final int CODE_DESTINATION = 4001;
    private GoogleMap mMap;
    private LatLng mLastLocation;
    private TextView originAddress;

    TextView tvOrigin, tvDestination;

    private static final double limitLatitude  = 0.002736;
    private static final double limitLongitude  = 0.003334;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.customerMap);
        mapFragment.getMapAsync(this);

        tvOrigin = findViewById(R.id.tvOrigin);
        tvDestination = findViewById(R.id.tvDestination);
        tvOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOriginAndDestination(CODE_ORIGIN);
            }
        });
        tvDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editOriginAndDestination(CODE_DESTINATION);
            }
        });


        // DELETE IT
        Customer.getInstance().getDriverList();

     /*   // TODO: delete later
        Intent intent = new Intent(CustomerMapActivity.this, ShowRouteActivity.class);
        intent.putExtra("ID", "CUSTOMERMAP");
        startActivity(intent);*/
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng defaultLocation = new LatLng(10.763024, 106.682309);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 11));

        setOriginAddressToTextView();
        //showNearDriver();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private void showNearDriver() {
        List<LatLng> driverList = Customer.getInstance().getDriverList();
        List<LatLng> nearDriverList = findNearDriver(driverList);
        for (int i=0; i<nearDriverList.size(); i++){
            MarkerOptions driverMarker = new MarkerOptions()
                    .position(nearDriverList.get(i)).title("Driver Location!");
            driverMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_biker_15x43));
            mMap.addMarker(driverMarker);
        }

    }

    private List<LatLng> findNearDriver(List<LatLng> driverList) {
        List<LatLng> list = new ArrayList<>();
        for (int i=0; i<driverList.size(); i++)
        {
            if (Math.abs(mLastLocation.latitude - driverList.get(i).latitude) <= limitLatitude &&
                    Math.abs(mLastLocation.longitude - driverList.get(i).longitude) <= limitLongitude){
                list.add(driverList.get(i));
            }


        }
        return list;
    }


   /* @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mLastLocation = latLng;
            Customer.getInstance().updateCustomerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            MyHelper.toast(getApplicationContext(), "Customer Location changed ON MAP!"+location.getLatitude()+", "+location.getLongitude());
            Log.d("xxx", "Customer location changed on map" +location.getLatitude()+", "+location.getLongitude());
            Log.d("xxx", "On location changed: on map " + latLng.toString());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }*/

    public void editOriginAndDestination(int requestCode) {
        Intent intent = new Intent(CustomerMapActivity.this, EditAddressActivity.class);
        this.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            String address = data.getStringExtra("address");
            String lat = data.getStringExtra("lat");
            String lng = data.getStringExtra("lng");
            if (requestCode == CODE_ORIGIN) {
                tvOrigin.setText(address);
                Customer.getInstance().setStartLocation(Float.valueOf(lat), Float.valueOf(lng));
                //
            } else if (requestCode == CODE_DESTINATION) {
                tvDestination.setText(address);
                Customer.getInstance().setEndLocation(Float.valueOf(lat), Float.valueOf(lng));
                //
            }

            if (tvOrigin.getText().toString() != "" && tvDestination.getText().toString() != "")
            {
                Intent intent = new Intent(CustomerMapActivity.this, ShowRouteActivity.class);
                intent.putExtra("ID", "CUSTOMERMAP");
                startActivity(intent);
            }


          //  this.textFeedback.setText(feedback);
        } else {
            //error
        }
    }

    private void setOriginAddressToTextView() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                originAddress = (TextView)findViewById(R.id.tvOrigin);

                mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());

                //Customer.getInstance().updateCustomerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                MyHelper.toast(getApplicationContext(), "Customer Location changed ON MAP!"+location.getLatitude()+", "+location.getLongitude());
                Log.d("xxx", "Customer location changed on map" +location.getLatitude()+", "+location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 18));
                Customer.getInstance().setStartLocation((float) mLastLocation.latitude, (float) mLastLocation.longitude);
                // TODO: update user location

                //showNearDriver();


                try{
                    Geocoder geo = new Geocoder(CustomerMapActivity.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.isEmpty()) {
                        originAddress.setText("Waiting for Location");
                    }
                    else {
                        if (addresses.size() > 0) {
                            originAddress.setText(addresses.get(0).getFeatureName()
                                    + ", " + addresses.get(0).getLocality()
                                    +", " + addresses.get(0).getAdminArea()
                                    + ", " + addresses.get(0).getCountryName());

                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
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
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

    }
}
