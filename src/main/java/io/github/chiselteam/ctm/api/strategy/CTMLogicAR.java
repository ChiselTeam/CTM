package io.github.chiselteam.ctm.api.strategy;

import io.github.chiselteam.ctm.api.texture.CTMTextureKeys;
import net.minecraft.core.BlockPos;

public enum CTMLogicAR {
    T0(CTMTextureKeys.AR_1), T1(CTMTextureKeys.AR_2), T2(CTMTextureKeys.AR_3), T3(CTMTextureKeys.AR_4);

    private final String textureSlot;

    CTMLogicAR(String textureSlot) {
        this.textureSlot = textureSlot;
    }

    public String getTextureSlot() {
        return textureSlot;
    }

    public static CTMLogicAR get(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        long seed = (long)x * 3129871L ^ (long)z * 116129781L ^ (long)y;
        seed = seed * seed * 42317861L + seed * 11L;
        
        int num = ((seed & 1) == 0) ? 0 : 2;
        boolean type = x % 2 != 0;
        if (y % 2 == 0) type = !type;
        if (z % 2 == 0) type = !type;
        
        num += type ? 0 : 1;
        return values()[num % 4];
    }
}
