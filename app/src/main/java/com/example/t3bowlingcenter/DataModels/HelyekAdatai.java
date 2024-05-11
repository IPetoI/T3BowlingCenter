package com.example.t3bowlingcenter.DataModels;

public class HelyekAdatai {

    private String melyikNap;
    private String melyikOra;
    private int szam;


    public HelyekAdatai() {
    }

    public HelyekAdatai(String melyikNap, String melyikOra, int szam) {
        this.melyikNap = melyikNap;
        this.szam = szam;
        this.melyikOra = melyikOra;
    }

    public String getMelyikNap() {
        return melyikNap;
    }

    public String getMelyikOra() {
        return melyikOra;
    }

    public int getSzam() {
        return szam;
    }
}
