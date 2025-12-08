package io.github.m3t4f1v3.seasonallods.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Blocks;

@Mixin(BlockColors.class)
public class MixinBlockColors {
    @Inject(
        method = "createDefault",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void overwriteBiomeLeafColors(CallbackInfoReturnable<BlockColors> cir) {
        BlockColors colors = cir.getReturnValue();
        BlockColor biomeFoliage = (state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                return BiomeColors.getAverageFoliageColor(world, pos);
            }
            return FoliageColor.get(pos.getX(), pos.getZ());
        };

        colors.register(biomeFoliage, Blocks.BIRCH_LEAVES);
        colors.register(biomeFoliage, Blocks.SPRUCE_LEAVES);

        cir.setReturnValue(colors);
    }

    // @WrapMethod(method = "getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I")
    // private int modifyGetColor(BlockState state, @Nullable Level level, @Nullable BlockPos pos, Operation<Integer> original) {
    //     return -1;
    // }

    // @WrapMethod(method = "getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I")
    // private int modifyGetColor(BlockState state, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i, Operation<Integer> original) {
    //     // return -1;
    //     return original.call(state, new BlockAndTintGetter() {
    //         @Override
    //         public float getShade(Direction direction, boolean shaded) {
    //             return 0;
    //         }

    //         @Override
    //         public int getBrightness(LightLayer type, BlockPos pos) {
    //             return 0;
    //         }

    //         @Override
    //         public LevelLightEngine getLightEngine() {
    //             return null;
    //         }

    //         @Override
    //         public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
    //             // return colorResolver.getColor(SeasonalReplacement.replaceBiomeIfPossible(Minecraft.getInstance().level.getBiome(pos).value()), 0, 0);
    //             return 0xff00ff;
    //         }

    //         @Nullable
    //         @Override
    //         public BlockEntity getBlockEntity(BlockPos pos) {
    //             return null;
    //         }

    //         @Override
    //         public BlockState getBlockState(BlockPos pos) {
    //             return state;
    //         }

    //         @Override
    //         public FluidState getFluidState(BlockPos pos) {
    //             return state.getFluidState();
    //         }

    //         @Override
    //         public int getHeight() {
    //             return 0;
    //         }

    //         @Override
    //         public int getMinY() {
    //             return 0;
    //         }
    //     }, blockPos, i);
    // }
}
