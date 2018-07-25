package com.deny.GeoLogi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;

import static java.lang.Math.abs;

/**
 * Created by bruzlzde on 27.2.2018.
 *
 * Interni format bodu GPS v decimalnim formatu.
 * Pouziva se pro zobrazovani na mape a detekci navstiveni daneho ciloveho bodu
 *
 */

public class GeoBod implements Serializable, OverWriter<GeoBod> {
    private double dLat;
    private double dLong;
    private String sPopis;
    private boolean bViditelny;
    private Timestamp time = new Timestamp(System.currentTimeMillis());

    public GeoBod (double dLat, double dLong, String sPopis, boolean bViditelny) {
        setdLat(dLat);
        setdLong(dLong);
        setPopis(sPopis);
        setbViditelny(bViditelny);
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
        return sPopis;
    }

    public Timestamp getTime() {return time;
    }
    public void setPopis(String popis) {
        sPopis = popis;
    }

    public boolean getbViditelny() {
        return bViditelny;
    }

    public void setbViditelny(boolean bViditelny) {
        this.bViditelny = bViditelny;
    }

    private void setTime(Timestamp time) {
        this.time = time;
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
    public boolean bOverWrite(GeoBod by) {
        return (((abs(by.getdLat() - getdLat()) < 0.00001)
                    && (abs(by.getdLong() - getdLong()) < 0.00001)) &&
                (by.getTime().before(time) || (getPopis().equals("")&&!by.getPopis().equals(""))));
    }

    @Override
    public void overwrite(GeoBod by) {
        if (by.getbViditelny()) setbViditelny(true);
        if (!by.getPopis().equals("")) setPopis(by.getPopis());
        if (by.getTime().before(time)) setTime(by.getTime());
    }

    @Override
    public boolean bEquals(GeoBod to) {
        return ((abs(to.getdLat() - getdLat()) < 0.000001) && (abs(to.getdLong() - getdLong()) < 0.000001));
    }

    @Override
    public GeoBod copy() {
        return new GeoBod(dLat, dLong,  sPopis, bViditelny);
    }
}
