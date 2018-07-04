package com.deny.GeoLogi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class VysledkyActivity extends AppCompatActivity {
    private final String TAG = "VysledkyActivity";
    private ListView listview;
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
                for (int i=0; i<Uzivatele.getInstance().aOddily.size(); i++) {
                    //initiate download of files
                    String sId = ""+Uzivatele.getInstance().aOddily.get(i).getiId();
                    String sFileNameHints = Nastaveni.getInstance(this).getsIdHry()+sId+"indicieziskane.bin";
                            String sFileNamePoints = Nastaveni.getInstance(this).getsIdHry()+sId+"bodynavstivene.bin";

                    new DownloadFTPFileTask(this, new AsyncResultFTPDownload() {
                        @Override
                        public void onResult(boolean res) {
                            updateOnFileDownload(res);
                        }
                    }).execute(sFileNamePoints, sFileNamePoints+"res");
                    new DownloadFTPFileTask(this, new AsyncResultFTPDownload() {
                        @Override
                        public void onResult(boolean res) {
                            updateOnFileDownload(res);
                        }
                    }).execute(sFileNameHints, sFileNameHints+"res");
                }
            } else {
                Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst");
            }
        } catch (Exception e) {
            Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst");
        }
        Log.d(TAG, "LEAVE: onResume");
   }

   private String getNumberFromFile(String sFileName) {
       int iPocet=0;
       String sResult = "?";

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

    private void updateOnFileDownload(boolean res) {
        ArrayList<Result> results = new ArrayList<>();

        for (int i=0; i<Uzivatele.getInstance().aOddily.size(); i++) {
            Uzivatel u = Uzivatele.getInstance().aOddily.get(i);

            if (!u.isbRoot()) {
                results.add(new Result(u.getsNazev(), getNumberFromFile(Nastaveni.getInstance(this).getsHra()+u.getiId()+"indicieziskane.binres"), getNumberFromFile(Nastaveni.getInstance(this).getsHra()+u.getiId()+"bodynavstivene.binres")));
            }
        }

        listview = (ListView) findViewById(R.id.restblrow);
        final ResultsAdapter adapter = new ResultsAdapter(this, R.layout.teamresult, results);

        listview.setAdapter(adapter);
    }
}
