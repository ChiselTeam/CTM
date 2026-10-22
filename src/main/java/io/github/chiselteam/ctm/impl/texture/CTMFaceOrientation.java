package io.github.chiselteam.ctm.impl.texture;

import io.github.chiselteam.ctm.client.model.CTMModelState;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public final class CTMFaceOrientation {
    public static final CTMFaceOrientation IDENTITY = new CTMFaceOrientation(new CTMModelState() {});
    private static final Matrix4fc IDENTITY_UV = new Matrix4f();

    private final Map<Direction, Direction[]> corners = new EnumMap<>(Direction.class);
    private final Map<Direction, Direction[]> textures = new EnumMap<>(Direction.class);

    public static CTMFaceOrientation of(CTMModelState state) {
        if (state.transformation().isIdentity()) {
            boolean identityUVs = true;
            for (var face : Direction.values()) {
                if (!state.faceTransformation(face).equals(IDENTITY_UV, 0.0F)) {
                    identityUVs = false;
                    break;
                }
            }
            if (identityUVs) return IDENTITY;
        }
        return new CTMFaceOrientation(state);
    }

    public CTMFaceOrientation(CTMModelState state) {
        for (var localFace : Direction.values()) {
            var worldFace = Direction.rotate(state.transformation().getMatrix(), localFace);
            var local = localDirections(localFace);
            var world = new Direction[4];
            for (int i = 0; i < 4; i++) {
                world[i] = Direction.rotate(state.transformation().getMatrix(), local[i]);
            }
            corners.put(worldFace, world);

            var top = state.faceTransformation(localFace).transformDirection(new Vector3f(0, -1, 0));
            var right = state.faceTransformation(localFace).transformDirection(new Vector3f(1, 0, 0));
            var textureTop = uvDirection(world, top);
            var textureRight = uvDirection(world, right);
            textures.put(worldFace, new Direction[]{textureTop, textureRight, textureTop.getOpposite(), textureRight.getOpposite()});
        }
    }

    public static Direction[] localDirections(Direction face) {
        return switch (face) {
            case DOWN -> new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
            case UP -> new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            case NORTH -> new Direction[]{Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST};
            case SOUTH -> new Direction[]{Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST};
            case WEST -> new Direction[]{Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH};
            case EAST -> new Direction[]{Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH};
        };
    }

    public Direction[] cornerDirections(Direction face) {
        return corners.get(face).clone();
    }

    public Direction[] textureDirections(Direction face) {
        return textures.get(face).clone();
    }

    public Direction cornerDirection(Direction face, int corner) {
        return corners.get(face)[corner];
    }

    public Direction textureDirection(Direction face, int index) {
        return textures.get(face)[index];
    }

    public int textureIndex(Direction face, Direction direction) {
        Direction[] directions = textures.get(face);
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == direction) return i;
        }
        throw new IllegalArgumentException(direction + " is not in the texture plane of " + face);
    }

    private static Direction uvDirection(Direction[] world, Vector3f uv) {
        return Math.abs(uv.x) > Math.abs(uv.y)
                ? world[uv.x > 0 ? 1 : 3]
                : world[uv.y > 0 ? 2 : 0];
    }
}
