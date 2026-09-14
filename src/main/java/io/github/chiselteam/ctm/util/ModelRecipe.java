package io.github.chiselteam.ctm.util;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record ModelRecipe(ModelBaker baker, ModelState state, Direction face, Direction cull, CuboidFace.UVs faceUvs,
                          Vector3f[] offsets, Map<Direction, CuboidFace.UVs> faceQuads,
                          Map<Direction, BakedQuad[]> bakedQuads, Set<Direction> unculledFaces, Material.Baked base) {

    public static class Builder {
        private ModelBaker baker;
        private ModelState state;
        private Direction face;
        private Direction cull;
        private CuboidFace.UVs faceUvs;
        private Vector3f[] offsets;
        private Map<Direction, CuboidFace.UVs> faceQuads;
        private Map<Direction, BakedQuad[]> bakedQuads;
        private Set<Direction> unculledFaces;
        private Material.Baked base;

        public Builder baker(ModelBaker baker) {
            this.baker = baker;
            return this;
        }

        public Builder state(ModelState state) {
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

        public Builder faceUvs(CuboidFace.UVs faceUvs) {
            this.faceUvs = faceUvs;
            return this;
        }

        public Builder offsets(Vector3f[] offsets) {
            this.offsets = offsets;
            return this;
        }

        public Builder faceQuads(Map<Direction, CuboidFace.UVs> faceQuads) {
            this.faceQuads = faceQuads;
            return this;
        }

        public Builder faceQuad(Direction direction, CuboidFace.UVs uvs) {
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

        public Builder base(Material.Baked base) {
            this.base = base;
            return this;
        }

        public ModelRecipe build() {
            return new ModelRecipe(
                    baker,
                    state,
                    face,
                    cull,
                    faceUvs,
                    offsets,
                    faceQuads,
                    bakedQuads,
                    unculledFaces,
                    base
            );
        }
    }
}
