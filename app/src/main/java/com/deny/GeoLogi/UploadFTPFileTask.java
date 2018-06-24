package com.deny.GeoLogi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class UploadFTPFileTask extends AsyncTask<String, Void, String> {
    private final String TAG = "UploadFtp";
    Context ctx = null;



    UploadFTPFileTask(Context context) {
        ctx = context;
    }

    @Override
    protected String doInBackground(String... sFile) {

        // params comes from the execute() call: params[0] is the url.
        try {
            return uploadFile(sFile[0]);
        } catch (Exception e) {
            //nedelej nic
        }

        return "";
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //
    }

    private String uploadFile(String sFilename) throws IOException {
        try
        {
            FTPSClient con = null;

            Log.d(TAG, "Upload: " + sFilename);

            con = new FTPSClient();
            con.connect("109.205.76.29");

            if (con.login("bruzl", "ASDKL."))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FileInputStream in = ctx.openFileInput( sFilename);

                boolean result = con.storeFile(sFilename, in);

                Log.d(TAG, "Vysledek uploadu:  " + result);

                in.close();
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.d (TAG, "ERROR: "+e.getMessage());
        }

        return "";
    }

}
