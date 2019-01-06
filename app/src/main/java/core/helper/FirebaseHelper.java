package core.helper;

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
import core.driver.DriverInfo;

public class FirebaseHelper {

    public static boolean hasUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void sendBookingLocation(LatLng mStartLocation, LatLng mEndLocation, LatLng mCurrLocation) {
        //Trường
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest").child(userId);
        setGeoFireLocation(ref, "startLoc", mStartLocation);
        setGeoFireLocation(ref, "endLoc", mEndLocation);
        setGeoFireLocation(ref, "currLoc", mCurrLocation);
    }

    private static void setGeoFireLocation(DatabaseReference ref, String key, LatLng loc) {
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(key, new GeoLocation(loc.latitude, loc.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
            }
        });
    }

    private static double radius = 5;
    private static Boolean driverFound = false;
    private static GeoQuery geoQuery;

    // Truong
    public static void receiveBookingResultFromFirebase() {
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
                    // this function use put customerRequestId to availbleDriver field
                    final DatabaseReference availableDriverRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable").child(Customer.getInstance().driverId);
                    final String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // delete customerRequest
                    final DatabaseReference customerRequestRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId);
                    customerRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // update location of customer to availabel driver
                                availableDriverRef.child(customerId).setValue(dataSnapshot.getValue());
                                HashMap map = new HashMap();
                                map.put("customerRequestId", customerId);
                                availableDriverRef.updateChildren(map);
                                customerRequestRef.removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        // update location of driver on customer's map
                                        Customer.getInstance().startUpdateDriverLocation();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverId).child("currLoc").child("l");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("xxx UpdateDriverLoc", dataSnapshot.toString());
                    LatLng driverLocation = parseGeoLocation(dataSnapshot);
                    Customer.getInstance().receiveDriverLocationFromFirebase(driverLocation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private static LatLng parseGeoLocation(DataSnapshot dataSnapshot) {
        List<Object> map = (List<Object>) dataSnapshot.getValue();
        double locationLat = 0;
        double locationLng = 0;
        if (map.get(0) != null) {
            locationLat = Double.parseDouble(map.get(0).toString());
        }
        if (map.get(1) != null) {
            locationLng = Double.parseDouble(map.get(1).toString());
        }
        LatLng loc = new LatLng(locationLat, locationLng);
        return loc;
    }

    private static String getUid() {
        return FirebaseHelper.getUser().getUid();
    }

    //Driver area
    public static void updateDriverLocationToFirebase(LatLng loc, String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        String driverId = FirebaseHelper.getUid();
        if (path.equals("driversWorking")) {
            setGeoFireLocation(myRef.child(driverId), "currLoc", loc);
        } else//availableDriver
        {
            setGeoFireLocation(myRef, driverId, loc);
        }


    }

    public static void startListenCustomerRequest() {
        final String driverId = FirebaseHelper.getUid();
        final DatabaseReference driversAvailableRef = FirebaseDatabase.getInstance().getReference("driversAvailable").child(driverId);

        driversAvailableRef.child("customerRequestId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String customerRequestId = dataSnapshot.getValue(String.class);
                    Log.d("xxx", "onDataChange: avail driver listen: cusId" + customerRequestId);
                    final DatabaseReference driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverId);
                    driversAvailableRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d("xxx", "working driver listen: " + dataSnapshot.getValue().toString());
                                driverWorkingRef.setValue(dataSnapshot.getValue());
                                driversAvailableRef.removeValue();

                                // change 'working = true' in driver to know it have already get a customer
                                Driver.getInstance().receiveAndStartTripWithCustomerRequest(customerRequestId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getDriversAvailableLocation(String driverId, final Driver.IOnGetDataDriverCallback onGetDataDriverCallback) {
        if (driverId == null) driverId = FirebaseHelper.getUid();
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("driversAvailable").child(driverId).child("l");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("xxx", "getDriversAvailableLocation: " + dataSnapshot.toString());
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null)
                        locationLat = Double.parseDouble(map.get(0).toString());
                    if (map.get(1) != null)
                        locationLng = Double.parseDouble(map.get(1).toString());

                    LatLng driverLocation = new LatLng(locationLat, locationLng);
//                    Log.d("xxx", "driver location: " + driverLocation.toString());
                    onGetDataDriverCallback.onGetLocationCallback(driverLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void getDriverInfo(final String driverId) {
//        Log.d("xxx get driverID: ", driverId);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("drivers").child(driverId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DriverInfo tmp = dataSnapshot.getValue(DriverInfo.class);
//                Log.d("xxx", "driver infor: " + tmp.toString());
                Customer.getInstance().updateDriverInfo(tmp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("xxx ", "get driver info Cancelled");
            }
        };
        databaseReference.addListenerForSingleValueEvent(listener);
    }

    public static void registerCustomerInfoToFirebase() {
        //tạm thời ghi lên customer ở đây, sau này đăng kí xong sẽ ghi lên
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database.getReference("customers").child(getUid());

        myRef2.child("name").setValue(getUser().getDisplayName());
        myRef2.child("email").setValue(getUser().getEmail());
    }

    public static void registerDriverInfoToFirebase() {
        //tạm thời ghi lên customer ở đây, sau này đăng kí xong sẽ ghi lên
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database.getReference("drivers").child(getUid());

        String name = getUser().getDisplayName();
        String email = getUser().getEmail();
        String vehicle = "81N1 - 12455";
        int star = 5;
        String phone = "0971096050";

        DriverInfo driverInfo = new DriverInfo(name, email, vehicle, phone, star);


        Map<String, Object> infoMap = driverInfo.toMap();
        myRef2.updateChildren(infoMap);


    }


    public static void registerDriverToFirebase(String path, LatLng location) {
        String driverId = getUid();
        Log.d("xxx", "registerDriver: [id:" + driverId + "|Path: " + path + "|Loc: " + location.toString() + "]");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        setGeoFireLocation(ref, driverId, location);
    }

    public static void updateCustomerLocationToFirebase(LatLng loc) {
        //Todo:xx

    }
}
