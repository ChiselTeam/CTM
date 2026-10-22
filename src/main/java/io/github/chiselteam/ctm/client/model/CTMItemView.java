package io.github.chiselteam.ctm.client.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public record CTMItemView(BlockState state) implements BlockAndTintGetter {

    @Override
    public @NotNull BlockState getBlockState(BlockPos pos) {
        return pos.equals(BlockPos.ZERO) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getMinBuildHeight() {
        return 0;
    }

    @Override
    public float getShade(@NotNull Direction direction, boolean shade) {
        return 1;
    }

    @Override
    public @NotNull LevelLightEngine getLightEngine() {
        throw new UnsupportedOperationException("Inventory texture selection has no light engine");
    }

    @Override
    public int getBlockTint(@NotNull BlockPos pos, @NotNull ColorResolver resolver) {
        return -1;
    }
}
