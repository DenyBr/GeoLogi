package com.deny.GeoLogi;

import android.os.Handler;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;


import java.sql.Timestamp;


public class IndicieActivity extends AppCompatActivity {
    private final String TAG = "IndicieActivity";
    private ListView listview;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Commented out, since the screen is big eough after removing the action bar
        if ((this.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
           setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }*/

        setContentView(R.layout.activity_indicie);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)
        getSupportActionBar().hide();
    }


    private Runnable update = new Runnable() {
        @Override
        public void run() {
            IndicieSeznam.getInstance(IndicieActivity.this).read(IndicieActivity.this);
            IndicieSeznam.getInstance(IndicieActivity.this).sfIndicie.readFile();

            prekresli();

            IndicieSeznam.getInstance(IndicieActivity.this).nactizwebu(IndicieActivity.this);

            handler.postDelayed(this, 60000);
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(update, 10);
    }

    public void buttonClickHandler(View view) {

        EditText etIndH = (EditText) findViewById(R.id.etIndH);
        if (!etIndH.getText().toString().equals("")) {

            if (IndicieSeznam.getInstance(IndicieActivity.this).uzMajiIndicii(etIndH.getText().toString())) {
                Okynka.zobrazOkynko(this, "Tuto indicii už máte");
            } else {
                for (int i = 0; i < IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.size(); i++) {
                    if (IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.get(i).jeToOno(etIndH.getText().toString())) {
                        IndicieSeznam.getInstance(IndicieActivity.this).sfIndicie.localList.add(IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.get(i));
                        //set timestamp\
                        Long tsLong = System.currentTimeMillis()/1000;

                        IndicieSeznam.getInstance(IndicieActivity.this).sfIndicie.localList.get(IndicieSeznam.getInstance(IndicieActivity.this).sfIndicie.localList.size()-1).setTime(new Timestamp(System.currentTimeMillis()));

                        IndicieSeznam.getInstance(IndicieActivity.this).sfIndicie.writeFile();

                        IndicieSeznam.getInstance(IndicieActivity.this).sfIndicie.syncFileNow();

                        prekresli();

                        setResult(RESULT_OK);

                        return;
                    }
                }
                Okynka.zobrazOkynko(this, "Neplatná indicie");
            }
        }
    }

    private void prekresli () {
        setContentView(R.layout.activity_indicie);
        listview = (ListView) findViewById(R.id.listview);

        Log.d(TAG, "prekresli Pocet indicii: "+IndicieSeznam.getInstance(this).sfIndicie.localList.size());

        final IndicieAdapter adapter = new IndicieAdapter(this, R.layout.indicie, IndicieSeznam.getInstance(this).sfIndicie.localList);
        listview.setAdapter(adapter);
    }

    @Override
    protected void onPause() {

        super.onPause();

        finish();
    }
}