package core;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    public Location mStartLocation;
    public Location mEndLocation;

    public Booking(Location startLocation, Location endLocation) {
        mStartLocation = startLocation;
        mEndLocation = endLocation;
    }
}
