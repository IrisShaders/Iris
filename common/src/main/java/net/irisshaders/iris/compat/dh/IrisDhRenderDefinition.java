package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthDirection;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthRange;
import com.seibel.distanthorizons.api.enums.config.EDhApiRenderingApi;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiCustomRenderRegister;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.AbstractDhApiRenderDefinition;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiAntiAliasRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiDebugWireframeRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiFarFadeRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiFogRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiGenericRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiMetaRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiSsaoRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiTerrainRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiTestTriangleRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.IDhApiVanillaFadeRenderer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiTerrainBufferContainer;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiTerrainContainerUniformBufferWrapper;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiVertexBufferWrapper;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.render.IDebugBox;
import com.seibel.distanthorizons.coreapi.interfaces.dependencyInjection.IOverrideInjector;
import net.irisshaders.iris.pipeline.GraphicsBackend;

public class IrisDhRenderDefinition extends AbstractDhApiRenderDefinition {
	private static final IDhApiTerrainContainerUniformBufferWrapper NO_UNIFORMS = new IDhApiTerrainContainerUniformBufferWrapper() {
		@Override
		public void tryUpload(IDhApiTerrainBufferContainer bufferContainer) {
		}

		@Override
		public void close() {
		}
	};

	private final IDhApiMetaRenderer metaRenderer = new IDhApiMetaRenderer() {
		@Override
		public void runRenderPassSetup(DhApiRenderParam params) {
			var compat = DHCompatInternal.current();
			if (compat != null) {
				compat.setup(params);
			}
		}

		@Override
		public void clearDhDepthAndColorTextures(DhApiRenderParam params) {
			var compat = DHCompatInternal.current();
			if (compat != null) {
				compat.clearDepth();
			}
		}

		@Override
		public void runRenderPassCleanup(DhApiRenderParam params) {
		}

		@Override
		public void copyToMcTexture(DhApiRenderParam params) {
		}
	};

	private final IDhApiTerrainRenderer terrainRenderer = (params, opaque, buffers) -> {
		var compat = DHCompatInternal.current();
		if (compat != null) {
			compat.renderTerrain(params, opaque, buffers);
		}
	};

	private final IDhApiGenericRenderer genericRenderer = new IDhApiGenericRenderer() {
		@Override
		public void render(DhApiRenderParam params, IDhApiCustomRenderRegister register, boolean ssao) {
			var compat = DHCompatInternal.current();
			if (compat != null) {
				compat.renderGeneric(params, register, ssao);
			}
		}

		@Override
		public void close() {
		}
	};

	private final IDhApiDebugWireframeRenderer debugRenderer = new IDhApiDebugWireframeRenderer() {
		@Override
		public void beginRenderBatch(DhApiRenderParam params) {
		}

		@Override
		public void renderBox(IDebugBox box) {
		}

		@Override
		public void endRenderBatch(DhApiRenderParam params) {
		}
	};

	@Override
	public String getName() {
		return "Iris";
	}

	@Override
	public int getPriority() {
		return IOverrideInjector.DEFAULT_NON_CORE_OVERRIDE_PRIORITY;
	}

	@Override
	public boolean isNativeRenderer() {
		return false;
	}

	@Override
	public EDhApiDepthDirection getDepthDirection() {
		return EDhApiDepthDirection.REVERSE_Z;
	}

	@Override
	public EDhApiDepthRange getDepthRange() {
		return RenderSystem.getDevice().getDeviceInfo()
			.isZZeroToOne() ? EDhApiDepthRange.ZERO_TO_POS_ONE : EDhApiDepthRange.NEG_ONE_TO_POS_ONE;
	}

	@Override
	public EDhApiRenderingApi getRenderApi() {
		return GraphicsBackend.isVulkan() ? EDhApiRenderingApi.VULKAN : EDhApiRenderingApi.OPEN_GL;
	}

	@Override
	public IDhApiMetaRenderer getMetaRenderer() {
		return this.metaRenderer;
	}

	@Override
	public IDhApiTerrainRenderer getTerrainRenderer() {
		return this.terrainRenderer;
	}

	@Override
	public IDhApiGenericRenderer getGenericRenderer() {
		return this.genericRenderer;
	}

	@Override
	public IDhApiSsaoRenderer getSsaoRenderer() {
		return params -> {
		};
	}

	@Override
	public IDhApiFogRenderer getFogRenderer() {
		return (params, fog) -> {
		};
	}

	@Override
	public IDhApiFarFadeRenderer getFarFadeRenderer() {
		return params -> {
		};
	}

	@Override
	public IDhApiAntiAliasRenderer getAntiAliasRenderer() {
		return params -> {
		};
	}

	@Override
	public IDhApiVanillaFadeRenderer getVanillaFadeRenderer() {
		return params -> {
		};
	}

	@Override
	public IDhApiTestTriangleRenderer getTestTriangleRenderer() {
		return params -> {
		};
	}

	@Override
	public IDhApiDebugWireframeRenderer getDebugWireframeRenderer() {
		return this.debugRenderer;
	}

	@Override
	public IDhApiVertexBufferWrapper createVboWrapper(String name) {
		return new IrisDhVertexBuffer(name);
	}

	@Override
	public IDhApiTerrainContainerUniformBufferWrapper createLodContainerUniformWrapper() {
		return NO_UNIFORMS;
	}

	@Override
	public IDhApiGenericObjectVertexBufferContainer createGenericObjectVboContainer() {
		return new IrisDhGenericBuffer();
	}
}
