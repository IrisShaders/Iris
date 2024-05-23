package net.irisshaders.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.renderer.RenderType;

public record BufferSegment(MeshData meshData,
							RenderType type) {
}
