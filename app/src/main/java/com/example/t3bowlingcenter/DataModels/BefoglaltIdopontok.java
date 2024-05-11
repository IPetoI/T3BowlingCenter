package com.example.t3bowlingcenter.DataModels;

public class BefoglaltIdopontok {
    private String idopontok;
    private int helyek;

    public BefoglaltIdopontok(String idopontok, int helyek) {
        this.idopontok = idopontok;
        this.helyek = helyek;
    }

    public String getIdopontok() {
        return idopontok;
    }

    public int getHelyek() {
        return helyek;
    }
}
