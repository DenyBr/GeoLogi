package com.deny.GeoLogi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;



/*Hlavni obrazovka aplikace




 */

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    private static final String TAG = "MAIN";
    Location location;
    ListView listview;
    int iSirka=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ENTER: OnCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        pristupy();

        Log.d(TAG, "LEAVE: OnCreate");
    }


    private void pristupy () {
        Button btnT = (Button) findViewById(R.id.btnTest);

        if (Global.isbSimulationMode()) {
            Log.d(TAG, "Simulation mode");
            btnT.setVisibility(btnT.VISIBLE);
        }
        else {
            Log.d(TAG, "Normal mode");
            btnT.setVisibility(btnT.GONE);
        }
    }

    private void Init () {
        Log.d(TAG, "ENTER: Init");

        //reload all parameters
        Nastaveni.getInstance(this).reload(this);

        //reload stored messages
        ZpravySeznam.getInstance(this).registerGuiCallback(this);
        ZpravySeznam.getInstance(this).read(this);

        Log.d(TAG, "LEAVE: Init");
    }

    private void resize () {
        //auxiliary method to set width of the screen

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                TextView tvVyplne = (TextView) findViewById(R.id.tvVyplne);

                if (iSirka>800) {
                    tvVyplne.getLayoutParams().width=iSirka-600;
                }
            }
        }, 1);
    }




    public void syncClickHandler(View view) {
       ZpravySeznam.getInstance(this).serverUpdate(true);
    }


    public void NastenkaClickHandler(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, Nastenka.class);
        startActivityForResult(intent, 0);
    }

    public void MapaClickHandler(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MapaActivity.class);
        startActivityForResult(intent, 3);
    }

    public void IndicieClickHandler(View view) {
        Intent intent = new Intent(this, IndicieActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //zkontrolujZpravy(false);

        //resize();
    }

    public void clearClickHandler(View view) {
        clearfile(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu()+"zpravy.bin");
        clearfile(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu()+"indicieziskane.bin");
        clearfile(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu()+"indicievsecny.bin");
        clearfile(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu()+"bodynavstivene.bin");

        Init();
    }


    private void clearfile (String fileName) {
        try {
            OutputStream fileOut = openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(0);

            out.close();
            fileOut.close();
        } catch (IOException ex) {
            Okynka.zobrazOkynko(this, "Chyba: " + ex.getMessage());
        }
    }




    public void testClickHandler(View view) {
        Log.d(TAG, "Simulace - next step");

        if (Global.isbSimulationMode()) {
            Simulator.next(this, ZpravySeznam.getInstance(this).zpravyZobraz, ZpravySeznam.getInstance(this).zpravyKomplet);
            ZpravySeznam.getInstance(this).zkontrolujZpravy(false);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume called");

        pristupy();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Zpravy jsou na sirku

        setContentView(R.layout.activity_main);
        Log.d(TAG, "Spusteno");

        //chceme full screan kvuli malejm telefonum
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        iSirka = this.getResources().getConfiguration().screenWidthDp;
        resize();

        Init();
    }

    @Override
    protected void onPause () {
        super.onPause();

        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop () {
        super.onStop();

        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();

        finish();

        Log.d(TAG, "onDestroy called");
    }


    @Override
    public boolean handleMessage(Message msg) {
        updateView();
        return true;
    }

    @Override
    public void finalize () {
        Log.d(TAG, "finalize called");
    }



    private void updateView () {
        Log.d(TAG, "ENTER: updateView");

        int iMin = 100000;

        listview = (ListView) findViewById(R.id.listview);

        TextView nadpis = (TextView) findViewById(R.id.nadpisek);
        if (nadpis != null) {
            nadpis.setText(Nastaveni.getInstance(this).getsHra() + "\n" + Nastaveni.getInstance(this).getProperty("Uzivatel", "") /* + " "+iPocerzobr*/);
        }
        TextView hledanebody = (TextView) findViewById(R.id.hledanebody);

        if (hledanebody != null) {
            hledanebody.setText("Cíle: " + GeoBody.getInstance(this).sfBodyNavsvivene.localList.size() + "/" + GeoBody.getInstance(this).aBody.size());
        }

        TextView indicie = (TextView) findViewById(R.id.indicii);
        if (indicie != null) {
            indicie.setText("Indicie: " + IndicieSeznam.getInstance(this).sfIndicie.localList.size() + "/" + IndicieSeznam.getInstance(this).aIndicieVsechny.size());
        }

        iMin = GeoBody.getInstance(this).iVzdalenostNejblizsiho(this);

        TextView vzd = (TextView) findViewById(R.id.vzdalenost);
        if (vzd != null) {
            if (iMin < 1000) {
                vzd.setText("Vzdálenost: " + iMin + "m");
            } else {
                vzd.setText("Vzdálenost: ?m");
            }
        }

        //Predame adamteru aktualni seznam zprav
        final ZpravyAdapter adapter = new ZpravyAdapter(this, R.layout.zprava, ZpravySeznam.getInstance(this).zpravyZobraz);
        listview.setAdapter(adapter);

        //a zaregistrujeme listener na kliknuti
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Zprava z = ZpravySeznam.getInstance(MainActivity.this).zpravyZobraz.get(position);

                Okynka.zobrazOkynko(arg0.getContext(), z.getsZprava());
                z.setbRead(true);
                if (null == z.getTsCasZobrazeni())
                    z.setTsCasZobrazeni(new Timestamp(Global.getTime()));

                ZpravySeznam.getInstance(MainActivity.this).zkontrolujZpravy(true);
                resize();
            }
        });

        Log.d(TAG, "LEAVE: updateView");
    }
}
