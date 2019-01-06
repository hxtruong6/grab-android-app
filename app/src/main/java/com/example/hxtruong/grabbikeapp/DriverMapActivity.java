package com.example.hxtruong.grabbikeapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

import core.customer.Customer;
import core.driver.Driver;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, Driver.IDriverListener, DirectionFinderListener {

    private GoogleMap mMap;
    int i = 0;
    private LatLng mLastLocation;

    private Button btnPickup, btnPickDown, btnFakeLoc ;
    TextView tvPrice;
    private ProgressDialog progressDialog;
    public static List<Polyline> polylinePaths;
    public static LatLng latLngStart, latLngEnd;
    Boolean statLocReady, endLocReady;
    List<LatLng> list;
    List<Route> myRoute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        list = new ArrayList<>();
        myRoute = new ArrayList<>();
        btnPickup = findViewById(R.id.btnPickup);
        btnPickDown = findViewById(R.id.btnPickDown);
        tvPrice = findViewById(R.id.txtPriceDriver);
        btnFakeLoc = findViewById(R.id.btnFakeLoc);


        btnFakeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLastLocation = list.get(i);

                Driver.getInstance().updateDriverLocation(mLastLocation);
                showBikeMarker();
                i++;
                if(i==list.size()-1)
                {
                    btnPickDown.setEnabled(true);
                    btnFakeLoc.setEnabled(false);
                }


            }
        });

        btnPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPickup.setEnabled(false);
            }
        });
        btnPickDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideButtons(true);
                hideRoutes();
                final AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this);
                builder.setTitle("Notification");
                builder.setMessage("You have already completed the trip. Thank you!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });
        polylinePaths = new ArrayList<>();
        statLocReady = endLocReady = false;
        hideButtons(true);

    }

    private void hideRoutes() {

    }

    private void hideButtons(boolean flag){
        if(flag == true){
            btnPickDown.setVisibility(View.GONE);
            btnPickup.setVisibility(View.GONE);
            tvPrice.setVisibility(View.GONE);

        }
        else {
            btnPickup.setVisibility(View.VISIBLE);
            btnPickDown.setVisibility(View.VISIBLE);
            tvPrice.setVisibility(View.VISIBLE);
        }
        btnPickDown.setEnabled(false);
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

    private void showBikeMarker()
    {
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(mLastLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_biker_15x43));
        mMap.addMarker(markerOptions);

        Marker markerOrigin = mMap.addMarker(new MarkerOptions()
                .position(myRoute.get(0).startLocation)
                .title(myRoute.get(0).startAddress.split(",")[0])
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_marker_red)));
        Marker markerDestination = mMap.addMarker(new MarkerOptions()
                .position(myRoute.get(0).endLocation)
                .title(myRoute.get(0).endAddress.split(",")[0])
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_marker_blue)));

        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.GREEN).
                width(10);
        for(int i = 0; i < myRoute.get(0).points.size(); i++) {
            polylineOptions.add(myRoute.get(0).points.get(i));
        }
        polylinePaths.add(mMap.addPolyline(polylineOptions));


    }


    private void setOriginAddressToTextView() {
        try {
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    //Driver.getInstance().updateDriverLocation(mLastLocation);
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
            hideButtons(false);
        }

    }

    @Override
    public void onEndLocationReady(LatLng endLoc) {
        latLngEnd = endLoc;
        endLocReady = true;
        if(statLocReady && endLocReady){
            ShowRoute();
            hideButtons(false);
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

        myRoute =routes;
        progressDialog.dismiss();
        showBikeMarker();
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
            for(int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
                list.add(route.points.get(i));
                Log.d("ccc", i+" - "+route.points.get(i).toString());
            }
            polylinePaths.add(mMap.addPolyline(polylineOptions));

            int price = 10000;
            if (route.distance.value > 2000){
                price += Math.round((route.distance.value - 2000)*3/100)*100;
            }
            ((TextView)findViewById(R.id.txtPriceDriver)).setText(String.valueOf(price) + "VND");
        }

    }
}
