package com.example.indianrailways.LocTracking;

public class Track {
    private String lat, long1;
    private String id;
    private String sped, speed2;

    public String getSpeed2() {
        return speed2;
    }

    public void setSpeed2(String speed2) {
        this.speed2 = speed2;
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
