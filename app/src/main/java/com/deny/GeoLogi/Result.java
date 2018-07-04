package com.deny.GeoLogi;

public class Result {
    private String sId;
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


    Result(String sId, String sHints, String sPoints) {
        sId = sId;
        sHints = sHints;
        sPoints = sPoints;
    }
}
