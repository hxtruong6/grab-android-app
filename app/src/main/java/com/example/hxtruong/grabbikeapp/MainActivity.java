package com.example.hxtruong.grabbikeapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hxtruong.grabbikeapp.authentication.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import core.Definition;
import core.helper.MyHelper;

public class MainActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
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
}
