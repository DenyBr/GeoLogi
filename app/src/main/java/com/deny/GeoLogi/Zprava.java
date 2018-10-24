package com.deny.GeoLogi;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;

public class Zprava implements Serializable {
    private final String TAG = "Zprava";

    private boolean bPublic;
    private int iId;
    private int iOddil;
    private String sPredmet;
    private String sZprava;
    private String sLink;
    private String sZobrazitPoCase;
    private int iPoZpraveCislo;
    private double fCilovyBodLat;
    private double fCilovyBodLong;
    private String sCilovyBodPopis;
    private double fZobrazitNaLat;
    private double fZobrazitNaLong;
    private int iPocetIndicii;
    private String sIndicieZeSkupiny;
    private String sPovinneIndicie;
    private String sNezobrazovatPokudMajiIndicii;
    private String sZobrazitPriUdalosti;
    private String sProvestAkci;
    private boolean bRead;
    private boolean bZobrazeno;
    private Timestamp tsCasZobrazeni;
    private Timestamp tsCasNacteni;
    private int iBarva;
    private String sCas;


    public Zprava(boolean bPublic,
                  int iId,
                  int iOddil,
                  String sPredmet,
                  String sZprava,
                  String sLink,
                  String sZobrazitPoCase,
                  int iPoZpraveCislo,
                  double fCilovyBodLat,
                  double fCilovyBodLong,
                  String sCilovyBodPopis,
                  double fZobrazitNaLat,
                  double fZobrazitNaLong,
                  int iPocetIndicii,
                  String sIndicieZeSkupiny,
                  String sPovinneIndicie,
                  String sNezobrazovatPokudMajiIndicii,
                  String sZobrazitPriUdalosti,
                  String sProvestAkci,
                  int iBarva,
                  String sCas)
    {
        setbPublic(bPublic);
        setiId(iId);
        setiOddil(iOddil);
        setsPredmet(sPredmet);
        setsZprava(sZprava);
        setsLink(sLink);
        setsZobrazitPoCase(sZobrazitPoCase);
        setiPoZpraveCislo(iPoZpraveCislo);
        setfCilovyBodLat(fCilovyBodLat);
        setfCilovyBodLong(fCilovyBodLong);
        setsCilovyBodPopis(sCilovyBodPopis);
        setfZobrazitNaLat(fZobrazitNaLat);
        setfZobrazitNaLong(fZobrazitNaLong);
        setiPocetIndicii(iPocetIndicii);
        setsIndicieZeSkupiny(sIndicieZeSkupiny);
        setsPovinneIndicie(sPovinneIndicie);
        setsZobrazitPriUdalosti(sZobrazitPriUdalosti);
        setsProvestAkci(sProvestAkci);
        setsNezobrazovatPokudMajiIndicii(sNezobrazovatPokudMajiIndicii);
        setiBarva(iBarva);
        setsCas(sCas);
    }

    public boolean getbPublic() {return bPublic;}
    public int getiId( ) { return this.iId;}
    public int getiOddil( ) { return this.iOddil;}
    public String getsPredmet( ) { return this.sPredmet;}
    public String getsZprava( ) { return this.sZprava;}
    public String getsLink( ) { return this.sLink;}
    public String getsZobrazitPoCase( ) { return this.sZobrazitPoCase;}
    public int getiPoZpraveCislo( ) { return this.iPoZpraveCislo;}
    public double getfCilovyBodLat( ) { return this.fCilovyBodLat;}
    public double getfCilovyBodLong( ) { return this.fCilovyBodLong;}
    public double getfZobrazitNaLat( ) { return this.fZobrazitNaLat;}
    public double getfZobrazitNaLong( ) { return this.fZobrazitNaLong;}
    public int getiPocetIndicii( ) { return this.iPocetIndicii;}
    public String getsPovinneIndicie( ) { return this.sPovinneIndicie;}
    public boolean getbRead () {return this.bRead;}
    public boolean getbZobrazeno () {return this.bZobrazeno;}
    public String getsNezobrazovatPokudMajiIndicii( ) { return this.sNezobrazovatPokudMajiIndicii;}
    public String getsIndicieZeSkupiny() {return sIndicieZeSkupiny; }
    public int getiBarva() { return iBarva; }
    public String getsZobrazitPriUdalosti() { return sZobrazitPriUdalosti; }
    public String getsProvestAkci() { return sProvestAkci; }
    public String getsCas() { return sCas; }
    public Timestamp getTsCasNacteni() { return tsCasNacteni; }

