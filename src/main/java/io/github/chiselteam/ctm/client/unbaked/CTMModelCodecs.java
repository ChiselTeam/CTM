package io.github.chiselteam.ctm.client.unbaked;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chiselteam.ctm.api.model.CTMOverlayCondition;
import io.github.chiselteam.ctm.api.model.CTMOverlayConditions;
import io.github.chiselteam.ctm.api.model.CTMOverlayRule;
import io.github.chiselteam.ctm.api.strategy.CTMBlockPredicate;
import io.github.chiselteam.ctm.api.strategy.ResolvedBlockStateMatcher;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

public class CTMModelCodecs {

    public static final MapCodec<ResolvedBlockStateMatcher> MATCHER_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("id").forGetter(ResolvedBlockStateMatcher::block),
            Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH).optionalFieldOf("state", Map.of()).forGetter(_ -> Map.of())
    ).apply(instance, (block, states) -> {
        Map<Property<?>, Comparable<?>> properties = new HashMap<>();
        for (Map.Entry<String, Dynamic<?>> entry : states.entrySet()) {
            Property<?> property = block.getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
                throw new IllegalArgumentException("Unknown property " + entry.getKey() + " for block " + BuiltInRegistries.BLOCK.getKey(block));
            }
            String valueStr = entry.getValue().asString().result().orElseGet(() -> 
                entry.getValue().asNumber().result().map(Object::toString).orElseGet(() -> 
                entry.getValue().asBoolean().result().map(Object::toString).orElse(""))
            );
            Optional<? extends Comparable<?>> value = property.getValue(valueStr);
            if (value.isEmpty()) {
                 throw new IllegalArgumentException("Invalid value " + valueStr + " for property " + entry.getKey() + " on block " + BuiltInRegistries.BLOCK.getKey(block));
            }
            properties.put(property, value.get());
        }
        return new ResolvedBlockStateMatcher(block, properties);
    }));

    public static final Codec<ResolvedBlockStateMatcher> MATCHER_CODEC = MATCHER_MAP_CODEC.codec();

    public static final Codec<CTMBlockPredicate> CONNECTION_ENTRY_CODEC = MATCHER_CODEC.xmap(
            CTMBlockPredicate.BlockStateMatchPredicate::new,
            predicate -> ((CTMBlockPredicate.BlockStateMatchPredicate) predicate).matcher()
    );

    public static final Codec<CTMBlockPredicate> CONNECTS_TO_CODEC = CONNECTION_ENTRY_CODEC.listOf().xmap(
            list -> list.size() == 1 ? list.getFirst() : new CTMBlockPredicate.AnyOfCTMBlockPredicate(list),
            predicate -> {
                if (predicate instanceof CTMBlockPredicate.AnyOfCTMBlockPredicate(List<CTMBlockPredicate> predicates)) return predicates;
                return List.of(predicate);
            }
    );

    // Accepts either a specific direction ("up", "north", etc.) or a default/explicit "any".
    // When omitted or set to "any", the condition matches if ANY neighbor around the block matches.
    public static final MapCodec<CTMOverlayCondition> NEIGHBOR_ENTRY_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("direction").forGetter(cond -> {
                if (cond instanceof CTMOverlayConditions.NeighborCondition nc) return Optional.of(nc.direction().getSerializedName());
                if (cond instanceof CTMOverlayConditions.AnyNeighborCondition) return Optional.of("any");
                return Optional.empty();
            }),
            MATCHER_MAP_CODEC.forGetter(cond -> {
                if (cond instanceof CTMOverlayConditions.NeighborCondition nc) return nc.matcher();
                if (cond instanceof CTMOverlayConditions.AnyNeighborCondition(ResolvedBlockStateMatcher matcher)) return matcher;
                return null;
            })
    ).apply(instance, (Optional<String> dirStr, ResolvedBlockStateMatcher matcher) -> {
        if (dirStr.isEmpty() || dirStr.get().equalsIgnoreCase("any")) {
            return new CTMOverlayConditions.AnyNeighborCondition(matcher);
        }
        Direction dir = Direction.byName(dirStr.get());
        if (dir == null) throw new IllegalArgumentException("Invalid direction '" + dirStr.get() + "' in neighbor entry; expected one of: up, down, north, south, west, east, or 'any'");
        return new CTMOverlayConditions.NeighborCondition(dir, matcher);
    }));

    public static final MapCodec<CTMOverlayCondition> ACTUAL_CONDITION_MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MATCHER_CODEC.optionalFieldOf("self").forGetter(_ -> Optional.empty()),
            NEIGHBOR_ENTRY_MAP_CODEC.codec().listOf().optionalFieldOf("neighbors", List.of()).forGetter(_ -> List.of())
    ).apply(instance, (Optional<ResolvedBlockStateMatcher> selfMatch, List<CTMOverlayCondition> neighbors) -> {
        List<CTMOverlayCondition> conditions = new ArrayList<>();
        selfMatch.ifPresent(matcher -> conditions.add(new CTMOverlayConditions.SelfStateCondition(matcher)));
        conditions.addAll(neighbors);
        if (conditions.isEmpty()) return (_, _, _, _) -> true;
        if (conditions.size() == 1) return conditions.getFirst();
        return new CTMOverlayConditions.AllOfOverlayCondition(conditions);
    }));

    public static final Codec<CTMOverlayCondition> ACTUAL_CONDITION_CODEC = ACTUAL_CONDITION_MAP_CODEC.codec();

    public record UnbakedOverlayRule(
            String material,
            Set<Direction> faces,
            CTMOverlayCondition condition,
            int priority,
            int tintIndex,
            int emissivity
    ) {
        public CTMOverlayRule bake(ResolvedModel model) {
            Material mat = model.getTopTextureSlots().getMaterial(material);
            return new CTMOverlayRule(mat, faces, condition, priority, tintIndex, emissivity);
        }
    }

    public static final Codec<Set<Direction>> FACES_CODEC = Codec.STRING.listOf().xmap(
            list -> {
                if (list.contains("all")) return EnumSet.allOf(Direction.class);
                Set<Direction> faces = EnumSet.noneOf(Direction.class);
                for (String s : list) {
                    Direction dir = Direction.byName(s);
                    if (dir != null) faces.add(dir);
                }
                return faces;
            },
            set -> set.size() == 6 ? List.of("all") : set.stream().map(Direction::getSerializedName).toList()
    );

    public static final Codec<UnbakedOverlayRule> OVERLAY_RULE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("material").forGetter(UnbakedOverlayRule::material),
            FACES_CODEC.optionalFieldOf("faces", EnumSet.allOf(Direction.class)).forGetter(UnbakedOverlayRule::faces),
            ACTUAL_CONDITION_CODEC.optionalFieldOf("conditions", (_, _, _, _) -> true).forGetter(UnbakedOverlayRule::condition),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(UnbakedOverlayRule::priority),
            Codec.INT.optionalFieldOf("tint_index", -1).forGetter(UnbakedOverlayRule::tintIndex),
            Codec.INT.optionalFieldOf("emissivity", 0).forGetter(UnbakedOverlayRule::emissivity)
    ).apply(instance, UnbakedOverlayRule::new));
}
