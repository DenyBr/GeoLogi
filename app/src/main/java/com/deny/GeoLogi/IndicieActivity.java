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
                if (IndicieSeznam.getInstance(this).addHint(etIndH.getText().toString())) {
                    prekresli();
                    setResult(RESULT_OK);

                } else Okynka.zobrazOkynko(this, "Neplatná indicie");
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