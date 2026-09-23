package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.IrisRenderPass;
import net.irisshaders.iris.backend.WorldPassCommands;
import net.irisshaders.iris.mixinterface.RenderPassInterface;
import net.irisshaders.iris.pipeline.WorldRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(FrontendRenderPass.class)
public abstract class MixinWorldUniforms implements RenderPassInterface {
	@Shadow
	protected GpuBuffer indexBuffer;
	@Shadow
	@Final
	protected HashMap<String, Object> uniforms;
	@Shadow
	@Final
	private RenderArea renderArea;
	@Shadow
	@Final
	private GpuBufferSlice[] vertexBuffers;
	@Unique
	private IrisRenderPass iris$pass;
	@Unique
	private IndexType iris$indexType = IndexType.INT;
	@Shadow
	private boolean isClosed;
	@Shadow
	@Final
	private RenderPassBackend backend;

	@Override
	public IrisRenderPass iris$getIrisPass() {
		if (this.isClosed) {
			throw new IllegalStateException("Minecraft render pass is closed");
		}

		if (this.iris$pass == null) {
			this.iris$pass = this.backend instanceof WorldRenderPass pass ? pass.getCommands() : Iris.getRenderBackend()
				.adaptRenderPass(this.backend, this.renderArea);

			if (this.indexBuffer != null) {
				this.iris$pass.setIndexBuffer(this.indexBuffer, this.iris$indexType);
			}

			for (int i = 0; i < this.vertexBuffers.length; i++) {
				if (this.vertexBuffers[i] != null) {
					this.iris$pass.setVertexBuffer(i, this.vertexBuffers[i]);
				}
			}
		}

		return this.iris$pass;
	}

	@Inject(method = "setVertexBuffer", at = @At("TAIL"))
	private void iris$vertexBuffer(int slot, GpuBufferSlice buffer, CallbackInfo ci) {
		if (this.iris$pass != null && !(this.backend instanceof WorldRenderPass)) {
			this.iris$pass.setVertexBuffer(slot, buffer);
		}
	}

	@Inject(method = "setIndexBuffer", at = @At("TAIL"))
	private void iris$indexBuffer(GpuBuffer buffer, IndexType type, CallbackInfo ci) {
		this.iris$indexType = type;

		if (this.iris$pass != null && !(this.backend instanceof WorldRenderPass)) {
			this.iris$pass.setIndexBuffer(buffer, type);
		}
	}

	@Inject(method = "close", at = @At("TAIL"))
	private void iris$endWorldPass(CallbackInfo ci) {
		if (this.backend instanceof WorldRenderPass pass) {
			pass.end();
		} else if (this.iris$pass instanceof WorldPassCommands commands) {
			commands.end();
		}
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$allWorldUniforms(CallbackInfo ci) {
		if (this.backend instanceof WorldRenderPass pass) {
			pass.setUniforms(this.uniforms);
		}
	}
}
