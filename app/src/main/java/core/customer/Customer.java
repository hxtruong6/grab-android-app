package core.customer;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;


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
        mLastKnownLocation = new LatLng(10.12423, 106.9141291);
        mStartLocation = new LatLng(10.12423f, 106.9141291f);
        mEndLocation = new LatLng(10.1234647f, 106.945142f);
        mDriverLocation = new LatLng(10.0123, 106.999291);
    }

    public void registerIUserInterface(IUserListener listener) {
        mListener = listener;
        FirebaseHelper.registerCustomerToFirebase();
    }

    // Truong
    public void sendBookingRequest() {
        //Booking booking = new Booking(mStartLocation, mEndLocation);
        // TODO: send the booking later. Just simple for now
        FirebaseHelper.sendBookingLocation(mStartLocation, mEndLocation, mLastKnownLocation);
        FirebaseHelper.receiveBookingResultFromFirebase();
    }

    public void startUpdateDriverLocation() {
        if (driverId != null) {
            // Call getUpdateDriverLocation to update driver location for customer UI
            Log.d("xxx", "startUpdateDriverLocation -> id: " + driverId);
            FirebaseHelper.getDriverInfo(driverId);
            //Log.d("xxx ", "get driver info: " + driverInfo.toString());
            FirebaseHelper.getUpdateDriverLocation(driverId);
            isBooking = true;

            if (mListener != null) {
                //Log.d("xxx ", "get driver info: " + driverInfo.toString());
                mListener.onBookingResult(driverId);
            }
        }
    }

    public void updateCustomerLocation(LatLng loc) {
        mLastKnownLocation = loc;
        if (isBooking)
            FirebaseHelper.updateCustomerLocationToFirebase(loc);

        if (mListener != null) {
            mListener.onCustomerLocationChanged(loc);
        }
    }


    // Truong
    public void receiveDriverLocationFromFirebase(LatLng driverLocation) {
        // this function is auto called from FirebaseHelper after find a driver
        mDriverLocation = driverLocation;
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
        Log.d("xxx", "updateDriverInfo: " + tmp.toString());
        this.driverInfo = tmp;
        if (mListener != null) {
            mListener.onDriverInfoReady();
        }
    }

    public DriverInfo getDriverInfo() {
        if (driverInfo != null && !driverInfo.isEmpty())
            return driverInfo;
        return null;
    }

    //Interface
    public interface IUserListener {
        void onCustomerLocationChanged(LatLng location);
        void onDriverLocationChanged(LatLng location);
        void onBookingResult(String driver);
        void onDriverInfoReady();
    }
}
