package com.example.hxtruong.grabbikeapp.WaitingForFindingDriver;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.CustomerMapActivity;
import com.example.hxtruong.grabbikeapp.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;

import core.customer.Customer;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

class AsyncTaskFindDriver extends AsyncTask<Void, Integer, Void> implements Customer.IUserListener{

    int count;
    int t1;
    boolean datimthaytaixe;
    TextView txtAnimation;
    Activity contextParent;

    public AsyncTaskFindDriver(Activity contextParent){
        this.contextParent = contextParent;
    }

    @Override
    protected void onPreExecute() {
        count = 0;
        t1 = 0;
        txtAnimation = contextParent.findViewById(R.id.txtAnimation);
        super.onPreExecute();
        datimthaytaixe = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Gửi thông tin xuống Backend, gọi hàm gọi tài xế

        while(true) {
            SystemClock.sleep(1000);
            publishProgress(1);
            t1++;
            if (datimthaytaixe) {
                publishProgress(0);
                return null;
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values[0] == 0) //tim thay tai xe
        {
            Intent intent = new Intent(contextParent, CustomerMapActivity.class);
            contextParent.startActivity(intent);
            MyHelper.toast(contextParent, "Da tim thay tai xe: " + FirebaseHelper.getDriverInfo(Customer.getInstance().driverFoundId));
        } else {
            String s = ". ";
            //if (values[0] == NOT_YET) //Chua tim duoc tai xe
            {
                for (int i = 0; i < count; i++) {
                    s += ". ";
                }
                count++;
                if (count > 3) {
                    count = 0;
                    s = "";
                }
                txtAnimation.setText(s);
            }
        }



    }

    @Override
    public void onCustomerLocationChanged(LatLng location) {

    }

    @Override
    public void onDriverLocationChanged(LatLng location) {

    }

    @Override
    public void onBookingResult(String driver) {
        //timf dudwojc tai xe
        datimthaytaixe = true;

    }
}