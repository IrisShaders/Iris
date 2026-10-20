package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
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
import java.util.HashSet;
import java.util.Set;

@Mixin(FrontendRenderPass.class)
public abstract class MixinWorldUniforms implements RenderPassInterface {
	@Unique
	private static final Set<String> iris$unmappedPipelines = new HashSet<>();
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
	@Unique
	private boolean iris$skipDraw;
	@Shadow
	private boolean isClosed;
	@Shadow
	@Final
	private RenderPassBackend backend;

	@WrapMethod(method = "setPipeline")
	private void iris$checkPipeline(CompiledRenderPipeline pipeline, Operation<Void> original) {
		this.iris$skipDraw = false;

		if (this.backend instanceof WorldRenderPass pass && pipeline instanceof FrontendRenderPipeline frontend) {
			var resolved = pass.overridePipeline(frontend);
			if (resolved == null) {
				this.iris$skipDraw = true;
				if (iris$unmappedPipelines.add(pass.getPhase() + "/" + frontend.name())) {
					Iris.logger.warn("Skipping {} draw: no Iris shader mapping for pipeline {}", pass.getPhase(), frontend.name());
				}
				return;
			}
			pipeline = resolved;
		}
		original.call(pipeline);
	}

	@Inject(method = {"draw", "drawIndexed", "drawIndirect", "drawIndexedIndirect",
		"multiDraw(Ljava/nio/IntBuffer;III)V", "multiDraw(Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;I)V",
		"multiDrawIndexed(Ljava/nio/IntBuffer;III)V", "multiDrawIndexed(Lorg/lwjgl/PointerBuffer;Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;I)V",
		"drawMultipleIndexed", "pushConstants"}, at = @At("HEAD"), cancellable = true)
	private void iris$skipUnmappedDraw(CallbackInfo ci) {
		if (this.iris$skipDraw) {
			ci.cancel();
		}
	}

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
