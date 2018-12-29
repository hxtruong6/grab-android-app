package core.driver;

import android.location.Location;

import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class Driver {
    private static final Driver ourInstance = new Driver();
    public static Driver getInstance() {
        return ourInstance;
    }

    private IDriverListener mListener;

    public Location mLastKnownLocation; //tracking gps

    private Driver() {
        initDriverData();
    }

    public void initDriverData() {
        mLastKnownLocation = MyHelper.createLocation(1.2f, 2.3f);
    }

    public void registerIDriverInterface(IDriverListener listener){
        mListener = listener;
        FirebaseHelper.registerDriverToFirebase(mLastKnownLocation);
        FirebaseHelper.startListenCustomerRequest();

    }

    public void updateDriverLocation(Location loc){
        mLastKnownLocation = loc;

        FirebaseHelper.updateDriverLocationToFirebase(loc);
        if(mListener!=null)
            mListener.onDriverLocationChanged(loc);
    }

    public void receiveBookingResultFromFirebase(Driver driver){

        if(mListener!=null)
            mListener.onBookingResult(driver);
    }

    public void receiveAndStartTripWithCustomerRequest(String customerRequestId) {
        updateDriverLocation(mLastKnownLocation);

        if(mListener!=null)
            mListener.receiveCustomerRequest(customerRequestId);
    }


    //Interface
    public interface IDriverListener {
        void onUserLocationChanged(Location loc);

        void onDriverLocationChanged(Location loc);
        void onBookingResult(Driver driver);

        void receiveCustomerRequest(String customerRequestId);
    }

}
