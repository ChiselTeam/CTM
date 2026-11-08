package io.github.chiselteam.ctm.test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import java.util.List;
import static org.mockito.Mockito.*;

/** Vanilla registry bootstrap for plain JUnit, without starting the NeoForge mod loader. */
public final class VanillaBootstrap {
    private static boolean initialized;

    private VanillaBootstrap() {}

    public static synchronized void initialize() {
        if (initialized) return;
        SharedConstants.tryDetectVersion();
        if (LoadingModList.get() != null) {
            Bootstrap.bootStrap();
        } else {
            var emptyMods = mock(LoadingModList.class);
            when(emptyMods.getModFiles()).thenReturn(List.of());
            // FeatureFlags consults LoadingModList while vanilla blocks initialize.
            // Scope this stand-in to bootstrap so it cannot mask loader use in tests.
            try (var loading = mockStatic(LoadingModList.class)) {
                loading.when(LoadingModList::get).thenReturn(emptyMods);
                Bootstrap.bootStrap();
            }
        }
        initialized = true;
    }
}
