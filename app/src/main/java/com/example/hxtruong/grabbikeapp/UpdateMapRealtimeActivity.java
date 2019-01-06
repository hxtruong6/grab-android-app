package com.example.hxtruong.grabbikeapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.route.ShowRouteActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import core.customer.Customer;
import core.driver.DriverInfo;
import core.helper.MyHelper;

public class UpdateMapRealtimeActivity extends FragmentActivity implements OnMapReadyCallback, Customer.IUserListener {

    private Marker markerDriver;
    private Marker markerCustomer;
    private GoogleMap mMap;
    private LatLng posDriver;
    private AsyncUpdatePosition asyncUpdate;
    private List<Polyline> newPolylinePaths;
    private LatLng latLngStart, latLngEnd;
    private LatLng posCustomer;
    private boolean initZoom;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_map_realtime);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        asyncUpdate = new AsyncUpdatePosition();
        asyncUpdate.execute();

        markerCustomer = null;
        markerDriver = null;
        initZoom = true;

        showDriverInfo();

    }

    void updateMarkerDriver() {
        if (markerDriver != null)
            markerDriver.remove();
        markerDriver = mMap.addMarker(new MarkerOptions()
                .position(posDriver)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_biker_15x43)));
    }

    void updateMarkerCustomer() {
        if (markerCustomer != null)
            markerCustomer.remove();
        markerCustomer = mMap.addMarker(new MarkerOptions()
                .position(posCustomer)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_street_view_40)));
    }

    private void showDriverInfo() {
        DriverInfo driverInfo = Customer.getInstance().getDriverInfo();
        if (driverInfo != null && !driverInfo.isEmpty()) {
            Log.d("showDriverInfo", ": " + driverInfo.name);
            MyHelper.toast(this, driverInfo.name + "\n" + driverInfo.email + "\n" + driverInfo.vehicle);
            //show driver info here

            final Dialog dialog = new Dialog(UpdateMapRealtimeActivity.this);
            dialog.setContentView(R.layout.dialog_infor_driver);

            ((TextView) dialog.findViewById(R.id.txtDriverName)).setText(driverInfo.name);
            ((TextView) dialog.findViewById(R.id.txtLicensePlate)).setText(driverInfo.vehicle);
            Button btnOk = dialog.findViewById(R.id.btnOK);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            dialog.getWindow().setLayout(1200, 1400);

        } else {
            MyHelper.toast(this, "driver not found!!!");
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

        //TODO: get position of Driver and Customer from Database
        GetPositionFromDatabase();
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (initZoom) {
                    showRoute();
                    ZoomMap();
                    initZoom = false;
                }
            }
        });
        updateMarkerCustomer();
        updateMarkerDriver();
        latLngStart = Customer.getInstance().mStartLocation;
        latLngEnd = Customer.getInstance().mEndLocation;
        Log.d("quoc", "lat: " + latLngStart.toString() + "| latEn:" + latLngEnd.toString());
        String s = ShowRouteActivity.polylinePaths.get(0).getPoints().get(0).toString();
        Log.d("Anhquoc",s);
//        mMap.setOnCameraMoveStartedListener();
//        new DirectionFinder(this, latLngStart, latLngEnd).execute();

    }

    private void ZoomMap() {
        Log.d("xxx", "posDriver" + posDriver.toString());
        Log.d("xxx", "posCustomer" + posCustomer.toString());
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(posCustomer);
        builder.include(posDriver);
        builder.include(Customer.getInstance().mStartLocation);
        builder.include(Customer.getInstance().mEndLocation);
//        LatLngBounds bounds = CreateBounds(posDriver, posCustomer);
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
    }

    private LatLngBounds CreateBounds(LatLng p1, LatLng p2) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(p1);
        builder.include(p2);
        LatLngBounds bounds = builder.build();
        return bounds;
    }


    private void GetPositionFromDatabase() {
        posDriver = Customer.getInstance().mDriverLocation;
        posCustomer = Customer.getInstance().mLastKnownLocation;
    }

    public void showRoute() {
        newPolylinePaths = ShowRouteActivity.polylinePaths;
        Marker markerOrigin = mMap.addMarker(new MarkerOptions()
                .position(Customer.getInstance().mStartLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_marker_red)));
        Marker markerDestination = mMap.addMarker(new MarkerOptions()
                .position(Customer.getInstance().mEndLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_marker_blue)));

        markerDestination.showInfoWindow();
        markerOrigin.showInfoWindow();
        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(10);
        for(int i = 0; i < newPolylinePaths.get(0).getPoints().size(); i++)
            polylineOptions.add(newPolylinePaths.get(0).getPoints().get(i));
        mMap.addPolyline(polylineOptions);
    }

    class AsyncUpdatePosition extends AsyncTask<Void, Integer, Void> {

        public AsyncUpdatePosition() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //TODO: cập nhật vị trí tài xế và khách hàng thông qua biến posDriver và posCustomer;

            while (true) //Cho đến khi tài xế tới được chỗ khách hàng hoặc tài xế bấm nút OnTrip
            {
                SystemClock.sleep(300);
                posDriver = Customer.getInstance().mDriverLocation;
                publishProgress(0);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateMarkerDriver();
            updateMarkerCustomer();
        }
    }


    @Override
    public void onCustomerLocationChanged(LatLng location) {
        //TODO: delete
    }

    @Override
    public void onDriverLocationChanged(LatLng location) {
        //TODO: Update Driver location here :))
        MyHelper.toast(this, "driver location changed: " + location.latitude + ", " + location.longitude);
    }

    @Override
    public void onBookingResult(String driver) {
        //TODO: delete
    }

    @Override
    public void onDriverInfoReady() {
        showDriverInfo();
    }
}
