package com.deny.GeoLogi;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZpravySeznam {
    private final static String TAG = "MessageList";
    final int iTimeoutUpdate = 1200000;
    String sVybrano;

    private static ZpravySeznam ourInstance = null;
    private Context context = null;
    ArrayList<Zprava> zpravyKomplet = new ArrayList<Zprava>();
    ArrayList<Zprava> zpravyZobraz = new ArrayList<Zprava>();
    final Object lock = new Object();
    Uri notification;
    Ringtone notificationRingtone;
    Vibrator v;
    int iPocetAkt = 0;
    Timestamp tsLastUpdate = null;
    boolean bConnectionLost = false;
    final Handler updateHandler = new Handler();

    public static ZpravySeznam getInstance(Context context) {
        if (null==ourInstance) {
            ourInstance=new ZpravySeznam(context) ;
        }

        return ourInstance;
    }

    private ZpravySeznam() throws Exception {
        throw new Exception(TAG + "Tento konstruktor se nesmi pouzivat");
    }

    private ZpravySeznam(Context context) {
        this.context = context;

        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationRingtone = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
        v = (Vibrator)context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void read (Context context) {
        Log.d(TAG, "ENTER: read");
        this.context = context;

        synchronized (lock) {
            //Save currently selected gam and user to detect change later
            sVybrano = Global.simPrexix() + Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu();

            zpravyKomplet = new ArrayList<Zprava>();

            try {
                InputStream inputStream = context.openFileInput(Global.simPrexix() + Nastaveni.getInstance(context).getsIdHry() + Nastaveni.getInstance(context).getiIDOddilu() + "zpravy.bin");

                ObjectInputStream in = new ObjectInputStream(inputStream);

                int iPocet = (int) in.readInt();
                Log.d(TAG, "Ctu z " + Global.simPrexix() + Nastaveni.getInstance(context).getsIdHry() + Nastaveni.getInstance(context).getiIDOddilu() + "zpravy.bin" + "Pocet: " + iPocet);

                for (int i = 0; i < iPocet; i++) {
                    Zprava z = (Zprava) in.readObject();
                    zpravyKomplet.add(z);
                }
                in.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                //ignoruj
            } catch (Exception e) {
                Okynka.zobrazOkynko(context, "Chyba: " + e.getMessage());
            }
        }
        Log.d(TAG, "LEAVE: read");
    }

    private void write (Context context) {
        Log.d(TAG, "ENTER: write");

        synchronized (lock) {
            try {
                FileOutputStream fileOut = context.openFileOutput(Global.simPrexix() + Nastaveni.getInstance(context).getsIdHry() + Nastaveni.getInstance(context).getiIDOddilu() + "zpravy.bin", Context.MODE_PRIVATE);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);

                out.writeInt(zpravyKomplet.size());

                Log.d(TAG, "zapisuju do " + Global.simPrexix() + Nastaveni.getInstance(context).getsIdHry() + Nastaveni.getInstance(context).getiIDOddilu() + "zpravy.bin" + " pocet: " + zpravyKomplet.size());

                for (int i = 0; i < zpravyKomplet.size(); i++) {
                    out.writeObject(zpravyKomplet.get(i));
                }
                out.flush();
                out.close();

                fileOut.flush();
                fileOut.close();
            } catch (IOException ex) {
                Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
            }
        }
        Log.d(TAG, "LEAVE: write");
    }
    private void processJson(JSONObject object) {
        Log.d(TAG, "ENTER: processJSON");

        try {
            JSONArray rows = object.getJSONArray("rows");

            synchronized (lock) {
                for (int r = 0; r < rows.length(); ++r) {
                    JSONObject row = rows.getJSONObject(r);
                    JSONArray columns = row.getJSONArray("c");
                    System.out.print(rows.length());

                    int iId = 0;
                    try {
                        iId = columns.getJSONObject(0).getInt("v");
                    } catch (Exception e) {
                    }

                    int iOddil = 0;
                    try {
                        iOddil = columns.getJSONObject(1).getInt("v");
                    } catch (Exception e) {
                    }

                    String sPredmet = "";
                    try {
                        sPredmet = columns.getJSONObject(2).getString("v");
                    } catch (Exception e) {
                    }

                    String sZprava = "";
                    try {
                        sZprava = columns.getJSONObject(3).getString("v");
                    } catch (Exception e) {
                    }

                    String sLink = "";
                    try {
                        sLink = columns.getJSONObject(4).getString("v");
                    } catch (Exception e) {
                    }

                    String sZobrazitPoCase = "";
                    try {
                        sZobrazitPoCase = columns.getJSONObject(5).getString("v");
                    } catch (Exception e) {
                    }

                    int iPoZpraveCislo = 0;
                    try {
                        iPoZpraveCislo = columns.getJSONObject(6).getInt("v");
                    } catch (Exception e) {
                    }

                    double fCilovyBodLat = 0;
                    try {
                        fCilovyBodLat = columns.getJSONObject(7).getDouble("v");
                    } catch (Exception e) {
                    }

                    double fCilovyBodLong = 0;
                    try {
                        fCilovyBodLong = columns.getJSONObject(8).getDouble("v");
                    } catch (Exception e) {
                    }

                    String sCilovyBodPopis = "";
                    try {
                        sCilovyBodPopis = columns.getJSONObject(9).getString("v");
                    } catch (Exception e) {
                    }


                    double fZobrazitNaLat = 0;
                    try {
                        fZobrazitNaLat = columns.getJSONObject(10).getDouble("v");
                    } catch (Exception e) {
                    }

                    double fZobrazitNaLong = 0;
                    try {
                        fZobrazitNaLong = columns.getJSONObject(11).getDouble("v");
                    } catch (Exception e) {
                    }

                    int iPocetIndicii = 0;
                    try {
                        iPocetIndicii = columns.getJSONObject(12).getInt("v");
                    } catch (Exception e) {
                    }

                    String sIndicieZeSkupiny = "";

                    try {
                        sIndicieZeSkupiny = columns.getJSONObject(13).getString("v");
                    } catch (Exception e) {
                    }

                    String sPovinneIndicie = "";

                    try {
                        sPovinneIndicie = columns.getJSONObject(14).getString("v");
                    } catch (Exception e) {
                    }

                    String sNezobrazovatPokudMajiIndicii = "";
                    try {
                        sNezobrazovatPokudMajiIndicii = columns.getJSONObject(15).getString("v");
                    } catch (Exception e) {
                    }

                    String sColor = "";
                    try {
                        sColor = columns.getJSONObject(16).getString("v");
                    } catch (Exception e) {
                    }

                    int iColor = 0;
                    if (!sColor.equals("")) {
                        try {
                            iColor = Integer.valueOf(sColor, 16);
                        } catch (Exception e) {
                            Okynka.zobrazOkynko(context, "Chybný formát barvy u zpravy " + sPredmet);
                        }
                    }

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
                            sNezobrazovatPokudMajiIndicii,
                            iColor
                    );

                    pridejNeboPrepis(zprava);
                }
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
            Log.d(TAG, "New message");
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
            z_zapsana.setsIndicieZeSkupiny(z.getsIndicieZeSkupiny());
            z_zapsana.setsPovinneIndicie(z.getsPovinneIndicie());
            z_zapsana.setiBarva(z.getiBarva());
            z_zapsana.setsNezobrazovatPokudMajiIndicii(z.getsNezobrazovatPokudMajiIndicii());
            Log.d(TAG, "Message overwritten");
        }
    }

    Zprava zpravaPodleId(int idZpravy) {
        for (int i = 0; i<zpravyKomplet.size(); i++) {
            if (zpravyKomplet.get(i).getiId()==idZpravy) return zpravyKomplet.get(i);
        }
        return null;
    }

    //vraci true, pokud maji tu spravnou indicii
    private boolean zkontrolujJestliMajiIndicie (Zprava z) {
        List<String> items = Arrays.asList(z.getsPovinneIndicie().split("[\\\\s,]+"));

        for (int i=0; i<items.size(); i++) {
            if ((!items.get(i).equals(""))&&(!IndicieSeznam.getInstance(context).uzMajiIndicii(items.get(i)))) return false;
        }

        return true;
    }


    void zkontrolujZpravy (boolean bPrekreslit) {
        ArrayList<Zprava> zpravy = new ArrayList<Zprava>();
        bPrekreslit = bPrekreslit || (zpravyZobraz.size() == 0);
        boolean bNova = false;

        Log.d(TAG, "ENTER: zkontrolujZpravy: " + zpravyKomplet.size());

        synchronized (lock) {
            try {
                GeoBody.getInstance(context).aBody = new ArrayList<GeoBod>();

                for (int i = zpravyKomplet.size() - 1; i >= 0; i--) {
                    Zprava z = zpravyKomplet.get(i);

                    Log.d(TAG, "Zprava: " + z.getiId() +
                            "  Oddil? " + ((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(context).getiIDOddilu())) +
                            "  Cas? " + (zkontrolujCas(z)) +
                            "  Pocet indicii? " + (IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) >= z.getiPocetIndicii()) +
                            "  Spravne indicie? " + zkontrolujJestliMajiIndicie(z) +
                            "  Nezobrazovaci indicie? " + ((z.getsNezobrazovatPokudMajiIndicii().equals("")) || (!IndicieSeznam.getInstance(context).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii()))) +
                            "  Lokace? " + zkontrolujLokaci(z));

                    //zkotrnolujeme, ze se ma zprava zobrazit
                    if (z.getbZobrazeno() // uz byla nekdy videt
                            ||

                            (((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(context).getiIDOddilu()))  //zprava je pro dany oddil
                                    && (zkontrolujCas(z)) //je cas na zobrazeni zpravy
                                    && (IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) >= z.getiPocetIndicii()) //maji dost indiciii
                                    && (zkontrolujJestliMajiIndicie(z)) //a maji ty spravne
                                    && ((z.getsNezobrazovatPokudMajiIndicii().equals("")) || (!IndicieSeznam.getInstance(context).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii()))))) //neni to zprava. ktera se nema zobrazovat, pokud ziskali nejakou jinou indicii
                    {
                        if (zkontrolujLokaci(z)) //jsou na cilovem bode nebo na nem byli
                        {
                            //pokud ano, tak pridame zpravu do seznamu zobrazovanych
                            zpravy.add(z);

                            //pridame cilovy bod na mapu
                            if ((z.getfCilovyBodLat() != 0) || (z.getfCilovyBodLong() != 0)) {
                                GeoBod cilovyBod = new GeoBod(z.getfCilovyBodLat(), z.getfCilovyBodLong(), z.getsCilovyBodPopis(), true);

                                if (!GeoBody.getInstance(context).jeHledanej(cilovyBod)) {
                                    GeoBody.getInstance(context).aBody.add(cilovyBod);
                                }
                            }

                            //ulozime si hledany (ale mozna nezobrazovany bod na mapu)
                            if ((z.getfZobrazitNaLat() != 0) || (z.getfZobrazitNaLong() != 0)) {
                                GeoBod hledanyBod = new GeoBod(z.getfZobrazitNaLat(), z.getfZobrazitNaLong(), "", false);

                                if (!GeoBody.getInstance(context).jeHledanej(hledanyBod)) {
                                    GeoBody.getInstance(context).aBody.add(hledanyBod);
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
                                if (!GeoBody.getInstance(context).jeHledanej(bod) &&
                                        (!GeoBody.getInstance(context).bylNavstivenej(bod))) {
                                    GeoBody.getInstance(context).aBody.add(bod);
                                }
                            }
                        }
                    }
                }


                if (bPrekreslit) {
                    zpravyZobraz = zpravy;

                }

                if (bNova) {
                    bNova = false;

                    //pokud jsme na hlavni obrazovce, tak ukaz okynko, jinak pouze zapipej
                    //tim se predejde tomu, ze se zobrazuje nekolik okynek s informaci, ze maji novou zpravu
                    //if (!bPaused) {
                    //    Okynka.zobrazOkynko(context, "Máte novou zprávu");
                    //}

                    try {
                        //zkus prehrat zvuk
                        notificationRingtone.play();
                        v.vibrate(500);

                        //sendNotification("Nová zpráva", "Nová zpráva", "Nová zpráva", true, true, 0);
                    } catch (Exception e) {
                        // nedelej nic
                    }
                }

                GeoBody.getInstance(context).aktualizujMapu();


                if (zpravyKomplet.size() > 0) {
                    write(context);
                }

            }
            catch (Exception e) {
                Log.e(TAG, "ERROR: Zkontroluj zpravy: " + e.getMessage());

            }
        }
        Log.d(TAG, "LEAVE: zkontrolujZpravy: celkem zprav: "+ zpravyKomplet.size()+" viditelnych zprav: " + zpravy.size());

    }


    private void downloadJson () {
        Log.d(TAG, "ENTER: downloadJSON");
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

                new DownloadWebpageTask(new AsyncResultJSON() {
                    @Override
                    public void onResult(JSONObject object) {
                        processJson(object);
                    }
                }).execute("https://docs.google.com/spreadsheets/d/" + Nastaveni.getInstance(context).getsIdWorkseet() + "/gviz/tq?sheet=Zpravy");

            } else {
                Okynka.zobrazOkynko(context, "Nejste připojení k těm internetům - chyba 1");
            }
        }
        catch(Exception e) {
            Okynka.zobrazOkynko(context, "Nejste připojení k těm internetům - chyba 2 ");
        }
        Log.d(TAG, "LEAVE: downloadJSON");
    }

    public void serverUpdate (boolean bProvedHned) {
        //Kazdych 20 minut - nebo okamzite, pokud jsme ziskali spojeni po ztrate
        //se zaktualizuje seznam zprav
        Log.d(TAG, "serverUpdate");

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if ((bProvedHned) || (tsLastUpdate == null) || bConnectionLost || (now.getTime() > (tsLastUpdate.getTime() + iTimeoutUpdate))) {
                    bConnectionLost = false;
                    tsLastUpdate = now;

                    Log.d(TAG, "Stahuju aktualni seznam zprav a indicii");
                    downloadJson();
                    IndicieSeznam.getInstance(context).nactizwebu(context);
                }
            } else {
                bConnectionLost = true;
            }
        }
        catch (Exception e) {
            bConnectionLost = true;
        }
        //tsLastUpdate = now;
    }


    public void casovyupdate () {
        int iTimeout = 30000;
        boolean bZmenaHry = !sVybrano.equals(Global.simPrexix()+ Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu());
        Log.d(TAG, "ENTER:Casovy update ");


        if (!bZmenaHry) {
            if (!Nastaveni.getInstance(context).getsHra().equals("")) {
                int iMin = GeoBody.getInstance(context).iVzdalenostNejblizsiho(context);

                //pri priblizovani zkratime timeout
                if (iMin < 20) iTimeout = 1000;
                else if (iMin < 30) iTimeout = 2000;
                else if (iMin < 50) iTimeout = 3000;
                else if (iMin < 100) iTimeout = 5000;
            }

            serverUpdate(false);

            iPocetAkt++;
        }

        Log.d(TAG, "LEAVE:Casovy update. Akt: "+iPocetAkt+ " Next run in "+iTimeout);
        updateHandler.postDelayed(updateRunabble, iTimeout);
    }

    private Runnable updateRunabble = new Runnable() {
        @Override
        public void run() {
            casovyupdate();
        }
    };


    private boolean zkontrolujLokaci (Zprava z) {
        if ((z.getfZobrazitNaLat()==0) || (z.getfZobrazitNaLong())==0) return true;

        GeoBod b = new GeoBod(z.getfZobrazitNaLat(), z.getfZobrazitNaLong(), "", false);

        return GeoBody.getInstance(context).bylNavstivenej(b);
    }

    private boolean zkontrolujCas (Zprava z) {

        if (z.getsZobrazitPoCase().equals("")) return true;

        try {

            if (z.getiPoZpraveCislo()==0) {
                Timestamp t = Timestamp.valueOf(z.getsZobrazitPoCase());

                return ((new Timestamp(Global.getTime())).after(t));
            }
            else
            {
                Time t = Time.valueOf(z.getsZobrazitPoCase());
                Time t0 = Time.valueOf("0:00:00");

                Timestamp now = new Timestamp(Global.getTime());
                Zprava z_po = zpravaPodleId(z.getiPoZpraveCislo());

                if (z_po == null) return false;

                if (z_po.getTsCasZobrazeni() == null) return false;

                boolean res = new Timestamp(now.getTime()).after(new Timestamp(z_po.getTsCasZobrazeni().getTime()+ t.getTime()-t0.getTime()));

                return res;
            }
        }
        catch (Exception e) {
            Okynka.zobrazOkynko(context, "chyba pri zpracovani času zobrazeni zpravy: " + z.getiId()+ " "+z.getsZobrazitPoCase() );
            return false;
        }
    }



}


