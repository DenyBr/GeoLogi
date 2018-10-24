package com.deny.GeoLogi;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;



/*Hlavni obrazovka aplikace




 */

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    private static final String TAG = "MAIN";
    ListView listview;
    int iSirka=0;
    Handler updateHandler = new Handler();
    ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ENTER: OnCreate");

        super.onCreate(savedInstanceState);
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Zpravy jsou na sirku
        setContentView(R.layout.activity_main);

        Log.d(TAG, "LEAVE: OnCreate");
    }


    private void pristupy () {
        Button btnT = findViewById(R.id.btnTest);
        Button btnT1 = findViewById(R.id.btnTest1);
        Button btnT2 = findViewById(R.id.btnTest2);

        if (Global.isbSimulationMode()) {
            btnT.setVisibility(View.VISIBLE);
            btnT.setWidth(50);
            btnT.setEnabled(true);
            btnT.setText("T");
            Log.d(TAG, "Simulation mode " + (btnT.getVisibility()==View.VISIBLE));

            btnT1.setVisibility(View.VISIBLE);
            btnT2.setVisibility(View.VISIBLE);
        }
        else {
            btnT.setVisibility(View.GONE);
            btnT.setWidth(0);
            btnT.setText("");
            btnT.setEnabled(false);

            btnT1.setVisibility(View.GONE);
            btnT2.setVisibility(View.GONE);
        }

        Log.d(TAG, "Simulation mode " + (btnT.getVisibility()==View.VISIBLE));

    }

    private void Init () {
        Log.d(TAG, "ENTER: Init");

        //reload all parameters
        Nastaveni.getInstance(this).reload(this);

        //reload stored messages
        ZpravySeznam.getInstance(this).registerGuiCallback(this);
        ZpravySeznam.getInstance(this).read(this);

        updateHandler.postDelayed(updateCasRunabble,10);

        Log.d(TAG, "LEAVE: Init");
    }



    private Runnable updateCasRunabble = new Runnable() {
        @Override
        public void run() {
            if (!Global.isbPaused()) updateCas();
        }
    };

    private void updateCas() {
        //TODO: toto patri refaktorovat do ZpravySeznam, tady je to spatne z pohledu encapsulace
        TableRow radek = (TableRow) findViewById(R.id.timeline);

        if (Nastaveni.getInstance(this).getisNaCas()) {

            radek.setVisibility(View.VISIBLE);

            TextView nadpis = findViewById(R.id.cas);
            //TextView popis = findViewById(R.id.cas_vysledek);
            if (nadpis != null) {
                //nadpis.setHeight(20);
                long lCas = 0;
                long now = System.currentTimeMillis();

                if (ZpravySeznam.getInstance(this).bTimeLimitedGame) {
                    //zobrazujeme zbyvajici cas
                    if (ZpravySeznam.getInstance(this).tsGameStarted!=null) {
                        //hra byla spustena
                        if ((ZpravySeznam.getInstance(this).bCilDosazen))  {
                            //hra by;a dokoncena
                            if ((ZpravySeznam.getInstance(this).tsGameFinished != null)) {
                                lCas = ZpravySeznam.getInstance(this).tsGameStarted.getTime() -
                                       ZpravySeznam.getInstance(this).tsGameFinished.getTime() +
                                       ZpravySeznam.getInstance(this).lTimeLimit * 1000;
                                //popis.setText("hra ukoncena: "+lCas);
                            } else {
                                //toto je spatne
                                lCas = Long.MIN_VALUE;
                                //popis.setText("chyba mereni casu: "+lCas);
                            }
                        }
                        else if (ZpravySeznam.getInstance(this).bCasVyprsel)
                        {
                            lCas = 0;
                            //popis.setText("hra ukoncena: cas vyprsel");
                        }
                        else {
                            lCas = ZpravySeznam.getInstance(this).tsGameStarted.getTime() -
                                    now +
                                    ZpravySeznam.getInstance(this).lTimeLimit * 1000;
                            //popis.setText("hra cas bezi: "+lCas);

                            if (lCas<=0) {
                                lCas = 0;
                                ZpravySeznam.getInstance(this).bCasVyprsel=true;
                                if (ZpravySeznam.getInstance(this).tsGameFinished==null) {
                                    ZpravySeznam.getInstance(this).tsGameFinished=new Timestamp(now);
                                }
                            }
                        }
                    }
                    else {
                        //hra jeste nebyla spustena - zobrazujeme casobou dotaci
                        lCas = ZpravySeznam.getInstance(this).lTimeLimit * 1000;
                        //popis.setText("hra jeste nebyla spustena");
                    }
                }
                else {
                    //zobrazujeme uplynuly cas
                   if (ZpravySeznam.getInstance(this).tsGameStarted!=null) {
                    //hra byla spustena
                        if ((ZpravySeznam.getInstance(this).tsGameFinished != null)) {
                            lCas =  ZpravySeznam.getInstance(this).tsGameFinished.getTime() -
                                    ZpravySeznam.getInstance(this).tsGameStarted.getTime();
                           // popis.setText("hra skoncila - namereny stav: "+lCas);
                        }
                        else {
                            lCas =  now -
                                    ZpravySeznam.getInstance(this).tsGameStarted.getTime();
                            //popis.setText("hra ukoncena: cas bezi: "+lCas);
                        }
                   }
                }
                if (lCas==Long.MIN_VALUE)
                    nadpis.setText("chyba");

                else {
                    if ((lCas>0) && (ZpravySeznam.getInstance(this).tsGameStarted!=null)) {
                        lCas=lCas/1000;
                        if (((lCas) < 60) ||
                                ((lCas < 300) && (lCas % 5 == 0)) ||
                                ((lCas < 600) && (lCas % 10 == 0)) ||
                                (((lCas % 60 == 0)))) {

                            if (!Global.isbPaused()) {
                                toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                            }
                        }
                    }

                    if (lCas<0) lCas=0;

                    long hours = lCas/3600;
                    long minutes = (lCas%3600)/60;
                    long seconds = (lCas%60);

                    nadpis.setText(String.format ("%02d:%02d:%02d", hours, minutes, seconds));
                }

            }


            updateHandler.postDelayed(updateCasRunabble,1000);
        }

        else
        {
            radek.setVisibility(View.GONE);
        }
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

    public void test1ClickHandler(View view) {
        Log.d(TAG, "Simulace - synchronizace indicii od zacatku");

        Simulator.simulujPridavaniIndicii(true);
    }

    public void test2ClickHandler(View view) {
        Log.d(TAG, "Simulace - synchronizace indicii od konce");

        Simulator.simulujPridavaniIndicii(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume called");

        Log.d(TAG, "Spusteno");

        Global.setCtx(this);

        //chceme full screan kvuli malejm telefonum
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        iSirka = this.getResources().getConfiguration().screenWidthDp;

        pristupy();

        Init();

        Global.setbPaused(false);
    }

    @Override
    protected void onPause () {
        super.onPause();
        Global.setbPaused(true);

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
        Log.d(TAG, "onDestroy called");

        finish();
    }


    @Override
    public boolean handleMessage(Message msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Stuff that updates the UI
                updateView();
            }
        });

         return true;
    }

    @Override
    public void finalize () {
        Log.d(TAG, "finalize called");
    }



    private void updateView () {
        Log.d(TAG, "ENTER: updateView");

        int iMin = 100000;

        listview = findViewById(R.id.listview);

        TextView nadpis = findViewById(R.id.nadpisek);
        if (nadpis != null) {
            nadpis.setText(Nastaveni.getInstance(this).getsHra() + "\n" + Nastaveni.getInstance(this).getProperty("Uzivatel", "") /* + " "+iPocerzobr*/);
        }
        TextView hledanebody = findViewById(R.id.hledanebody);

        if (hledanebody != null) {
            hledanebody.setText("Cíle: " + GeoBody.getInstance(this).sfBodyNavsvivene.iSize() + "/" + GeoBody.getInstance(this).aBody.size());
        }

        TextView indicie = findViewById(R.id.indicii);
        if (indicie != null) {
            indicie.setText("Indicie: " + IndicieSeznam.getInstance(this).sfIndicie.iSize() + "/" + IndicieSeznam.getInstance(this).aIndicieVsechny.size());
        }

        iMin = GeoBody.getInstance(this).iVzdalenostNejblizsiho(this);

        TextView vzd = findViewById(R.id.vzdalenost);
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

                if (z.getsLink().equals("")) {
                    Okynka.zobrazOkynko(arg0.getContext(), z.getsZprava());
                }
                else
                {
                    openWebWiew(z.getsLink());
                }
                z.setbRead(true);
                if (null == z.getTsCasZobrazeni()) {
                    z.setTsCasZobrazeni(new Timestamp(Global.getTime()));
                }

                ZpravySeznam.getInstance(MainActivity.this).zkontrolujZpravy(true);
            }
        });

        Log.d(TAG, "LEAVE: updateView");
    }

    public void openWebWiew(String sPage) {
        Intent intent = new Intent(this, Nastenka.class);

        intent.putExtra("Page", sPage);

        startActivityForResult(intent, 0);
    }
}
