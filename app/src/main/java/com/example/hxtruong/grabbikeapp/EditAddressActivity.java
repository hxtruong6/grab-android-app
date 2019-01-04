package com.example.hxtruong.grabbikeapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EditAddressActivity extends AppCompatActivity {
    private  String current0rigin;
    private String edtDestination;
    private String currentLatitude, currentLongitude;
    private String lat ="";
    private String lng ="";
    private static String GOOGLE_KEY = "AIzaSyDzGTfU97JHRG9n2U29EAojIhFll2RE09I";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        EditText edtOrigin = (EditText)findViewById(R.id.edtOrigin);
        final EditText edtDestination = (EditText)findViewById(R.id.edtDestination);
        Button btnSubmit = (Button)findViewById(R.id.btnSubmitAddress);

        Intent intent = this.getIntent();

        this.currentLatitude= intent.getStringExtra("currentLatitude");
        this.currentLongitude = intent.getStringExtra("currentLongitude");
        this.current0rigin = intent.getStringExtra("origin");


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetCoordinates().execute(edtDestination.getText().toString().replace(" ","+"));
                sendIntent();
            }
        });
    }

    private void convertAddressToGPS() {
    }

    private void sendIntent() {


    }

    private class GetCoordinates extends AsyncTask<String, Void, String> {

        ProgressDialog dialogInfo = new ProgressDialog(EditAddressActivity.this);
        ProgressDialog dialog = new ProgressDialog(EditAddressActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Vui lòng đợi...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try{
                String address = strings[0];
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",address,GOOGLE_KEY);
                response = http.getHTTPData(url);
                return response;
            }
            catch (Exception ex)
            {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonObject = new JSONObject(s);

                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();

                TextView textView = (TextView)findViewById(R.id.tvResultGPS);
                textView.setText(String.format("Coordinates: %s / %s ",lat, lng));
                if(dialog.isShowing())
                    dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
                //dialogInfo.setMessage("API đang dùng đã vượt quá số lượng truy cập!!");
                //dialogInfo.show();
                //if(dialog.isShowing())
                //  dialog.dismiss();

            }
        }

    }
}
