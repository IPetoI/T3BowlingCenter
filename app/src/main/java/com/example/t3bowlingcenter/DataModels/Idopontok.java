package com.example.t3bowlingcenter.DataModels;

public class Idopontok {
    private String idopontok;
    private boolean isSelected;

    public Idopontok(String idopontok, boolean isSelected) {
        this.idopontok = idopontok;
        this.isSelected = isSelected;
    }

    public String getIdopontok() {
        return idopontok;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean b) {
        isSelected = b;
    }
}
