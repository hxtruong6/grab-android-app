package core.helper;

import android.location.Location;
import android.support.annotation.NonNull;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import core.customer.Customer;
import core.driver.Driver;
import core.driver.DriverInfo;

public class FirebaseHelper {

    public static boolean hasUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void sendBookingLocation(LatLng mStartLocation, LatLng mEndLocation) {
        //Trường DO cái này
       /*
       - Viết vào customerRequsest
        - Hàm này là customer bắt đầu gủi request lên server, t có truyền vào cái Booking (startLatLng với end location)
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
        geoFire.setLocation(userId, new GeoLocation(mStartLocation.latitude, mStartLocation.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Toast.makeText(getApplicationContext(), "Set customer pick up location", Toast.LENGTH_SHORT).show();
            }
        });

        Customer.getInstance().mStartLocation = new LatLng(mStartLocation.latitude, mStartLocation.longitude);
        //mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));

        //((Button) view.findViewById(R.id.request)).setText("Getting your Driver....");

    }

    private static double radius = 5;
    private static Boolean driverFound = false;
    private static GeoQuery geoQuery;

    // Truong
    public static void receiveBookingResultFromFirebase() {
        /*Trường
        //- Tìm 1 driver gần nhất
        //- Hàm này trả về Driver đã nhận chuyenr mới gửi lên server
        //- Trong này m đặt listener database, tạo query gì gì như bên SimCode làm
        - Chuyển availabe driver -> working driver
        - Xóa customerRequest
        */
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocationRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(Customer.getInstance().mStartLocation.latitude, Customer.getInstance().mStartLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("driverx", "id: " + Customer.getInstance().driverId);
                if (!driverFound) {
                    driverFound = true;
                    Customer.getInstance().driverId = key;
                    //DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    // this function use put customerRequestId to availbleDriver field
                    DatabaseReference availableDriverRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable").child(Customer.getInstance().driverId);
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
                Log.d("driverx", "exit");
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("driverx", "moved");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("driverx", "ready");
                if (!driverFound) {
                    radius++;
                    Log.d("driverx", "radius++");
                    receiveBookingResultFromFirebase();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("driver", "error");
            }
        });
    }

    // Truong
    public static void getUpdateDriverLocation(String driverId) {
        /*
         * Đặt reference tới workingDriver/driverUID/currentlocation
         */
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverId).child("l");
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

                    LatLng driverLocation = new LatLng(locationLat, locationLng);
                    // send LatLng to user by call a function in user when every LatLng change
                    Customer.getInstance().receiveDriverLocationFromFirebase(driverLocation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private static String getUid() {
        return FirebaseHelper.getUser().getUid();
    }

    //Driver area
    public static void updateDriverLocationToFirebase(LatLng loc, String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path).child(getUid()).child("location");

        myRef.child("lat").setValue(loc.latitude);
        myRef.child("lng").setValue(loc.longitude);
    }

    /* Đăng kí driver lên firebase mỗi khi mở app
     * Ghi lên firebase/availableDriver: UID, current location
     *
     */
//    public static void registerDriverToFirebase(LatLng currentLocation) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("driversAvailable").child(getUid());
//
//        myRef.child("customerRequestId").setValue("empty");
//        myRef.child("location").child("lat").setValue(currentLocation.latitude);
//        myRef.child("location").child("lng").setValue(currentLocation.longitude());

    //tạm thời ghi lên driver ở đây, sau này đăng kí xong sẽ ghi lên
//        DatabaseReference myRef2 = database.getReference("driver").child(getUid());
//        myRef2.child("name").setValue("Anh tài xế tốt bụng");
//        myRef2.child("email").setValue(getUser().getEmail());
//        myRef2.child("vehicle").setValue("SH");
    // }

    public static void startListenCustomerRequest() {
        final String driverId = "anhtaixe001";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("driversAvailable").
                child(driverId);

        myRef.child("customerRequestId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String customerRequestId = dataSnapshot.getValue(String.class);
                Log.d("ListenCustomerRequest:", customerRequestId);

                if (customerRequestId != null && !customerRequestId.isEmpty() && !customerRequestId.equals("empty")) {
                    //moveAvailableDriverToWorkingDriver(dataSnapshot);
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null)
                        locationLat = Double.parseDouble(map.get(0).toString());

                    if (map.get(1) != null)
                        locationLng = Double.parseDouble(map.get(1).toString());

                    LatLng driverLocation = new LatLng(locationLat, locationLng);
                    registerDriverToFirebase(driverId, "driversWorking", driverLocation);

                    Driver.getInstance().receiveAndStartTripWithCustomerRequest(customerRequestId);
                    myRef.removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void moveAvailableDriverToWorkingDriver(DataSnapshot dataSnapshot) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference availableDriver = database.getReference("driversAvailable").child(getUid());

        DatabaseReference workingDriver = database.getReference("driversWorking").child(getUid());
        workingDriver.setValue(dataSnapshot);
        availableDriver.removeValue();

    }

    public static void getDriverInfo(final String driverId) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("drivers").child(driverId);

        final DriverInfo driverInfo = new DriverInfo();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DriverInfo tmp = dataSnapshot.getValue(DriverInfo.class);
                Customer.getInstance().updateDriverInfo(tmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getDriverInfo", "Cancelled");
            }
        };
        databaseReference.addListenerForSingleValueEvent(listener);
    }

    public static void registerCustomerToFirebase() {
        //tạm thời ghi lên customer ở đây, sau này đăng kí xong sẽ ghi lên
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database.getReference("customers").child(getUid());

        myRef2.child("name").setValue(getUser().getDisplayName());
        myRef2.child("email").setValue(getUser().getEmail());
    }

    public static void updateCustomerLocationToFirebase(LatLng loc) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("").child(getUid()).child("location");
//
//        myRef.child("lat").setValue(loc.latitude);
//        myRef.child("lng").setValue(loc.longitude());

        // Khi customer đã booking thì thông tin request chueyenr đi đâu để tài xế cần cập nhật vị trí customer thì lisner chỗ nào
        //.....
    }

    public static void registerDriverToFirebase(String driverId, String path, LatLng location) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        GeoFire geoFire = new GeoFire(ref);

        geoFire.setLocation(driverId, new GeoLocation(location.latitude, location.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Toast.makeText(getApplicationContext(), "Set customer pick up location", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
