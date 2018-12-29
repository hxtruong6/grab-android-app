package core.user;

import android.location.Location;

import core.driver.Driver;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class User {
    private static final User ourInstance = new User();
    public static User getInstance() {
        return ourInstance;
    }

    private IUserListener mListener;

    public Location mLastKnownLocation; //tracking gps
    Location mDriverLoction;

    Location mStartLoction;
    Location mEndLocation;

    private User() {
        initUserData();
    }

    public void initUserData() {
        mLastKnownLocation = MyHelper.createLocation(1.2f, 2.3f);
        mStartLoction = MyHelper.createLocation(2.0f, 10.2f);
        mEndLocation = MyHelper.createLocation(2.2f, .9f);
    }

    public void registerIUserInterface(IUserListener listener){
        mListener = listener;
    }



    public void sendBookingRequest(){
            Booking booking = new Booking(mStartLoction, mEndLocation);
            FirebaseHelper.sendBookingLocation(booking);

            String driverId = FirebaseHelper.receiveBookingResultFromFirebase();
            if(!driverId.isEmpty()){

                FirebaseHelper.getUpdateDriverLocation(driverId);

            }


    }

    public void updateUserLocation(Location loc){
          mLastKnownLocation = loc;
          FirebaseHelper.updateUserLocationToFirebase(loc);

          if(mListener!=null)
            mListener.onMyLocationChanged(loc);
    }

    public void updateDriverLocation(Location location){

        if(mListener!=null)
            mListener.onDriverLocationChanged(location);
    }


    //Interface
    public interface IUserListener {
        void onMyLocationChanged(Location location);
        void onDriverLocationChanged(Location location);
        void onBookingResult(Driver driver);
    }

}
