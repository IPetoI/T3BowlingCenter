package com.example.t3bowlingcenter.DataModels;

public class FelhasznaloFoglaltIdopontjai {
    private String szemelyekSzama;
    private String nap;
    private String ora;
    private String ar;
    private String foglalasIdopontId;


    public FelhasznaloFoglaltIdopontjai() {}

    public FelhasznaloFoglaltIdopontjai(String szemelyekSzama, String nap, String ora, String ar, String foglalasIdopontId) {
        this.szemelyekSzama = szemelyekSzama;
        this.nap = nap;
        this.ar = ar;
        this.ora = ora;
        this.foglalasIdopontId = foglalasIdopontId;
    }

    public String getSzemelyekSzama() {
        return szemelyekSzama;
    }
    public String getNap() {
        return nap;
    }
    public String getOra() {
        return ora;
    }
    public String getAr() {
        return ar;
    }
    public String getFoglalasIdopontId() {
        return foglalasIdopontId;
    }
}
