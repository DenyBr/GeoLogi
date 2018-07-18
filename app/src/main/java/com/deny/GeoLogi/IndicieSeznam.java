package com.deny.GeoLogi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by bruzlzde on 21.3.2018.
 */

public class IndicieSeznam {
    private final static String TAG = "IndicieSeznam";

    private static IndicieSeznam ourInstance = null;
    public static ArrayList<Indicie> aIndicieVsechny = new ArrayList<Indicie>();

    private static Context ctx = null;
    private static Handler.Callback updateCallback = null;
    private static Handler updateHandler = new Handler();

    public static SyncFiles<Indicie> sfIndicie = null;

    public static IndicieSeznam getInstance(Context context)  {
        ctx = context;

        if (ourInstance == null) {
            ourInstance = new IndicieSeznam(context);
        }
        return ourInstance;
    }

    public static IndicieSeznam getInstance()  {
        //vim, ze tohle muze vratit null, ale z logiky programu bych se tomu mel vyhnout

        return ourInstance;
    }

    public static void setCallback(Handler.Callback callback) {
        IndicieSeznam.updateCallback = callback;
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            syncFileNow(IndicieSeznam.ctx);

            updateHandler.postDelayed(updateRunnable, Global.iUpdateInterval);
        }
    };


    public void read (Context context) {
        try {
            Log.d(TAG, "ENTER: read");

            updateHandler.removeCallbacks(updateRunnable);

            aIndicieVsechny = new ArrayList<Indicie>();
            InputStream inputStream =  context.openFileInput( Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"indicievsechny.bin");

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();
            //Okynka.zobrazOkynko(this, "Pocet zprav" + iPocet);

            for (int i=0; i<iPocet; i++) {
                Indicie ind = (Indicie) in.readObject();
                aIndicieVsechny.add(ind);
            }
            in.close();
        }
        catch(Exception e) {
            //Okynka.zobrazOkynko(this, "Chyba: " + e.getMessage());
        }

        if (null!=sfIndicie) sfIndicie.finalize();
        sfIndicie = new SyncFiles<Indicie>(ctx, Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"indicieziskane.bin", Global.iUpdateInterval, updateCallback);

        updateHandler.postDelayed(updateRunnable, 10);

        Log.d(TAG, "LEAVE: read. Indicii celkem " + aIndicieVsechny.size());
    }




    public void write (Context context) {
        Log.d(TAG, "ENTER: write");

        try {
            OutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"indicievsechny.bin", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(aIndicieVsechny.size());

            for (int i=0; i<aIndicieVsechny.size(); i++) {
                out.writeObject(aIndicieVsechny.get(i));
            }

            out.close();
            fileOut.close();

            Log.d(TAG, "LEAVE: write " + aIndicieVsechny.size());
        } catch (IOException ex) {Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
        }
    }

    public void syncFileNow(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            new DownloadWebpageTask(new AsyncResultJSON() {
                @Override
                public void onResult(JSONObject object) {
                    processJson(object);
                }
            }).execute("https://docs.google.com/spreadsheets/d/" + Nastaveni.getInstance(context).getsIdWorkseet() + "/gviz/tq?sheet=Indicie");
        }
        else
        {
            //Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům, seznam platných indicíí nemusí být aktuální");
        }
    }

    private void processJson(JSONObject object) {

        try {
            JSONArray rows = object.getJSONArray("rows");
            IndicieSeznam.getInstance().aIndicieVsechny = new ArrayList<Indicie>();

            for (int r = 1; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                ArrayList<String> aInd = new ArrayList<String>();


                int iPlatnaPo = 0;
                try {
                    iPlatnaPo = columns.getJSONObject(0).getInt("v");
                } catch (Exception e) {
                }

                String sGroup = "";
                try {
                    sGroup = columns.getJSONObject(1).getString("v");
                } catch (Exception e) {
                }

                for (int iSloupec=2; iSloupec<7; iSloupec++) {
                    String sText = "";
                    try {
                        sText = columns.getJSONObject(iSloupec).getString("v");
                        aInd.add(sText);
                    } catch (Exception e) {
                    }
                }

                IndicieSeznam.getInstance().aIndicieVsechny.add(new Indicie(iPlatnaPo, sGroup, aInd));
            }

            IndicieSeznam.getInstance().write(ctx);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static int indiciiZeSkupiny(String sGroup) {
        int iRes=0;

        for (int i = 0; i< sfIndicie.localList.size(); i++) {
            if (sfIndicie.localList.get(i).getsGroup().equals(sGroup)) {
                iRes++;
            }
        }

        return iRes;
    }

    public static boolean uzMajiIndicii(String uzMaji) {
        for (int i = 0; i< sfIndicie.localList.size(); i++) {
            if (sfIndicie.localList.get(i).jeToOno(uzMaji)) {
                return true;
            }
        }
        return false;
    }

    private IndicieSeznam(Context context) {
        ctx = context;
        read(context);
   }

   public boolean addHint(String sInd) {
       for (int i = 0; i < aIndicieVsechny.size(); i++) {
           Indicie indicie = aIndicieVsechny.get(i);
           if (indicie.jeToOno(sInd)
                   &&(indicie.getiPlatnaPo()==0 || ZpravySeznam.getInstance(ctx).zpravaPodleId(indicie.getiPlatnaPo()).getbZobrazeno())) {
               indicie.setTime(new Timestamp(Global.getTime()));
               sfIndicie.localList.add(indicie);

               sfIndicie.writeFile();

               sfIndicie.syncFileNow();

               return true;
           }
       }
       return false;
   }

    public String simAddOneOfGroup(String sGroup) {
        for (int i=0; i<aIndicieVsechny.size(); i++) {
            Indicie indicie = aIndicieVsechny.get(i);
            if (!uzMajiIndicii(indicie.getsTexty().get(0)) && indicie.getsGroup().equals(sGroup)) {
                addHint(indicie.getsTexty().get(0));
                return indicie.getsTexty().get(0);
            }
        }
        return "";
    }
}