package com.deny.GeoLogi;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by bruzlzde on 27.2.2018.
 *
 * Singleton udrzujici eeznam navstivenych bodu
 * a meri vzdalenost k nejblizsimu z nich
 */

class GeoBody {
    private final String TAG = "GeoBody";

    private static GeoBody ourInstance = null;
    //seznam cilovych bodu - to jsou body, ktere budou zobrazeny na mape
    //a bodu na kterych bude zobrazena zprava -
    //ne nutne musi byt zobrazeny na mape - mohou napriklad dostat popis, ze maji dojit k rybniku
    ArrayList<GeoBod> aBody = new ArrayList<>();

    //seznam navstivenych bodu - to jsou body, kam uz dosli
    //pokud jsou zobrazene, tak budou sede - ale mohou byt tajne
    //navstivene body se synchronizuji na ftp server
    public static SyncFiles<GeoBod> sfBodyNavsvivene = null;
    public static Handler.Callback callback = null;

    private Context ctx = null;

    List<IGeoPoint> aMapa_nove = new ArrayList<IGeoPoint>();
    List<IGeoPoint> aMapa_navstivene = new ArrayList<IGeoPoint>();
    List<IGeoPoint> aMapa_tajne = new ArrayList<IGeoPoint>();

    public void aktualizujMapu() {
        Log.d(TAG, "ENTER: AktualizujMapu");

        aMapa_nove = new ArrayList<IGeoPoint>();
        aMapa_navstivene = new ArrayList<IGeoPoint>();
        aMapa_tajne = new ArrayList<IGeoPoint>();

        for (int i = 0; i < GeoBody.getInstance(ctx).aBody.size(); i++) {
            GeoBod actBod = GeoBody.getInstance(ctx).aBody.get(i);

            if (!GeoBody.getInstance(ctx).bylNavstivenej(actBod)) {
                if (actBod.getbViditelny())
                    aMapa_nove.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis()));
                else
                    aMapa_tajne.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis()));
            }
            else {
                aMapa_navstivene.add(new LabelledGeoPoint(actBod.getdLat(), actBod.getdLong(), actBod.getPopis()));
            }
        }
        Log.d(TAG, "LEAVE: AktualizujMapu");

    }

    static GeoBody getInstance(Context context) {
        if (null == ourInstance) ourInstance = new GeoBody(context);

        return ourInstance;
    }

    private GeoBody(Context ctx) {
        this.ctx = ctx;

        init();
    }

    public void init() {
        if (null!=sfBodyNavsvivene) sfBodyNavsvivene.finalize();
        sfBodyNavsvivene = new SyncFiles<GeoBod>(ctx, Nastaveni.getInstance(ctx).getsIdHry() + Nastaveni.getInstance(ctx).getiIDOddilu() + "bodynavstivene.bin", 60000, callback);
        Global.init(ctx, 1000, 10);
    }
    

    public boolean bylNavstivenej(GeoBod b) {
        for (int i = 0; i < sfBodyNavsvivene.localList.size(); i++) {
            if ((abs(b.getdLat() - sfBodyNavsvivene.localList.get(i).getdLat()) < 0.00001) && (abs(b.getdLong() - sfBodyNavsvivene.localList.get(i).getdLong()) < 0.00001))
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
            //Projdi cilove body a eventuelne oznac navstivenej
            for (int i = 0; i < aBody.size(); i++) {
            try {
                GeoBod bodAct = aBody.get(i);

                if (!bylNavstivenej(bodAct)) {
                    Location locAct = new Location("");
                    locAct.setLatitude(bodAct.getdLat());
                    locAct.setLongitude(bodAct.getdLong());

                    iAct = (int) Global.distanceTo(locAct);

                    if (iAct < iMin) {
                        iMin = iAct;
                    }

                    if ((iAct < 20) && (!bylNavstivenej(bodAct))) {
                        //pridame bod mezi navstivene
                        bodAct.setTime(new Timestamp(Global.getTime()));
                        sfBodyNavsvivene.localList.add(bodAct);

                        //a ulozime a sesynchronizujeme
                        sfBodyNavsvivene.writeFile();
                        sfBodyNavsvivene.syncFileNow();

                        aktualizujMapu();
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

        int iTimeout=100000;
        int iDistance=100;

        //pri priblizovani zkratime timeout
        if (iMin < 20) { iTimeout = 1000; iDistance=1;}
        else if (iMin < 30) {iTimeout = 2000; iDistance=1;}
        else if (iMin < 50) {iTimeout = 3000; iDistance=2;}
        else if (iMin < 100) {iTimeout = 10000; iDistance=20;}

        Global.init(ctx, iTimeout, iDistance);

        return iMin;
    }

    public static void setCallback(Handler.Callback callback) {
        GeoBody.callback = callback;
    }


}