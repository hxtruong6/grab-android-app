package com.example.hxtruong.grabbikeapp;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.hxtruong.grabbikeapp.authentication.Authentication;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import core.Definition;
import core.driver.Driver;
import core.helper.MyHelper;
import core.user.User;

public class MainActivity extends AppCompatActivity implements User.IUserListener
{
    public FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    EditText etAuto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        User.getInstance().registerIUserInterface(this);

        findViewById(R.id.btnBook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              User.getInstance().sendBookingRequest();
            }
        });

        findViewById(R.id.btnCreateGPS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.getInstance().updateUserLocation(MyHelper.createRandomLocation());
            }
        });

        etAuto = findViewById(R.id.etPlaceAuto);
        etAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getParent());
                    startActivityForResult(intent, Definition.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
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

            case Definition.PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    Log.i("AutoPlace", "Place: " + place.getName());
                    etAuto.setText(place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i("AutoPlace    ", status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
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
        MyHelper.toast(getApplicationContext(), "changed location: "+ User.getInstance().mLastKnownLocation.toString());
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
