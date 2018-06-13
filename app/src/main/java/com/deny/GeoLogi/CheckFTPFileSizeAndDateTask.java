package com.deny.GeoLogi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.FileOutputStream;
import java.io.IOException;

public class CheckFTPFileSizeAndDateTask extends AsyncTask<String, Void, String> {
    Context ctx = null;
    AsyncResultFTPCheckSizeAndDate callback = null;

    CheckFTPFileSizeAndDateTask(Context context, AsyncResultFTPCheckSizeAndDate callback) {
        ctx = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... sFile) {

        // params comes from the execute() call: params[0] is name of the file.
        try {
            return check(sFile[0]);
        } catch (Exception e) {
            //nedelej nic
        }
        return "";
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        callback.onResult(result);
    }

    private String check (String sFilename) throws IOException {
        String sResult = "";

        try
        {
            FTPClient con = null;

            Log.d("ftp", "Check file " + sFilename);

            con = new FTPClient();
            con.connect("109.205.76.29");

            if (con.login("bruzl", "ASDKL."))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FTPFile ftpFile = con.mlistFile(sFilename);
                if (ftpFile != null) {
                    String size = (new Long(ftpFile.getSize())).toString();
                    String timestamp = ftpFile.getTimestamp().getTime().toString();

                    sResult=size+timestamp;
                }
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return sResult;
    }
}
