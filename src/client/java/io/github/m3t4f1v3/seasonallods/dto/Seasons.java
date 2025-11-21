package io.github.m3t4f1v3.seasonallods.dto;

import java.util.List;

public class Seasons {
    private String WINTER;
    private String SPRING;
    private String SUMMER;
    private List<String> FALL;

    public String getWINTER() { return WINTER; }
    public void setWINTER(String WINTER) { this.WINTER = WINTER; }

    public String getSPRING() { return SPRING; }
    public void setSPRING(String SPRING) { this.SPRING = SPRING; }

    public String getSUMMER() { return SUMMER; }
    public void setSUMMER(String SUMMER) { this.SUMMER = SUMMER; }

    public List<String> getFALL() { return FALL; }
    public void setFALL(List<String> FALL) { this.FALL = FALL; }

    public boolean isEmpty() {
        return WINTER == null && SPRING == null && SUMMER == null && (FALL == null || FALL.isEmpty());
    }
}