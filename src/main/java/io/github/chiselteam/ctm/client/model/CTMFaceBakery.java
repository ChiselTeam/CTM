package io.github.chiselteam.ctm.client.model;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.SimpleModelState;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class CTMFaceBakery {
    private CTMFaceBakery() {
    }

    public static BakedQuad bakeQuad(CTMBakingContext baker, Vector3f from, Vector3f to, CTMFace face, BakedTexture texture, Direction direction, CTMModelState state, BlockElementRotation rotation, boolean shade, int emissivity) {
        var uv = face.uvs();
        BlockFaceUV nativeUV = new BlockFaceUV(new float[]{uv.minU(), uv.minV(), uv.maxU(), uv.maxV()}, face.rotation().ordinal() * 90);
        nativeUV = transformUV(nativeUV, state.faceTransformation(direction));
        var nativeFace = new BlockElementFace(face.cullForDirection(), face.tintIndex(), face.texture(), nativeUV, new ExtraFaceData(-1, emissivity, emissivity, baker.ambientOcclusion()), new MutableObject<>());
        var geometryState = new SimpleModelState(state.getRotation(), false);
        return new FaceBakery().bakeQuad(from, to, nativeFace, texture.sprite(), direction, geometryState, rotation, shade);
    }

    static BlockFaceUV transformUV(BlockFaceUV uv, Matrix4fc matrix) {
        float u0 = uv.getU(uv.getReverseIndex(0)), v0 = uv.getV(uv.getReverseIndex(0));
        float u1 = uv.getU(uv.getReverseIndex(2)), v1 = uv.getV(uv.getReverseIndex(2));
        var start = matrix.transform(new Vector4f(u0 / 16, v0 / 16, 0, 1));
        var end = matrix.transform(new Vector4f(u1 / 16, v1 / 16, 0, 1));
        float minU = start.x * 16, maxU = end.x * 16;
        float minV = start.y * 16, maxV = end.y * 16;

        if (Math.signum(u1 - u0) != Math.signum(maxU - minU)) {
            float swap = minU;
            minU = maxU;
            maxU = swap;
        }

        if (Math.signum(v1 - v0) != Math.signum(maxV - minV)) {
            float swap = minV;
            minV = maxV;
            maxV = swap;
        }

        double radians = Math.toRadians(uv.rotation);
        var axis = matrix.transformDirection(new Vector3f((float) Math.cos(radians), (float) Math.sin(radians), 0));
        int rotation = Math.floorMod(-((int) Math.round(Math.toDegrees(Math.atan2(axis.y, axis.x)) / 90)) * 90, 360);
        return new BlockFaceUV(new float[]{minU, minV, maxU, maxV}, rotation);
    }
}
