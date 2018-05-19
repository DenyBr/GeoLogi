package com.deny.taborofka_zpravy;

import android.app.Activity;
import android.content.Context;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;


import com.example.progress.Taborofka.R;

import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MapaActivity extends Activity {
    MapView map = null;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView( R.layout.activity_mapa);

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

        if (nove.size()>0) {
            mapController.animateTo(nove.get(nove.size()-1));
        } else if (navstivene.size()>0) {
            mapController.animateTo(nove.get(navstivene.size()-1));
        }

        SimplePointTheme pt_nove = new SimplePointTheme(nove, true);
        SimplePointTheme pt_navstivene = new SimplePointTheme(navstivene, true);

        Paint textStyleNove = new Paint();
        textStyleNove.setStyle(Paint.Style.FILL);
        textStyleNove.setColor(Color.parseColor("#000000"));
        textStyleNove.setTextAlign(Paint.Align.CENTER);
        textStyleNove.setTextSize(24);

        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(true).setCellSize(15).setTextStyle(textStyleNove);

        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt_nove, opt);

        map.getOverlays().add(sfpo);

        Paint textStyleNavstivene = new Paint();
        textStyleNavstivene.setStyle(Paint.Style.FILL);
        textStyleNavstivene.setColor(Color.parseColor("#AAAAAA"));
        textStyleNavstivene.setTextAlign(Paint.Align.CENTER);
        textStyleNavstivene.setTextSize(24);

        Paint ps_navstivene = new Paint();
        ps_navstivene.setColor(textStyleNavstivene.getColor());

        SimpleFastPointOverlayOptions optNavstivene = SimpleFastPointOverlayOptions.getDefaultStyle().setPointStyle(ps_navstivene)
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(true).setCellSize(15).setTextStyle(textStyleNavstivene);

        final SimpleFastPointOverlay sfpoNavstivene = new SimpleFastPointOverlay(pt_navstivene, optNavstivene);

        map.getOverlays().add(sfpoNavstivene);

        setResult(RESULT_OK);
 }


    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        //map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}