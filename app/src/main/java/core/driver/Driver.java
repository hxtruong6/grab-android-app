package core.driver;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class Driver {
    private static final Driver ourInstance = new Driver();
    public List<LatLng> routePoints;

    public static Driver getInstance() {
        return ourInstance;
    }
    Boolean isWorking = false;
    private IDriverListener mListener;

    public LatLng mLastKnownLocation; //tracking gps

    private Driver() {
        initDriverData();
    }

    public void initDriverData() {
        mLastKnownLocation = new LatLng(10.234512f, 106.3872551f);
    }

    public void registerIDriverInterface(IDriverListener listener){
        mListener = listener;
        FirebaseHelper.registerDriverToFirebase(FirebaseHelper.getUser().getUid(), "driversAvailable", mLastKnownLocation);
        FirebaseHelper.startListenCustomerRequest();
    }

    public void updateDriverLocation(LatLng loc){
        mLastKnownLocation = loc;

        if(isWorking)
            FirebaseHelper.updateDriverLocationToFirebase(loc, "driversWorking");
        else
            FirebaseHelper.updateDriverLocationToFirebase(loc, "driversAvailable");

        if(mListener!=null)
            mListener.onDriverLocationChanged(loc);
    }


    public void receiveAndStartTripWithCustomerRequest(String customerRequestId) {
        isWorking = true;
        updateDriverLocation(mLastKnownLocation);

        if(mListener!=null)
            mListener.receiveCustomerRequest(customerRequestId);
    }


    //Interface
    public interface IDriverListener {
        void onDriverLocationChanged(LatLng loc);
        void receiveCustomerRequest(String customerRequestId);
    }

    public interface IOnGetDataDriverCallback {
        void onGetLocationCallback(LatLng driverLoc);
    }

}
