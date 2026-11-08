package io.github.chiselteam.ctm.impl.model;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import io.github.chiselteam.ctm.api.model.CTMVariant;
import io.github.chiselteam.ctm.api.strategy.*;
import io.github.chiselteam.ctm.client.model.*;
import io.github.chiselteam.ctm.client.unbaked.StandardUnbakedCTMModel;
import io.github.chiselteam.ctm.impl.texture.CTMTextureSet;
import io.github.chiselteam.ctm.test.VanillaBootstrap;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

class LazyStandardGeometryTest {
    @BeforeAll static void bootstrap() {
        VanillaBootstrap.initialize();
    }

    @Test void lazyGeometryMatchesExistingEagerStandardBakeForAllFacesAndLogicTextures() {
        var textures = new CTMTextureSet<CTMLogic>(CTMLogic.class);
        for (var logic : CTMLogic.values()) textures.put(logic, new BakedTexture(sprite(logic.ordinal()), false));
        for (boolean ao : new boolean[]{false, true}) for (boolean shade : new boolean[]{false, true}) {
            var scope = new CTMBakeCache.Scope();
            var lazy = new LazyStandardGeometry(scope, textures, 3, 7, shade, ao).bake();
            var eager = new EagerFixture(textures, shade, ao).bake(new CTMBakingContext(null, null,
                    CTMModelState.of(Transformation.identity(), false), null, ao));
            var arrays = new ArrayList<BakedQuad[]>();
            ((CTMQuadStorage) eager).visitQuadArrays(arrays::add);
            assertEquals(24, arrays.size());
            int index = 0;
            for (var face : Direction.values()) for (int corner = 0; corner < 4; corner++) {
                var expected = arrays.get(index++);
                var actual = lazy.get(face)[corner];
                assertEquals(expected.length, actual.length);
                for (int logic = 0; logic < actual.length; logic++) {
                    assertArrayEquals(expected[logic].getVertices(), actual[logic].getVertices(), face + ":" + corner + ":" + logic);
                    assertSame(expected[logic].getSprite(), actual[logic].getSprite());
                    assertEquals(expected[logic].getDirection(), actual[logic].getDirection());
                    assertEquals(expected[logic].getTintIndex(), actual[logic].getTintIndex());
                    assertEquals(expected[logic].isShade(), actual[logic].isShade());
                    assertEquals(expected[logic].hasAmbientOcclusion(), actual[logic].hasAmbientOcclusion());
                }
            }
            var again = new LazyStandardGeometry(scope, textures, 3, 7, shade, ao).bake();
            assertSame(lazy.get(Direction.UP)[0][0], again.get(Direction.UP)[0][0]);
        }
    }

    private static TextureAtlasSprite sprite(int index) {
        var sprite = mock(TextureAtlasSprite.class);
        var contents = mock(SpriteContents.class);
        when(sprite.contents()).thenReturn(contents);
        when(contents.width()).thenReturn(16);
        when(contents.height()).thenReturn(16);
        when(sprite.getU0()).thenReturn(index / 16f);
        when(sprite.getU1()).thenReturn((index + 1) / 16f);
        when(sprite.getV0()).thenReturn(0f);
        when(sprite.getV1()).thenReturn(1f);
        when(sprite.getU(anyFloat())).thenAnswer(call -> (index + (float) call.getArgument(0)) / 16f);
        when(sprite.getV(anyFloat())).thenAnswer(call -> (float) call.getArgument(0));
        when(sprite.uvShrinkRatio()).thenReturn(0.001f);
        return sprite;
    }

    /** Forces the existing eager production path without a bakery or resource manager. */
    private static class EagerFixture extends StandardUnbakedCTMModel {
        private final CTMTextureSet<CTMLogic> textures;
        EagerFixture(CTMTextureSet<CTMLogic> textures, boolean shade, boolean ao) {
            super(ResourceLocation.fromNamespaceAndPath("ctm", "test"), Pair.of(new Vector3f(), new Vector3f(16)),
                    EnumSet.allOf(Direction.class), false, mock(CTMVariant.class), -1, 0, 3, 7, shade, ao,
                    false, mock(CTMBlockPredicate.class), List.of(), Map.of());
            this.textures = textures;
        }
        @Override protected void prepareBakery(CTMBakingContext context) {
            state = CTMModelState.of(Transformation.identity(), false);
            rootTransform = Transformation.identity();
        }
        @Override protected Material getMaterial(String name) { return null; }
        @Override protected CTMTextureSet<CTMLogic> bakeStandardTextureSet(CTMBakingContext context) { return textures; }
    }
}
