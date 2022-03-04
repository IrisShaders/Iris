package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.transformers.SpriteTexturedVertexTransformer;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VanillaVertexType;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexBufferWriterNio;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexWriterFallback;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class EntityVertexType implements VanillaVertexType<EntityVertexSink>, BlittableVertexType<EntityVertexSink> {
    @Override
    public EntityVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new EntityVertexWriterFallback(consumer);
    }

    @Override
    public EntityVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
        return new EntityVertexBufferWriterNio(buffer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return EntityVertexSink.VERTEX_FORMAT;
    }

    @Override
    public BlittableVertexType<EntityVertexSink> asBlittable() {
        return this;
    }

	public static class EntitySpriteTexturedVertexTransformer extends SpriteTexturedVertexTransformer implements EntityVertexSink {
		public EntitySpriteTexturedVertexTransformer(EntityVertexSink delegate, TextureAtlasSprite sprite) {
			super(delegate, sprite);
		}

		@Override
		public void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
			u = this.transformTextureU(u);
			v = this.transformTextureV(v);
			((EntityVertexSink)this.delegate).writeQuad(x, y, z, color, u, v, light, overlay, normal);
		}

		@Override
		public void endQuad(int length) {
			((EntityVertexSink)this.delegate).endQuad(length);
		}
	}
}
