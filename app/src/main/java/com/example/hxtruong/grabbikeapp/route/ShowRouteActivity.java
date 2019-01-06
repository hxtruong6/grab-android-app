package com.example.hxtruong.grabbikeapp.route;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.R;
import com.example.hxtruong.grabbikeapp.WaitingForFindingDriver.WaitForFindingDriver;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import core.customer.Customer;

public class ShowRouteActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private LatLng latLngStart;
    private LatLng latLngEnd;
    public static List<Polyline> polylinePaths;
    public static LatLngBounds bounds;
    private ProgressDialog progressDialog;
    private String parentActivity;
    private TextView txtPrice;
    private Button btnBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtPrice = findViewById(R.id.txtPrice);
        btnBook = findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Book();
            }
        });
        Intent intent = getIntent();
        if (intent != null)
            parentActivity = intent.getStringExtra("ID");
        else parentActivity = null;
        polylinePaths = new ArrayList<>();

    }

    private void Book() {
        Customer.getInstance().sendBookingRequest();
        Intent intent = new Intent(ShowRouteActivity.this, WaitForFindingDriver.class);
        startActivity(intent);
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
        LatLng hcmus = new LatLng(10.763261, 106.682215);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 15));

        if (parentActivity.compareTo("CUSTOMERMAP") == 0)
        {
            LatLng temp = Customer.getInstance().mStartLocation;
            latLngStart = new LatLng(temp.latitude, temp.longitude);
            temp = Customer.getInstance().mEndLocation;
            latLngEnd = new LatLng(temp.latitude, temp.longitude);
//            latLngStart = new LatLng(10.7627345, 106.6822347);
//            latLngEnd = new LatLng(10.7803201, 106.6984916);
            Customer.getInstance().mStartLocation = latLngStart;
            Customer.getInstance().mEndLocation = latLngEnd;
            ShowRoute();
        }
    }

    private void ShowRoute() {
        new DirectionFinder(this, latLngStart, latLngEnd).execute();
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
            bounds = route.bounds;
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
//            mMap.addPolyline(polylineOptions);

            int price = 10000;
            if (route.distance.value > 2000){
                price += Math.round((route.distance.value - 2000)*3/100)*100;
            }
            txtPrice.setText(String.valueOf(price) + "VND");
        }
    }
}
