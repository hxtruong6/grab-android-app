package core.helper;

import android.location.Location;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import core.user.Booking;

public class FirebaseHelper {

    public static boolean hasUser() {
        return FirebaseAuth.getInstance().getCurrentUser()!=null;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void sendBookingLocation(Booking booking) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("booking").child(getUid());

        myRef.child("startLocation").child("lat").setValue(booking.mStartLocation.getLatitude());
        myRef.child("startLocation").child("long").setValue(booking.mStartLocation.getLongitude());
        myRef.child("endLocation").child("lat").setValue(booking.mEndLocation.getLatitude());
        myRef.child("endLocation").child("long").setValue(booking.mEndLocation.getLongitude());

    }

    public  static  void updateUserLocation(Location loc){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("booking").child(getUid()).child("currentLocation");

        myRef.child("lat").setValue(loc.getLatitude());
        myRef.child("long").setValue(loc.getLongitude());
    }

    private static String getUid() {
        return FirebaseHelper.getUser().getUid();
    }
}
