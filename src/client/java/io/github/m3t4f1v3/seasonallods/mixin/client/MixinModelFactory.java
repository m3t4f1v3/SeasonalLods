package io.github.m3t4f1v3.seasonallods.mixin.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.m3t4f1v3.seasonallods.SeasonalLodsConfig;
import io.github.m3t4f1v3.seasonallods.SeasonalReplacement;
import me.cortex.voxy.client.core.model.ModelFactory;

@Mixin(ModelFactory.class)
public class MixinModelFactory {
	@WrapMethod(method = "captureColourConstant")
	private static int modifyColourConstant(BlockColor colorProvider, BlockState state, Biome biome, Operation<Integer> original) {
		if (SeasonalLodsConfig.INSTANCE.overrideSpringColors && SeasonalReplacement.currentSeason.equalsIgnoreCase("SPRING") && state.getBlock() instanceof LeavesBlock) {
			// SeasonalLods.pluginLogger.info(SeasonsAPI.getInstance().getReplacementSeasonBiome(Biome.PLAINS, Season.SPRING).getFoliageColorsHex() (this is in gbr format, so convert to rgb);
			return 0xe1b8d0;
		}
		return original.call(colorProvider, state, SeasonalReplacement.replaceBiomeIfPossible(biome));
	}
}