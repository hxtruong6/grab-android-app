package com.example.hxtruong.grabbikeapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hxtruong.grabbikeapp.route.DirectionFinder;
import com.example.hxtruong.grabbikeapp.route.DirectionFinderListener;
import com.example.hxtruong.grabbikeapp.route.Route;
import com.google.android.gms.common.internal.FallbackServiceBroker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import core.driver.Driver;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, Driver.IDriverListener, DirectionFinderListener {

    private GoogleMap mMap;

    private LatLng mLastLocation;

    private Button btnPickup, btnReturn;
    private ProgressDialog progressDialog;
    private List<Polyline> polylinePaths;
    private LatLng latLngStart, latLngEnd;
    Boolean statLocReady, endLocReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnPickup = findViewById(R.id.btnPickup);
        btnReturn = findViewById(R.id.btnReturn);
        btnPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPickup.setEnabled(false);
            }
        });
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideButtons(true);
                hideRoutes();
            }
        });
        polylinePaths = new ArrayList<>();
        statLocReady = endLocReady = false;
        hideButtons(false);

    }

    private void hideRoutes() {

    }

    private void hideButtons(boolean flag){
        if(flag == true){
            btnReturn.setVisibility(View.GONE);
            btnPickup.setVisibility(View.GONE);
        }
        else {
            btnPickup.setVisibility(View.VISIBLE);
            btnReturn.setVisibility(View.VISIBLE);
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        mMap.setMyLocationEnabled(true);

        setOriginAddressToTextView();

        Driver.getInstance().registerIDriverInterface(this);
    }

    private void ShowRoute() {
        new DirectionFinder(this, latLngStart, latLngEnd).execute();
    }


    private void setOriginAddressToTextView() {
        try {
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastLocation, 18));
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

        } catch (Exception e) {

        }
    }

    @Override
    public void onDriverLocationChanged(LatLng loc) {
        //

    }

    @Override
    public void receiveCustomerRequest(String customerRequestId) {
        //TODO:show a dialog
       FirebaseHelper.getCustomerLocation(customerRequestId, "startLoc");
       FirebaseHelper.getCustomerLocation(customerRequestId, "endLoc");

    }

    @Override
    public void onStartLocationReady(LatLng statLoc) {
        latLngStart = statLoc;
        statLocReady = true;
        if(statLocReady && endLocReady){
            ShowRoute();
            hideButtons(true);
        }

    }

    @Override
    public void onEndLocationReady(LatLng endLoc) {
        latLngEnd = endLoc;
        endLocReady = true;
        if(statLocReady && endLocReady){
            ShowRoute();
            hideButtons(true);
        }
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        for (Route route : routes){
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(route.bounds,200));

            Marker markerOrigin = mMap.addMarker(new MarkerOptions()
                    .position(route.startLocation)
                    .title(route.startAddress.split(",")[0])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_marker_red)));
            Marker markerDestination = mMap.addMarker(new MarkerOptions()
                    .position(route.endLocation)
                    .title(route.endAddress.split(",")[0])
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_marker_blue)));

            markerDestination.showInfoWindow();
            markerOrigin.showInfoWindow();
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);
            for(int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            int price = 10000;
            if (route.distance.value > 2000){
                price += Math.round((route.distance.value - 2000)*3/100)*100;
            }
            ((TextView)findViewById(R.id.txtPriceDriver)).setText(String.valueOf(price) + "VND");
        }

    }
}
