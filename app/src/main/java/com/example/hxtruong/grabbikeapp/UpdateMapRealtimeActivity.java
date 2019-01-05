package com.example.hxtruong.grabbikeapp;

import android.app.Dialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import core.customer.Customer;
import core.driver.DriverInfo;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class UpdateMapRealtimeActivity extends FragmentActivity implements OnMapReadyCallback, Customer.IUserListener {

    private Marker markerDriver;
    private Marker markerCustomer;
    private GoogleMap mMap;
    private LatLng posDriver;
    private AsyncUpdatePosition asyncUpdate;
    private LatLng posCustomer;


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
        showDriverInfo();
    }

    void updateMarkerDriver()
    {
        if (markerDriver != null)
            markerDriver.remove();
        markerDriver = mMap.addMarker(new MarkerOptions()
                .position(posDriver)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_biker_30)));
    }

    void updateMarkerCustomer(){
        if (markerCustomer!= null)
            markerCustomer.remove();
        markerCustomer = mMap.addMarker(new MarkerOptions()
                .position(posCustomer)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_street_view_40)));
    }

    private void showDriverInfo() {
        DriverInfo driverInfo = Customer.getInstance().getDriverInfo();
        if(!driverInfo.isEmpty()){
            Log.d("showDriverInfo", ": "+driverInfo.name);
            MyHelper.toast(this, driverInfo.name +"\n"+driverInfo.email+"\n"+driverInfo.vehicle);
            //show driver info here

            final Dialog dialog = new Dialog(UpdateMapRealtimeActivity.this);
            dialog.setContentView(R.layout.dialog_infor_driver);

            ((TextView)dialog.findViewById(R.id.txtDriverName)).setText(driverInfo.name);
            ((TextView)dialog.findViewById(R.id.txtLicensePlate)).setText(driverInfo.vehicle);
            Button btnOk = dialog.findViewById(R.id.btnOK);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            dialog.getWindow().setLayout(1200, 1400);

        }
        else{
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
        GetPositionFromDatase();
        updateMarkerCustomer();
        updateMarkerDriver();
    }

    private void GetPositionFromDatase() {
        Location t = Customer.getInstance().mDriverLoction;
        posDriver = new LatLng(t.getLatitude(), t.getLongitude());
        t = Customer.getInstance().mLastKnownLocation;
        posCustomer = new LatLng(t.getLatitude(), t.getLongitude());
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

            while(true) //Cho đến khi tài xế tới được chỗ khách hàng hoặc tài xế bấm nút OnTrip
            {
                SystemClock.sleep(300);
                Location t = Customer.getInstance().mDriverLoction;
                posDriver = new LatLng(t.getLatitude(), t.getLongitude());
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
    public void onCustomerLocationChanged(Location location) {
        //TODO: delete
    }

    @Override
    public void onDriverLocationChanged(Location location) {
        //TODO: Update Driver location here :))
        MyHelper.toast(this, "driver location changed: " + location.getLongitude() +", "+location.getLongitude());
    }

    @Override
    public void onBookingResult(String driver) {
        //TODO: delete
    }
}
