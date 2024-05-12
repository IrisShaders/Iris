package net.irisshaders.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderType;

public record BufferSegment(BufferBuilder.RenderedBuffer renderedBuffer,
							RenderType type) {
}
