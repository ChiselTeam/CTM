package io.github.chiselteam.ctm.client.model;

import net.minecraft.core.Direction;

public record CTMFace(Direction cullForDirection, int tintIndex, String texture, UVs uvs, Quadrant rotation) {
    public enum Quadrant {R0, R90, R180, R270}

    public record UVs(float minU, float minV, float maxU, float maxV) {
    }
}
