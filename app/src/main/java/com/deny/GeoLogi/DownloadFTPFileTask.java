package com.deny.GeoLogi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileOutputStream;
import java.net.InetAddress;

public class DownloadFTPFileTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "DownloadFTP";

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

            InetAddress address = InetAddress.getByName( Nastaveni.getInstance(ctx).getsFTP());

            con = new FTPClient();
            con.setDataTimeout((int) Global.iConTimeout);
            con.setConnectTimeout((int) Global.iConTimeout);
            con.connect(address.getHostAddress());

            if (con.login(Nastaveni.getInstance(ctx).getsFTPUser(), Nastaveni.getInstance(ctx).getsFTPHeslo()))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FileOutputStream out = ctx.openFileOutput(sFileNameLocal, Context.MODE_PRIVATE);

                if (!Global.FTP_PREFIX.equals("")) {
                    if (!con.changeWorkingDirectory(Global.FTP_PREFIX)) {
                        Log.e(TAG, "Chdir failed");
                    }
                }

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
            Log.d(TAG, e.getMessage());
        }

        return "-1";
    }
}
