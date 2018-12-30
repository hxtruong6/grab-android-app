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

    public LatLng pickupLocation;
    public String driverFoundId;

    private Customer() {
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

    // Truong
    public void sendBookingRequest() {
        //Booking booking = new Booking(mStartLoction, mEndLocation);
        // TODO: send the booking later. Just simple for now
        FirebaseHelper.sendBookingLocation(mLastKnownLocation);
    }

    public void updateMyLocation(Location loc) {
        mLastKnownLocation = loc;

        //.updateUserLocation(loc);
        if (mListener != null)
            mListener.onMyLocationChanged(loc);
    }

    // Truong
    public void receiveBookingResultFromFirebase(Driver driver) {
        FirebaseHelper.receiveBookingResultFromFirebase();
        if (mListener != null)
            mListener.onBookingResult(driver);
        // TODO: show info driver on screeen UI
    }

    // Truong
    public void receiveDriverLocationFromFirebase(LatLng driverLatLng) {
        // this function is auto called from FirebaseHelper after find a driver
        if (mListener != null)
            mListener.onDriverLocationChanged();
    }


    //Interface
    public interface IUserListener {
        void onMyLocationChanged(Location loc);
        void onDriverLocationChanged();
        void onBookingResult(Driver driver);
    }

}
