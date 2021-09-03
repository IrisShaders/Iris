package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
 * Uses a priority of 1001 so that we apply after Carpet's mixins to WorldRenderer, avoiding a conflict:
 * https://github.com/gnembon/fabric-carpet/blob/776f798aecb792a5881ccae8784888156207a047/src/main/java/carpet/mixins/WorldRenderer_pausedShakeMixin.java#L23
 */
@Mixin(value = WorldRenderer.class, priority = 1001)
public class MixinWorldRenderer_EntityListSorting {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/world/ClientWorld.getEntities ()Ljava/lang/Iterable;"))
    private Iterable<Entity> batchedentityrendering$sortEntityList(ClientWorld world) {
        // Sort the entity list first in order to allow vanilla's entity batching code to work better.
        Iterable<Entity> entityIterable = world.getEntities();

        if (MinecraftClient.getInstance().player.isSneaking()) {
            // TODO: Don't disable optimization when sneaking
            return entityIterable;
        }

        world.getProfiler().push("sortEntityList");

        Map<EntityType<?>, List<Entity>> sortedEntities = new HashMap<>();

        List<Entity> entities = new ArrayList<>();
        entityIterable.forEach(entity -> {
            sortedEntities.computeIfAbsent(entity.getType(), entityType -> new ArrayList<>(32)).add(entity);
        });

        sortedEntities.values().forEach(entities::addAll);

        world.getProfiler().pop();

        return entities;
    }
}
