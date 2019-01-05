package com.example.hxtruong.grabbikeapp.WaitingForFindingDriver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.hxtruong.grabbikeapp.R;

public class WaitForFindingDriver extends AppCompatActivity {

    AsyncTaskFindDriver myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_finding_driver);

        myAsyncTask = new AsyncTaskFindDriver(WaitForFindingDriver.this);
        myAsyncTask.execute();
    }
}
