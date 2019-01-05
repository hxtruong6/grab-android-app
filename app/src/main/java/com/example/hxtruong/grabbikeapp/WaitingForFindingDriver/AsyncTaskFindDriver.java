package com.example.hxtruong.grabbikeapp.WaitingForFindingDriver;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.R;

class AsyncTaskFindDriver extends AsyncTask<Void, Integer, Void> {

    int count;
    TextView txtAnimation;
    Activity contextParent;

    public AsyncTaskFindDriver(Activity contextParent){
        this.contextParent = contextParent;
    }

    @Override
    protected void onPreExecute() {
        count = 0;
        txtAnimation = contextParent.findViewById(R.id.txtAnimation);
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Gửi thông tin xuống Backend, gọi hàm gọi tài xế

        for(int i = 0; i <= 20; i++) {
            SystemClock.sleep(1000);
            publishProgress(i);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        String s = ". ";
        //if (values[0] == NOT_YET) //Chua tim duoc tai xe
        {
            for (int i = 0; i < count; i++){
                s += ". ";
            }
            count++;
            if (count > 3) {
                count = 0;
                s = "";
            }
            txtAnimation.setText(s);
        }

        //Chuyen Activity thoi

    }
}
