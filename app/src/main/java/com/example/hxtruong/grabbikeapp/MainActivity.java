package com.example.hxtruong.grabbikeapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.authentication.Authentication;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import core.Definition;
import core.customer.Customer;
import core.driver.Driver;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;
import core.services.GpsServices;

import static core.Definition.PERMISSIONS;

public class MainActivity extends AppCompatActivity implements Customer.IUserListener, NavigationView.OnNavigationItemSelectedListener {
    public FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        mAuth = FirebaseAuth.getInstance();


        findViewById(R.id.btnBook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Customer.getInstance().sendBookingRequest();
            }
        });

        findViewById(R.id.btnChangeCustomerLoction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Customer.getInstance().updateCustomerLocation(MyHelper.createRandomLocation());
            }
        });

        findViewById(R.id.btnReceiveBook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Driver.getInstance().registerIDriverInterface(new Driver.IDriverListener() {
                    @Override
                    public void onDriverLocationChanged(LatLng loc) {

                    }

                    @Override
                    public void receiveCustomerRequest(String customerRequestId) {

                    }
                });
                Driver.getInstance().updateDriverLocation(MyHelper.createRandomLocation());
            }
        });
    }

    private void startGpsService() {
        Intent gpsService = new Intent(MainActivity.this, GpsServices.class);
        MainActivity.this.startService(gpsService);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isLogined()){
            login();
        }
        else {
            Customer.getInstance().registerIUserInterface(this);
            checkAllPermissions();
            updateUI();
        }
    }

    private void checkAllPermissions() {
        if (!MyHelper.hasSMSPermissions(this, Definition.PERMISSIONS)) {
            showRequestPermissionsInfoAlertDialog();
        }
    }

    private void showRequestPermissionsInfoAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Request");
        builder.setMessage("Turn the GPS on to use the app");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                requestMyPermissions();
            }
        });
        builder.show();
    }

    private void requestMyPermissions() {
        if (
                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.INTERNET)
                        && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                )
            return;
        ActivityCompat.requestPermissions(this, PERMISSIONS, Definition.REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case Definition.REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    MyHelper.toast(this, "Permissions Granted!");
                    startGpsService();
                } else
                {
                    MyHelper.toast(this, "Permissions Denied!");
                }
            }
        }

    }


    private void updateUI() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case Definition.REQUEST_LOGIN:
                if(resultCode == Definition.RESULT_LOGIN_SUCCESSFUL) {
                    if(data!=null)
                        MyHelper.toast(this, data.getStringExtra("AccessToken"));
                    else {
                        MyHelper.toast(this, "login successful");
                        //loadUserData();
                        updateUI();
                    }
                }
                else{
                    MyHelper.toast(this, "Login Failed");
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    * Our funtions
    *
    * */


    private void login() {
        Intent intentLogin = new Intent(this, Authentication.class);
        startActivityForResult(intentLogin, Definition.REQUEST_LOGIN);
    }

    private boolean isLogined() {
        return this.mAuth.getCurrentUser() != null;
    }

    private void logOut() {
        if(isLogined())
        {
            mAuth.signOut();
            login();
        }
    }


    public void goToDriver(View view) {

        Intent intent = new Intent(MainActivity.this, DriverMapActivity.class);
        startActivity(intent);

    }

    public void gotToCustomer(View view) {
        Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
        startActivity(intent);

    }
    
    @Override
    public void onCustomerLocationChanged(LatLng loc) {
        // Cập nhật vị trí customer
        MyHelper.toast(getApplicationContext(), "changed location: "+ Customer.getInstance().mLastKnownLocation.toString());
        showDebugMsg("Customer Location Changed: " + loc.latitude+", "+loc.longitude);
    }

    @Override
    public void onDriverLocationChanged(LatLng loc) {
        showDebugMsg("Driver Location Changed: "+loc.latitude+", " + loc.longitude);
        //set lai location tai xe
    }

    @Override
    public void onBookingResult(String driver) {
        //String driverInfo = FirebaseHelper.getDriverInfo(driver);
        //showDebugMsg("Found a driver: "+ driver+"->"+ driverInfo);
        //
    }

    @Override
    public void onDriverInfoReady() {

    }

    void showDebugMsg(String msg){
        ((TextView)findViewById(R.id.tvDebug)).setText(msg);
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
