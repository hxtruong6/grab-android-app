package com.example.hxtruong.grabbikeapp.WaitingForFindingDriver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.hxtruong.grabbikeapp.R;

public class WaitForFindingDriver extends AppCompatActivity {

    AsyncTaskFindDriver myAsyncTask;
    TextView txtAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_finding_driver);

        myAsyncTask = new AsyncTaskFindDriver(WaitForFindingDriver.this);
        myAsyncTask.execute();
        txtAnimation = findViewById(R.id.txtAnimation);


    }
}
