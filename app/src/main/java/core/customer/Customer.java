package core.customer;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;

import core.driver.Driver;
import core.driver.DriverInfo;
import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class Customer {
    private static final Customer ourInstance = new Customer();
    private DriverInfo driverInfo;

    public static Customer getInstance() {
        return ourInstance;
    }

    private IUserListener mListener;
    Boolean isBooking = false;
    public String driverId = "";
    public Location mLastKnownLocation; //tracking gps
    public Location mDriverLoction;

    public Location mStartLoction;
    public Location mEndLocation;

    public LatLng pickupLocation;

    private Customer() {
        initCustomerData();
    }

    public void initCustomerData() {
        driverInfo = new DriverInfo();
        mLastKnownLocation = MyHelper.createLocation(1.2f, 2.3f);
        mStartLoction = MyHelper.createLocation(2.0f, 10.2f);
        mEndLocation = MyHelper.createLocation(2.2f, .9f);
    }

    public void registerIUserInterface(IUserListener listener){
        mListener = listener;
        FirebaseHelper.registerCustomerToFirebase();
    }

    // Truong
    public void sendBookingRequest() {
        //Booking booking = new Booking(mStartLoction, mEndLocation);
        // TODO: send the booking later. Just simple for now
        FirebaseHelper.sendBookingLocation(mLastKnownLocation);
        //driverId = FirebaseHelper.receiveBookingResultFromFirebase();

        driverId = "anhtaixe001";

        if(driverId!=null && !driverId.isEmpty()){
            // Call getUpdateDriverLocation to update driver location for customer UI
            isBooking = true;
            if(mListener!=null)
                mListener.onBookingResult(driverId);
            FirebaseHelper.getDriverInfo(driverId);
            FirebaseHelper.getUpdateDriverLocation(driverId);
        }
    }

    public void updateCustomerLocation(Location loc) {
        mLastKnownLocation = loc;
        if(isBooking)
            FirebaseHelper.updateCustomerLocationToFirebase(loc);

        if (mListener != null)
            mListener.onCustomerLocationChanged(loc);
    }


    // Truong
    public void receiveDriverLocationFromFirebase(Location driverLocation) {
        // this function is auto called from FirebaseHelper after find a driver
        if (mListener != null)
            mListener.onDriverLocationChanged(driverLocation);
    }

    public void setStartLocation(float lat, float lng) {
        mStartLoction.setLatitude(lat);
        mStartLoction.setLongitude(lng);
    }


    public void setEndLocation(float lat, float lng) {
        mEndLocation.setLatitude(lat);
        mEndLocation.setLongitude(lng);
    }

    public void updateDriverInfo(DriverInfo tmp) {
        this.driverInfo = tmp;
    }

    public DriverInfo getDriverInfo() {
        if(!driverInfo.isEmpty())
            return driverInfo;
        return null;
    }


    //Interface
    public interface IUserListener {
        void onCustomerLocationChanged(Location location);
        void onDriverLocationChanged(Location location);
        void onBookingResult(String driver);
    }

}
