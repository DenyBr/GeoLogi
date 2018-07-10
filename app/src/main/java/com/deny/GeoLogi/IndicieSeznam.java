package com.deny.GeoLogi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

/*
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
*/

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
    private static IndicieSeznam ourInstance = null;
    public static ArrayList<Indicie> aIndicieVsechny = new ArrayList<Indicie>();
    //public static ArrayList<Indicie> aIndicieZiskane = new ArrayList<Indicie>();
    private static Context ctx = null;
    private static Handler.Callback callback = null;

    public static SyncFiles<Indicie> sfIndicie;

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
        IndicieSeznam.callback = callback;
    }


    public void read (Context context) {
        try {
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

        sfIndicie = new SyncFiles<Indicie>(ctx, Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"indicieziskane.bin", 60000, callback);
    }

    public void write (Context context) {
        try {
            OutputStream fileOut = context.openFileOutput(Nastaveni.getInstance(context).getsIdHry()+Nastaveni.getInstance(context).getiIDOddilu()+"indicievsechny.bin", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(aIndicieVsechny.size());

            for (int i=0; i<aIndicieVsechny.size(); i++) {
                out.writeObject(aIndicieVsechny.get(i));
            }

            out.close();
            fileOut.close();
        } catch (IOException ex) {Okynka.zobrazOkynko(context, "Chyba: " + ex.getMessage());
        }
    }

    public void nactizwebu(Context context) {
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

                String sGroup = "";
                try {
                    sGroup = columns.getJSONObject(0).getString("v");
                } catch (Exception e) {
                }

                for (int iSloupec=1; iSloupec<6; iSloupec++) {
                    String sText = "";
                    try {
                        sText = columns.getJSONObject(iSloupec).getString("v");
                        aInd.add(sText);
                    } catch (Exception e) {
                    }
                }

                IndicieSeznam.getInstance().aIndicieVsechny.add(new Indicie(sGroup, aInd));
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
           if (aIndicieVsechny.get(i).jeToOno(sInd)) {
               sfIndicie.localList.add(aIndicieVsechny.get(i));
               sfIndicie.localList.get(IndicieSeznam.sfIndicie.localList.size()-1).setTime(new Timestamp(Global.getTime()));

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
