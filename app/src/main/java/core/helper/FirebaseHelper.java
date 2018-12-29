package core.helper;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.customer.Customer;
import core.driver.Driver;

public class FirebaseHelper {

    public static boolean hasUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // Truong
    public static void sendBookingLocation(Location pickupLoc) {
       /*
       - Viết vào customerRequsest
        - Hàm này là customer bắt đầu gủi request lên server, t có truyền vào cái Booking (startLocation với end location)
        - Ghi requset lên server
        - lấy Uid nhanh: String strID = getUid();
       */
        // TODO: need to review what exactly get Uid here? how about driver?
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        // TODO: change this hard code here -> need to get curretun location
//        pickupLoc = new Location("");
//        pickupLoc.setLatitude(0.15);
//        pickupLoc.setLongitude(0.31);
        geoFire.setLocation(userId, new GeoLocation(pickupLoc.getLatitude(), pickupLoc.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Toast.makeText(getApplicationContext(), "Set customer pick up location", Toast.LENGTH_SHORT).show();
            }
        });

        Customer.getInstance().pickupLocation = new LatLng(pickupLoc.getLatitude(), pickupLoc.getLongitude());
        //mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

        //((Button) view.findViewById(R.id.request)).setText("Getting your Driver....");

    }

    private static int radius = 1;
    private static Boolean driverFound = false;
    private static String driverFoundId;

    static GeoQuery geoQuery;

    // Truong
    public static void receiveBookingResultFromFirebase() {
        /*Trường
        //- Tìm 1 driver gần nhất
        - 
        //- Hàm này trả về Driver đã nhận chuyenr mới gửi lên server
        //- Trong này m đặt listener database, tạo query gì gì như bên SimCode làm
        - Chuyển availabe driver -> working driver
        - Xóa customerRequest
        */
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(Customer.getInstance().pickupLocation.latitude, Customer.getInstance().pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound) {
                    driverFound = true;
                    driverFoundId = key;
                    Customer.getInstance().driverFoundId = key;
                    //DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    // this function use put customerRequestId to availbleDriver field
                    DatabaseReference availableDriverRef = FirebaseDatabase.getInstance().getReference().child("availableDriver").child(driverFoundId);
                    // TODO: what Uid here????
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRequestId", customerId);
                    availableDriverRef.updateChildren(map);

                    // delete customerRequest
                    DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId);
                    customerRequestRef.removeValue();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    receiveBookingResultFromFirebase();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

        // Call getUpdateDriverLocation to update driver location for customer UI
        getUpdateDriverLocation(driverFoundId);

    }

    // Truong
    public static void getUpdateDriverLocation(String driverId) {
        /*
         * Đặt reference tới workingDriver/driverUID/currentlocation
         */
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("workingDriver").child(driverId);
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("xxx UpdateDriverLoc", dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    // send location to user by call a function in user when every location change
                    Customer.getInstance().receiveDriverLocationFromFirebase(driverLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        // reference.onChanged(){
        //     Customer.getInstance().getUpdateDriverLocation(loc);
        // }

        //return MyHelper.createLocation(lat, lng);

    }

    // TODO: hàm này viết có vấn đề rồi. Vì trong class này có cả customer và driver. => get User get cái nào???
    private static String getUid() {
        return FirebaseHelper.getUser().getUid();
    }
}
