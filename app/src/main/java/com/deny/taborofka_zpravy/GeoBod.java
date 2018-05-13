package com.deny.taborofka_zpravy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by bruzlzde on 27.2.2018.
 */

public class GeoBod implements Serializable {
    private double dLat;
    private double dLong;
    private String Popis;

    public GeoBod (double dLat, double dLong, String sPopis) {
        setdLat(dLat);
        setdLong(dLong);
        setPopis(sPopis);
    }


    public double getdLat() {
        return dLat;
    }

    public void setdLat(double dLat) {
        this.dLat = dLat;
    }

    public double getdLong() {
        return dLong;
    }

    public void setdLong(double dLong) {
        this.dLong = dLong;
    }

    public String getPopis() {
        return Popis;
    }

    public void setPopis(String popis) {
        Popis = popis;
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
