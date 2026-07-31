package io.github.chiselteam.ctm.api.geometry;

import net.minecraft.core.Direction;

/// Connectivity key for EDGES/EDGES\_FULL kinds.
/// Each face is packed into an int of flags:
/// bit 0 = top
/// bit 1 = right
/// bit 2 = bottom
/// bit 3 = left
/// bit 4 = topLeft
/// bit 5 = topRight
/// bit 6 = bottomRight
/// bit 7 = bottomLeft
/// bit 8 = obscured
public record EdgesCTMKey(int down, int up, int north, int south, int west, int east) implements CTMGeometryKey {

    public static final int TOP = 1;
    public static final int RIGHT = 1 << 1;
    public static final int BOTTOM = 1 << 2;
    public static final int LEFT = 1 << 3;

    public static final int TOP_LEFT = 1 << 4;
    public static final int TOP_RIGHT = 1 << 5;
    public static final int BOTTOM_RIGHT = 1 << 6;
    public static final int BOTTOM_LEFT = 1 << 7;

    public static final int OBSCURED = 1 << 8;

    public int packedFace(Direction face) {
        return switch (face) {
            case DOWN -> down;
            case UP -> up;
            case NORTH -> north;
            case SOUTH -> south;
            case WEST -> west;
            case EAST -> east;
        };
    }

    public static boolean isConnected(int packed, int connection) {
        return (packed & connection) != 0;
    }

    public static boolean isObscured(int packed) {
        return (packed & OBSCURED) != 0;
    }

    public static boolean hasConnections(int packed) {
        return (packed & 0xFF) != 0;
    }

    public static boolean hasNoConnections(int packed) {
        return !hasConnections(packed);
    }
}
