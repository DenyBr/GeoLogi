package com.deny.GeoLogi;

import android.app.Activity;
import android.content.Context;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


import com.deny.GeoLogi.R;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.List;

public class MapaActivity extends Activity {
    private final static String TAG = "MapaActivity";

    MapView map = null;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_mapa);
    }


    public void onResume(){
        super.onResume();
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

        setResult(RESULT_OK);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }

    private void showUsers () {
        if (Nastaveni.getInstance(this).getisSdileniPolohy()) {
            //nactem seznam oddilu
            

        }
    }

}