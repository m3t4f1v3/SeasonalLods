package io.github.m3t4f1v3.seasonallods.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.m3t4f1v3.seasonallods.SeasonalReplacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

@Mixin(Biome.class)
public class MixinBiome {
    @WrapMethod(method = "getSkyColor")
    public int getSkyColor(Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getSkyColor();
        }
        return original.call();
    }

    @WrapMethod(method = "getFogColor")
    public int getFogColor(Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getFogColor();
        }
        return original.call();
    }

    @WrapMethod(method = "getGrassColor")
    public int getGrassColor(double d, double e, Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getGrassColor(d, e);
        }
        return original.call(d, e);
    }

    @WrapMethod(method = "getFoliageColor")
    public int getFoliageColor(Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getFoliageColor();
        }
        return original.call();
    }

    @WrapMethod(method = "getDryFoliageColor")
    public int getDryFoliageColor(Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getDryFoliageColor();
        }
        return original.call();
    }

    @WrapMethod(method = "getWaterColor")
    public int getWaterColor(Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getWaterColor();
        }
        return original.call();
    }

    @WrapMethod(method = "getWaterFogColor")
    public int getWaterFogColor(Operation<Integer> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getWaterFogColor();
        }
        return original.call();
    }

    @WrapMethod(method = "hasPrecipitation")
    public boolean hasPrecipitation(Operation<Boolean> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.hasPrecipitation();
        }
        return original.call();
    }

    @WrapMethod(method = "getPrecipitationAt")
    public Biome.Precipitation getPrecipitationAt(BlockPos pos, int i, Operation<Biome.Precipitation> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getPrecipitationAt(pos, i);
        }
        return original.call(pos, i);
    }

    @WrapMethod(method = "coldEnoughToSnow")
    public boolean coldEnoughToSnow(BlockPos pos, int i, Operation<Boolean> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.coldEnoughToSnow(pos, i);
        }
        return original.call(pos, i);
    }

    @WrapMethod(method = "warmEnoughToRain")
    public boolean warmEnoughToRain(BlockPos pos, int i, Operation<Boolean> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.warmEnoughToRain(pos, i);
        }
        return original.call(pos, i);
    }

    @WrapMethod(method = "shouldSnow")
    public boolean shouldSnow(LevelReader levelReader, BlockPos blockPos, Operation<Boolean> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.shouldSnow(levelReader, blockPos);
        }
        return original.call(levelReader, blockPos);
    }

    @WrapMethod(method = "getBaseTemperature")
    public float getBaseTemperature(Operation<Float> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getBaseTemperature();
        }
        return original.call();
    }

    @WrapMethod(method = "getSpecialEffects")
    public BiomeSpecialEffects getSpecialEffects(Operation<BiomeSpecialEffects> original) {
        if (SeasonalReplacement.hasReplacement((Biome)(Object)this)) {
            Biome replacedBiome = SeasonalReplacement.replaceBiomeIfPossible((Biome)(Object)this);
            return replacedBiome.getSpecialEffects();
        }
        return original.call();
    }
}