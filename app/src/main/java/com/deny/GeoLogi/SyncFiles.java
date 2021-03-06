package com.deny.GeoLogi;

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bruzlzde on 12.3.2018.
 *
 * Serves for synchronisation of locally stored list of certain type
   with the common file on ftp server
 */

public class SyncFiles<T extends OverWriter&Serializable> {
    private final String TAG = "SyncFiles";

    private String sFilename;
    private Timer timer;
    private Context ctx;

    public ArrayList<T> localList;


    public SyncFiles(Context ctx, String sFileName, int iPerioda){
        Log.d(TAG, "ENTER: SyncFiles: "+sFilename);
        setsFilename(sFileName);
        setCtx(ctx);

        localList = new ArrayList<>();

        readFile();

        timer = new Timer();
        timer.schedule(
                    new TimerTask() {

                    @Override
                    public void run() {
                        syncFileNow();
                    }
                }, 1, iPerioda);

        Log.d(TAG, "LEAVE: SyncFiles: "+sFilename);
    }

     public String getsFilename() {
        return sFilename;
    }
    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public void setsFilename(String sFilename) {
        this.sFilename = sFilename;
    }

    //metoda iniciuje zesynchronizovani souboru
    //vola se jednak pravidelne tak jak je nastaveno
    //a muze se volat "rucne" - napriklad kduyz uzivatel zada novou indicii
    public void syncFileNow() {
        Log.d(TAG, "ENTER: syncFileNow " + sFilename);

        new CheckFTPFileSizeAndDateTask(ctx, new AsyncResultFTPCheckSizeAndDate() {
            @Override
            public void onResult(String res) {
                processDateAndSize(res);
            }
        }).execute(sFilename);

        Log.d(TAG, "LEAVE: syncFileNow " + sFilename);
    }

    private void processDateAndSize(String res) {
        //tato metoda bude zavolana potom, co se zjisti vlastnosti vzdaleneho souboru
        //ted zjistime velikost a datum verze na zarizeni
        Log.d(TAG, "ENTER: processDateAndSize. RemoteFile: " + res);

        File localFile = new File (ctx.getFilesDir().getAbsolutePath() + "/" + sFilename);

        Log.d(TAG, "processDateAndSize. Local file: " + localFile.length()+ (localFile.lastModified()/100000));

        if (!res.equals(""+localFile.length()+ new TimeStamp(localFile.lastModified()).toString())) {
            if (res.equals("")) {
                //verze na netu neexistuje, takze nahrajem aktualni
                upload();
            } else {
                new DownloadFTPFileTask(ctx, new AsyncResultFTPDownload() {
                    @Override
                    public void onResult(boolean res) {
                        processDownload(res);
                    }
                }).execute(sFilename, sFilename+"tmp");
            }
        }

        Log.d(TAG, "LEAVE: processDateAndSize: ");
    }

    private void addOrRewrite (T remoteItem) {
        boolean bAdd = true;

        //go through the list and check if the item is already there
        for (int i=0; i<localList.size(); i++) {
            if (remoteItem.bEquals((T) localList.get(i))) {
                bAdd = false;

                //if yes, check if it should be rewritten
                if (remoteItem.bOverWrite((T) localList.get(i))) {
                    localList.get(i).overwrite(remoteItem);                }

                //the item was found, break the cycle
                break;
            }
        }

        if (bAdd) {
            localList.add(remoteItem);
        }
    }



    private void mergeRemoteList(ArrayList<T> remoteList) {
        for (int i=0; i<remoteList.size(); i++) {
            addOrRewrite(remoteList.get(i));
        }
    }

    private void processDownload(boolean res) {
        int iPocetLocalPred = localList.size();
        Log.d(TAG, "ENTER: processDownload: " + res);

        ArrayList<T> remoteList = new ArrayList<>();

        readFile(sFilename+"tmp", remoteList);
        int iPocetRemotePred = remoteList.size();

        Log.d(TAG, "Lokalni pocet= " + localList.size() + " ftp pocet= " +remoteList.size());

        //merge the remote list into the local one
        mergeRemoteList(remoteList);

        Log.d(TAG, "Pocet po spojeni " + localList.size() + " ftp pocet= " +remoteList.size());

        if (iPocetLocalPred != localList.size()) {
            //pribyla nejaka indicie  =>
            //zapiseme soubor
            writeFile();

            /*
                TODO a jeste musime zavolat callback, ktery aktualizuje to, co je potreba - napr. obrazovku a prekresli napr. seznam indicii, pokud je zrovna otevreny
                TODO resp hlavni stranku a pripadne zobrazi nove zpravy, ktere se zobrazi po ziskani indicii ...
            */
        }

        if (iPocetRemotePred != localList.size()) {
            //lokalne mame nejaky indicie navic, takze musime uploadovat
            upload();
        }

        Log.d(TAG, "LEAVE: processDownload");
    }

    private void upload() {
        Log.d(TAG, "ENTER: upload");
        new UploadFTPFileTask(ctx).execute(sFilename);
    }

        public void readFile () {
            localList = new ArrayList<T>();
            readFile(sFilename, localList);

            Log.d(TAG, "LEAVE: lokalni seznam - nacteno: " + localList.size());
        }


        private void readFile (String sFilename, ArrayList<T> arrayList) {
        Log.d(TAG, "ENTER: readFile: "+sFilename);
        try {
            InputStream inputStream =  ctx.openFileInput(sFilename);

            ObjectInputStream in = new ObjectInputStream(inputStream);

            int iPocet = (int) in.readInt();

            for (int i=0; i<iPocet; i++) {
                T obj = (T) in.readObject();
                arrayList.add(obj);
            }
            in.close();
        }
        catch(Exception e) {
            Log.d (TAG, "ERROR: readFile " + e.getMessage());
        }

        Log.d(TAG, "LEAVE: Ze souboru: "+sFilename+" nacteno: " + arrayList.size());
    }

    public void writeFile () {
        Log.d(TAG, "ENTER: writeFile: "+sFilename + " pocet zapisovanych " + localList.size());
        try {
            OutputStream fileOut = ctx.openFileOutput(sFilename, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeInt(localList.size());

            for (int i=0; i<localList.size(); i++) {
                out.writeObject((T) localList.get(i));
            }

            out.close();
            fileOut.close();
        }
            catch (IOException ex) {
                Okynka.zobrazOkynko(ctx, "Chyba: " + ex.getMessage());
                Log.d(TAG, "ERROR: "+"writeFile " + ex.getMessage());
        }
        Log.d(TAG, "LEAVE: writeFile");
    }
}
