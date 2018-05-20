package com.deny.GeoLogi;

/**
 * Created by bruzlzde on 22.3.2018.
 */

public class Hra {
    private String sHra;
    private String sIdWorkseet;
    private String sNastenka;

    public Hra(String sHra, String sIdWorkseet, String sNastenka) {
        this.sHra = sHra;
        this.sIdWorkseet = sIdWorkseet;
        this.sNastenka = sNastenka;
    }

    public String getsHra() {
        return sHra;
    }

    public String getsIdWorkseet() { return sIdWorkseet; }

    public String getsNastenka() {
        return sNastenka;
    }
}
