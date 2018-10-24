package com.deny.GeoLogi;

import android.app.Activity;
import android.content.Context;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;

import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;


import com.deny.GeoLogi.R;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MapaActivity extends Activity {
    private final static String TAG = "MapaActivity";
    final Handler updateHandler = new Handler();
    boolean bClosed = false;

    MapView map = null;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_mapa);
    }

    SimpleFastPointOverlay sfpo_uzivatele = null;
    SimplePointTheme pt_uzivatele = null;

    public void onResume(){
        super.onResume();

        bClosed = false;

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        map = (MapView) findViewById(R.id.mapview);
        map.setTileSource(TileSourceFactory.MAPNIK);

        MyLocationNewOverlay myLocationoverlay = new MyLocationNewOverlay(map);
        myLocationoverlay.enableFollowLocation();
        myLocationoverlay.enableMyLocation();
        map.getOverlays().add(myLocationoverlay);

        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        map.getOverlays().add(mScaleBarOverlay);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(13);

        Marker.ENABLE_TEXT_LABELS_WHEN_NO_IMAGE = true;

        List<IGeoPoint> nove = GeoBody.getInstance(this).aMapa_nove;
        List<IGeoPoint> navstivene = GeoBody.getInstance(this).aMapa_navstivene;
        List<IGeoPoint> tajne = GeoBody.getInstance(this).aMapa_tajne;


        if (nove.size()>0) {
            mapController.animateTo(nove.get(nove.size()-1));
        } else if (navstivene.size()>0) {
            mapController.animateTo(navstivene.get(navstivene.size()-1));
        }

        SimplePointTheme pt_nove = new SimplePointTheme(nove, true);
        SimplePointTheme pt_navstivene = new SimplePointTheme(navstivene, true);
        SimplePointTheme pt_tajne = new SimplePointTheme(tajne, true);

        Paint textStyleNove = new Paint();
        textStyleNove.setStyle(Paint.Style.FILL);
        textStyleNove.setColor(Color.parseColor("#000000"));
        textStyleNove.setTextAlign(Paint.Align.CENTER);
        textStyleNove.setTextSize(36);


        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(false).setCellSize(15).setTextStyle(textStyleNove);

        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt_nove, opt);

        map.getOverlays().add(sfpo);

        Paint textStyleNavstivene = new Paint();
        textStyleNavstivene.setStyle(Paint.Style.FILL);
        textStyleNavstivene.setColor(Color.parseColor("#AAAAAA"));
        textStyleNavstivene.setTextAlign(Paint.Align.CENTER);
        textStyleNavstivene.setTextSize(36);

        Paint ps_navstivene = new Paint();
        ps_navstivene.setColor(textStyleNavstivene.getColor());

        SimpleFastPointOverlayOptions optNavstivene = SimpleFastPointOverlayOptions.getDefaultStyle().setPointStyle(ps_navstivene)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(false).setCellSize(15).setTextStyle(textStyleNavstivene);

        final SimpleFastPointOverlay sfpoNavstivene = new SimpleFastPointOverlay(pt_navstivene, optNavstivene);

        map.getOverlays().add(sfpoNavstivene);

        if (Global.isbSimulationMode()) {
            Paint textStyleTajne = new Paint();
            textStyleTajne.setStyle(Paint.Style.FILL);
            textStyleTajne.setColor(Color.parseColor("#ffff0000"));
            textStyleTajne.setTextAlign(Paint.Align.CENTER);
            textStyleTajne.setTextSize(36);

            Paint psTajne = new Paint();
            ps_navstivene.setColor(textStyleTajne.getColor());

            SimpleFastPointOverlayOptions optTajne = SimpleFastPointOverlayOptions.getDefaultStyle().setPointStyle(psTajne)
                    .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                    .setRadius(7).setIsClickable(false).setCellSize(15).setTextStyle(textStyleTajne);

            final SimpleFastPointOverlay sfptajne = new SimpleFastPointOverlay(pt_tajne, optTajne);

            map.getOverlays().add(sfptajne);
        }

        Log.d(TAG, "Sdileni polohy ve hre: "+Nastaveni.getInstance(ctx).getisSdileniPolohy());

        if (Nastaveni.getInstance(ctx).getisSdileniPolohy()) {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            try {
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {

                    Uzivatele.getInstance().reload(Nastaveni.getInstance().getsIdWorkseet(), new MapaActivity.UsersUpdated());

                } else {
                    Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Není možné sledovat polohu ostatnich ");
                }
            } catch (Exception e) {
                Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst. Není možné sledovat polohu ostatnich");
            }
        }

        setResult(RESULT_OK);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }

    @Override
    protected void onPause () {
        super.onPause();

        bClosed = true;

        Log.d(TAG, "onPause called");
    }

    private class UsersUpdated implements Runnable {
        public void run() {
            Log.d(TAG, "ENTER: UsersUpdated");

            for (int i = 0; i < Uzivatele.getInstance().aOddily.size(); i++) {
                if (Uzivatele.getInstance().aOddily.get(i).isbSdiletPolohu()) {
                    String sId = "" + Uzivatele.getInstance().aOddily.get(i).getiId();
                    String sFileNameLokace = Global.simPrexix() + Nastaveni.getInstance(MapaActivity.this).getsIdHry() + sId + "lokace.bin";

                    new DownloadFTPFileTask(MapaActivity.this, new AsyncResultFTPDownload() {
                        @Override
                        public void onResult(int iRes) {
                            updateOnFileDownload(iRes);
                        }
                    }).execute(sFileNameLokace, sFileNameLokace + "res");
                }
            }

            //this method is called in statup and then it needs to repeat every 10 seconds till the activity is closed
            if (!bClosed) {
                updateHandler.postDelayed(new UsersUpdated(), 10000);
            }

            Log.d(TAG, "LEAVE: UsersUpdated");
        }
    }


    private Location getLocationFromFile(String sFileName) {
        Location location;

        Log.d(TAG, "ENTER: getLocationFromFile: "+sFileName);
        try {
            InputStream inputStream =  this.openFileInput(sFileName);

            ObjectInputStream in = new ObjectInputStream(inputStream);

            double dLong = in.readDouble();
            double dLat = in.readDouble();

            location = new Location("");
            location.setLatitude(dLat);
            location.setLongitude(dLong);

            Log.d(TAG, "LEAVE: V souboru: "+sFileName+" je lokace " + location.getLongitude() + ":" +location.getLatitude());

            in.close();
            return location;
        }
        catch(Exception e) {
            Log.d (TAG, "ERROR: readFile " + e.getMessage());
        }

        Log.d(TAG, "LEAVE: Soubor: "+sFileName+" se nepodarilo nacist");
        return null;
    }


    private void updateOnFileDownload(int iRes) {
        Log.d(TAG, "ENTER: updateOnFileDownload " + iRes);

        Paint textStyleUzivatele = new Paint();
        textStyleUzivatele.setStyle(Paint.Style.FILL);
        textStyleUzivatele.setColor(Color.parseColor("#0000FF"));
        textStyleUzivatele.setTextAlign(Paint.Align.CENTER);
        textStyleUzivatele.setTextSize(36);

        if (sfpo_uzivatele!=null) {
            map.getOverlays().remove(sfpo_uzivatele);
        }

        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(false).setCellSize(15).setTextStyle(textStyleUzivatele);        ;

        ArrayList<IGeoPoint> aUzivatele= new ArrayList<IGeoPoint>();

        if (iRes == 1) {
            for (int i = 0; i < Uzivatele.getInstance().aOddily.size(); i++) {
                if (Uzivatele.getInstance().aOddily.get(i).isbSdiletPolohu()) {
                    String sId = "" + Uzivatele.getInstance().aOddily.get(i).getiId();
                    String sFileNameLokace = Global.simPrexix() + Nastaveni.getInstance(MapaActivity.this).getsIdHry() + sId + "lokace.binres";

                    Location loc = getLocationFromFile(sFileNameLokace);
                    aUzivatele.add(new LabelledGeoPoint(loc.getLatitude(), loc.getLongitude(), Uzivatele.getInstance().aOddily.get(i).getsNazev()));
                }
            }
        }

        pt_uzivatele = new SimplePointTheme(aUzivatele, true);
        sfpo_uzivatele = new SimpleFastPointOverlay(pt_uzivatele, opt);

        map.getOverlays().add(sfpo_uzivatele);

        Log.d(TAG, "LEAVE: updateOnFileDownload");
    }
}