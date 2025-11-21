package io.github.m3t4f1v3.seasonallods.dto;

import java.util.Map;

public class BiomeReplacement {
    private Seasons SEASONS;
    private Map<String, Seasons> SUB_SEASONS; // key = SubSeason name

    public Seasons getSEASONS() { return SEASONS; }
    public void setSEASONS(Seasons SEASONS) { this.SEASONS = SEASONS; }

    public Map<String, Seasons> getSUB_SEASONS() { return SUB_SEASONS; }
    public void setSUB_SEASONS(Map<String, Seasons> SUB_SEASONS) { this.SUB_SEASONS = SUB_SEASONS; }

    public boolean isEmpty() {
        return (SEASONS == null || SEASONS.isEmpty()) && (SUB_SEASONS == null || SUB_SEASONS.isEmpty());
    }
}