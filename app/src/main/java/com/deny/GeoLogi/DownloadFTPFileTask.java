package com.deny.GeoLogi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileOutputStream;

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
        return "-1";
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        //
        int iResult = Integer.valueOf(result);
        callback.onResult(iResult);
    }

    private String downloadFile(String sFilenameServer, String sFileNameLocal) {
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

                if (result) return "1";
                            return "0";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "-1";
    }
}
