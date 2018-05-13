package com.deny.taborofka_zpravy;

/**
 * Created by bruzlzde on 23.2.2018.
 */

public class Oddil {
    int iId;
    String sNazev;
    String sHeslo;

    public Oddil(int iId, String sNazev, String sHeslo) {
        this.setiId(iId);
        this.setsNazev(sNazev);
        this.setsHeslo(sHeslo);
    }

    public int getiId( ) { return this.iId;}
    public String getsNazev( ) { return this.sNazev;}
    public String getsHeslo( ) { return this.sHeslo;}

    public void setiId( int iId) {  this.iId=iId;}
    public void setsNazev( String sNazev) {  this.sNazev=sNazev;}
    public void setsHeslo( String sHeslo) {  this.sHeslo=sHeslo;}
}