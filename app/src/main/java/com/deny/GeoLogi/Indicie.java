package com.deny.GeoLogi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by bruzlzde on 23.2.2018.
 */

public class Indicie implements Serializable {
    private ArrayList<String> sTexty;

    public Indicie(ArrayList<String> sTexty) {
        this.setsTexty(sTexty);
    }

    public boolean jeToOno(String sInd) {
        for (int i=0; i<sTexty.size(); i++) {
            if (sInd.toLowerCase().equals(sTexty.get(i).toLowerCase())) return true;
        }

        return false;
    }

    public void setsTexty(ArrayList <String> sTexty) {
        this.sTexty = sTexty;
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
}