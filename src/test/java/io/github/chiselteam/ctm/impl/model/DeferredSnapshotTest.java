package io.github.chiselteam.ctm.impl.model;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.client.model.*;
import io.github.chiselteam.ctm.client.unbaked.StandardUnbakedCTMModel;
import io.github.chiselteam.ctm.test.VanillaBootstrap;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.joml.*;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeferredSnapshotTest {
    @BeforeAll static void bootstrap() { VanillaBootstrap.initialize(); }

    @Test void recipeRestoresEachSnapshotAndResetsForANewLiveBakeContext() {
        var source = BlockModel.fromString("{\"textures\":{\"particle\":\"ctm:live\"}}");
        var liveMaterial = source.getMaterial("particle");
        var firstMaterial = new Material(liveMaterial.atlasLocation(), ResourceLocation.fromNamespaceAndPath("ctm", "first"));
        var secondMaterial = new Material(liveMaterial.atlasLocation(), ResourceLocation.fromNamespaceAndPath("ctm", "second"));
        var sprite = mock(TextureAtlasSprite.class);
        var firstState = CTMModelState.of(Transformation.identity(), false);
        var secondState = CTMModelState.of(new Transformation(new Matrix4f().rotateY(1)), true);
        var firstInputs = new DeferredCTMModel.Inputs(Map.of("particle", firstMaterial), Map.of(firstMaterial, sprite), firstState);
        var secondInputs = new DeferredCTMModel.Inputs(Map.of("particle", secondMaterial), Map.of(secondMaterial, sprite), secondState);
        var first = new CTMBakingContext(null, firstInputs, firstState, null, true);
        var second = new CTMBakingContext(null, secondInputs, secondState, null, true);
        var baker = mock(ModelBaker.class);
        when(baker.getModel(anyLocation())).thenReturn(source);
        var geometry = mock(IGeometryBakingContext.class);
        when(geometry.getRootTransform()).thenReturn(Transformation.identity());
        var live = new CTMBakingContext(baker, material -> sprite, firstState, geometry, true);
        var recipe = new Recipe();
        recipe.prepare(first);
        assertSame(firstMaterial, recipe.particle());
        assertTrue(recipe.deferred());
        recipe.prepare(second);
        assertSame(secondMaterial, recipe.particle());
        assertSame(secondState, recipe.state());
        recipe.prepare(live);
        assertEquals(liveMaterial, recipe.particle());
        assertFalse(recipe.deferred());
        assertTrue(recipe.state().getRotation().isIdentity());
        assertFalse(recipe.state().isUvLocked());
        recipe.prepare(first);
        assertSame(firstMaterial, recipe.particle());
        assertSame(firstState, recipe.state());
    }

    private static ResourceLocation anyLocation() { return org.mockito.ArgumentMatchers.any(ResourceLocation.class); }

    private static class Recipe extends StandardUnbakedCTMModel {
        Recipe() {
            super(ResourceLocation.fromNamespaceAndPath("ctm", "test"), Pair.of(new Vector3f(), new Vector3f(16)),
                    Set.of(), false, mock(CTMVariant.class), -1, 0, -1, 0, true, true,
                    false, mock(CTMBlockPredicate.class), List.of(), Map.of());
        }
        void prepare(CTMBakingContext context) { prepareBakery(context); }
        Material particle() { return getMaterial("particle"); }
        CTMModelState state() { return state; }
        boolean deferred() { return isDeferredBake(); }
    }
}
