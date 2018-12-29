package com.example.hxtruong.grabbikeapp;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.hxtruong.grabbikeapp.authentication.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import core.Definition;
import core.customer.Customer;
import core.driver.Driver;
import core.helper.MyHelper;

public class MainActivity extends AppCompatActivity implements Customer.IUserListener
{
    public FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        Customer.getInstance().registerIUserInterface(this);

        findViewById(R.id.btnBook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Customer.getInstance().sendBookingRequest();
            }
        });

        findViewById(R.id.btnCreateGPS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Customer.getInstance().updateMyLocation(MyHelper.createRandomLocation());
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
    public void onMyLocationChanged(Location loc) {
        // Cập nhật vị trí customer
        MyHelper.toast(getApplicationContext(), "changed location: "+ Customer.getInstance().mLastKnownLocation.toString());
    }


    @Override
    public void onSendingBookingRequest(){
        // Đang gửi request lên server 
        // Bật màn hình chờ lên
    }

    @Override
    public void onBookingResult(Driver driver) {
        // Đã nhận được driver
        // Bắt đầu cập nhận vị trí driver

    }

    
    @Override
    public void onDriverLocationChanged(Location loc) {
        // Cập nhật vị trris driver

    }
}
