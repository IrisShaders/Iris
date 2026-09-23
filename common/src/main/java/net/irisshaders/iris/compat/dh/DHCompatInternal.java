package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiCustomRenderRegister;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiTerrainBufferContainer;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeBufferRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeGenericObjectRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeGenericRenderCleanupEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeGenericRenderSetupEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderPassEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.math.DhApiVec3f;
import com.seibel.distanthorizons.api.objects.util.IDhApiReadOnlyList;
import com.seibel.distanthorizons.common.render.blaze.wrappers.texture.BlazeBlockTextureAtlas;
import com.seibel.distanthorizons.core.dataObjects.render.bufferBuilding.LodBufferContainer;
import com.seibel.distanthorizons.core.render.renderer.RenderableBoxGroup;
import com.seibel.distanthorizons.coreapi.DependencyInjection.ApiEventInjector;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.MatrixUtils;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.CloudSetting;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DHCompatInternal implements AutoCloseable {
	private final IrisRenderingPipeline pipeline;
	private final List<IrisDhProgram> programs = new ArrayList<>();
	private final Vector3f modelOffset = new Vector3f();
	private final Matrix4f modelView = new Matrix4f();
	private final Matrix4f modelViewInverse = new Matrix4f();
	private final Matrix4f projection = new Matrix4f();
	private final Matrix4f projectionInverse = new Matrix4f();
	private final Matrix3f normalMatrix = new Matrix3f();
	private final DhApiBeforeBufferRenderEvent.EventParam bufferEvent = new DhApiBeforeBufferRenderEvent.EventParam();
	private final DhApiBeforeGenericObjectRenderEvent.EventParam genericEvent = new DhApiBeforeGenericObjectRenderEvent.EventParam();
	private IrisDhProgram terrain;
	private IrisDhProgram water;
	private IrisDhProgram generic;
	private IrisDhProgram shadow;
	private IrisDhProgram genericShadow;
	private GpuTexture depth;
	private GpuTexture opaqueDepth;
	private GpuTextureView depthView;
	private GpuTextureView opaqueDepthView;
	private float clipDistance;
	private int skyLight;
	private int blockLight;
	private boolean closed;

	public DHCompatInternal(IrisRenderingPipeline pipeline, boolean renderShadows) {
		this.pipeline = pipeline;
		var terrain = pipeline.getDHTerrainShader();
		if (terrain.isEmpty()) {
			return;
		}

		try {
			this.resizeDepth();
			this.terrain = this.createProgram(terrain.get(), false, false, false);
			this.water = pipeline.getDHWaterShader().map(source -> this.createProgram(source, false, false, true)).orElse(this.terrain);
			this.generic = pipeline.getDHGenericShader().map(source -> this.createProgram(source, true, false, false)).orElse(null);
			if (renderShadows && pipeline.getDHShadowShader().isPresent()) {
				var source = pipeline.getDHShadowShader().get();
				this.shadow = this.createProgram(source, false, true, false);
				this.genericShadow = this.createProgram(source, true, true, false);
			}
		} catch (RuntimeException | Error failure) {
			this.close();
			throw failure;
		}
	}

	public static DHCompatInternal current() {
		var pipeline = Iris.getPipelineManager().getPipelineNullable();
		var compat = pipeline == null ? null : pipeline.getDHCompat();
		var internal = compat == null ? null : compat.getInstance();
		return internal == null || internal.closed ? null : internal;
	}

	public static boolean hasRenderingEnabled() {
		return DhApi.Delayed.configs != null && DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
	}

	public static int getRenderDistance() {
		return DhApi.Delayed.configs == null ? Minecraft.getInstance().options.getEffectiveRenderDistance() : DhApi.Delayed.configs.graphics()
			.chunkRenderDistance().getValue() * 16;
	}

	public static float getFarPlane() {
		return (float) ((getRenderDistance() + 512) * Math.sqrt(2));
	}

	public static float getNearPlane() {
		return DhApi.Delayed.renderProxy == null ? 0.01f : Math.max(0.01f,
			DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(CapturedRenderingState.INSTANCE.getRealTickDelta()));
	}

	private IrisDhProgram createProgram(ProgramSource source, boolean generic, boolean shadow, boolean translucent) {
		var program = new IrisDhProgram(this, this.pipeline, source, generic, shadow, translucent);
		this.programs.add(program);
		return program;
	}

	public void prepare() {
		this.programs.forEach(IrisDhProgram::prepare);
	}

	public boolean incompatiblePack() {
		return this.terrain == null;
	}

	public boolean hasShadowShader() {
		return this.shadow != null;
	}

	public void setup(DhApiRenderParam params) {
		if (this.incompatiblePack()) {
			return;
		}
		this.resizeDepth();
		boolean shadow = ShadowRenderingState.areShadowsCurrentlyBeingRendered();
		this.modelView.set(shadow ? ShadowRenderer.MODELVIEW : CapturedRenderingState.INSTANCE.getGbufferModelView());
		this.modelView.invert(this.modelViewInverse);
		this.modelView.normal(this.normalMatrix);
		this.projection.set(shadow ? MatrixUtils.toMinusOneToOne(ShadowRenderer.PROJECTION) : DHCompat.getProjection(params.nearClipPlane,
			params.farClipPlane));
		this.projection.invert(this.projectionInverse);
		this.clipDistance = shadow ? 0 : params.nearClipPlane;
	}

	private void resizeDepth() {
		var main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		if (this.depth != null && this.depthView.getWidth(0) == main.width && this.depthView.getHeight(0) == main.height) {
			return;
		}
		this.closeDepth();
		var device = RenderSystem.getDevice();
		int usage = GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST;
		this.depth = device.createTexture("DH depth", usage, GpuFormat.D32_FLOAT, main.width, main.height, 1, 1);
		this.opaqueDepth = device.createTexture("DH depth / Opaque", usage, GpuFormat.D32_FLOAT, main.width, main.height, 1, 1);
		this.depthView = device.createTextureView(this.depth);
		this.opaqueDepthView = device.createTextureView(this.opaqueDepth);
		for (var program : this.programs) {
			if (program != this.shadow && program != this.genericShadow) {
				program.getFramebuffer().addDepthAttachment(this.depth);
			}
		}
		this.clearDepth();
	}

	public void clearDepth() {
		if (this.depth == null || ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return;
		}
		var encoder = RenderSystem.getDevice().createCommandEncoder();
		encoder.clearDepthTexture(this.depth, 0);
		encoder.clearDepthTexture(this.opaqueDepth, 0);
	}

	public void renderTerrain(DhApiRenderParam params, boolean opaque, IDhApiReadOnlyList<? extends IDhApiTerrainBufferContainer> buffers) {
		boolean shadow = ShadowRenderingState.areShadowsCurrentlyBeingRendered();
		var program = shadow ? this.shadow : opaque ? this.terrain : this.water;
		if (program == null) {
			return;
		}
		if (!opaque && !shadow) {
			Iris.getRenderBackend().commands()
				.copyTexture(this.depth, this.opaqueDepth, this.depthView.getWidth(0), this.depthView.getHeight(0));
		}
		BlazeBlockTextureAtlas.INSTANCE.uploadPendingTiles();
		ApiEventInjector.INSTANCE.fireAllEvents(DhApiBeforeRenderPassEvent.class, params);
		var camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
		for (int i = 0; i < buffers.size(); i++) {
			var container = (LodBufferContainer) buffers.get(i);
			var origin = container.minCornerBlockPos;
			this.modelOffset.set((float) (origin.getX() - camera.x), (float) (origin.getY() - camera.y),
				(float) (origin.getZ() - camera.z));
			this.bufferEvent.update(params, new DhApiVec3f(this.modelOffset.x, this.modelOffset.y, this.modelOffset.z));
			ApiEventInjector.INSTANCE.fireAllEvents(DhApiBeforeBufferRenderEvent.class, this.bufferEvent);
			var vertices = opaque ? container.getVboOpaqueWrappers() : container.getVboTransparentWrappers();
			for (var wrapper : vertices) {
				var buffer = (IrisDhVertexBuffer) wrapper;
				if (buffer.getVertexCount() != 0) {
					program.draw(buffer.getVertexBuffer(), buffer.getIndexBuffer(), buffer.getIndexType(), buffer.getIndexCount());
				}
			}
		}
		if (opaque && !shadow) {
			Iris.getRenderBackend().commands()
				.copyTexture(this.depth, this.opaqueDepth, this.depthView.getWidth(0), this.depthView.getHeight(0));
		}
	}

	public void renderGeneric(DhApiRenderParam params, IDhApiCustomRenderRegister register, boolean ssao) {
		boolean shadow = ShadowRenderingState.areShadowsCurrentlyBeingRendered();
		var program = shadow ? this.genericShadow : this.generic;
		if (program == null) {
			return;
		}
		ApiEventInjector.INSTANCE.fireAllEvents(DhApiBeforeGenericRenderSetupEvent.class, params);
		var camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
		for (var group : register.getRenderBoxList()) {
			if (group == null || group.isSsaoEnabled() != ssao) {
				continue;
			}
			var boxGroup = (RenderableBoxGroup) group;
			boxGroup.preRender(params);
			if (!group.isActive() || this.avoidRenderingClouds() && group.getResourceLocationPath().equalsIgnoreCase("Clouds")) {
				continue;
			}
			this.genericEvent.update(params, group);
			if (ApiEventInjector.INSTANCE.fireAllEvents(DhApiBeforeGenericObjectRenderEvent.class, this.genericEvent)) {
				continue;
			}
			boxGroup.tryUpdateInstancedDataAsync(params);
			if (boxGroup.vertexBufferContainer.getState() != IDhApiGenericObjectVertexBufferContainer.EState.RENDER) {
				continue;
			}
			var buffer = (IrisDhGenericBuffer) boxGroup.vertexBufferContainer;
			var origin = group.getOriginBlockPos();
			this.modelOffset.set((float) (origin.x - camera.x), (float) (origin.y - camera.y), (float) (origin.z - camera.z));
			this.skyLight = group.getSkyLight();
			this.blockLight = group.getBlockLight();
			if (buffer.getVertexCount() != 0) {
				program.draw(buffer.getVertexBuffer(), buffer.getIndexBuffer(), buffer.getIndexType(), buffer.getIndexCount());
			}
			boxGroup.postRender(params);
		}
		ApiEventInjector.INSTANCE.fireAllEvents(DhApiBeforeGenericRenderCleanupEvent.class, params);
	}

	private boolean avoidRenderingClouds() {
		return this.pipeline.getDHCloudSetting() == CloudSetting.OFF || (this.pipeline.getDHCloudSetting() == CloudSetting.DEFAULT && this.pipeline.getCloudSetting() == CloudSetting.OFF);
	}

	public GpuTextureView getDepthTexture(boolean opaque) {
		return opaque ? this.opaqueDepthView : this.depthView;
	}

	public GpuTextureView getBlockAtlas() {
		var atlas = BlazeBlockTextureAtlas.INSTANCE.getTextureWrapper().getTextureView();
		return atlas == null ? this.pipeline.getWhitePixel().getTextureView() : atlas;
	}

	public Vector3f getModelOffset() {
		return this.modelOffset;
	}

	public Matrix4f getModelView() {
		return this.modelView;
	}

	public Matrix4f getModelViewInverse() {
		return this.modelViewInverse;
	}

	public Matrix4f getProjection() {
		return this.projection;
	}

	public Matrix4f getProjectionInverse() {
		return this.projectionInverse;
	}

	public Matrix3f getNormalMatrix() {
		return this.normalMatrix;
	}

	public float getClipDistance() {
		return this.clipDistance;
	}

	public int getSkyLight() {
		return this.skyLight;
	}

	public int getBlockLight() {
		return this.blockLight;
	}

	private void closeDepth() {
		if (this.depth != null) {
			this.depthView.close();
			this.opaqueDepthView.close();
			this.depth.close();
			this.opaqueDepth.close();
		}
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		this.programs.forEach(IrisDhProgram::close);
		this.programs.clear();
		this.closeDepth();
	}
}
