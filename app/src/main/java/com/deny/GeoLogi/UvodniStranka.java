package com.deny.GeoLogi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.deny.GeoLogi.R;

import java.sql.Timestamp;
import java.util.Random;

//import com.deny.taborofka_zpravy.R;

public class UvodniStranka extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    private final String TAG = "Uvodni";

    private long tsClick=0;
    private int iNumfClicks;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private int  iSirka;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    Intent intentSettings = null;
    Intent intentMain = null;
    Intent intentResults = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_uvodni_stranka);
        Log.d("Uvodni", "Spusteno");

        TableRow tr = (TableRow) findViewById(R.id.sim);
        tr.setVisibility(Global.isbSimulationMode() ? View.VISIBLE : View.INVISIBLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)
        getSupportActionBar().hide();

        pristupy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }

            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }

            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
        resize();

        intentSettings = new Intent(this, Settings.class);
        intentMain = new Intent(this, MainActivity.class);
        intentMain.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intentResults = new Intent(this, VysledkyActivity.class);
    }


    private void resize() {
        final Handler handler = new Handler();

        iSirka = this.getResources().getConfiguration().screenWidthDp;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            //otestuj rozliseni a zmen velikost obrazku
            TextView tvV1= (TextView) findViewById(R.id.vypln1);
            TextView tvV2= (TextView) findViewById(R.id.vypln2);
            TableRow trObr= (TableRow) findViewById(R.id.tblobraze);

            if (iSirka>800) {
                tvV1.getLayoutParams().width=(iSirka-400)/2;
                tvV2.getLayoutParams().width=(iSirka-400)/2;
            } else
            {
                tvV1.getLayoutParams().width=100;
                tvV2.getLayoutParams().width=100;

            }
            }
        }, 1);


    }


    private void pristupy() {
        Button btnVyber = (Button) findViewById(R.id.btnVyberHry);
        Button btnPokracovat = (Button) findViewById(R.id.btnPokracovat);
        Button btnResults = (Button) findViewById(R.id.btnVysledky);
        TextView tvTextik= (TextView) findViewById(R.id.tvVybranaHra);


        //vyber hry jde vzdycky
        btnVyber.setEnabled(true);
        Nastaveni.getInstance(this).reload(this);

        if (Nastaveni.getInstance(this).getsHra().equals("")){
            btnPokracovat.setEnabled(false);
            btnResults.setEnabled(false);
            tvTextik.setText("");
         }
         else {
            btnPokracovat.setEnabled(true);
            btnResults.setEnabled(true);
            tvTextik.setText("Vybraná hra: "+Nastaveni.getInstance(this).getsHra());

            if (!Nastaveni.getInstance(this).getProperty("Uzivatel","").equals("")) {
                tvTextik.setText(tvTextik.getText()+"                   Uživatel: "+Nastaveni.getInstance(this).getProperty("Uzivatel",""));
            }

        }
    }

    public void vyberClickHandler(View view) {
        // Do something in response to button
        startActivityForResult(intentSettings, 1);
    }

    public void pokracujClickHandler(View view) {
        // Do something in response to button
        intentMain.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intentMain, 2);


    }

    public void vysledkyClickHandler(View view) {
        startActivityForResult(intentResults, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        pristupy();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }

    public void toggleSimulationMode(View view) {
        if (Nastaveni.getInstance(this).getisRoot()) {

            long now = System.currentTimeMillis();

            if (now - tsClick > 5000) {
                tsClick = now;
                iNumfClicks = 1;
                Log.d(TAG, "Sim "+iNumfClicks);
            } else {
                iNumfClicks++;
                Log.d(TAG, "Sim "+iNumfClicks);

                if (iNumfClicks > 7) {
                    Global.setbSimulationMode(!Global.isbSimulationMode());

                    TableRow tr = (TableRow) findViewById(R.id.sim);
                    tr.setVisibility(Global.isbSimulationMode() ? View.VISIBLE : View.INVISIBLE);
                    iNumfClicks=0;
                }
            }
        }
    }
}
