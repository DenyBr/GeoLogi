package com.deny.GeoLogi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class VysledkyActivity extends AppCompatActivity {
    private final String TAG = "VysledkyActivity";
    private ListView listview;
    ArrayList<Result> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vysledky);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)
        getSupportActionBar().hide();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "ENTER: onResume");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

            Uzivatele.getInstance().reload(Nastaveni.getInstance().getsIdWorkseet(), new UsersUpdated());

            } else {
                Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst");
            }
        } catch (Exception e) {
            Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst");
        }
        Log.d(TAG, "LEAVE: onResume");
   }

   private class UsersUpdated implements Runnable {
       public void run() {
           for (int i = 0; i < Uzivatele.getInstance().aOddily.size(); i++) {
               //initiate download of files
               String sId = "" + Uzivatele.getInstance().aOddily.get(i).getiId();
               String sFileNameHints = Global.simPrexix() + Nastaveni.getInstance(VysledkyActivity.this).getsIdHry() + sId + "indicieziskane.bin";
               String sFileNameHintsFailed = Global.simPrexix() + Nastaveni.getInstance(VysledkyActivity.this).getsIdHry() + sId + "indicieneplatne.bin";
               String sFileNamePoints = Global.simPrexix() + Nastaveni.getInstance(VysledkyActivity.this).getsIdHry() + sId + "bodynavstivene.bin";
               Log.d(TAG, "Downloading: " + sFileNameHints + " " + sFileNamePoints);

               new DownloadFTPFileTask(VysledkyActivity.this, new AsyncResultFTPDownload() {
                   @Override
                   public void onResult(int iRes) {
                       updateOnFileDownload(iRes);
                   }
               }).execute(sFileNamePoints, sFileNamePoints + "res");
               new DownloadFTPFileTask(VysledkyActivity.this, new AsyncResultFTPDownload() {
                   @Override
                   public void onResult(int iRes) {
                       updateOnFileDownload(iRes);
                   }
               }).execute(sFileNameHints, sFileNameHints + "res");
               new DownloadFTPFileTask(VysledkyActivity.this, new AsyncResultFTPDownload() {
                   @Override
                   public void onResult(int iRes) {
                       updateOnFileDownload(iRes);
                   }
               }).execute(sFileNameHintsFailed, sFileNameHintsFailed + "res");
           }

       }
   }

   private String getNumberFromFile(String sFileName) {
       int iPocet=0;

       Log.d(TAG, "ENTER: readFile: "+sFileName);
       try {
           InputStream inputStream =  this.openFileInput(sFileName);

           ObjectInputStream in = new ObjectInputStream(inputStream);

           iPocet = (int) in.readInt();
           Log.d(TAG, "LEAVE: V souboru: "+sFileName+" je: " + iPocet);

           in.close();
           return ""+iPocet;
       }
       catch(Exception e) {
           Log.d (TAG, "ERROR: readFile " + e.getMessage());
       }

       Log.d(TAG, "LEAVE: Soubor: "+sFileName+" se nepodarilo nacist");
       return "?";
   }

    private void updateOnFileDownload(int iRes) {
        Log.d(TAG, "ENTER: updateOnFileDownload " + iRes);

        if (iRes == 1) {
            results = new ArrayList<>();

            for (int i = 0; i < Uzivatele.getInstance().aOddily.size(); i++) {
                Uzivatel u = Uzivatele.getInstance().aOddily.get(i);
                Log.d(TAG, "ENTER: updateOnFileDownload: " + u.getsNazev());

                if (!u.isbRoot()) {
                    results.add(new Result(u.getsNazev(), "" + u.getiId(),
                            getNumberFromFile(Global.simPrexix() + Nastaveni.getInstance(this).getsIdHry() + u.getiId() + "indicieziskane.binres"),
                            getNumberFromFile(Global.simPrexix() + Nastaveni.getInstance(this).getsIdHry() + u.getiId() + "indicieneplatne.binres"),
                            getNumberFromFile(Global.simPrexix() + Nastaveni.getInstance(this).getsIdHry() + u.getiId() + "bodynavstivene.binres")));
                }
            }

            listview = (ListView) findViewById(R.id.resutsview);
            final ResultsAdapter adapter = new ResultsAdapter(this, R.layout.teamresult, results);

            listview.setAdapter(adapter);

            if (Nastaveni.getInstance(this).getisRoot() || Global.isbSimulationMode()) {
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        showTeamResults(results.get(position));
                    }
                });
            }
        }

        Log.d(TAG, "LEAVE: updateOnFileDownload");


    }




    private String hintsToString(Result r) {
            Log.d(TAG, "ENTER: hintsToString");
            String sFilename=Global.simPrexix() + Nastaveni.getInstance(VysledkyActivity.this).getsIdHry() + r.getsIdUzivatele() + "indicieziskane.binres";
            StringBuffer sRes = new StringBuffer("Indicie\n");

            try {
                InputStream inputStream =  this.openFileInput(sFilename);

                ObjectInputStream in = new ObjectInputStream(inputStream);

                int iPocet = (int) in.readInt();

                for (int i=0; i<iPocet; i++) {
                    Indicie obj = (Indicie) in.readObject();

                    sRes.append(obj.getTime().toString());
                    sRes.append(" ");
                    sRes.append(obj.getsTexty().get(0));
                    sRes.append("\n");
                }
                in.close();
            }
            catch(Exception e) {
                Log.d (TAG, "ERROR: readFile " + e.getMessage());
            }

            Log.d(TAG, "LEAVE: Ze souboru: "+sFilename+" nacteno: " + sRes);

            return sRes.toString();
        }

    private String hintsFailedToString(Result r) {
        Log.d(TAG, "ENTER: hintsFailedToString");
        String sFilename=Global.simPrexix() + Nastaveni.getInstance(VysledkyActivity.this).getsIdHry() + r.getsIdUzivatele() + "indicieneplatne.binres";
        StringBuffer sRes = new StringBuffer("Neplatné indicie \n");

        try {
            InputStream inputStream =  this.openFileInput(sFilename);

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();

            for (int i=0; i<iPocet; i++) {
                IndicieNeplatna obj = (IndicieNeplatna) in.readObject();

                sRes.append(obj.getTime().toString());
                sRes.append(" ");
                sRes.append(obj.getsIndicie());
                sRes.append("\n");
            }
            in.close();
        }
        catch(Exception e) {
            Log.d (TAG, "ERROR: readFile " + e.getMessage());
        }

        Log.d(TAG, "LEAVE: Ze souboru: "+sFilename+" nacteno: " + sRes);

        return sRes.toString();
    }


    private String pointToString(Result r) {
        Log.d(TAG, "ENTER: hintsToString");
        String sFilename=Global.simPrexix() +Nastaveni.getInstance(VysledkyActivity.this).getsIdHry() + r.getsIdUzivatele() + "bodynavstivene.binres";
        StringBuffer sRes = new StringBuffer("Navštívené body\n");

        try {
            InputStream inputStream =  this.openFileInput(sFilename);

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();

            for (int i=0; i<iPocet; i++) {
                GeoBod obj = (GeoBod) in.readObject();

                sRes.append(obj.getTime().toString());
                sRes.append(" ");
                sRes.append(obj.getdLat());
                sRes.append(" ");
                sRes.append(obj.getdLong());
                sRes.append(":");
                sRes.append(obj.getdLat());
                sRes.append(" ");
                sRes.append(obj.getPopis());
                sRes.append("\n");
            }
            in.close();
        }
        catch(Exception e) {
            Log.d (TAG, "ERROR: readFile " + e.getMessage());
        }

        Log.d(TAG, "LEAVE: Ze souboru: "+sFilename+" nacteno: " + sRes);

        return sRes.toString();
    }


    private void showTeamResults(Result r) {
        StringBuffer sRes = new StringBuffer();

        sRes.append(hintsToString(r));
        sRes.append("\n");
        sRes.append(hintsFailedToString(r));
        sRes.append("\n");
        sRes.append(pointToString(r));

        Okynka.zobrazOkynko(this, sRes.toString());
    }

}
