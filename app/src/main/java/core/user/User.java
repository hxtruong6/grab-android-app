package core.user;

import android.location.Location;

import core.helper.FirebaseHelper;

public class User {
    private static final User ourInstance = new User();
    public static User getInstance() {
        return ourInstance;
    }

    Location mLastKnownLocation; //tracking gps

      private User() {
        initUserData();
    }

    public void initUserData() {

    }


    public void createBooking(){

    }

    public void sendBookingRequest(){

    }

    public void updateLocation(Location loc){
          mLastKnownLocation = loc;
          //update location to map on UI...
    }

}
