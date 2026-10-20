package net.irisshaders.iris.backend;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.irisshaders.iris.samplers.ProgramSamplers;

public interface PackResources extends AutoCloseable {
	public void beginFrame(GpuBufferSlice frameUniforms);

	public default void register(ProgramSamplers resources) {
	}

	@Override
	public void close();
}
