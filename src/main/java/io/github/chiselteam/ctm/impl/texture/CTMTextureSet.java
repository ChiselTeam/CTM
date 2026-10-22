package io.github.chiselteam.ctm.impl.texture;

import io.github.chiselteam.ctm.client.model.BakedTexture;

import java.util.Arrays;
import java.util.EnumMap;

public class CTMTextureSet<T extends Enum<T>> {

    private final Class<T> type;
    private final EnumMap<T, BakedTexture> materials;

    public CTMTextureSet(Class<T> type) {
        this.type = type;
        this.materials = new EnumMap<>(type);
    }

    public void put(T state, BakedTexture material) {
        if(material != null) materials.put(state, material);
    }

    public BakedTexture get(T state) {
        return materials.get(state);
    }

    public boolean contains(T state) {
        return materials.containsKey(state);
    }

    public boolean isComplete() {
        return materials.size() == type.getEnumConstants().length;
    }

    public void requireComplete(String model) {
        if (!isComplete()) {
            var missing = Arrays.stream(type.getEnumConstants())
                    .filter(state -> !materials.containsKey(state)).toList();
            throw new IllegalArgumentException("Missing standalone CTM textures for " + model + ": " + missing);
        }
    }

    public Class<T> getType() {
        return type;
    }
}
