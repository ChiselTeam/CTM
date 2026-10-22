package io.github.chiselteam.ctm.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.SimpleModelState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public interface CTMModelState extends ModelState {
    static CTMModelState of(Transformation rotation, boolean uvLock) {
        return of(new SimpleModelState(rotation, uvLock));
    }

    static CTMModelState of(ModelState state) {
        return new CTMModelState() {
            @Override
            public @NotNull Transformation getRotation() {
                return state.getRotation();
            }

            @Override
            public boolean isUvLocked() {
                return state.isUvLocked();
            }
        };
    }

    default Transformation transformation() {
        return getRotation();
    }

    default Matrix4fc faceTransformation(Direction face) {
        return isUvLocked() ? BlockMath.getUVLockTransform(getRotation(), face).getMatrix() : new Matrix4f();
    }

    default Matrix4fc inverseFaceTransformation(Direction face) {
        return new Matrix4f(faceTransformation(face)).invert();
    }
}
