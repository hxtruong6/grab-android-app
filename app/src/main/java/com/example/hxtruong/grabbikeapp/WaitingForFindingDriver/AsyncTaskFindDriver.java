package com.example.hxtruong.grabbikeapp.WaitingForFindingDriver;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.CustomerMapActivity;
import com.example.hxtruong.grabbikeapp.R;
import com.example.hxtruong.grabbikeapp.UpdateMapRealtimeActivity;
import com.example.hxtruong.grabbikeapp.route.ShowRouteActivity;

import core.customer.Customer;

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
            if (datimthaytaixe || t1 > 5) {
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
            Intent intent = new Intent(contextParent, UpdateMapRealtimeActivity.class);
            contextParent.startActivity(intent);
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
    public void onCustomerLocationChanged(Location location) {

    }

    @Override
    public void onDriverLocationChanged(Location location) {

    }

    @Override
    public void onBookingResult(String driver) {
        //timf dudwojc tai xe
        datimthaytaixe = true;

    }
}
