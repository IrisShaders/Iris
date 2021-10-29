package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.*;

/**
 * Captures and tracks the current block being rendered.
 *
 * Uses a priority of 1001 so that we apply after Carpet's mixins to LevelRenderer (WorldRenderer), avoiding a conflict:
 * https://github.com/gnembon/fabric-carpet/blob/776f798aecb792a5881ccae8784888156207a047/src/main/java/carpet/mixins/WorldRenderer_pausedShakeMixin.java#L23
 */
@Mixin(value = LevelRenderer.class, priority = 1001)
public class MixinLevelRenderer_EntityListSorting {
	@Shadow
	private ClientLevel level;

	@ModifyVariable(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"),
					to = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunks:Lit/unimi/dsi/fastutil/objects/ObjectList;")), allow = 1)
    private Iterator<Entity> batchedentityrendering$sortEntityList(Iterator<Entity> iterator) {
        // Sort the entity list first in order to allow vanilla's entity batching code to work better.
        this.level.getProfiler().push("sortEntityList");

        Map<EntityType<?>, List<Entity>> sortedEntities = new HashMap<>();

        List<Entity> entities = new ArrayList<>();
        iterator.forEachRemaining(entity -> {
            sortedEntities.computeIfAbsent(entity.getType(), entityType -> new ArrayList<>(32)).add(entity);
        });

        sortedEntities.values().forEach(entities::addAll);

        this.level.getProfiler().pop();

        return entities.iterator();
    }
}
