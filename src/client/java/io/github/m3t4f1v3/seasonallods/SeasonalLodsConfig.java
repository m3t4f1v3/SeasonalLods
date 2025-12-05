package io.github.m3t4f1v3.seasonallods;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SeasonalLodsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/seasonallods.json");

    public boolean reloadOnSeasonChange = true;

    public static SeasonalLodsConfig INSTANCE = load();

    private static SeasonalLodsConfig load() {
        try {
            if (!FILE.exists()) {
                SeasonalLodsConfig cfg = new SeasonalLodsConfig();
                cfg.save();
                return cfg;
            }
            try (FileReader reader = new FileReader(FILE)) {
                return GSON.fromJson(reader, SeasonalLodsConfig.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new SeasonalLodsConfig();
        }
    }

    public void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
