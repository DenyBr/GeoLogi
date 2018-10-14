package com.deny.GeoLogi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

public class CheckFTPFileSizeAndDateTask extends AsyncTask<String, Void, String> {
    Context ctx = null;
    static private final String TAG = "CheckFTP";

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
            Log.d (TAG, "doInBackground: "+e.getMessage());
        }
        return "";
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        try {
            long lSize = 0;
            long lTimestamp = 0;

            List<String> items = Arrays.asList(result.split("[\\\\s,]+"));

            if (2 == items.size()) {
                lSize = Long.parseLong(items.get(0));
                lTimestamp = Long.parseLong(items.get(1));
            }

            callback.onResult(lSize, lTimestamp);
        } catch (Exception e) {
            Log.d (TAG, "onPostExecute: "+e.getMessage());
        }

}

    private String check (String sFilename) throws IOException {
        String sResult = "";

        try
        {
            FTPClient con = null;

            Log.d("ftp", "Check file " + sFilename);

            InetAddress address = InetAddress.getByName(Nastaveni.getInstance(ctx).getsFTP());

            con = new FTPClient();
            con.connect(address.getHostAddress());
            con.setDataTimeout((int) Global.iConTimeout);
            con.setConnectTimeout((int) Global.iConTimeout);

            if (con.login(Nastaveni.getInstance(ctx).getsFTPUser(), Nastaveni.getInstance(ctx).getsFTPHeslo()))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                FTPFile[] ftpFile = con.listFiles(sFilename);

                if ((null!=ftpFile) && (null!=ftpFile[0])) {
                    String size = (Long.valueOf(ftpFile[0].getSize())).toString();
                    Long timestamp =  ftpFile[0].getTimestamp().getTimeInMillis()/100000;

                    sResult=size+","+timestamp;
                }
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "check: " + e.getMessage());
        }

        return sResult;
    }
}
