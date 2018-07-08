package com.deny.GeoLogi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by bruzlzde on 23.2.2018.
 */

public class Uzivatele {
    public ArrayList<Uzivatel> aOddily = new ArrayList<Uzivatel>();
    private static Uzivatele instance=null;
    Runnable r_callback=null;

    static public Uzivatele getInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new Uzivatele();
        }

        return instance;
    }

    private Uzivatele() {

    }

    public void reload(String wID, Runnable callback) {
        if (wID != null) {
            r_callback = callback;

            new DownloadWebpageTask(new AsyncResultJSON() {
                @Override
                public void onResult(JSONObject object) {
                    processJson(object);
                }
            }).execute("https://docs.google.com/spreadsheets/d/" + wID + "/gviz/tq?sheet=Uzivatele");
        }
    }

    private void processJson(JSONObject object) {

        try {
            JSONArray rows = object.getJSONArray("rows");
            aOddily = new ArrayList<Uzivatel>();

            for (int r = 0; r < rows.length(); ++r) {
                JSONObject row = rows.getJSONObject(r);
                JSONArray columns = row.getJSONArray("c");

                int iId = 0;
                try {
                    iId = columns.getJSONObject(0).getInt("v");
                } catch (Exception e) {
                }
                String sOddil = "";
                try {
                    sOddil = columns.getJSONObject(1).getString("v");
                } catch (Exception e) {
                }

                String sHeslo = "";
                try {
                    sHeslo = columns.getJSONObject(2).getString("v");
                } catch (Exception e) {
                }

                boolean bRoot = false;
                try {
                    bRoot = columns.getJSONObject(3).getBoolean("v");
                } catch (Exception e) {
                }

                Uzivatel o = new Uzivatel(iId, sOddil, sHeslo, bRoot);
                aOddily.add(o);

            }

            if (r_callback!=null) r_callback.run();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}