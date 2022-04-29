package net.coderbot.batchedentityrendering.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sorts the entity list to allow entities of the same type to be properly batched. Without sorting, entities are
 * essentially rendered in random order, which makes it harder for entity batching to work properly.
 *
 * Sorting reduces the number of RenderTypes that need to be active at any given time for batching to be effective.
 * For example, instead of the batching system needing to be prepared to render a pig at any time, it can know that
 * every pig is being rendered all at once, then it can use unused space within the buffer previously used for pigs for
 * something else.
 *
 * This is even more effective with vanilla's entity rendering, since it only has a single buffer for most purposes,
 * except for a configured set of batched render types.
 *
 * This injection point has been carefully chosen to avoid conflicts with other mixins such as one from Carpet:
 * https://github.com/gnembon/fabric-carpet/blob/776f798aecb792a5881ccae8784888156207a047/src/main/java/carpet/mixins/WorldRenderer_pausedShakeMixin.java#L23
 *
 * By using ModifyVariable instead of Redirect, it is more likely to be compatible with other rendering mods. We also
 * use a priority of 999 to apply before most other mixins to this method, meaning that other mods adding entities to
 * the rendering list (like Twilight Forest) are more likely to have these added entities sorted.
 */
@Mixin(value = LevelRenderer.class, priority = 999)
public class MixinLevelRenderer_EntityListSorting {
	@Shadow
	private ClientLevel level;

	@ModifyVariable(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z")), allow = 1)
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
