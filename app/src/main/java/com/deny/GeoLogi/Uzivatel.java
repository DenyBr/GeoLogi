package com.deny.GeoLogi;

/**
 * Created by bruzlzde on 23.2.2018.
 */

public class Uzivatel {
    private int iId;
    private String sNazev;
    private String sHeslo;
    private boolean bRoot;

    public Uzivatel(int iId, String sNazev, String sHeslo, boolean bRoot) {
        this.setiId(iId);
        this.setsNazev(sNazev);
        this.setsHeslo(sHeslo);
        this.setbRoot(bRoot);
    }

    public int getiId( ) { return this.iId;}
    public String getsNazev( ) { return this.sNazev;}
    public String getsHeslo( ) { return this.sHeslo;}
    public boolean isbRoot() {return bRoot;}

    public void setiId( int iId) {  this.iId=iId;}
    public void setsNazev( String sNazev) {  this.sNazev=sNazev;}
    public void setsHeslo( String sHeslo) {  this.sHeslo=sHeslo;}
    public void setbRoot(boolean bRoot) { this.bRoot = bRoot; }
}