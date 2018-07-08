package com.deny.GeoLogi;

public class Result {

    private String sId;

    private String sIdUzivatele;
    private String sHints;
    private String sPoints;

    public String getsId() {
        return sId;
    }

    public String getsHints() {
        return sHints;
    }

    public String getsPoints() {
        return sPoints;
    }

    public String getsIdUzivatele() {
        return sIdUzivatele;
    }


    public void setsId(String sId) {
        this.sId = sId;
    }

    public void setsHints(String sHints) {
        this.sHints = sHints;
    }

    public void setsPoints(String sPoints) {
        this.sPoints = sPoints;
    }

    public void setsIdUzivatele(String sIdUzivatele) {
        this.sIdUzivatele = sIdUzivatele;
    }

    Result(String sId, String sIdUzivatele, String sHints, String sPoints) {
        setsId(sId);
        setsHints(sHints);
        setsPoints(sPoints);
        setsIdUzivatele(sIdUzivatele);
    }
}
