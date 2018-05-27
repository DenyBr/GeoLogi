package com.deny.GeoLogi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.progress.Taborofka.R;

//import com.deny.taborofka_zpravy.R;

public class UvodniStranka extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Zpravy jsou na sirku



        setContentView(R.layout.activity_uvodni_stranka);

        ImageView iv=(ImageView) findViewById(R.id.ivUvodni);

        //otestuj rozliseni a zmen velikost obrazku
        if ((this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            iv.getLayoutParams().width = 800;
            iv.getLayoutParams().height = 240;

        } else
        {
            iv.getLayoutParams().width = 400;
            iv.getLayoutParams().height = 120;
        }

        pristupy();
    }

    private void pristupy() {
        Button btnVyber = (Button) findViewById(R.id.btnVyberHry);
        Button btnPokracovat = (Button) findViewById(R.id.btnPokracovat);
        TextView tvTextik= (TextView) findViewById(R.id.tvVybranaHra);

        //vyber hry jde vzdycky
        btnVyber.setEnabled(true);

        if (Nastaveni.getInstance(this).getsHra().equals("")){
            btnPokracovat.setEnabled(false);
            tvTextik.setText("");
         }
         else {
            tvTextik.setText("Vybraná hra: "+Nastaveni.getInstance(this).getsHra());

            if (!Nastaveni.getInstance(this).getProperty("Uzivatel","").equals("")) {
                tvTextik.setText(tvTextik.getText()+"                   Uživatel: "+Nastaveni.getInstance(this).getProperty("Uzivatel",""));
            }
        }
    }

    public void vyberClickHandler(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, Settings.class);
        startActivityForResult(intent, 1);
    }

    public void pokracujClickHandler(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 2);
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




}
