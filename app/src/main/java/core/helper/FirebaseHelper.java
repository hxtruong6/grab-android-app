package core.helper;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import core.driver.Driver;
import core.user.Booking;
import core.user.User;

public class FirebaseHelper {

    public static boolean hasUser() {
        return FirebaseAuth.getInstance().getCurrentUser()!=null;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void sendBookingLocation(Booking booking) {
       //Trường DO cái này
       /*
       - Viết vào customerRequsest
        - Hàm này là customer bắt đầu gủi request lên server, t có truyền vào cái Booking (startLocation với end location)
        - Ghi requset lên server
        - lấy Uid nhanh: String strID = getUid();

       */
        
    }

    public static String receiveBookingResultFromFirebase(){
        /*Trường
        - Tìm 1 driver gần nhất
        - 
        - Hàm này trả về Driver đã nhận chuyenr mới gửi lên server
        - Trong này m đặt listener database, tạo query gì gì như bên SimCode làm
        - Chuyển availabe driver -> working driver
        - Xóa customerRequest
        */
        return "driverID";

    }

    public static Location getUpdateDriverLocation(String driverId){
        /*
        * Đặt reference tới workingDriver/driverUID/currentlocation
        */

        // reference.onChanged(){
        //     User.getInstance().getUpdateDriverLocation(loc);
        // }


        float lat = 1, lng = 2.0f;
        Location location = MyHelper.createLocation(lat, lng);
        User.getInstance().updateDriverLocation(location);
        return location;
    }

    private static String getUid() {
        return FirebaseHelper.getUser().getUid();
    }

    public static void updateUserLocationToFirebase(Location loc) {

    }

    public static void updateDriverLocationToFirebase(Location loc) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("availableDriver").child(getUid()).child("location");

        myRef.child("lat").setValue(loc.getLatitude());
        myRef.child("lng").setValue(loc.getLongitude());
    }

    /* Đăng kí driver lên firebase mỗi khi mở app
     * Ghi lên firebase/availableDriver: UID, current location
     *
     */
    public static void registerDriverToFirebase(Location currentLocation) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("availableDriver").child(getUid());

        myRef.child("customerRequestId").setValue("null");
        myRef.child("location").child("lat").setValue(currentLocation.getLatitude());
        myRef.child("location").child("lng").setValue(currentLocation.getLongitude());

    }

    public static void startListenCustomerRequest() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("availableDriver").
                child(getUid()).child("customerRequestId");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String customerRequestId = (String) dataSnapshot.getValue();
                Log.d("ListenCustomerRequest:", customerRequestId);

                if(customerRequestId != null && !customerRequestId.isEmpty() && !customerRequestId.equals("null")){
                    moveAvailableDriverToWorkingDriver(dataSnapshot);
                    Driver.getInstance().receiveAndStartTripWithCustomerRequest(customerRequestId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void moveAvailableDriverToWorkingDriver(DataSnapshot dataSnapshot) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference availableDriver = database.getReference("availableDriver").child(getUid());

        DatabaseReference workingDriver = database.getReference("workingDriver").child(getUid());
        workingDriver.setValue(dataSnapshot);
        availableDriver.setValue(null);

    }
}
