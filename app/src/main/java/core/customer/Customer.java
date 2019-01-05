package core.customer;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


import core.driver.Driver;
import core.driver.DriverInfo;
import core.helper.FirebaseHelper;

public class Customer {
    private static final Customer ourInstance = new Customer();
    private DriverInfo driverInfo;

    public static Customer getInstance() {
        return ourInstance;
    }

    private IUserListener mListener;
    Boolean isBooking = false;
    public String driverId;
    public LatLng mLastKnownLocation; //tracking gps
    public LatLng mDriverLocation;
    public LatLng mStartLocation;
    public LatLng mEndLocation;


    private Customer() {
        initCustomerData();
    }

    public void initCustomerData() {
        mLastKnownLocation = new LatLng(1.2f, 2.3f);
        mStartLocation = new LatLng(2.0f, 10.2f);
        mEndLocation = new LatLng(2.2f, .9f);

    }

    public void registerIUserInterface(IUserListener listener) {
        mListener = listener;
        FirebaseHelper.registerCustomerToFirebase();
    }

    // Truong
    public void sendBookingRequest() {
        //Booking booking = new Booking(mStartLocation, mEndLocation);
        // TODO: send the booking later. Just simple for now
        FirebaseHelper.sendBookingLocation(mStartLocation, mEndLocation);
        FirebaseHelper.receiveBookingResultFromFirebase();
        Log.d("xxx ", "found driver Id"+ driverId);
    }

    public void startUpdateDriverLocation() {
        if (driverId != null && !driverId.isEmpty()) {
            // Call getUpdateDriverLocation to update driver location for customer UI
            isBooking = true;
            if (mListener != null)
                mListener.onBookingResult(driverId);
            FirebaseHelper.getUpdateDriverLocation(driverId);
        }
    }

    public void updateCustomerLocation(LatLng loc) {
        mLastKnownLocation = loc;
        if (isBooking)
            FirebaseHelper.updateCustomerLocationToFirebase(loc);

        if (mListener != null)
            mListener.onCustomerLocationChanged(loc);
    }


    // Truong
    public void receiveDriverLocationFromFirebase(LatLng driverLocation) {
        // this function is auto called from FirebaseHelper after find a driver
        if (mListener != null)
            mListener.onDriverLocationChanged(driverLocation);
    }

    public void setStartLocation(float lat, float lng) {
        mStartLocation = new LatLng(lat, lng);
    }


    public void setEndLocation(float lat, float lng) {
        mEndLocation = new LatLng(lat, lng);
    }

    public void updateDriverInfo(DriverInfo tmp) {
        this.driverInfo = tmp;
    }

    public DriverInfo getDriverInfo() {
        if (!driverInfo.isEmpty())
            return driverInfo;
        return null;
    }


    //Interface
    public interface IUserListener {
        void onCustomerLocationChanged(LatLng location);

        void onDriverLocationChanged(LatLng location);

        void onBookingResult(String driver);
    }

}
