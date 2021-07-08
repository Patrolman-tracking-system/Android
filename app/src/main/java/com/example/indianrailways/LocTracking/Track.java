package com.example.indianrailways.LocTracking;

public class Track {
    private String lat, long1;
    private String id;
    private String sped, devCount;
    private int objCount;



    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getInit() {
        return init;
    }

    public void setInit(String init) {
        this.init = init;
    }

    private String current, init;

    public int getObjCount() {
        return objCount;
    }

    public void setObjCount(int objCount) {
        this.objCount = objCount;
    }

    public String getdevCount() {
        return devCount;
    }

    public void setdevCount(String devCount) {
        this.devCount = devCount;
    }

    public String getSped() {
        return sped;
    }

    public void setSped(String sped) {
        this.sped = sped;
    }

    public Track() {
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLong1() {
        return long1;
    }

    public void setLong1(String long1) {
        this.long1 = long1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
