package io.github.chiselteam.ctm.impl.texture;

import net.minecraft.client.resources.model.sprite.Material;

import java.util.EnumMap;
import java.util.Map;

public class CTMTextureSet<T extends Enum<T>> {

    private final Class<T> type;
    private final EnumMap<T, Material.Baked> materials;

    public CTMTextureSet(Class<T> type) {
        this.type = type;
        this.materials = new EnumMap<>(type);
    }

    public void put(T state, Material.Baked material) {
        if(material != null) materials.put(state, material);
    }

    public Material.Baked get(T state) {
        return materials.get(state);
    }

    public boolean contains(T state) {
        return materials.containsKey(state);
    }

    public boolean isComplete() {
        return materials.size() == type.getEnumConstants().length;
    }

    public Map<T, Material.Baked> getMaterials() {
        return materials;
    }
}
