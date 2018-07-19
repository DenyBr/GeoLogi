package com.deny.GeoLogi;

public class Result {

    private String sId;

    private String sIdUzivatele;
    private String sHints;
    private String sHintsFailed;
    private String sPoints;


    public String getsId() {
        return sId;
    }

    public String getsHints() {
        return sHints;
    }

    public String getsHintsFailed() { return sHintsFailed; }

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

    public void setsHintsFailed(String sHintsFailed) { this.sHintsFailed = sHintsFailed; }

    public void setsPoints(String sPoints) {
        this.sPoints = sPoints;
    }

    public void setsIdUzivatele(String sIdUzivatele) {
        this.sIdUzivatele = sIdUzivatele;
    }


    Result(String sId, String sIdUzivatele, String sHints, String sHintsFailed, String sPoints) {
        setsId(sId);
        setsHints(sHints);
        setsHintsFailed(sHintsFailed);
        setsPoints(sPoints);
        setsIdUzivatele(sIdUzivatele);
    }
}
