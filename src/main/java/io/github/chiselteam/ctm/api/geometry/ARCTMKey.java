package io.github.chiselteam.ctm.api.geometry;

import io.github.chiselteam.ctm.api.strategy.CTMLogicAR;

public record ARCTMKey(int ar) implements CTMGeometryKey {
    private static final CTMLogicAR[] VALUES = CTMLogicAR.values();

    public CTMLogicAR logic() {
        return VALUES[ar];
    }

    public int ordinal() {
        return ar;
    }

    public static ARCTMKey of(CTMLogicAR logic) {
        return new ARCTMKey(logic.ordinal());
    }
}
