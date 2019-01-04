package core.driver;

import android.location.Location;

import core.helper.FirebaseHelper;
import core.helper.MyHelper;

public class Driver {
    private static final Driver ourInstance = new Driver();
    public static Driver getInstance() {
        return ourInstance;
    }
    Boolean isWorking = false;
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
        FirebaseHelper.registerDriverToFirebase(FirebaseHelper.getUser().getUid(), "driversAvailable", mLastKnownLocation);
        FirebaseHelper.startListenCustomerRequest();

    }

    public void updateDriverLocation(Location loc){
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
        void onDriverLocationChanged(Location loc);
        void receiveCustomerRequest(String customerRequestId);
    }

}
