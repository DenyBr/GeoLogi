package com.deny.GeoLogi;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

public class Global {
    private final static String TAG = "Global";
    private static long lTime = System.currentTimeMillis();
    private static double dLat = 0;
    private static double dLong = 0;
    private static boolean bSimulationMode = false;
    private static Context ctx = null;
    private static Location location = null;
    private static LocationManager locationManager = null;

    private Global() {
        //
    }

    public static void init (Context ctx, int iTimeout, int iDistance) {
        Log.d(TAG, "ENTER: RegistrujGPS");

        locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
        try {
            // Register the listener with the Location Manager to receive location updates

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, iTimeout, iDistance, locationListener);

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Okynka.zobrazOkynko(ctx, "" + e.getMessage());
        } catch (Exception e) {
            //Okynka.zobrazOkynko(context, "" + e.getMessage());
        }

        Log.d(TAG, "LEAVE: RegistrujGPS");





        try
        {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch (SecurityException e) {
            //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
        }
    }

    public static boolean isbSimulationMode() {
        return bSimulationMode;
    }

    public static void setbSimulationMode(boolean bSimulationMode) {
        Global.bSimulationMode = bSimulationMode;
    }


    public static void setCtx (Context context) {
        ctx = context;
    }

    public static double getLat () {
        if (bSimulationMode) {
            return dLat;
        }
        else
        {
            if (null != ctx) {
                try
                {
                    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
                }
                catch (SecurityException e) {
                    //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
                }
            }

            return 0;
        }
    }
    public static double getLong () {
        if (bSimulationMode) {
            return dLong;
        }
        else
        {
            if (null != ctx) {
                try
                {
                    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                }
                catch (SecurityException e) {
                    //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
                }
            }

            return 0;
        }
    }

    public static long getTime () {
        if (bSimulationMode) {
            return lTime;
        }
        else
        {
            return System.currentTimeMillis();
        }
    }


    public static float distanceTo(Location location) {
        if (bSimulationMode) {
            Location locSim = new Location("");

            locSim.setLatitude(dLat);
            locSim.setLongitude(dLong);

            return locSim.distanceTo(location);
        } else
        {
            if (null != ctx) {
                try
                {
                    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).distanceTo(location);
                }
                catch (SecurityException e) {
                    //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
                }
            }

            return 100000;
        }
    }
    public static String simPrexix() {
        if (isbSimulationMode()) {return "s";}
        else return "";
    }

    // Define a listener that responds to location updates
    private static LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


}
