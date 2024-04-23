package net.irisshaders.batchedentityrendering.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SectionBufferBuilderPack.class)
public interface SectionBufferBuilderPackAccessor {
	@Accessor
	Map<RenderType, BufferBuilder> getBuilders();
}
