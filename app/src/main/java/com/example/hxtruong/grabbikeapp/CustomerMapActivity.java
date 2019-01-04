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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

import core.customer.Customer;
import core.helper.FirebaseHelper;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationSource.OnLocationChangedListener, Customer.IUserListener {
    private static final int CODE_ORIGIN = 4000;
    private static final int CODE_DESTINATION = 4001;
    private GoogleMap mMap;
    private Location mLastLocation;
    private LatLng pickupLocation;
    private TextView originAddress;
    private Double currentLatitude, currentLongitude;

    TextView tvOrigin, tvDestination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.customerMap);
        mapFragment.getMapAsync(this);
        Customer.getInstance().registerIUserInterface(this);
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

        setOriginAddressToTextView();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng hcmus = new LatLng(10.763261, 106.682215);
        mMap.addMarker(new MarkerOptions()
                .position(hcmus)
                .title("Marker in HCMUS")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 15));


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            mLastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d("xcustomer", "On location changed: " + latLng.toString());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }


    public void OnCustomerRequestDriverBtn(View view) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.d("xxx customer", "userId NULL");
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        // TODO: change this hard code here -> need to get curretun location
        mLastLocation = new Location("");
        mLastLocation.setLatitude(0.15);
        mLastLocation.setLongitude(0.31);
        geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Set customer pick up location", Toast.LENGTH_SHORT).show();
            }
        });

        pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

        //((Button) view.findViewById(R.id.request)).setText("Getting your Driver....");
    }

    @Override
    public void onCustomerLocationChanged(Location location) {
        //

    }

    @Override
    public void onDriverLocationChanged(Location location) {

    }

    @Override
    public void onBookingResult(String driver) {

        //String string = FirebaseHelper.getDriverInfo(driver);
    }

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
            if(requestCode == CODE_ORIGIN){
                tvOrigin.setText(address);
                Customer.getInstance().setStartLocation(Float.valueOf(lat), Float.valueOf(lng));
                //
            }else if(requestCode == CODE_DESTINATION){
                tvDestination.setText(address);
                Customer.getInstance().setEndLocation(Float.valueOf(lat), Float.valueOf(lng));
                //
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
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
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
