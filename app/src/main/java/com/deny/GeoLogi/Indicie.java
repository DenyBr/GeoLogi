package com.deny.GeoLogi;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.ArrayList;

/**
 * Created by bruzlzde on 23.2.2018.
 *
 *  objekt drzici informace o jedne indicii - indicie muze mit az pet tvaru
 */

public class Indicie implements Serializable, OverWriter<Indicie> {
    private int iPlatnaPo;
    private String sGroup;
    private ArrayList<String> sTexty;
    private Timestamp time = new Timestamp(System.currentTimeMillis());
    private final static String TAG = "Indicie";

    public Indicie(int iPlatnaPo, String sGroup, ArrayList<String> sTexty) {
        setiPlatnaPo(iPlatnaPo);
        setsGroup(sGroup);
        setsTexty(sTexty);
    }

    //porovnani
    public boolean jeToOno(String sInd) {
        String sIndBezHacku=Normalizer.normalize(sInd, Normalizer.Form.NFD);
        sIndBezHacku = sIndBezHacku.replaceAll("[^\\p{ASCII}]", "");

        for (int i=0; i<sTexty.size(); i++) {
            String sHledanaBezHacku = Normalizer.normalize(sTexty.get(i), Normalizer.Form.NFD);
            sHledanaBezHacku = sHledanaBezHacku.replaceAll("[^\\p{ASCII}]", "");

            if (sIndBezHacku.toLowerCase().equals(sHledanaBezHacku.toLowerCase())) return true;
        }
        return false;
    }

    public void setsTexty(ArrayList <String> sTexty) {
        this.sTexty = sTexty;
    }
    public void setTime(Timestamp time) {
        this.time = time;
    }

    public ArrayList<String> getsTexty() {
        return sTexty;
    }

    private void readObject(
            ObjectInputStream aInputStream
    ) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();
    }

    private void writeObject(
            ObjectOutputStream aOutputStream
    ) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

    @Override
    public boolean bEquals (Indicie to) {
        return jeToOno(to.sTexty.get(0));
    }

    public boolean bOverWrite(Indicie by) {


        if ( by.getsGroup().equals(sGroup) && jeToOno(by.sTexty.get(0)) &&
            time.after(by.time)) {
            Log.d(TAG, "Overwite : " + time.toString() + " by " + by.time.toString());
            return true;
        }

        return false;
    }

    @Override
    public void overwrite (Indicie by) {
        sTexty = by.sTexty;
        time = by.time;
    }

    public String getsGroup() {
        return sGroup;
    }

    public void setsGroup(String sGroup) {
        this.sGroup = sGroup;
    }

    public Timestamp getTime() { return time; }

    public int getiPlatnaPo() {
        return iPlatnaPo;
    }

    public void setiPlatnaPo(int iPlatnaPo) {
        this.iPlatnaPo = iPlatnaPo;
    }

    @Override
    public Indicie copy() {
        return new Indicie(iPlatnaPo, sGroup, sTexty);
    }
}