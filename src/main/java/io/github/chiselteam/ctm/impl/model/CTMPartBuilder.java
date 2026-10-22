package io.github.chiselteam.ctm.impl.model;

import io.github.chiselteam.ctm.api.model.ConnectedTextureBlockModelPart;
import io.github.chiselteam.ctm.api.strategy.CTMLogic;
import io.github.chiselteam.ctm.api.texture.FaceQuadAppender;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import java.util.*;

public final class CTMPartBuilder {

    private static final int LOGIC_COUNT = CTMLogic.values().length;

    private CTMPartBuilder() {}

    public static ConnectedTextureBlockModelPart create(Map<Direction, BakedQuad[]> bakedQuads, Set<Direction> unculledFaces, Material.Baked particleMaterial, FaceQuadAppender appender) {
        return create(bakedQuads, unculledFaces, particleMaterial, true, appender);
    }

    public static ConnectedTextureBlockModelPart create(Map<Direction, BakedQuad[]> bakedQuads, Set<Direction> unculledFaces, Material.Baked particleMaterial, boolean ambientOcclusion, FaceQuadAppender appender) {
        Map<Direction, List<BakedQuad>> quadsMap = new EnumMap<>(Direction.class);
        List<BakedQuad> unculled = new ArrayList<>();
        int flags = 0;

        for(Direction face : Direction.values()) {
            List<BakedQuad> faceQuads = new ArrayList<>(8);
            appendBaseQuads(bakedQuads.get(face), faceQuads);
            appender.append(face, faceQuads);

            for(BakedQuad quad : faceQuads) {
                flags |= quad.materialInfo().flags();
            }

            List<BakedQuad> immutableQuads = Collections.unmodifiableList(faceQuads);
            quadsMap.put(face, immutableQuads);

            if(unculledFaces.contains(face)) {
                unculled.addAll(faceQuads);
            }
        }

        return new ConnectedTextureBlockModelPart(quadsMap, List.copyOf(unculled), flags, particleMaterial, ambientOcclusion);
    }

    public static void appendIndexedQuad(BakedQuad[] quads, int index, List<BakedQuad> faceQuads) {
        if (quads == null || index < 0 || index >= quads.length) {
            return;
        }

        BakedQuad quad = quads[index];
        if (quad != null) {
            faceQuads.add(quad);
        }
    }


    public static void appendPatternQuads(BakedQuad[][] quads, int pattern, List<BakedQuad> faceQuads) {
        if (quads == null) return;

        for (int corner = 0; corner < Math.min(4, quads.length); corner++) {
            int logic = pattern % LOGIC_COUNT;
            pattern /= LOGIC_COUNT;
            appendIndexedQuad(quads[corner], logic, faceQuads);
        }
    }

    private static void appendBaseQuads(BakedQuad[] base, List<BakedQuad> faceQuads) {
        if(base == null) return;

        for(BakedQuad quad : base) {
            if(quad != null) faceQuads.add(quad);
        }
    }
}
