package com.deny.GeoLogi;

/**
 * Created by bruzlzde on 23.2.2018.
 */

public class Uzivatel {
    private int iId;
    private String sNazev;
    private String sHeslo;
    private boolean bRoot;
    private boolean bSdiletPolohu;

    public Uzivatel(int iId, String sNazev, String sHeslo, boolean bRoot, boolean bSdiletPolohu) {
        this.setiId(iId);
        this.setsNazev(sNazev);
        this.setsHeslo(sHeslo);
        this.setbRoot(bRoot);
        this.setbSdiletPolohu(bSdiletPolohu);
    }

    public int getiId( ) { return this.iId;}
    public String getsNazev( ) { return this.sNazev;}
    public String getsHeslo( ) { return this.sHeslo;}
    public boolean isbRoot() {return bRoot;}
    public boolean isbSdiletPolohu() {return bSdiletPolohu;}

    public void setiId( int iId) {  this.iId=iId;}
    public void setsNazev( String sNazev) {  this.sNazev=sNazev;}
    public void setsHeslo( String sHeslo) {  this.sHeslo=sHeslo;}
    public void setbRoot(boolean bRoot) { this.bRoot = bRoot; }
    public void setbSdiletPolohu(boolean bSdiletPolohu) {this.bSdiletPolohu = bSdiletPolohu;}
}