package com.deny.GeoLogi;

/**
 * Created by bruzlzde on 22.3.2018.
 */

public class Hra {
    private String idHra;
    private String sHra;
    private String sIdWorkseet;
    private String sNastenka;
    private boolean bVerejna;


    public Hra(String idHra, String sHra, String sIdWorkseet, String sNastenka, boolean bVerejna) {
        this.sHra = sHra;
        this.sIdWorkseet = sIdWorkseet;
        this.sNastenka = sNastenka;
        this.bVerejna = bVerejna;
        this.idHra = idHra;

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


}
