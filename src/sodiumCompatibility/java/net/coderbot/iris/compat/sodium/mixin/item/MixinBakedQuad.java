package net.coderbot.iris.compat.sodium.mixin.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.coderbot.iris.compat.sodium.impl.entities.IrisBakedQuad;
import net.coderbot.iris.compat.sodium.impl.entities.VertexHistory;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class MixinBakedQuad implements IrisBakedQuad {
    @Unique
    private Int2ObjectOpenHashMap<VertexHistory> idToHistory = new Int2ObjectOpenHashMap<>();

    @Override
    public VertexHistory getPrevious() {
        return idToHistory.computeIfAbsent(CapturedRenderingState.INSTANCE.getUniqueEntityId(), (i) -> new VertexHistory(i, 4));
    }
}
