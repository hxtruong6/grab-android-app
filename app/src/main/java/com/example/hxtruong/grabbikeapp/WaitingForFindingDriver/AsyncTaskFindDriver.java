package com.example.hxtruong.grabbikeapp.WaitingForFindingDriver;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.R;
import com.google.android.gms.maps.model.LatLng;
import com.example.hxtruong.grabbikeapp.UpdateMapRealtimeActivity;

import core.customer.Customer;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

class AsyncTaskFindDriver extends AsyncTask<Void, Integer, Void> implements Customer.IUserListener {

    int count;
    int t1;
    boolean datimthaytaixe;
    TextView txtAnimation;
    Activity contextParent;
    private boolean datimthaythongtintaixe;

    public AsyncTaskFindDriver(Activity contextParent) {
        this.contextParent = contextParent;
        Customer.getInstance().registerIUserInterface(this);
    }

    @Override
    protected void onPreExecute() {
        count = 0;
        t1 = 0;
        txtAnimation = contextParent.findViewById(R.id.txtAnimation);
        super.onPreExecute();
        datimthaytaixe = false;
        datimthaythongtintaixe = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Gửi thông tin xuống Backend, gọi hàm gọi tài xế

        while (true) {
            SystemClock.sleep(1000);
            publishProgress(1);
            t1++;
            if (datimthaytaixe && datimthaythongtintaixe) {
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
    public void onCustomerLocationChanged(LatLng location) {

    }

    @Override
    public void onDriverLocationChanged(LatLng location) {

    }

    @Override
    public void onBookingResult(String driver) {
        //timf dudwojc tai xe
        datimthaytaixe = true;
        Log.d("xxx", "da tim duoc tai xe roi ne :))" + driver);

    }

    @Override
    public void onDriverInfoReady() {
        datimthaythongtintaixe = true;
    }
}
