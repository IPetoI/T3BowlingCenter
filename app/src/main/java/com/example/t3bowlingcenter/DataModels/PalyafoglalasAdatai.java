package com.example.t3bowlingcenter.DataModels;

public class PalyafoglalasAdatai {

    private String felhasznalo_neve;
    private String felhasznalo_email;
    private String felhasznalo_telefonszam;
    private int hanyan_jonnek;
    private String melyik_nap;
    private String melyik_ora;
    private String ar;
    private String foglalas_id;


    public PalyafoglalasAdatai() {
    }

    public PalyafoglalasAdatai(String felhasznaloNeve, String felhasznaloEmail,
                               String felhasznaloTel, int felhasznaloHanyan, String melyikNap, String melyikOra, String ar, String foglalasId) {
        this.felhasznalo_neve = felhasznaloNeve;
        this.felhasznalo_email = felhasznaloEmail;
        this.felhasznalo_telefonszam = felhasznaloTel;
        this.hanyan_jonnek = felhasznaloHanyan;
        this.melyik_nap = melyikNap;
        this.ar = ar;
        this.melyik_ora = melyikOra;
        this.foglalas_id = foglalasId;
    }

    public String getFelhasznalo_neve() {
        return felhasznalo_neve;
    }

    public String getFelhasznalo_email() {
        return felhasznalo_email;
    }

    public String getFelhasznalo_telefonszam() {
        return felhasznalo_telefonszam;
    }

    public int getHanyan_jonnek() {
        return hanyan_jonnek;
    }

    public String getMelyik_nap() {
        return melyik_nap;
    }

    public String getMelyik_ora() {
        return melyik_ora;
    }

    public String getAr() {
        return ar;
    }

    public String getFoglalas_id() {
        return foglalas_id;
    }
}
