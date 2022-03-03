package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexWriterFallback;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VanillaVertexType;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexBufferWriterNio;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexBufferWriterUnsafe;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexWriterFallback;

public class EntityVertexType implements VanillaVertexType<EntityVertexSink>, BlittableVertexType<EntityVertexSink> {
    @Override
    public EntityVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new EntityVertexWriterFallback(consumer);
    }

    @Override
    public EntityVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
        return direct ? new EntityVertexBufferWriterUnsafe(buffer) : new EntityVertexBufferWriterNio(buffer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return EntityVertexSink.VERTEX_FORMAT;
    }

    @Override
    public BlittableVertexType<EntityVertexSink> asBlittable() {
        return this;
    }
}