    public void setTsCasNacteni(Timestamp tsCasNacteni) {this.tsCasNacteni = tsCasNacteni;}

    public void setbPublic (boolean bPublic) {this.bPublic = bPublic;}
    public void setiId( int iId) {  this.iId=iId;}
    public void setiOddil( int iOddil) {  this.iOddil=iOddil;}
    public void setsPredmet( String sPredmet) {  this.sPredmet=sPredmet;}
    public void setsZprava( String sZprava) {  this.sZprava=sZprava;}
    public void setsLink( String sLink) {  this.sLink=sLink;}
    public void setsZobrazitPoCase( String sZobrazitPoCase) {  this.sZobrazitPoCase=sZobrazitPoCase;}
    public void setiPoZpraveCislo( int iPoZpraveCislo) {  this.iPoZpraveCislo=iPoZpraveCislo;}
    public void setfCilovyBodLat( double fCilovyBodLat) {  this.fCilovyBodLat=fCilovyBodLat;}
    public void setfCilovyBodLong( double fCilovyBodLong) {  this.fCilovyBodLong=fCilovyBodLong;}
    public void setfZobrazitNaLat( double fZobrazitNaLat) {  this.fZobrazitNaLat=fZobrazitNaLat;}
    public void setfZobrazitNaLong( double fZobrazitNaLong) {  this.fZobrazitNaLong=fZobrazitNaLong;}
    public void setiPocetIndicii( int iPocetIndicii) {  this.iPocetIndicii=iPocetIndicii;}
    public void setsPovinneIndicie( String sPovinneIndicie) {  this.sPovinneIndicie=sPovinneIndicie;}
    public void setsNezobrazovatPokudMajiIndicii( String sNezobrazovatPokudMajiIndicii) {  this.sNezobrazovatPokudMajiIndicii=sNezobrazovatPokudMajiIndicii;}
    public void setbRead (boolean bRead) {
        Log.d ("TAG", "Message " + getsPredmet() + " Read: "+bRead);
        this.bRead = bRead;}
    public void setbZobrazeno (boolean bZobrazeno) {
        Log.d ("TAG", "Message " + getsPredmet() + " Zobrazeno: "+bZobrazeno);
        this.bZobrazeno = bZobrazeno;}
    public void setsIndicieZeSkupiny(String sIndicieZeSkupiny) {this.sIndicieZeSkupiny = sIndicieZeSkupiny; }
    public void setiBarva(int iBarva) { this.iBarva = iBarva; Log.d ("TAG", "Barva " + iBarva);}
    public void setsZobrazitPriUdalosti(String sZobrazitPriUdalosti) { this.sZobrazitPriUdalosti = sZobrazitPriUdalosti; }
    public void setsProvestAkci(String sProvestAkci) { this.sProvestAkci = sProvestAkci; }
    public void setsCas(String sCas) { this.sCas = sCas; }

    private void readObject(
            ObjectInputStream aInputStream
    ) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();
    }

    /**
     * This is the default implementation of writeObject.
     * Customise if necessary.
     */
    private void writeObject(
            ObjectOutputStream aOutputStream
    ) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

    public String getsCilovyBodPopis() {
        return sCilovyBodPopis;
    }

    public void setsCilovyBodPopis(String sCilovyBodPopis) {
        this.sCilovyBodPopis = sCilovyBodPopis;
    }

    public Timestamp getTsCasZobrazeni() {
        return tsCasZobrazeni;
    }

    public void setTsCasZobrazeni(Timestamp tsCasZobrazeni) {
        this.tsCasZobrazeni = tsCasZobrazeni;
    }
}

