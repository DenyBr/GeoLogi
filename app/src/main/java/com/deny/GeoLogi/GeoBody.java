package com.deny.GeoLogi;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static java.lang.Math.abs;

/**
 * Created by bruzlzde on 27.2.2018.
 */

class GeoBody {
    private static GeoBody ourInstance = null;
    //seznam cilovych bodu - to jsou body, ktere budou zobrazeny na mape
    //a bodu na kterych bude zobrazena zprava -
    //ne nutne musi byt zobrazeny na mape - mohou napriklad dostat popis, ze maji dojit k rybniku
    ArrayList<GeoBod> aBody = new ArrayList<>();
    //public ArrayList<GeoBod> aBodyHledane = new ArrayList<GeoBod>();
    //seznam navstivenych bodu - to jsou body, kam uz dosli
    //pokud jsou zobrazene, tak budou sede - ale mohou byt tajne
    ArrayList<GeoBod> aBodyNavstivene = aBody = new ArrayList<>();


    private Context context = null;
    private Criteria criteria = null;
    private String bestProvider = null;
    private Location location = null;
    private LocationManager locationManager = null;

    List<IGeoPoint> aMapa_nove = new ArrayList<IGeoPoint>();
    List<IGeoPoint> aMapa_navstivene = new ArrayList<IGeoPoint>();

    public void aktualizujMapu() {
        aMapa_nove = new ArrayList<IGeoPoint>();
        aMapa_navstivene = new ArrayList<IGeoPoint>();

        for (int i = 0; i < GeoBody.getInstance(context).aBody.size(); i++) {
            GeoBod actBod = GeoBody.getInstance(context).aBody.get(i);

            if (actBod.getbViditelny()) {
                if (!GeoBody.getInstance(context).bylNavstivenej(actBod))
                    aMapa_nove.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis()));
                else
                    aMapa_navstivene.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis()));
            }
        }

    }

    static GeoBody getInstance(Context context) {
        if (null == ourInstance) ourInstance = new GeoBody(context);

        return ourInstance;
    }

    private void registrujGPS (Context ctx, int iTimeout, int iDistance) {
        locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
        try {
            // Register the listener with the Location Manager to receive location updates

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, iTimeout, iDistance, locationListener);

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Okynka.zobrazOkynko(context, "" + e.getMessage());
        } catch (Exception e) {
            //Okynka.zobrazOkynko(context, "" + e.getMessage());
        }

    }

    private GeoBody(Context ctx) {
        context = ctx;

        registrujGPS(ctx, 1000, 10);
    }



    public void read_navstivene(Context context) {
        try {
            aBodyNavstivene = new ArrayList<GeoBod>();
            InputStream inputStream = context.openFileInput(Nastaveni.getInstance(context).getsHra() + Nastaveni.getInstance(context).getiIDOddilu() + "bodynavstivene.txt");

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();

            for (int i = 0; i < iPocet; i++) {
                GeoBod bod = (GeoBod) in.readObject();
                aBodyNavstivene.add(bod);
            }
            in.close();
        } catch (Exception e) {
            //Okynka.zobrazOkynko(this, "Chyba: " + e.getMessage());
        }
        aktualizujMapu();
    }


    public void write_navstivene(Context context) {
        try {
            OutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsHra() + Nastaveni.getInstance(context).getiIDOddilu() + "bodynavstivene.txt", Context.MODE_PRIVATE);
            OutputStream fileOutC = context.openFileOutput(Nastaveni.getInstance(context).getsHra().replace(' ','_') + Nastaveni.getInstance(context).getiIDOddilu() + "bodynavstivenec.txt", Context.MODE_PRIVATE);

            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fileOutC));


            out.writeInt(aBodyNavstivene.size());

            for (int i = 0; i < aBodyNavstivene.size(); i++) {
                out.writeObject(aBodyNavstivene.get(i));

                writer.write(aBodyNavstivene.get(i).getdLat()+":"+aBodyNavstivene.get(i).getdLong()+":"+aBodyNavstivene.get(i).getPopis()+"\n\r");
            }

            out.close();
            fileOut.close();
            writer.close();
            fileOutC.close();

        } catch (IOException ex) {
            Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
        }
    }

    public boolean bylNavstivenej(GeoBod b) {
        for (int i = 0; i < aBodyNavstivene.size(); i++) {
            if ((abs(b.getdLat() - aBodyNavstivene.get(i).getdLat()) < 0.00001) && (abs(b.getdLong() - aBodyNavstivene.get(i).getdLong()) < 0.00001))
                return true;
        }
        return false;
    }

    public boolean jeHledanej(GeoBod b) {
        for (int i = 0; i < aBody.size(); i++) {
            GeoBod bodzeseznamu = aBody.get(i);

            if ((abs(b.getdLat() - bodzeseznamu.getdLat()) < 0.00001) && (abs(b.getdLong() - bodzeseznamu.getdLong()) < 0.00001)) {
                //pokud je pod s seznamu neviditelny, ale ted se hleda viditelny, tak ho zviditelnime na mape
                if (b.getbViditelny() && !bodzeseznamu.getbViditelny()) {
                    bodzeseznamu.setbViditelny(true);
                    bodzeseznamu.setPopis(b.getPopis());

                    aktualizujMapu();
                }

                return true;
            }
        }
        return false;
    }


    public int iVzdalenostNejblizsiho(Context ctx) {
        //zjisti aktualni polohu
        int iMin = 100000;
        int iAct;

        try {
                try {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                catch (SecurityException e) {
                    //nedelej nic, snad se uzivatel polepsi :-) ptame se po startu
                }

                if (null != location) {


                //Projdi cilove body a eventuelne oznac navstivenej
                for (int i = 0; i < aBody.size(); i++) {
                    try {
                        GeoBod bodAct = aBody.get(i);
                        Location locAct = new Location("");
                        locAct.setLatitude(bodAct.getdLat());
                        locAct.setLongitude(bodAct.getdLong());

                        iAct = (int) location.distanceTo(locAct);

                        if (iAct < iMin) {
                            iMin = iAct;
                        }

                        if ((iAct < 20) && (!bylNavstivenej(bodAct))) {
                            //pridame bod mezi navstivene
                            aBodyNavstivene.add(bodAct);

                            //a ulozime
                            write_navstivene(ctx);

                            aktualizujMapu();
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int iTimeout=100000;
        int iDistance=100;

        //pri priblizovani zkratime timeout
        if (iMin < 20) { iTimeout = 1000; iDistance=0;}
        else if (iMin < 30) {iTimeout = 2000; iDistance=1;}
        else if (iMin < 50) {iTimeout = 3000; iDistance=2;}
        else if (iMin < 100) {iTimeout = 30000; iDistance=20;}

        registrujGPS(ctx, iTimeout, iDistance);

        //Okynka.zobrazOkynko(ctx, "nejblizsi je "+iMin);
        return iMin;
    }


    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
           //Okynka.zobrazOkynko(context, "location update");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

}