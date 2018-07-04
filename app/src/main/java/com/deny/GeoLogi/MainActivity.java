package com.deny.GeoLogi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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



/*Hlavni obrazovka aplikace




 */

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    private static final String TAG = "MAIN";
    //ArrayList<Zprava> zpravy = new ArrayList<Zprava>();
    ArrayList<Zprava> zpravyKomplet = new ArrayList<Zprava>();
    ArrayList<Zprava> zpravyZobraz = new ArrayList<Zprava>();
    Uri notification;
    Ringtone notificationRingtone;
    Vibrator v;
    Location location;
    ListView listview;
    int iPocetAkt = 0;
    boolean bPaused = false;
    Timestamp tsLastUpdate = null;
    boolean bConnectionLost = false;
    //jak casto se bude stahovat a updatovat server
    //jednou za 20 minut staci
    final int iTimeoutUpdate = 1200000;
    int iSirka=0;
    final Object lock = new Object();
    String sVybrano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ENTER: OnCreate");

        super.onCreate(savedInstanceState);

        v = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        Log.d(TAG, "LEAVE: OnCreate");
    }

    private void Init () {
        Log.d(TAG, "ENTER: Init");

        //Save currently selected gam and user to detect change later
        sVybrano = Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu();

        //reload all parameters
        Nastaveni.getInstance(this).reload(this);

        //reload stored messages
        read(this);

        //register callback which will be called in case of update of Hints
        //instantiate Hints singleton and read stored Hints
        IndicieSeznam.setCallback(this);
        IndicieSeznam.getInstance(this).read(this);

        //the same for visited points
        GeoBody.setCallback(this);
        GeoBody.getInstance(this).init();

        //check and update messages
        zkontrolujZpravy(true);

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

    private void read (Context context) {
        Log.d(TAG, "ENTER: read");

        zpravyKomplet = new ArrayList<Zprava>();

        try {
            InputStream inputStream = context.openFileInput(Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"zpravy.bin");

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();
            Log.d(TAG, "Ctu z " + Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"zpravy.bin" + "Pocet: " + iPocet);

            for (int i=0; i<iPocet; i++) {
                Zprava z = (Zprava) in.readObject();
                zpravyKomplet.add(z);
            }
            in.close();
            inputStream.close();
        }
        catch (FileNotFoundException e) {
            //ignoruj
        }
        catch(Exception e) {
            Okynka.zobrazOkynko(this, "Chyba: " + e.getMessage());
        }

        Log.d(TAG, "LEAVE: read");
    }


    private void write (Context context) {
        Log.d(TAG, "ENTER: write");
        try {
            FileOutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"zpravy.bin", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(zpravyKomplet.size());

            Log.d(TAG , "zapisuju do "+ Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"zpravy.bin" + " pocet: " + zpravyKomplet.size());

            for (int i=0; i<zpravyKomplet.size(); i++) {
                out.writeObject(zpravyKomplet.get(i));
            }
            out.flush();
            out.close();

            fileOut.flush();
            fileOut.close();
        } catch (IOException ex) {
            Okynka.zobrazOkynko(this, "Chyba: " + ex.getMessage());
        }
        Log.d(TAG, "LEAVE: write");
    }

    public void syncClickHandler(View view) {
       serverUpdate(true);
    }

    private void downloadJson () {
        Log.d(TAG, "ENTER: downloadJSON");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

                new DownloadWebpageTask(new AsyncResultJSON() {
                    @Override
                    public void onResult(JSONObject object) {
                        processJson(object);
                    }
                }).execute("https://docs.google.com/spreadsheets/d/" + Nastaveni.getInstance(this).getsIdWorkseet() + "/gviz/tq?sheet=Zpravy");

            } else {
                Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům - chyba 1");
            }
        }
        catch(Exception e) {
            Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům - chyba 2 ");
        }
       Log.d(TAG, "LEAVE: downloadJSON");
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

    public void zapisNavstivenyTest (String fileName) {
        try {
            OutputStream fileOut = openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            GeoBod g1 = new GeoBod(49.1975289,16.6508889, "", true);
            //GeoBod g2 = new GeoBod(49.302,	16.784, "");

            out.writeInt(1);
            out.writeObject(g1);
            ///out.writeObject(g2);

            out.close();
            fileOut.flush();
            fileOut.close();
        } catch (IOException ex) {
            Okynka.zobrazOkynko(this, "Chyba: " + ex.getMessage());
        }
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


    private void processJson(JSONObject object) {
        Log.d(TAG, "ENTER: processJSON");

        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");
                System.out.print(rows.length());

                int iId = 0;
                try {
                    iId = columns.getJSONObject(0).getInt("v"); }
                catch (Exception e) {}

                int iOddil = 0;
                try {
                    iOddil = columns.getJSONObject(1).getInt("v"); }
                catch (Exception e) {}

                String sPredmet = "";
                try {
                    sPredmet = columns.getJSONObject(2).getString("v"); }
                catch (Exception e) {}

                String sZprava = "";
                try {
                    sZprava = columns.getJSONObject(3).getString("v"); }
                catch (Exception e) {}

                String sLink = "";
                try {
                    sLink = columns.getJSONObject(4).getString("v"); }
                catch (Exception e) {}

                String sZobrazitPoCase = "";
                try {
                    sZobrazitPoCase = columns.getJSONObject(5).getString("v");}
                catch (Exception e) {}

                int iPoZpraveCislo = 0;
                try {
                    iPoZpraveCislo = columns.getJSONObject(6).getInt("v"); }
                catch (Exception e) {}

                double fCilovyBodLat = 0;
                try {
                    fCilovyBodLat = columns.getJSONObject(7).getDouble("v");
                }
                catch (Exception e) {}

                double fCilovyBodLong = 0;
                try {
                    fCilovyBodLong = columns.getJSONObject(8).getDouble("v");
                }
                catch (Exception e) {}

                String sCilovyBodPopis = "";
                try {
                    sCilovyBodPopis = columns.getJSONObject(9).getString("v"); }
                catch (Exception e) {}


                double fZobrazitNaLat = 0;
                try {
                    fZobrazitNaLat = columns.getJSONObject(10).getDouble("v"); }
                catch (Exception e) {}

                double fZobrazitNaLong = 0;
                try {
                    fZobrazitNaLong = columns.getJSONObject(11).getDouble("v"); }
                catch (Exception e) {}

                int iPocetIndicii = 0;
                try {
                    iPocetIndicii = columns.getJSONObject(12).getInt("v"); }
                catch (Exception e) {}

                String sIndicieZeSkupiny = "";

                try {
                    sIndicieZeSkupiny = columns.getJSONObject(13).getString("v"); }
                catch (Exception e) {}

                String sPovinneIndicie = "";

                try {
                    sPovinneIndicie = columns.getJSONObject(14).getString("v"); }
                catch (Exception e) {}

                String sNezobrazovatPokudMajiIndicii = "";
                try {
                    sNezobrazovatPokudMajiIndicii = columns.getJSONObject(15).getString("v"); }
                catch (Exception e) {}

                Zprava zprava = new Zprava(iId,
                        iOddil,
                        sPredmet,
                        sZprava,
                        sLink,
                        sZobrazitPoCase,
                        iPoZpraveCislo,
                        fCilovyBodLat,
                        fCilovyBodLong,
                        sCilovyBodPopis,
                        fZobrazitNaLat,
                        fZobrazitNaLong,
                        iPocetIndicii,
                        sIndicieZeSkupiny,
                        sPovinneIndicie,
                        sNezobrazovatPokudMajiIndicii
                );

                pridejNeboPrepis(zprava);
            }

            zkontrolujZpravy(false);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "LEAVE: processJSON");
    }

    private void pridejNeboPrepis (Zprava z) {
        Zprava z_zapsana = zpravaPodleId(z.getiId());

        if (null == z_zapsana) {
            zpravyKomplet.add(z);
        } else {
            z_zapsana.setiOddil(z.getiOddil());
            z_zapsana.setsPredmet(z.getsPredmet());
            z_zapsana.setsZprava(z.getsZprava());
            z_zapsana.setsLink(z.getsLink());
            z_zapsana.setsZobrazitPoCase(z.getsZobrazitPoCase());
            z_zapsana.setiPoZpraveCislo(z.getiPoZpraveCislo());
            z_zapsana.setfCilovyBodLat(z.getfCilovyBodLat());
            z_zapsana.setfCilovyBodLong(z.getfCilovyBodLong());
            z_zapsana.setsCilovyBodPopis(z.getsCilovyBodPopis());
            z_zapsana.setfZobrazitNaLat(z.getfZobrazitNaLat());
            z_zapsana.setfZobrazitNaLong(z.getfZobrazitNaLong());
            z_zapsana.setiPocetIndicii(z.getiPocetIndicii());
            z_zapsana.setsPovinneIndicie(z.getsPovinneIndicie());
            z_zapsana.setsNezobrazovatPokudMajiIndicii(z.getsNezobrazovatPokudMajiIndicii());
        }

    }

    //vraci true, pokud maji tu spravnou indicii
    private boolean zkontrolujJestliMajiIndicie (Zprava z) {
        List<String> items = Arrays.asList(z.getsPovinneIndicie().split("[\\\\s,]+"));

        for (int i=0; i<items.size(); i++) {
            if ((!items.get(i).equals(""))&&(!IndicieSeznam.getInstance(this).uzMajiIndicii(items.get(i)))) return false;
        }

        return true;
    }

    private void serverUpdate (boolean bProvedHned) {
        //Kazdych 20 minut - nebo okamzite, pokud jsme ziskali spojeni po ztrate
        //se zaktualizuje seznam zpra a odesla aktualni stav indicii a navstivenych bodu

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if ((bProvedHned) || (tsLastUpdate == null) || bConnectionLost || (now.getTime() > (tsLastUpdate.getTime() + iTimeoutUpdate))) {
                    bConnectionLost = false;
                    tsLastUpdate = now;

                    Log.d("Main", "Stahuju aktualni seznam zprav a indicii");
                    downloadJson();
                    IndicieSeznam.getInstance(this).nactizwebu(this);

                    //Log.d("Main", "Odesilam data na server");
                    //uploadFile(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu()+"indicieziskanec.bin");
                    //uploadFile(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu()+"bodynavstivenec.bin");
                }
            } else {
                bConnectionLost = true;
            }
        }
        catch (Exception e) {
            bConnectionLost = true;
        }
        tsLastUpdate = now;
    }


    private void casovyupdate () {
        int iTimeout = 30000;
        boolean bZmenaHry = !sVybrano.equals(Nastaveni.getInstance(this).getsIdHry()+Nastaveni.getInstance(this).getiIDOddilu());

        if (!bZmenaHry) {
            if (!Nastaveni.getInstance(this).getsHra().equals("")) {
                int iMin = zkontrolujZpravy(false);

                //pri priblizovani zkratime timeout
                if (iMin < 20) iTimeout = 1000;
                else if (iMin < 30) iTimeout = 2000;
                else if (iMin < 50) iTimeout = 3000;
                else if (iMin < 100) iTimeout = 5000;
            }

            serverUpdate(false);

            iPocetAkt++;
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!Thread.currentThread().isInterrupted()) {
                    casovyupdate();
                }
            }
        }, iTimeout);
    }

    private boolean zkontrolujLokaci (Zprava z) {
        if ((z.getfZobrazitNaLat()==0) || (z.getfZobrazitNaLong())==0) return true;

        GeoBod b = new GeoBod(z.getfZobrazitNaLat(), z.getfZobrazitNaLong(), "", false);

        return GeoBody.getInstance(this).bylNavstivenej(b);
    }

    private boolean zkontrolujCas (Zprava z) {

        if (z.getsZobrazitPoCase().equals("")) return true;

        try {

            if (z.getiPoZpraveCislo()==0) {
                Timestamp t = Timestamp.valueOf(z.getsZobrazitPoCase());

                return ((new Timestamp(System.currentTimeMillis())).after(t));
            }
            else
            {
                Time t = Time.valueOf(z.getsZobrazitPoCase());
                Time t0 = Time.valueOf("0:00:00");

                Timestamp now = new Timestamp(System.currentTimeMillis());
                Zprava z_po = zpravaPodleId(z.getiPoZpraveCislo());

                if (z_po == null) return false;

                if (z_po.getTsCasZobrazeni() == null) return false;

                boolean res = new Timestamp(now.getTime()).after(new Timestamp(z_po.getTsCasZobrazeni().getTime()+ t.getTime()-t0.getTime()));

                return res;
            }
         }
         catch (Exception e) {
            Okynka.zobrazOkynko(this, "chyba pri zpracovani času zobrazeni zpravy: " + z.getiId()+ " "+z.getsZobrazitPoCase() );
             return false;
        }
    }

    Zprava zpravaPodleId(int idZpravy) {
        for (int i = 0; i<zpravyKomplet.size(); i++) {
            if (zpravyKomplet.get(i).getiId()==idZpravy) return zpravyKomplet.get(i);
        }
        return null;
    }

    int zkontrolujZpravy (boolean bPrekreslit) {
        ArrayList<Zprava> zpravy = new ArrayList<Zprava>();
        bPrekreslit = bPrekreslit || (zpravyZobraz.size() == 0);
        boolean bNova = false;
        int iMin = 100000;

        Log.d(TAG, "ENTER: zkontrolujZpravy: " + zpravyKomplet.size());

        synchronized (lock) {
            try {
                Log.d(TAG, "su tady " + zpravyKomplet.size());

                GeoBody.getInstance(this).aBody = new ArrayList<GeoBod>();

                for (int i = zpravyKomplet.size() - 1; i >= 0; i--) {
                    Zprava z = zpravyKomplet.get(i);
                    Log.d(TAG, "su tady " + i);

                    Log.d(TAG, "Zprava: " + z.getiId() +
                                    "  Oddil? " + ((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(this).getiIDOddilu())) +
                                    "  Cas? " + (zkontrolujCas(z)) +
                                    "  Pocet indicii? " + (IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) >= z.getiPocetIndicii()) +
                                    "  Spravne indicie? " + zkontrolujJestliMajiIndicie(z) +
                                    "  Nezobrazovaci indicie? " + ((z.getsNezobrazovatPokudMajiIndicii().equals("")) || (!IndicieSeznam.getInstance(this).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii()))) +
                                    "  Lokace? " + zkontrolujLokaci(z));

                    //zkotrnolujeme, ze se ma zprava zobrazit
                    if (((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(this).getiIDOddilu()))  //zprava je pro dany oddil
                            && (zkontrolujCas(z)) //je cas na zobrazeni zpravy
                            && (IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) >= z.getiPocetIndicii()) //maji dost indiciii
                            && (zkontrolujJestliMajiIndicie(z)) //a maji ty spravne
                            && ((z.getsNezobrazovatPokudMajiIndicii().equals("")) || (!IndicieSeznam.getInstance(this).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii())))) //neni to zprava. ktera se nema zobrazovat, pokud ziskali nejakou jinou indicii
                    {
                        if (zkontrolujLokaci(z)) //jsou na cilovem bode nebo na nem byli
                        {
                            //pokud ano, tak pridame zpravu do seznamu zobrazovanych
                            zpravy.add(z);

                            //pridame cilovy bod na mapu
                            if ((z.getfCilovyBodLat() != 0) || (z.getfCilovyBodLong() != 0)) {
                                GeoBod cilovyBod = new GeoBod(z.getfCilovyBodLat(), z.getfCilovyBodLong(), z.getsCilovyBodPopis(), true);

                                if (!GeoBody.getInstance(this).jeHledanej(cilovyBod)) {
                                    GeoBody.getInstance(this).aBody.add(cilovyBod);
                                }
                            }

                            //ulozime si hledany (ale mozna nezobrazovany bod na mapu)
                            if ((z.getfZobrazitNaLat() != 0) || (z.getfZobrazitNaLong() != 0)) {
                                GeoBod hledanyBod = new GeoBod(z.getfZobrazitNaLat(), z.getfZobrazitNaLong(), "", false);

                                if (!GeoBody.getInstance(this).jeHledanej(hledanyBod)) {
                                    GeoBody.getInstance(this).aBody.add(hledanyBod);
                                }
                            }

                            //a pokud je to nova zprava, tak iniciujeme prekresleni
                            if (!(z.getbZobrazeno())) {
                                bPrekreslit = true;
                                bNova = true;

                                z.setbZobrazeno(true);
                            }
                        } else {
                            //pokud je vsechno splneno, ale na lokaci jeste nebyli, je mozna potreba pridac cilovy bod do seznamu hledanych
                            if ((z.getfZobrazitNaLong() != 0) || (z.getfZobrazitNaLat() != 0)) {
                                GeoBod bod = new GeoBod(z.getfZobrazitNaLat(), z.getfZobrazitNaLong(), "", false);
                                if (!GeoBody.getInstance(this).jeHledanej(bod) &&
                                        (!GeoBody.getInstance(this).bylNavstivenej(bod))) {
                                    GeoBody.getInstance(this).aBody.add(bod);
                                }
                            }
                        }
                    }
                }


                if (bPrekreslit) {
                    zpravyZobraz = zpravy;

                    setContentView(R.layout.activity_main);
                    listview = (ListView) findViewById(R.id.listview);

                    //Predame adamteru aktualni seznam zprav
                    final ZpravyAdapter adapter = new ZpravyAdapter(this, R.layout.zprava, zpravyZobraz);
                    listview.setAdapter(adapter);

                    //a zaregistrujeme listener na kliknuti
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                            Okynka.zobrazOkynko(arg0.getContext(), zpravyZobraz.get(position).getsZprava());
                            zpravyZobraz.get(position).setbRead(true);
                            if (null == zpravyZobraz.get(position).getTsCasZobrazeni())
                                zpravyZobraz.get(position).setTsCasZobrazeni(new Timestamp(System.currentTimeMillis()));

                            zkontrolujZpravy(true);
                            resize();
                        }
                    });
                }

                if (bNova) {
                    bNova = false;

                    //pokud jsme na hlavni obrazovce, tak ukaz okynko, jinak pouze zapipej
                    //tim se predejde tomu, ze se zobrazuje nekolik okynek s informaci, ze maji novou zpravu
                    if (!bPaused) Okynka.zobrazOkynko(this, "Máte novou zprávu");

                    try {
                        //zkus prehrat zvuk
                        notificationRingtone.play();
                        v.vibrate(500);

                        //sendNotification("Nová zpráva", "Nová zpráva", "Nová zpráva", true, true, 0);
                    } catch (Exception e) {
                        // nedelej nic
                    }
                }

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
                GeoBody.getInstance(this).aktualizujMapu();


                if (zpravyKomplet.size() > 0) {
                    write(this);
                    //read(this);
                }

            }
            catch (Exception e) {
                Log.e(TAG, "ERROR: Zkontroluj zpravy: " + e.getMessage());

            }
        }
        Log.d(TAG, "LEAVE: zkontrolujZpravy: celkem zprav: "+ zpravyKomplet.size()+" viditelnych zprav: " + zpravy.size() + " nejbizsi bod: "+iMin);

        return iMin;
    }

    public void testClickHandler(View view) {

        //read(this);
        try {
            InputStream inputStream = this.openFileInput(Nastaveni.getInstance(this).getsIdHry() + Nastaveni.getInstance(this).getiIDOddilu() + "zpravy.bin");

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();
            //Okynka.zobrazOkynko(this, "Ctu z " + Nastaveni.getInstance(this).getsIdHry() + Nastaveni.getInstance(this).getiIDOddilu() + "zpravy.bin" + "Pocet: " + iPocet);
            //Log.d("")

            in.close();
            inputStream.close();
        }
        catch(Exception e) {
           // Okynka.zobrazOkynko(this, e.getMessage());
        }
  }


    @Override
    protected void onResume() {
        bPaused = false;

        super.onResume();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Zpravy jsou na sirku

        setContentView(R.layout.activity_main);
        Log.d("Main", "Spusteno");

        //chceme full screan kvuli malejm telefonum
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        Init();

        iSirka = this.getResources().getConfiguration().screenWidthDp;
        resize();

        Button btnT = (Button) findViewById(R.id.btnTest);
        btnT.setVisibility ((Nastaveni.getInstance(this).getisRoot()?View.VISIBLE:View.INVISIBLE));

        casovyupdate();
    }

    @Override
    protected void onPause () {
        super.onPause();

        bPaused = true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        zkontrolujZpravy(false);

        return true;
    }
}
