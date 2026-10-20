package net.irisshaders.iris.compat.dh;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.override.IDhApiOverrideable;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShadowCullingFrustum;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeDeferredRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderCleanupEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderSetupEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.DhApiResult;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import net.caffeinemc.mods.sodium.client.util.GameRendererStorage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4fc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

public class LodRendererEvents {
	private static IrisDhRenderDefinition definition;
	private static CompletableFuture<DhApiResult<String>> switchFuture;
	private static boolean requested;
	private static boolean applied;
	private static boolean previousDeferred;
	private static boolean previousRenderInjections;
	private static boolean manualRendering;
	private static boolean renderingEnabled;
	private static IDhApiOverrideable shadowFrustum;

	public static void setupEventHandlers() {
		DhApi.events.bind(DhApiAfterDhInitEvent.class, new DhApiAfterDhInitEvent() {
			@Override
			public void afterDistantHorizonsInit(DhApiEventParam<Void> event) {
				renderingEnabled = DHCompatInternal.hasRenderingEnabled();
				Iris.loadShaderpackWhenPossible();
			}
		});
		DhApi.events.bind(DhApiBeforeRenderEvent.class, new DhApiBeforeRenderEvent() {
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
				cancelUnsupportedPass(event);
			}
		});
		DhApi.events.bind(DhApiBeforeDeferredRenderEvent.class, new DhApiBeforeDeferredRenderEvent() {
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
				cancelUnsupportedPass(event);
			}
		});
		DhApi.events.bind(DhApiBeforeRenderSetupEvent.class, new DhApiBeforeRenderSetupEvent() {
			@Override
			public void beforeSetup(DhApiEventParam<DhApiRenderParam> event) {
				clearShadowFrustum();
				if (event.value.renderDefinition == definition && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
					shadowFrustum = (IDhApiOverrideable) ShadowRenderer.FRUSTUM;
					DhApi.overrides.bind(IDhApiShadowCullingFrustum.class, shadowFrustum);
				}
			}
		});
		DhApi.events.bind(DhApiBeforeRenderCleanupEvent.class, new DhApiBeforeRenderCleanupEvent() {
			@Override
			public void beforeCleanup(DhApiEventParam<DhApiRenderParam> event) {
				clearShadowFrustum();
			}
		});
	}

	public static void updateRenderer() {
		if (DhApi.Delayed.renderProxy == null) {
			return;
		}
		if (Iris.isPackInUseQuick() && !manualRendering) {
			previousRenderInjections = DhApi.Delayed.renderProxy.getUseDhRenderInjections();
			DhApi.Delayed.renderProxy.setUseDhRenderInjections(false);
			manualRendering = true;
		}
		if (switchFuture != null) {
			if (!switchFuture.isDone()) {
				return;
			}
			var result = switchFuture.join();
			switchFuture = null;
			if (!result.success) {
				throw new IllegalStateException("unable to switch DH renderer: " + result.message);
			}
			applied = requested;
			if (!applied) {
				DhApi.Delayed.renderProxy.setDeferTransparentRendering(previousDeferred);
			}
		}
		boolean enabled = DHCompatInternal.hasRenderingEnabled();
		if (renderingEnabled != enabled) {
			renderingEnabled = enabled;
			if (Iris.isPackInUseQuick()) {
				try {
					Iris.reload();
				} catch (IOException e) {
					throw new UncheckedIOException("unable to reload DH shaders", e);
				}
			}
		}
		var compat = DHCompatInternal.current();
		boolean desired = compat != null && !compat.incompatiblePack() && DHCompatInternal.hasRenderingEnabled();
		if (desired == applied) {
			if (manualRendering && !Iris.isPackInUseQuick() && !applied) {
				DhApi.Delayed.renderProxy.setUseDhRenderInjections(previousRenderInjections);
				manualRendering = false;
			}
			return;
		}
		if (definition == null) {
			definition = new IrisDhRenderDefinition();
		}
		requested = desired;
		if (desired) {
			previousDeferred = DhApi.Delayed.renderProxy.getDeferTransparentRendering();
			DhApi.Delayed.renderProxy.setDeferTransparentRendering(true);
		}
		switchFuture = desired ? DhApi.Delayed.renderProxy.setRenderDefinitionOverrideAsync(definition,
			"Iris") : DhApi.Delayed.renderProxy.clearRenderDefinitionOverrideAsync();
	}

	public static void renderLodPass(boolean deferred) {
		if (!manualRendering || DhApi.Delayed.renderProxy == null || DhApi.Delayed.worldProxy == null || !DhApi.Delayed.worldProxy.worldLoaded()) {
			return;
		}
		var level = DhApi.Delayed.worldProxy.getSinglePlayerLevel();
		if (level == null) {
			return;
		}
		boolean shadow = ShadowRenderingState.areShadowsCurrentlyBeingRendered();
		var state = CapturedRenderingState.INSTANCE;
		var modelView = shadow ? ShadowRenderer.MODELVIEW : state.getGbufferModelView();
		var projection = shadow ? ShadowRenderer.PROJECTION
			: ((GameRendererStorage) Minecraft.getInstance().gameRenderer).sodium$getProjectionMatrix();
		DhApi.Delayed.renderProxy.renderLodPass(toDhMatrix(modelView), toDhMatrix(projection), state.getRealTickDelta(), level, deferred);
	}

	private static DhApiMat4f toDhMatrix(Matrix4fc matrix) {
		var result = new DhApiMat4f(matrix.get(new float[16]));
		result.transpose();
		return result;
	}

	private static void cancelUnsupportedPass(DhApiCancelableEventParam<DhApiRenderParam> event) {
		var compat = DHCompatInternal.current();
		if (Iris.isPackInUseQuick() && (compat == null || compat.incompatiblePack())) {
			event.cancelEvent();
		} else if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && (compat == null || !compat.hasShadowShader() || event.value.renderDefinition != definition)) {
			event.cancelEvent();
		} else if ((event.value.renderDefinition == definition && (compat == null || compat.incompatiblePack())) || (compat != null && !compat.incompatiblePack() && event.value.renderDefinition != definition)) {
			event.cancelEvent();
		}
	}

	private static void clearShadowFrustum() {
		if (shadowFrustum != null) {
			DhApi.overrides.unbind(IDhApiShadowCullingFrustum.class, shadowFrustum);
			shadowFrustum = null;
		}
	}
}
