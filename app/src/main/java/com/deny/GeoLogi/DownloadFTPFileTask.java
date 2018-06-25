package com.deny.GeoLogi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadFTPFileTask extends AsyncTask<String, Void, String> {
    Context ctx = null;
    AsyncResultFTPDownload callback = null;

    DownloadFTPFileTask(Context context, AsyncResultFTPDownload callback) {
        ctx = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... sFile) {

        // params comes from the execute() call: params[0] is the name of the remote file.
        //params[1] is the local name of the downloaded file
        try {
            return downloadFile(sFile[0], sFile[1]);
        } catch (Exception e) {
            //nedelej nic
        }
        return "";
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //
        boolean bResult = (new Boolean(result)).booleanValue();
        callback.onResult(bResult);
    }

    private String downloadFile(String sFilenameServer, String sFileNameLocal) throws IOException {
        boolean result = false;

        try
        {
            FTPClient con = null;

            Log.d("ftp", "Download " + sFilenameServer + " do "+ sFileNameLocal);

            con = new FTPClient();
            con.connect("109.205.76.29");

            if (con.login("bruzl", "ASDKL."))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FileOutputStream out = ctx.openFileOutput(sFileNameLocal, Context.MODE_PRIVATE);

                result = con.retrieveFile(sFilenameServer, out);

                Log.d("ftp", "Vysledek downloadu:  " + result);

                out.close();
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ""+result;
    }
}
