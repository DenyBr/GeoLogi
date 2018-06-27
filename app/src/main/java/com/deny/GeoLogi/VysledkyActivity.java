package com.deny.GeoLogi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ListView;

import org.json.JSONObject;

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


        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                for (int i=0; i<Uzivatele.getInstance().aOddily.size(); i++) {
                    //initiate download of files

                }
            } else {
                Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst");
            }
        } catch (Exception e) {
            Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Výsledky není možné načíst");
        }
   }

    private void updateOnFileDownload() {

    }
}
