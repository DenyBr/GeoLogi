package com.deny.taborofka_zpravy;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bruzlzde on 12.3.2018.
 */

public class SyncFiles {
    private String sFilename;
    private Timer timer;
    private Context ctx;

    private ArrayList<Object> localList;
    private ArrayList<Object> remoteList;
    private ArrayList<Object> mergeList;


    public SyncFiles(Context ctx, String sFileName, int iPerioda){
        setsFilename(sFileName);
        setCtx(ctx);

        timer = new Timer();
        timer.schedule(
                    new TimerTask() {

                    @Override
                    public void run() {
                        syncFileInit();
                    }
                }, 1, iPerioda);
    }

     public String getsFilename() {
        return sFilename;
    }

    public void setsFilename(String sFilename) {
        this.sFilename = sFilename;
    }

    private void syncFileInit() {
        //Okynka.zobrazOkynko(ctx, "Joooo");


    }



    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }
}
