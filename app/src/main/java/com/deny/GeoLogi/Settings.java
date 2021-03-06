package com.deny.GeoLogi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {

    Nastaveni pNastaveni = Nastaveni.getInstance(this);
    ArrayList<Hra> hry = new ArrayList<Hra>();
    int iVybranaHra;
    int iVybranyOddil;
    VyberOddil updateOddily = new VyberOddil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void processJson(JSONObject object) {
        try {
            JSONArray rows = object.getJSONArray("rows");

            for (int r = 1; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                String sId = "";
                try {
                    sId = columns.getJSONObject(0).getString("v");
                } catch (Exception e) {
                }

                String sHra = "";
                try {
                    sHra = columns.getJSONObject(1).getString("v");
                } catch (Exception e) {
                }

                String sIdWorksheet = "";
                try {
                    sIdWorksheet = columns.getJSONObject(2).getString("v");
                } catch (Exception e) {
                }

                String sNastenka = "";
                try {
                    sNastenka = columns.getJSONObject(3).getString("v");
                } catch (Exception e) {
                }

                String sVerejna = "";
                boolean bVerejna = false;
                try {
                    sVerejna = columns.getJSONObject(4).getString("v");
                    bVerejna = (sVerejna.toLowerCase().equals("Ano"));
                } catch (Exception e) {
                }

                Hra hra = new Hra(sId, sHra, sIdWorksheet, sNastenka, bVerejna);
                hry.add(hra);
            }


            Spinner sp = (Spinner) findViewById(R.id.spinHry);

            List<String> lHry = new ArrayList<String>();
            int iPoziceVybraneho = 0;

            for (int i = 0; i < hry.size(); i++) {
                lHry.add(hry.get(i).getsHra());
                if ((Nastaveni.getInstance(this).getsHra()!=null) && (Nastaveni.getInstance(this).getsHra().equals(hry.get(i).getsHra()))) {
                    iPoziceVybraneho = i;
                }
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, lHry);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(dataAdapter);

            sp.setSelection(iPoziceVybraneho);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public class VyberOddil implements Runnable {

        public void run() {
            //Okynka.zobrazOkynko(Settings.this, "Oddilu je : "+ Uzivatele.getInstance().aOddily.size());


            Spinner sp = (Spinner) findViewById(R.id.spinOddily);
            int iVybranyOddil = 0;

            ArrayList<String> ao = new ArrayList<String>();

            for(int i = 0; i< Uzivatele.getInstance().aOddily.size(); i++) {
                ao.add(Uzivatele.getInstance().aOddily.get(i).getsNazev());

               /* if (Uzivatele.getInstance().aOddily.get(i).getiId() == pNastaveni.getiIDOddilu()) {
                    iVybranyOddil = i;
                }*/
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Settings.this,
                    android.R.layout.simple_spinner_item, ao);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(dataAdapter);

            sp.setSelection(iVybranyOddil);

        }
    }

    public void hraVybrana(int iPozice) {
        //Okynka.zobrazOkynko(this, ""+iPozice + " " + hry.get(iPozice).getsIdWorkseet());

        iVybranaHra = iPozice;

        if (!"".equals(hry.get(iPozice).getsIdWorkseet())) {
            Uzivatele.getInstance().reload(hry.get(iPozice).getsIdWorkseet(), updateOddily);
        }

    }

    public void buttonClickHandler(View view) {
        TextView tvHeslo = (EditText) findViewById(R.id.etHesloH);

        //pNastaveni.setProperty("Uzivatel", tvOddil.getText().toString());
        Spinner spOddil = (Spinner) findViewById(R.id.spinOddily);
        Spinner spHra = (Spinner) findViewById(R.id.spinHry);

        //Okynka.zobrazOkynko(Settings.this, "x"+Uzivatele.getInstance().aOddily.get(spOddil.getSelectedItemPosition()).getsHeslo()  + "=" + tvHeslo.getText()+"x");

        if (!(Uzivatele.getInstance().aOddily.get(spOddil.getSelectedItemPosition()).getsHeslo().equals(tvHeslo.getText().toString())))
        {
            Okynka.zobrazOkynko(this, "Neplatné heslo");

        } else {
            pNastaveni.setProperty("Heslo", tvHeslo.getText().toString());
            pNastaveni.setProperty("ID", ""+ Uzivatele.getInstance().aOddily.get(spOddil.getSelectedItemPosition()).getiId());
            pNastaveni.setProperty("Uzivatel", Uzivatele.getInstance().aOddily.get(spOddil.getSelectedItemPosition()).getsNazev());
            pNastaveni.setProperty("Hra", hry.get(spHra.getSelectedItemPosition()).getsHra());
            pNastaveni.setProperty("IDHra", hry.get(spHra.getSelectedItemPosition()).getIdHra());
            pNastaveni.setProperty("Verejna", ""+hry.get(spHra.getSelectedItemPosition()).isbVerejna());
            pNastaveni.setProperty("Nastenka", hry.get(spHra.getSelectedItemPosition()).getsNastenka());
            pNastaveni.setProperty("IdWorkseet", hry.get(spHra.getSelectedItemPosition()).getsIdWorkseet());
            pNastaveni.setProperty("Root", ""+Uzivatele.getInstance().aOddily.get(spOddil.getSelectedItemPosition()).isbRoot());

            try {
                OutputStream outputStream = openFileOutput("config.properties", Context.MODE_PRIVATE);
                pNastaveni.store(outputStream, "");
                outputStream.flush();
                outputStream.close();

                pNastaveni.reload(this);

                setResult(RESULT_OK);
            } catch (Exception e) {
            }

            this.finish();
        }

    }


    class VyberHry implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            Settings.this.hraVybrana(pos);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    class VyberOddilu implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            //asi nic
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.activity_settings);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //to remove the action bar (title bar)
        getSupportActionBar().hide();

        Spinner spHra = (Spinner) findViewById(R.id.spinHry);
        spHra.setOnItemSelectedListener(new VyberHry());

        Spinner spOddil = (Spinner) findViewById(R.id.spinOddily);
        spOddil.setOnItemSelectedListener(new VyberOddilu());

        EditText tvHeslo = (EditText) findViewById(R.id.etHesloH);
        tvHeslo.setText(pNastaveni.getProperty("Heslo", ""));

        //zjisti jestli jsme na internetu a pokud ano, tak stahni seznam her
        //jinak zakaz vyber hry
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask(new AsyncResultJSON() {
                    @Override
                    public void onResult(JSONObject object) {
                        processJson(object);
                    }
                }).execute("https://spreadsheets.google.com/tq?key=12GdpmQ9Y7tgEBs6k--t-zqH_fYRzooGhc3VEtgdrB7Q");

            } else {
                Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Nastavení není možné změnit");
            }
        } catch (Exception e) {
            Okynka.zobrazOkynko(this, "Nejste připojení k těm internetům. Nastavení není možné změnit");
        }
    }
}


