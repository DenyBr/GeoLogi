package com.deny.GeoLogi;

import android.util.Log;

/**
 * Created by bruzlzde on 22.3.2018.
 *
 *  Objekt drzici informace o hre
 */

public class Hra {
    private final String TAG = "Hra";

    private String idHra;
    private String sHra;
    private String sIdWorkseet;
    private String sNastenka;
    private boolean bVerejna;
    private boolean bNaCas;
    private boolean bSdileniPolohy;
    private String sFTP;
    private String sFTPUser;
    private String sFTPHeslo;


    public Hra(String idHra, String sHra, String sIdWorkseet, String sNastenka, boolean bVerejna, boolean bNaCas, boolean bSdileniPolohy, String sFTP, String sFTPUser, String sFTPHeslo) {
        this.sHra = sHra;
        this.sIdWorkseet = sIdWorkseet;
        this.sNastenka = sNastenka;
        this.bVerejna = bVerejna;
        this.idHra = idHra;
        this.bNaCas = bNaCas;
        this.bSdileniPolohy = bSdileniPolohy;
        this.sFTP = sFTP;
        this.sFTPUser = sFTPUser;
        this.sFTPHeslo = sFTPHeslo;

        Log.d(TAG, "idHra: "+this.idHra + "Hra: " + this.sHra + "SdileniPolohy: "+bSdileniPolohy);
    }

    public String getsHra() {
        return sHra;
    }

    public String getsIdWorkseet() { return sIdWorkseet; }

    public String getsNastenka() {
        return sNastenka;
    }

    public String getIdHra() {
        return idHra;
    }

    public boolean isbVerejna() {
        return bVerejna;
    }

    public boolean isbNaCas() {
        return bNaCas;
    }

    public String getsFTP() {
        return sFTP;
    }

    public String getsFTPUser() {
        return sFTPUser;
    }
    public String getsFTPHeslo() {
        return sFTPHeslo;
    }

    public boolean isbSdileniPolohy() {
        return bSdileniPolohy;
    }
}
