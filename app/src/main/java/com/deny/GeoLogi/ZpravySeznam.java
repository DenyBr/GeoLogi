package com.deny.GeoLogi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class ZpravySeznam implements Handler.Callback {
    private final static String TAG = "MessageList";
    String sVybrano;

    private static ZpravySeznam ourInstance = null;
    private Context context = null;
    ArrayList<Zprava> zpravyKomplet = new ArrayList<>();
    ArrayList<Zprava> zpravyZobraz = new ArrayList<>();
    final Object lock = new Object();
    Uri notification;
    Ringtone notificationRingtone;
    Vibrator v;
    int iPocetAkt = 0;
    Timestamp tsLastUpdate = null;
    boolean bConnectionLost = false;
    final Handler updateHandler = new Handler();
    Handler.Callback guiUpdate =null;
    Timestamp tsGameStarted = null;
    Timestamp tsGameFinished = null;
    long lTimeLimit = 0;
    boolean bTimeLimitedGame = false;
    boolean bCasVyprsel = false;
    boolean bCilDosazen = false;


    boolean bCardFalse1Received = false;
    boolean bCardFalse2Received = false;
    boolean bCardFalse3Received = false;
    boolean bCardTrueReceived = false;

    HashSet<String> ssReceivedEvents = new HashSet<String>();
    LinkedHashSet<String> ssEventsToSend = new LinkedHashSet<String>();

    private IOServer ioServer;


    public static ZpravySeznam getInstance(Context context) {
        if (null==ourInstance) {
            ourInstance=new ZpravySeznam(context) ;
        }

        return ourInstance;
    }

    public static ZpravySeznam getInstance() throws Exception {
        if (null==ourInstance) {
            throw new Exception(TAG + "Instance nebyla jeste vytvorena");
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

        ioServer = new IOServer(context, 5001);

    }

    public void registerGuiCallback(Handler.Callback callback) {
        guiUpdate = callback;
    }


    public void read (Context context) {
        Log.d(TAG, "ENTER: read");
        this.context = context;

        updateHandler.removeCallbacks(updateRunabble);

        bCardFalse1Received = false;
        bCardFalse2Received = false;
        bCardFalse3Received = false;
        bCardTrueReceived = false;
        ssReceivedEvents = new HashSet<String>();
        ssEventsToSend = new LinkedHashSet<String>();


        synchronized (lock) {
            //Save currently selected gam and user to detect change later
            sVybrano = Global.simPrexix() + Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu();

            zpravyKomplet = new ArrayList<>();

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

        //register callback which will be called in case of update of Hints
        //instantiate Hints singleton and read stored Hints
        IndicieSeznam.setCallback(this);
        IndicieSeznam.getInstance(context).read(context);

        //the same for visited points
        GeoBody.setCallback(this);
        GeoBody.getInstance(context).init();

        //check and update messages
        ZpravySeznam.getInstance(context).zkontrolujZpravy(true);
        tsLastUpdate = null;

        casovyupdate();

        rekonstruujCas();

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

                    boolean bPublic = false;
                    try {
                        bPublic = columns.getJSONObject(0).getBoolean("v");
                    } catch (Exception e) {
                    }

                    int iId = 0;
                    try {
                        iId = columns.getJSONObject(1).getInt("v");
                    } catch (Exception e) {
                    }

                    int iOddil = 0;
                    try {
                        iOddil = columns.getJSONObject(2).getInt("v");
                    } catch (Exception e) {
                    }

                    String sPredmet = "";
                    try {
                        sPredmet = columns.getJSONObject(3).getString("v");
                    } catch (Exception e) {
                    }

                    String sZprava = "";
                    try {
                        sZprava = columns.getJSONObject(4).getString("v");
                    } catch (Exception e) {
                    }

                    String sLink = "";
                    try {
                        sLink = columns.getJSONObject(5).getString("v");
                    } catch (Exception e) {
                    }

                    String sZobrazitPoCase = "";
                    try {
                        sZobrazitPoCase = columns.getJSONObject(6).getString("v");
                    } catch (Exception e) {
                    }

                    int iPoZpraveCislo = 0;
                    try {
                        iPoZpraveCislo = columns.getJSONObject(7).getInt("v");
                    } catch (Exception e) {
                    }

                    double fCilovyBodLat = 0;
                    try {
                        fCilovyBodLat = columns.getJSONObject(8).getDouble("v");
                    } catch (Exception e) {
                    }

                    double fCilovyBodLong = 0;
                    try {
                        fCilovyBodLong = columns.getJSONObject(9).getDouble("v");
                    } catch (Exception e) {
                    }

                    String sCilovyBodPopis = "";
                    try {
                        sCilovyBodPopis = columns.getJSONObject(10).getString("v");
                    } catch (Exception e) {
                    }


                    double fZobrazitNaLat = 0;
                    try {
                        fZobrazitNaLat = columns.getJSONObject(11).getDouble("v");
                    } catch (Exception e) {
                    }

                    double fZobrazitNaLong = 0;
                    try {
                        fZobrazitNaLong = columns.getJSONObject(12).getDouble("v");
                    } catch (Exception e) {
                    }

                    int iPocetIndicii = 0;
                    try {
                        iPocetIndicii = columns.getJSONObject(13).getInt("v");
                    } catch (Exception e) {
                    }

                    String sIndicieZeSkupiny = "";

                    try {
                        sIndicieZeSkupiny = columns.getJSONObject(14).getString("v");
                    } catch (Exception e) {
                    }

                    String sPovinneIndicie = "";

                    try {
                        sPovinneIndicie = columns.getJSONObject(15).getString("v");
                    } catch (Exception e) {
                    }

                    String sNezobrazovatPokudMajiIndicii = "";
                    try {
                        sNezobrazovatPokudMajiIndicii = columns.getJSONObject(16).getString("v");
                    } catch (Exception e) {
                    }

                    String sZobrazitPriUdalosti = "";
                    try {
                        sZobrazitPriUdalosti = columns.getJSONObject(17).getString("v");
                    } catch (Exception e) {
                    }

                    String sProvestAkci = "";
                    try {
                        sProvestAkci = columns.getJSONObject(18).getString("v");
                    } catch (Exception e) {
                    }

                    String sColor = "";
                    try {
                        sColor = columns.getJSONObject(19).getString("v");
                    } catch (Exception e) {
                    }

                    int iColor = 0;
                    if (!sColor.equals("")) {
                        try {
                            iColor = Integer.valueOf(sColor, 19);
                        } catch (Exception e) {
                            Okynka.zobrazOkynko(context, "Chybný formát barvy u zpravy " + sPredmet);
                        }
                    }

                    String sCas = "";
                    try {
                        sCas = columns.getJSONObject(20).getString("v");
                    } catch (Exception e) {
                    }


                    Zprava zprava = new Zprava(bPublic,
                            iId,
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
                            sZobrazitPriUdalosti,
                            sProvestAkci,
                            iColor,
                            sCas
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
            z_zapsana.setbPublic(z.getbPublic());
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
            z_zapsana.setsProvestAkci(z.getsProvestAkci());
            z_zapsana.setsZobrazitPriUdalosti(z.getsZobrazitPriUdalosti());
            z_zapsana.setsCas(z.getsCas());
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
        List<String> items = Arrays.asList(z.getsPovinneIndicie().split("[\\\\,]+"));

        for (int i=0; i<items.size(); i++) {
            if ((!items.get(i).equals(""))&&(!IndicieSeznam.getInstance(context).uzMajiIndicii(items.get(i)))) {
                Log.d(TAG, "Nemaji indicii: "+items.get(i));
                return false;
            }
        }

        return true;
    }

    public void handleIOMessage (String sMessage) {
        Log.d(TAG, "ENTER; HandleIOMessage: " + sMessage);

        Log.d(TAG, sMessage.substring(0, 4).toLowerCase());
        Log.d(TAG, sMessage.substring(4, 8).toLowerCase());


        if (sMessage.substring(0, 4).toLowerCase().equals("card")) {
            Log.d(TAG,"Udaost je od ctecky karet");
            if (sMessage.substring(4, 8).toLowerCase().equals("true")) {
                bCardTrueReceived = true;
                Log.d(TAG,"Spravna karta");
            }
            else
            {
                Log.d(TAG,"Spatna karta");
                if (!bCardFalse1Received) bCardFalse1Received=true;
                else if (!bCardFalse2Received) bCardFalse2Received=true;
                else if (!bCardFalse3Received) bCardFalse3Received=true;
            }
        } else {
           ssReceivedEvents.add(sMessage);
        }

        zkontrolujZpravy(true);
    }

    private boolean zkontrolujCasoveUdalosti(Zprava z) {
        Log.d(TAG, "zkontrolujCasoveUdalosti: "+z.getsCas()+" "+z.getiId()+" " + z.getTsCasNacteni());
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (z.getsCas().trim().equals("")) return true;

        if (z.getsCas().contains("Plus")) {
            if (z.getTsCasNacteni()!=null) {
                lTimeLimit += Long.valueOf(z.getsCas().trim().substring(5));
                Log.d(TAG, "Pricitam cas: " + Long.valueOf(z.getsCas().trim().substring(5)));

                bTimeLimitedGame = true;
            }
            return true;
        }
        if (z.getsCas().contains("Minus")) {
            if (z.getTsCasNacteni()!=null) {
                lTimeLimit -= Long.valueOf(z.getsCas().trim().substring(6));
            }
            Log.d(TAG, "Odecitam cas: "+Long.valueOf(z.getsCas().trim().substring(6)));

            bTimeLimitedGame = true;
            return true;
        }

        if ((z.getsCas().trim().contains("Start")) || (z.getsCas().trim().equals("Start"))) {
            if (z.getTsCasNacteni()!=null) {
                tsGameStarted = z.getTsCasNacteni();
            }
            else {
                tsGameStarted = now;
            }
            Log.d(TAG, "Hra spustena");
            return true;
        }

        if ((z.getsCas().contains("Cil") || (z.getsCas().trim().equals("Cil")))) {
            if (z.getTsCasNacteni()!=null) tsGameFinished=z.getTsCasNacteni();

            Log.d(TAG, "Hra dokoncena uspesne");
            bCilDosazen = true;
            return true;
        }

        if ((z.getsCas().contains("Timeout") || (z.getsCas().trim().equals("Timeout"))) &&
                bTimeLimitedGame &&
                !bCilDosazen &&
                tsGameStarted!=null &&
                ((now.getTime()-tsGameStarted.getTime())>lTimeLimit*1000)
               ) {
            if (z.getTsCasNacteni() != null) {
                Log.d(TAG, "Hra dokoncena - cas vyprsel");

                tsGameFinished = z.getTsCasNacteni();

                bCilDosazen = false;
                bCasVyprsel = true;
            }
            return true;
        }

        return false;
    }

     private void rekonstruujCas() {
        bCasVyprsel = false;
        bCilDosazen = false;
        bTimeLimitedGame = false;
        tsGameStarted = null;
        tsGameFinished = null;
        lTimeLimit = 0;

        for (int i=0; i<zpravyZobraz.size(); i++) {
           Zprava z = zpravyZobraz.get(i);

           if (z.getTsCasNacteni()!=null) {
               zkontrolujCasoveUdalosti(z);
           }
        }
    }


    private boolean zkontrolujIOUdalosti(Zprava z) {
        //pokud zrava nema zadnou udalost, tak OK
        if (z.getsZobrazitPriUdalosti().trim().equals("")) return true;

        //vyhodnoceni karet
        //pokud uz pouzili spravnou kartu, tak uz na spatny kaslem
        if (bCardTrueReceived)
        {
            if (z.getsZobrazitPriUdalosti().equals("SpravnaKarta")) return true;
        } else {
            //pokud nepouzili spravnou, tak musime vyhodnotit, kolik spatnych uz pouzili
            if (z.getsZobrazitPriUdalosti().equals("SpatnaKarta1")) return bCardFalse1Received;
            if (z.getsZobrazitPriUdalosti().equals("SpatnaKarta2")) return bCardFalse2Received;
            if (z.getsZobrazitPriUdalosti().equals("SpatnaKarta3")) return bCardFalse3Received;
        }

        //vyhodnoseni ostatnich IO udalosti
        if (ssReceivedEvents.contains(z.getsZobrazitPriUdalosti())) {
            ssEventsToSend.add(z.getsProvestAkci());
            return true;
        }
        return false;
    }

    private void rekonstuujIO() {
        //toto tu je kvuli restartu aplikace v prubehu hry.
        //protoze obdrzene udalosti doufam povedou k zobrazeni zpravy, tak muzeme rekonstruovat

        for (int i = zpravyKomplet.size() - 1; i >= 0; i--) {
            Zprava z = zpravyKomplet.get(i);
            if (z.getbZobrazeno()) {
                if (!(z.getsProvestAkci().trim().equals(""))) {
                    ssEventsToSend.add(z.getsProvestAkci());
                }

                if (!(z.getsZobrazitPriUdalosti().trim().equals(""))) {
                    if ((z.getsZobrazitPriUdalosti().trim().equals("SpravnaKarta")))
                        bCardTrueReceived = true;
                    else if ((z.getsZobrazitPriUdalosti().trim().equals("SpatnaKarta1")))
                        bCardFalse1Received = true;
                    else if ((z.getsZobrazitPriUdalosti().trim().equals("SpatnaKarta2")))
                        bCardFalse2Received = true;
                    else if ((z.getsZobrazitPriUdalosti().trim().equals("SpatnaKarta3")))
                        bCardFalse3Received = true;
                    else ssReceivedEvents.add(z.getsZobrazitPriUdalosti().trim());
                }
            }
        }
    }

    void zkontrolujZpravy (boolean bPrekreslit) {
        ArrayList<Zprava> zpravy = new ArrayList<>();
        bPrekreslit = bPrekreslit || (zpravyZobraz.size() == 0);
        boolean bNova = false;
        String sNova = "";

        Log.d(TAG, "ENTER: zkontrolujZpravy: " + zpravyKomplet.size());

        synchronized (lock) {
            try {
                GeoBody.getInstance(context).aBody = new ArrayList<>();

                for (int i = zpravyKomplet.size() - 1; i >= 0; i--) {
                    Zprava z = zpravyKomplet.get(i);

                    Log.d(TAG, "Zprava: " + z.getiId() +
                            "  Public? " + z.getbPublic() +
                            "  Oddil? " + ((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(context).getiIDOddilu())) +
                            "  Cas? " + (zkontrolujCas(z)) +
                            "  Pocet indicii? " + (IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) >= z.getiPocetIndicii()) +
                            "  Spravne indicie? " + zkontrolujJestliMajiIndicie(z) +
                            "  Nezobrazovaci indicie? " + ((z.getsNezobrazovatPokudMajiIndicii().equals("")) || (!IndicieSeznam.getInstance(context).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii()))) +
                            "  Lokace? " + zkontrolujLokaci(z) +
                            "  IOUdalosti? " + zkontrolujIOUdalosti(z));

                    //zkotrnolujeme, ze se ma zprava zobrazit
                    if (z.getbZobrazeno() // uz byla nekdy videt
                            ||
                            (       ((z.getbPublic()) || (Global.isbSimulationMode())) //zprava je verejna nebo jsme v simulacnim rezimu
                                    && ((z.getiOddil() == 0) || (z.getiOddil() == Nastaveni.getInstance(context).getiIDOddilu()))  //zprava je pro dany oddil
                                    && (zkontrolujCas(z)) //je cas na zobrazeni zpravy
                                    && (IndicieSeznam.indiciiZeSkupiny(z.getsIndicieZeSkupiny()) >= z.getiPocetIndicii()) //maji dost indiciii
                                    && (zkontrolujJestliMajiIndicie(z)) //a maji ty spravne
                                    && ((z.getsNezobrazovatPokudMajiIndicii().equals("")) || (!IndicieSeznam.getInstance(context).uzMajiIndicii(z.getsNezobrazovatPokudMajiIndicii())))) //neni to zprava. ktera se nema zobrazovat, pokud ziskali nejakou jinou indicii
                                    && zkontrolujIOUdalosti(z)
                                    && zkontrolujCasoveUdalosti(z))
                    //toto musi byt posledni, aby nebyl cas pricten nebo odecten vicekrat
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
                                sNova = z.getsPredmet();

                                z.setbZobrazeno(true);
                                if (z.getTsCasNacteni()==null) z.setTsCasNacteni(new Timestamp(System.currentTimeMillis()));
                                zkontrolujCasoveUdalosti(z);

                                if (!z.getsProvestAkci().equals("")) {
                                    Log.d(TAG, "IO akce: " + z.getsProvestAkci());

                                    IOServer.sTextToSend=z.getsProvestAkci();
                                }
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

                    if (null!=guiUpdate) {
                        guiUpdate.handleMessage(null);
                    }
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

                        if (Global.isbPaused()) {
                            Log.d(TAG, "Channel creation");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                CharSequence name = "GeoLogi";
                                String description = "Geologi Notification";
                                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                                String CHANNEL_ID = "GL";
                                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                                channel.setDescription(description);
                                // Register the channel with the system; you can't change the importance
                                // or other notification behaviors after this
                                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                                notificationManager.createNotificationChannel(channel);
                            }

                            Intent intent = new Intent(context, UvodniStranka.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                            Log.d(TAG, "Notificaiton creation");
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "GL")
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle("Nová zpráva")
                                    .setContentText(sNova)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setVisibility(VISIBILITY_PUBLIC)
                                    .setAutoCancel(true);

                            Log.d(TAG, "Notification managet creation");
                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            Log.d(TAG, "Sending notification");
                            mNotificationManager.notify(0, mBuilder.build());

                        }
                    } catch(Exception e){
                        // nedelej nic
                    }
                }

                GeoBody.getInstance(context).aktualizujMapu();


                if (zpravyKomplet.size() > 0) {
                    write(context);
                }

                Log.d(TAG, "Rekonstruuj stav prijatych a odeslanych zprav");
                rekonstuujIO();
                if (IOServer.sTextToSend.equals("") && !ssEventsToSend.isEmpty()) {
                    IOServer.sTextToSend = (String) ssEventsToSend.toArray()[ 0 ];
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
        //Kazdych X minut - nebo okamzite, pokud jsme ziskali spojeni po ztrate
        //se zaktualizuje seznam zprav
        Log.d(TAG, "serverUpdate" + (null==tsLastUpdate?"0":tsLastUpdate.toString()));

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if ((bProvedHned) || (tsLastUpdate == null) || bConnectionLost || (now.getTime() > (tsLastUpdate.getTime() + Nastaveni.getInstance(context).getiUpdate()))) {
                    bConnectionLost = false;
                    tsLastUpdate = now;

                    Log.d(TAG, "Stahuju aktualni seznam zprav a indicii");
                    downloadJson();

                    if (bProvedHned) {
                        //if the user initiates immediate action, synchronise also hints and geopoints
                        //otherwise there are separated timer handlers
                        IndicieSeznam.getInstance(context).syncFileNow(context);
                        IndicieSeznam.sfIndicie.syncFileNow();
                        GeoBody.sfBodyNavsvivene.syncFileNow();
                    }
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

    @Override
    public boolean handleMessage(Message message) {

        zkontrolujZpravy(false);
        return true;
    }

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