package com.example.hxtruong.grabbikeapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import core.Definition;

public class EditAddressActivity extends AppCompatActivity {
    private  String current0rigin;
    private String address;
    private String currentLatitude, currentLongitude;
    private String lat ="";
    private String lng ="";
    private static String GOOGLE_KEY = "AIzaSyDzGTfU97JHRG9n2U29EAojIhFll2RE09I";

    int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

       // EditText edtOrigin = (EditText)findViewById(R.id.edtOrigin);
        //final EditText edtDestination = (EditText)findViewById(R.id.edtDestination);

        Intent intent = this.getIntent();

        registPlaceAutoComplete();


    }

    @Override
    public void finish() {

        // Chuẩn bị dữ liệu Intent.
        Intent data = new Intent();
        data.putExtra("address", address);
        data.putExtra("lat",lat);
        data.putExtra("lng",lng);

        // Activity đã hoàn thành OK, trả về dữ liệu.
        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }



    private void registPlaceAutoComplete() {

        final PlaceAutocompleteFragment placeDestination = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);

        AutocompleteFilter filter = new AutocompleteFilter.Builder()
                .setCountry("VN")
                .build();

        placeDestination.setFilter(filter);

        LatLngBounds latLngBounds = new LatLngBounds(
                new LatLng(10.428096, 106.436985),
                new LatLng(11.144442, 106.961429));

        placeDestination.setBoundsBias(latLngBounds);

        placeDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeDestination.setText(place.getName());
                //set string destination
                address = place.getAddress().toString();
                if(!address.isEmpty()){
                    new GetCoordinates().execute(address.replace(" ","+"));
                }


            }
            @Override
            public void onError(Status status) {
                placeDestination.setText(status.toString());
            }
        });


    }


    private class GetCoordinates extends AsyncTask<String,Void,String> {
        ProgressDialog dialog = new ProgressDialog(EditAddressActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait....");
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

                if(dialog.isShowing())
                    dialog.dismiss();
                finish();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
