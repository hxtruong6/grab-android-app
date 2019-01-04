package com.example.hxtruong.grabbikeapp;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.authentication.Authentication;
import com.google.firebase.auth.FirebaseAuth;

import core.Definition;
import core.customer.Customer;
import core.driver.Driver;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class MainActivity extends AppCompatActivity implements Customer.IUserListener
{
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        //

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
                    public void onDriverLocationChanged(Location loc) {

                    }

                    @Override
                    public void receiveCustomerRequest(String customerRequestId) {

                    }
                });
                Driver.getInstance().updateDriverLocation(MyHelper.createRandomLocation());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!isLogined()){
            login();
        }
        else {
            Customer.getInstance().registerIUserInterface(this);
            updateUI();
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
    }

    public void gotToCustomer(View view) {
        Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onCustomerLocationChanged(Location loc) {
        // Cập nhật vị trí customer
        MyHelper.toast(getApplicationContext(), "changed location: "+ Customer.getInstance().mLastKnownLocation.toString());
        showDebugMsg("Customer Location Changed: " + loc.getLatitude()+", "+loc.getLongitude());
    }

    @Override
    public void onDriverLocationChanged(Location loc) {
        showDebugMsg("Driver Location Changed: "+loc.getLatitude()+", " + loc.getLongitude());
    }

    @Override
    public void onBookingResult(String driver) {
        String driverInfo = FirebaseHelper.getDriverInfo(driver);
        showDebugMsg("Found a driver: "+ driver+"->"+ driverInfo);

    }

    void showDebugMsg(String msg){
        ((TextView)findViewById(R.id.tvDebug)).setText(msg);
    }
}
