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

    //public final static int iUpdateInterval = 300000; //miliseconds
   // public final static int iUpdateInterval = 30000; //miliseconds

    private static long lTime = System.currentTimeMillis();
    private static double dLat = 0;
    private static double dLong = 0;
    private static Context ctx = null;
    private static Location location = null;
    private static LocationManager locationManager = null;
    private static boolean bPaused = true;

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
    }

    public static boolean isbSimulationMode() {
        return Nastaveni.getInstance().getisSimulation();
    }

    public static void togglebSimulationMode() {
        Log.d(TAG, "Prepni simulaci: Stav pred : " + isbSimulationMode());

        Nastaveni.getInstance(ctx).setProperty("Simulation", ""+!(isbSimulationMode()));


        Log.d(TAG, "Prepni simulaci: Stav po : " + isbSimulationMode());
    }


    public static void setCtx (Context context) {
        ctx = context;
    }

    public static void setdLat(double dLat) {
        Global.dLat = dLat;
    }

    public static void setdLong(double dLong) {
        Global.dLong = dLong;
    }

    public static double getLat () {
        if (isbSimulationMode()) {
            return dLat;
        }
        else
        {
            if (null != ctx) {
                try
                {
                    return location.getLongitude();
                }
                catch (SecurityException e) {
                    //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
                }
                catch (Exception e) {
                    Log.d(TAG, "getLongtitude: " + e.getMessage());
                }
            }

            return 0;
        }
    }
    public static double getLong () {
        if (isbSimulationMode()) {
            return dLong;
        }
        else
        {
            if (null != ctx) {
                try
                {
                    return location.getLatitude();
                }
                catch (SecurityException e) {
                    //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
                }
                catch (Exception e) {
                    Log.d(TAG, "getLatitude: " + e.getMessage());
                }
            }

            return 0;
        }
    }

    public static long getTime () {
        if (isbSimulationMode()) {
            return lTime;
        }
        else
        {
            return System.currentTimeMillis();
        }
    }


    public static float distanceTo(Location locationDistant) {
        if (isbSimulationMode()) {
            Location locSim = new Location("");

            locSim.setLatitude(getLat());
            locSim.setLongitude(getLong());

            return locSim.distanceTo(locationDistant);
        } else
        {
            try
            {
                Log.d(TAG, "Distance to: " + Global.location.distanceTo(locationDistant));
                return Global.location.distanceTo(locationDistant);
            }
            catch (SecurityException e) {
                //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
            }
            catch (Exception e) {
                Log.d(TAG, "DistanceTo: " + e.getMessage());
            }
        }

        return 100000;
    }
    public static String simPrexix() {
        if (isbSimulationMode()) {return "s";}
        else return "";
    }

    public static boolean isbPaused() {
        return bPaused;
    }

    public static void setbPaused(boolean bPaused) {
        Global.bPaused = bPaused;
    }

    public static void setlTime(long lTime) {
        Global.lTime = lTime;
    }

    // Define a listener that responds to location updates
    private static LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location update: " + location.toString());

            Global.location = location;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
}
