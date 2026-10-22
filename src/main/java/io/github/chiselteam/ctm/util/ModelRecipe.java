package io.github.chiselteam.ctm.util;

import io.github.chiselteam.ctm.client.model.BakedTexture;
import io.github.chiselteam.ctm.client.model.CTMModelState;
import io.github.chiselteam.ctm.client.model.CTMBakingContext;
import io.github.chiselteam.ctm.client.model.CTMFace;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record ModelRecipe(CTMBakingContext baker, CTMModelState state, Direction face, Direction cull, CTMFace.UVs faceUvs,
                          Vector3f[] offsets, Map<Direction, CTMFace.UVs> faceQuads,
                          Map<Direction, BakedQuad[]> bakedQuads, Set<Direction> unculledFaces, BakedTexture base) {

    public static class Builder {
        private CTMBakingContext baker;
        private CTMModelState state;
        private Direction face;
        private Direction cull;
        private CTMFace.UVs faceUvs;
        private Vector3f[] offsets;
        private Map<Direction, CTMFace.UVs> faceQuads;
        private Map<Direction, BakedQuad[]> bakedQuads;
        private Set<Direction> unculledFaces;
        private BakedTexture base;

        public Builder baker(CTMBakingContext baker) {
            this.baker = baker;
            return this;
        }

        public Builder state(CTMModelState state) {
            this.state = state;
            return this;
        }

        public Builder face(Direction face) {
            this.face = face;
            return this;
        }

        public Builder cull(Direction cull) {
            this.cull = cull;
            return this;
        }

        public Builder faceUvs(CTMFace.UVs faceUvs) {
            this.faceUvs = faceUvs;
            return this;
        }

        public Builder offsets(Vector3f[] offsets) {
            this.offsets = offsets;
            return this;
        }

        public Builder faceQuads(Map<Direction, CTMFace.UVs> faceQuads) {
            this.faceQuads = faceQuads;
            return this;
        }

        public Builder faceQuad(Direction direction, CTMFace.UVs uvs) {
            if (this.faceQuads == null) {
                this.faceQuads = new HashMap<>();
            }
            this.faceQuads.put(direction, uvs);
            return this;
        }

        public Builder bakedQuads(Map<Direction, BakedQuad[]> bakedQuads) {
            this.bakedQuads = bakedQuads;
            return this;
        }

        public Builder unculledFaces(Set<Direction> unculledFaces) {
            this.unculledFaces = unculledFaces;
            return this;
        }

        public Builder base(BakedTexture base) {
            this.base = base;
            return this;
        }

        public ModelRecipe build() {
            return new ModelRecipe(baker, state, face, cull, faceUvs, offsets, faceQuads, bakedQuads, unculledFaces, base);
        }
    }
}
