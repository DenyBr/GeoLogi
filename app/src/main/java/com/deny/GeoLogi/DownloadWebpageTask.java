package com.deny.GeoLogi;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/* inspired by kstancev
    AsyncTask pouzivany pro zpracovani json formatu google spreadsheet
 */

public class DownloadWebpageTask extends AsyncTask<String, Void, String> {
    AsyncResultJSON callback;
    final static private String TAG = "DownloadWebPage";


    public DownloadWebpageTask(AsyncResultJSON callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urls) {

        // params comes from the execute() call: params[0] is the url.
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (!result.equals("")) {
            // remove the unnecessary parts from the response and construct a JSON
            int start = result.indexOf("{", result.indexOf("{") + 1);
            int end = result.lastIndexOf("}");
            String jsonResponse = result.substring(start, end);
            try {
                JSONObject table = new JSONObject(jsonResponse);
                callback.onResult(table);
            } catch (JSONException e) {
                Log.d (TAG, e.getMessage());
            }
        }
    }

    private String downloadUrl(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout((int) Global.iConTimeout /* milliseconds */);
            conn.setConnectTimeout((int) Global.iConTimeout /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int responseCode = conn.getResponseCode();

            Log.d("conn", "HTTP Response Code: "+responseCode);

            is = conn.getInputStream();

            String contentAsString = convertStreamToString(is);

            Log.d("result: ", contentAsString);

            if (responseCode==200) {
                return contentAsString;
            }
        }
        catch (Exception e) {
            Log.d (TAG, e.getMessage());
        }
        finally {
            if (is != null) {
                is.close();
            }
        }

        Log.d (TAG, "Webpage download failed");
        return "";
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            Log.d (TAG, e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.d (TAG, e.getMessage());
            }
        }
        return sb.toString();
    }
}
