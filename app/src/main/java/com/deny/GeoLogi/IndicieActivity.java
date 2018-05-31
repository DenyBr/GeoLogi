package com.deny.GeoLogi;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;


import com.deny.GeoLogi.R;


public class IndicieActivity extends AppCompatActivity {
    private ListView listview;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
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

        handler.postDelayed(update, 10);
    }


    private Runnable update = new Runnable() {
        @Override
        public void run() {
            IndicieSeznam.getInstance(IndicieActivity.this).read(IndicieActivity.this);
            prekresli();

            IndicieSeznam.getInstance(IndicieActivity.this).nactizwebu(IndicieActivity.this);

            handler.postDelayed(this, 60000);
        }
    };


    public void buttonClickHandler(View view) {

        EditText etIndH = (EditText) findViewById(R.id.etIndH);
        if (!etIndH.getText().toString().equals("")) {

            if (IndicieSeznam.getInstance(IndicieActivity.this).uzMajiIndicii(etIndH.getText().toString())) {
                Okynka.zobrazOkynko(this, "Tuto indicii už máte");
            } else {
                for (int i = 0; i < IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.size(); i++) {
                    if (IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.get(i).jeToOno(etIndH.getText().toString())) {
                        IndicieSeznam.getInstance(IndicieActivity.this).aIndicieZiskane.add(IndicieSeznam.getInstance(IndicieActivity.this).aIndicieVsechny.get(i));
                        IndicieSeznam.getInstance(IndicieActivity.this).write(this);
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

        final IndicieAdapter adapter = new IndicieAdapter(this, R.layout.indicie, IndicieSeznam.getInstance(this).aIndicieZiskane);
        listview.setAdapter(adapter);
    }
}