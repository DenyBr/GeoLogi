package com.deny.taborofka_zpravy;

import com.google.common.base.Function;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by bruzlzde on 23.2.2018.
 */

public class Oddily {
    public ArrayList<Oddil> aOddily = new ArrayList<Oddil>();
    private static Oddily instance=null;
    Runnable r_callback=null;

    static public Oddily getInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new Oddily();
        }

        return instance;
    }

    private Oddily() {

    }

    public void reload(String wID, Runnable callback) {
        if (wID != null) {
            new DownloadWebpageTask(new AsyncResult() {
                @Override
                public void onResult(JSONObject object) {
                    processJson(object);
                }
            }).execute("https://docs.google.com/spreadsheets/d/" + wID + "/gviz/tq?sheet=Oddily");

            r_callback = callback;
        }
    }

    private void processJson(JSONObject object) {

        try {
            JSONArray rows = object.getJSONArray("rows");
            aOddily = new ArrayList<Oddil>();

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

                Oddil o = new Oddil(iId, sOddil, sHeslo);
                aOddily.add(o);

                }

            if (r_callback!=null) r_callback.run();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}