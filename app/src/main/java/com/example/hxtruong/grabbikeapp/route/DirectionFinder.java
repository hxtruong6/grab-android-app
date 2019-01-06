package com.example.hxtruong.grabbikeapp.route;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import core.driver.Driver;

public class DirectionFinder {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDzGTfU97JHRG9n2U29EAojIhFll2RE09I";
    private DirectionFinderListener listener;
    private LatLng origin;
    private LatLng destination;

    public DirectionFinder(DirectionFinderListener listener, LatLng origin, LatLng destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }


    public void execute() {
        listener.onDirectionFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    public String convertToString(LatLng latLng){
        return String.valueOf(latLng.latitude) +"," + String.valueOf(latLng.longitude);
    }

    private String createUrl() {
        return DIRECTION_URL_API +
                "origin=" + convertToString(origin) +
                "&destination=" + convertToString(destination) +
                "&key=" + GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String link = strings[0];
            HttpURLConnection urlConnection = null;

            //region NewVersion

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(link);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }


            //endregion

            return result.toString();
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJson(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJson(String data) throws JSONException {
        if (data == null)
            return;

        List<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");

            JSONObject jsonBound = jsonRoute.getJSONObject("bounds");
            JSONObject jsonNortheast = jsonBound.getJSONObject("northeast");
            JSONObject jsonSouthwest = jsonBound.getJSONObject("southwest");

            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.bounds = new LatLngBounds(
                    new LatLng(jsonSouthwest.getDouble("lat"),jsonSouthwest.getDouble("lng")),
                    new LatLng(jsonNortheast.getDouble("lat"),jsonNortheast.getDouble("lng"))
            );
            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            Driver.getInstance().routePoints = route.points;

            routes.add(route);
        }

        listener.onDirectionFinderSuccess(routes);
    }

    private List<LatLng> decodePolyLine(String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }


}
