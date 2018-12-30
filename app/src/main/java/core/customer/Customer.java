package core.customer;

import android.location.Location;

import core.Booking;
import core.driver.Driver;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class Customer {
    private static final Customer ourInstance = new Customer();
    public static Customer getInstance() {
        return ourInstance;
    }

    private IUserListener mListener;

    public Location mLastKnownLocation; //tracking gps
    Location mDriverLoction;

    Location mStartLoction;
    Location mEndLocation;

    private Customer() {
        initCustomerData();
    }

    public void initCustomerData() {
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
