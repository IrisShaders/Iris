package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures and tracks the current block being rendered.
 *
 * Uses a priority of 1001 so that we apply after Carpet's mixins to LevelRenderer (WorldRenderer), avoiding a conflict:
 * https://github.com/gnembon/fabric-carpet/blob/776f798aecb792a5881ccae8784888156207a047/src/main/java/carpet/mixins/WorldRenderer_pausedShakeMixin.java#L23
 */
@Mixin(value = LevelRenderer.class, priority = 1001)
public class MixinLevelRenderer_EntityListSorting {
    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private Iterable<Entity> batchedentityrendering$sortEntityList(ClientLevel clientLevel) {
        // Sort the entity list first in order to allow vanilla's entity batching code to work better.
        Iterable<Entity> entityIterable = clientLevel.entitiesForRendering();

        clientLevel.getProfiler().push("sortEntityList");

        Map<EntityType<?>, List<Entity>> sortedEntities = new HashMap<>();

        List<Entity> entities = new ArrayList<>();
        entityIterable.forEach(entity -> {
            sortedEntities.computeIfAbsent(entity.getType(), entityType -> new ArrayList<>(32)).add(entity);
        });

        sortedEntities.values().forEach(entities::addAll);

        clientLevel.getProfiler().pop();

        return entities;
    }
}
