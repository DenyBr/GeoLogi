package com.deny.GeoLogi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by bruzlzde on 23.2.2018.
 *
 *  objekt drzici informace o jedne indicii - indicie muze mit az pet tvaru
 */

public class IndicieNeplatna implements Serializable, OverWriter<IndicieNeplatna> {
    private String sIndicie;
    private Timestamp time = new Timestamp(System.currentTimeMillis());

    public Timestamp getTime() {
        return time;
    }

    public IndicieNeplatna(String sIndicie) {
        setsIndicie(sIndicie);
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
    public boolean bEquals (IndicieNeplatna to) {
        return to.getsIndicie().equals(sIndicie);
    }

    public boolean bOverWrite(IndicieNeplatna by) {
        return by.getTime().before(getTime());
    }

    @Override
    public void overwrite (IndicieNeplatna by) {
        sIndicie = by.sIndicie;
        time = by.time;
    }

    public String getsIndicie() {
        return sIndicie;
    }

    private void setsIndicie(String sIndicie) {
        this.sIndicie = sIndicie;
    }

    @Override
    public IndicieNeplatna copy() {
        return new IndicieNeplatna(sIndicie);
    }
}