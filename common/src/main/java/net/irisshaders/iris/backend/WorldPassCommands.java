package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.RenderPassBackend;

import java.util.List;

public interface WorldPassCommands extends RenderPassBackend, IrisRenderPass {
	default void end() {
	}

	long getTexelBufferView(GpuBufferSlice slice, GpuFormat format);

	void setAttachments(List<GpuTextureView> colors, GpuTextureView depth, RenderArea area);
}
