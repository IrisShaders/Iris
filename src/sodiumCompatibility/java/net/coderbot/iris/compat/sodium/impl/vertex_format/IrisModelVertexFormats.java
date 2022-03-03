package net.coderbot.iris.compat.sodium.impl.vertex_format;

import me.jellysquid.mods.sodium.client.model.vertex.type.VanillaVertexType;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexSink;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexType;
import net.coderbot.iris.compat.sodium.impl.vertex_format.xhfp.XHFPModelVertexType;

public class IrisModelVertexFormats {
    public static final XHFPModelVertexType MODEL_VERTEX_XHFP = new XHFPModelVertexType();
	public static final VanillaVertexType<EntityVertexSink> ENTITIES = new EntityVertexType();
}
