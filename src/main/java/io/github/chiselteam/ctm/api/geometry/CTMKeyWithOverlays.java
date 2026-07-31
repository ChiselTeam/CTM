package io.github.chiselteam.ctm.api.geometry;

import java.util.Objects;

public record CTMKeyWithOverlays<K>(K baseKey, long overlayMask) implements CTMGeometryKey {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CTMKeyWithOverlays<?> that = (CTMKeyWithOverlays<?>) o;
        return overlayMask == that.overlayMask && Objects.equals(baseKey, that.baseKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseKey, overlayMask);
    }
}
