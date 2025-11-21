package io.github.m3t4f1v3.seasonallods;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.m3t4f1v3.seasonallods.dto.BiomeReplacement;
import io.github.m3t4f1v3.seasonallods.dto.Seasons;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class SeasonalReplacement {
    public static Map<String, BiomeReplacement> biomeReplacements = new ConcurrentHashMap<>();
    public static String currentSeason = "DISABLED";
    public static Integer currentSubSeasonPhase = -1;

    public static Biome replaceBiomeIfPossible(Biome biome) {
        ResourceLocation biomeKey = Minecraft.getInstance().level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getKey(biome);

        String biomeIdString = biomeKey.toString();

        if (biomeKey.getNamespace().equalsIgnoreCase("realisticseasons")) {
            String originalBiomeId = backtrackToOriginal(biomeIdString);
            if (originalBiomeId != null) biomeIdString = originalBiomeId;
        }

        BiomeReplacement replacement = biomeReplacements.get(biomeIdString);
        if (replacement == null) return biome;

        Seasons seasonData;

        if (currentSubSeasonPhase != null && replacement.getSUB_SEASONS() != null) {
            for (Map.Entry<String, Seasons> subEntry : replacement.getSUB_SEASONS().entrySet()) {
                int phase = parseSubSeasonPhase(subEntry.getKey());
                if (phase == currentSubSeasonPhase || (phase == 3 && currentSubSeasonPhase > 3)) {
                    seasonData = subEntry.getValue();
                    Biome result = getSeasonalBiome(seasonData, currentSeason);
                    // System.out.println("Sub-season biome replacement check for " + biomeIdString + " in sub-season " + subEntry.getKey() + " for season " + currentSeason + " returned " + (result != null ? biomeKey.toString() : "null"));
                    if (result != null) return result;
                }
            }
        }

        seasonData = replacement.getSEASONS();
        if (seasonData != null) {
            Biome result = getSeasonalBiome(seasonData, currentSeason);
            if (result != null) return result;
        }

        return biome;
    }

    private static Biome getSeasonalBiome(Seasons seasonData, String seasonName) {
        String replacementId = null;
        switch (seasonName.toUpperCase()) {
            case "WINTER" -> replacementId = seasonData.getWINTER();
            case "SPRING" -> replacementId = seasonData.getSPRING();
            case "SUMMER" -> replacementId = seasonData.getSUMMER();
            case "FALL" -> {
                if (seasonData.getFALL() != null && !seasonData.getFALL().isEmpty()) {
                    // TODO: figure out how to pick among multiple fall replacements, need to check the realisticseasons logic
                    replacementId = seasonData.getFALL().get(0);
                }
            }
        }

        if (replacementId != null) {
            return Minecraft.getInstance().level.registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getValue(ResourceLocation.tryParse(replacementId));
        }

        return null;
    }

    private static String backtrackToOriginal(String rsBiomeId) {
        for (Map.Entry<String, BiomeReplacement> entry : biomeReplacements.entrySet()) {
            BiomeReplacement br = entry.getValue();
            Seasons seasons = br.getSEASONS();
            if (seasons != null) {
                if (seasons.getFALL() != null && seasons.getFALL().contains(rsBiomeId)) return entry.getKey();
                if (rsBiomeId.equals(seasons.getWINTER()) || rsBiomeId.equals(seasons.getSPRING()) || rsBiomeId.equals(seasons.getSUMMER())) {
                    return entry.getKey();
                }
            }

            if (br.getSUB_SEASONS() != null) {
                for (Seasons subSeason : br.getSUB_SEASONS().values()) {
                    if (subSeason.getFALL() != null && subSeason.getFALL().contains(rsBiomeId)) return entry.getKey();
                    if (rsBiomeId.equals(subSeason.getWINTER()) || rsBiomeId.equals(subSeason.getSPRING()) || rsBiomeId.equals(subSeason.getSUMMER())) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    private static int parseSubSeasonPhase(String subSeasonName) {
        return switch (subSeasonName.toUpperCase()) {
            case "START" -> 0;
            case "EARLY" -> 1;
            case "MIDDLE" -> 2;
            case "LATE" -> 3;
            default -> throw new IllegalArgumentException("Unknown sub-season phase: " + subSeasonName);
        };
    }
}