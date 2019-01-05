package core.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class MyHelper {
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static  boolean hasSMSPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public static Location createLocation(double lat, double lng) {
        Location loc = new Location("dummyprovider");
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }

    public static LatLng createRandomLocation() {
        Random rnd = new Random();
        return new LatLng(rnd.nextDouble(), rnd.nextDouble());
    }
}
