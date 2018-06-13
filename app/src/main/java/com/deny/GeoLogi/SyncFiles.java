package com.deny.GeoLogi;

import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ntp.TimeStamp;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bruzlzde on 12.3.2018.
 */

public class SyncFiles<T extends Serializable> {
    private final String TAG = "SyncFiles";

    private String sFilename;
    private Timer timer;
    private Context ctx;

    private ArrayList<T> localList;
    private ArrayList<T> remoteList;
    private ArrayList<T> mergeList;


    public SyncFiles(Context ctx, String sFileName, int iPerioda){
        setsFilename(sFileName);
        setCtx(ctx);

        timer = new Timer();
        timer.schedule(
                    new TimerTask() {

                    @Override
                    public void run() {
                        syncFileNow();
                    }
                }, 1, iPerioda);
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
        Log.d(TAG, "Synchronizace " + sFilename);

        new CheckFTPFileSizeAndDateTask(ctx, new AsyncResultFTPCheckSizeAndDate() {
            @Override
            public void onResult(String res) {
                processDateAndSize(res);
            }
        }).execute(sFilename);
    }

    private void processDateAndSize(String res) {
        //tato metoda bude zavolana potom, co se zjisti vlastnosti vzdaleneho souboru
        //ted zjistime velikost a datum verze na zarizeni

        File localFile = new File (sFilename);

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
    }

    private void processDownload(boolean res) {
        Log.d(TAG, "Vysledek downloadu: " + res);

        ArrayList<T> localList = new ArrayList<>();
        ArrayList<T> remoteList = new ArrayList<>();

        readFile(sFilename, localList);
        readFile(sFilename+"tmp", remoteList);

        Log.d(TAG, "Lokalni pocet= " + localList.size() + " ftp pocet= " +remoteList.size());

        localList.addAll(remoteList);

        Log.d(TAG, "Pocet po spojeni " + localList.size() + " ftp pocet= " +remoteList.size());
    }

    private void upload() {

    }

    private void readFile (String sFilename, ArrayList<T> arrayList) {
        try {
            arrayList = new ArrayList<T>();
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

        }
    }
}
