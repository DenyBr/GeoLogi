package com.deny.taborofka_zpravy;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static java.lang.Math.abs;

/**
 * Created by bruzlzde on 27.2.2018.
 */

class GeoBody {
    private static final GeoBody ourInstance = new GeoBody();
    //seznam cilovych bodu - to jsou body, ktere budou zobrazeny na mape
    public ArrayList<GeoBod> aBody = new ArrayList<GeoBod>();
    //seznam hledanych bodu - to jsou body, na kterych bude zobrazena zprava -
    //ne nutne musi byt zobrazeny na mape - mohou napriklad dostat popis, ze maji dojit k rybniku
    public ArrayList<GeoBod> aBodyHledane = new ArrayList<GeoBod>();
    //seznam navstivenych bodu - to jsou body, kam uz dosli
    //pokud jsou zobrazene, tak budou sede - ale mohou byt tajne
    public ArrayList<GeoBod> aBodyNavstivene = aBody = new ArrayList<GeoBod>();

    List<IGeoPoint> aMapa_nove = new ArrayList<IGeoPoint>();
    List<IGeoPoint> aMapa_navstivene = new ArrayList<IGeoPoint>();

    public void aktualizujMapu () {
        aMapa_nove = new ArrayList<IGeoPoint>();
        aMapa_navstivene = new ArrayList<IGeoPoint>();

        for (int i = 0; i < GeoBody.getInstance().aBody.size(); i++) {
            GeoBod actBod = GeoBody.getInstance().aBody.get(i);

            if (!GeoBody.getInstance().bylNavstivenej(actBod)) aMapa_nove.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis ()));
            else aMapa_navstivene.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis ()));
        }

    }

    static GeoBody getInstance() {
        return ourInstance;
    }

    private GeoBody() {
    }

    public void read_navstivene (Context context) {
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


    public void write_navstivene (Context context) {
        try {
            OutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsHra()+Nastaveni.getInstance(context).getiIDOddilu()+"bodynavstivene.txt", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(aBodyNavstivene.size());

            for (int i=0; i<aBodyNavstivene.size(); i++) {
                out.writeObject(aBodyNavstivene.get(i));
            }

            out.close();
            fileOut.close();
        } catch (IOException ex) {Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
        }
    }

    public boolean bylNavstivenej(GeoBod b) {
        for (int i=0; i<aBodyNavstivene.size(); i++) {
           if ( (abs(b.getdLat()- aBodyNavstivene.get(i).getdLat())<0.0001)  && (abs(b.getdLong()-aBodyNavstivene.get(i).getdLong())<0.0001)) return true;
        }
        return false;
    }

    public boolean jeHledanej(GeoBod b) {
        for (int i=0; i<aBodyHledane.size(); i++) {
            if ( (abs(b.getdLat()- aBodyHledane.get(i).getdLat())<0.0001)  && (abs(b.getdLong()-aBodyHledane.get(i).getdLong())<0.0001)) return true;
        }
        return false;
    }


    public int iVzdalenostNejblizsiho(Context ctx) {
        //zjisti aktualni polohu
        int iMin=100000;
        int iAct;

        LocationManager locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);

        try {
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, false);

            if (null!=bestProvider) {

                Location location = locationManager.getLastKnownLocation(bestProvider);

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

                    //projdi hledane cilove body a pripadne zapis, ze byl navstivenej
                    for (int i = 0; i < aBodyHledane.size(); i++) {
                        try {
                            GeoBod bodAct = aBodyHledane.get(i);
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
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        //Okynka.zobrazOkynko(ctx, "nejblizsi je "+iMin);
        return iMin;
    }
}