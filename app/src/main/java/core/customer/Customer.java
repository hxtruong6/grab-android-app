package core.customer;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;

import core.driver.Driver;
=======
import java.util.List;

>>>>>>> bba245d1af3e367aff5667144e50517d8110164e
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

    public Boolean customerReady = false;
    public Boolean driverLocationReady = false;

    public String driverId;
    public LatLng mLastKnownLocation; //tracking gps
    public LatLng mDriverLocation;
    public LatLng mStartLocation;
    public LatLng mEndLocation;


    private Customer() {
        initCustomerData();
    }

    public void initCustomerData() {
<<<<<<< HEAD
        mLastKnownLocation = new LatLng(0, 0);
        mStartLocation = new LatLng(10.12423f, 106.9141291f);
        mEndLocation = new LatLng(10.1234647f, 106.945142f);
        mDriverLocation = new LatLng(10.0123, 106.999291);
=======
        mLastKnownLocation = new LatLng(0,0);//10.12423, 106.9141291);
        mStartLocation = new LatLng(0,0);//10.12423f, 106.9141291f);
        mEndLocation = new LatLng(0,0);//10.1234647f, 106.945142f);
        mDriverLocation = new LatLng(0,0);//10.0123, 106.999291);
>>>>>>> bba245d1af3e367aff5667144e50517d8110164e
    }

    public void registerIUserInterface(IUserListener listener) {
        mListener = listener;
        FirebaseHelper.registerCustomerInfoToFirebase();
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
        driverLocationReady = true;

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

    public List<LatLng> getDriverList() {
<<<<<<< HEAD
        //TODO: get available driver list from firebase

        // Example:
        List<LatLng> list = new ArrayList<>();
        list.add(new LatLng(10.765455, 106.682078));
        list.add(new LatLng(10.763276, 106.681475));
        list.add(new LatLng(10.763218, 106.683428));
        list.add(new LatLng(10.761902, 106.683055));
        list.add(new LatLng(10.767548, 106.678859));
        list.add(new LatLng(10.755547, 106.685260));
        return list;
=======
        Log.d("xxx", "start get list driver");
        FirebaseHelper.getDriverLocationList();
        Log.d("xxx", "end get list driver");
        return null;
>>>>>>> bba245d1af3e367aff5667144e50517d8110164e
    }

    //Interface
    public interface IUserListener {
        void onCustomerLocationChanged(LatLng location);
        void onDriverLocationChanged(LatLng location);
        void onBookingResult(String driver);
        void onDriverInfoReady();
    }

    public interface IFirebaseHelperCallBack {
        void onGetListAvailableDriversCallBack();
    }
}
