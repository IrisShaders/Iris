package net.coderbot.iris.compat.sodium.mixin.buffer_builder_intrinsics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.client.model.vertex.VertexDrain;
import me.jellysquid.mods.sodium.client.model.vertex.VertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.transformers.SpriteTexturedVertexTransformer;
import me.jellysquid.mods.sodium.client.model.vertex.type.VertexType;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpriteCoordinateExpander.class)
public abstract class MixinSpriteCoordinateExpander implements VertexDrain {
    @Shadow
    @Final
    private TextureAtlasSprite sprite;

    @Shadow
    @Final
    private VertexConsumer delegate;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends VertexSink> T createSink(VertexType<T> type) {
        if (type == IrisModelVertexFormats.ENTITIES) {
            return (T) new SpriteTexturedVertexTransformer.Quad(VertexDrain.of(this.delegate)
                    .createSink(IrisModelVertexFormats.ENTITIES), this.sprite);
        } else if (type == VanillaVertexTypes.QUADS) {
            return (T) new SpriteTexturedVertexTransformer.Quad(VertexDrain.of(this.delegate)
                    .createSink(VanillaVertexTypes.QUADS), this.sprite);
        } else if (type == VanillaVertexTypes.PARTICLES) {
            return (T) new SpriteTexturedVertexTransformer.Particle(VertexDrain.of(this.delegate)
                    .createSink(VanillaVertexTypes.PARTICLES), this.sprite);
        } else if (type == VanillaVertexTypes.GLYPHS) {
            return (T) new SpriteTexturedVertexTransformer.Glyph(VertexDrain.of(this.delegate)
                    .createSink(VanillaVertexTypes.GLYPHS), this.sprite);
        }

        return type.createFallbackWriter((VertexConsumer) this);
    }
}
