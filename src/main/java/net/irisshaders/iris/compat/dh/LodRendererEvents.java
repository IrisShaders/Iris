package net.irisshaders.iris.compat.dh;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiRenderPass;
import com.seibel.distanthorizons.api.enums.rendering.EFogDrawMode;
import com.seibel.distanthorizons.api.interfaces.override.IDhApiOverrideable;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShadowCullingFrustum;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeApplyShaderRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeBufferRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeDeferredRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderCleanupEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderPassEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderSetupEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeTextureClearEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiScreenResizeEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL46C;

public class LodRendererEvents {
	private static boolean eventHandlersBound = false;

	private static boolean atTranslucent = false;
	private static int textureWidth;
	private static int textureHeight;


	// constructor //

	public static void setupEventHandlers() {
		if (!eventHandlersBound) {
			eventHandlersBound = true;
			Iris.logger.info("Queuing DH event binding...");

			DhApiAfterDhInitEvent beforeCleanupEvent = new DhApiAfterDhInitEvent() {
				@Override
				public void afterDistantHorizonsInit(DhApiEventParam<Void> event) {
					Iris.logger.info("DH Ready, binding Iris event handlers...");

					setupSetDeferredBeforeRenderingEvent();
					setupReconnectDepthTextureEvent();
					setupCreateDepthTextureEvent();
					setupTransparentRendererEventCancling();
					setupBeforeBufferClearEvent();
					setupBeforeRenderCleanupEvent();
					beforeBufferRenderEvent();
					setupBeforeRenderFrameBufferBinding();
					setupBeforeRenderPassEvent();
					setupBeforeApplyShaderEvent();
					DHCompatInternal.dhEnabled = DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
					Iris.logger.info("DH Iris events bound.");
				}
			};
			DhApi.events.bind(DhApiAfterDhInitEvent.class, beforeCleanupEvent);
		}
	}


	// setup event handlers //


	private static void setupSetDeferredBeforeRenderingEvent() {
		DhApiBeforeRenderEvent beforeRenderEvent = new DhApiBeforeRenderEvent() {
			// this event is called before DH starts any rendering prep
			// canceling it will prevent DH from rendering for that frame
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {

				DhApi.Delayed.renderProxy.setDeferTransparentRendering(IrisApi.getInstance().isShaderPackInUse() && getInstance().shouldOverride);
				DhApi.Delayed.configs.graphics().fog().drawMode().setValue(getInstance().shouldOverride ? EFogDrawMode.FOG_DISABLED : EFogDrawMode.FOG_ENABLED);
			}
		};

		DhApi.events.bind(DhApiBeforeRenderEvent.class, beforeRenderEvent);
	}

	private static void setupReconnectDepthTextureEvent() {
		DhApiBeforeTextureClearEvent beforeRenderEvent = new DhApiBeforeTextureClearEvent() {
			@Override
			public void beforeClear(DhApiCancelableEventParam<DhApiRenderParam> event) {
				var getResult = DhApi.Delayed.renderProxy.getDhDepthTextureId();
				if (getResult.success) {
					int depthTextureId = getResult.payload;
					getInstance().reconnectDHTextures(depthTextureId);
				}
			}
		};

		DhApi.events.bind(DhApiBeforeTextureClearEvent.class, beforeRenderEvent);
	}

	private static DHCompatInternal getInstance() {
		return (DHCompatInternal) Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::getDHCompat).map(DHCompat::getInstance).orElse(DHCompatInternal.SHADERLESS);
	}

	private static void setupCreateDepthTextureEvent() {
		DhApiScreenResizeEvent beforeRenderEvent = new DhApiScreenResizeEvent() {
			@Override
			public void onResize(DhApiEventParam<EventParam> input) {
				textureWidth = input.value.newWidth;
				textureHeight = input.value.newHeight;
				getInstance().createDepthTex(textureWidth, textureHeight);
			}
		};

		DhApi.events.bind(DhApiScreenResizeEvent.class, beforeRenderEvent);
	}

	private static void setupTransparentRendererEventCancling() {
		DhApiBeforeRenderEvent beforeRenderEvent = new DhApiBeforeRenderEvent() {
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
				if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && (!getInstance().shouldOverrideShadow)) {
					event.cancelEvent();
				}
			}
		};
		DhApiBeforeDeferredRenderEvent beforeRenderEvent2 = new DhApiBeforeDeferredRenderEvent() {
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
				if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && (!getInstance().shouldOverrideShadow)) {
					event.cancelEvent();
				}
			}
		};

		DhApi.events.bind(DhApiBeforeRenderEvent.class, beforeRenderEvent);
		DhApi.events.bind(DhApiBeforeDeferredRenderEvent.class, beforeRenderEvent2);
	}

	private static void setupBeforeRenderCleanupEvent() {
		DhApiBeforeRenderCleanupEvent beforeCleanupEvent = new DhApiBeforeRenderCleanupEvent() {
			@Override
			public void beforeCleanup(DhApiEventParam<DhApiRenderParam> event) {
				if (getInstance().shouldOverride) {
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
						getInstance().getShadowShader().unbind();
					} else {
						getInstance().getSolidShader().unbind();
					}
				}
			}
		};

		DhApi.events.bind(DhApiBeforeRenderCleanupEvent.class, beforeCleanupEvent);
	}

	private static void setupBeforeBufferClearEvent() {
		DhApiBeforeTextureClearEvent beforeCleanupEvent = new DhApiBeforeTextureClearEvent() {
			@Override
			public void beforeClear(DhApiCancelableEventParam<DhApiRenderParam> event) {
				if (event.value.renderPass == EDhApiRenderPass.OPAQUE) {
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
						event.cancelEvent();
					} else if (getInstance().shouldOverride) {
						GL43C.glClear(GL43C.GL_DEPTH_BUFFER_BIT);
						event.cancelEvent();
					}
				}
			}
		};

		DhApi.events.bind(DhApiBeforeTextureClearEvent.class, beforeCleanupEvent);
	}

	private static void beforeBufferRenderEvent() {
		DhApiBeforeBufferRenderEvent beforeCleanupEvent = new DhApiBeforeBufferRenderEvent() {
			@Override
			public void beforeRender(DhApiEventParam<EventParam> input) {
				DHCompatInternal instance = getInstance();
				if (instance.shouldOverride) {
					Vec3f modelPos = input.value.modelPos;
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
						instance.getShadowShader().bind();
						instance.getShadowShader().setModelPos(modelPos);
					} else if (atTranslucent) {
						instance.getTranslucentShader().bind();
						instance.getTranslucentShader().setModelPos(modelPos);
					} else {
						instance.getSolidShader().bind();
						instance.getSolidShader().setModelPos(modelPos);
					}
				}
			}
		};

		DhApi.events.bind(DhApiBeforeBufferRenderEvent.class, beforeCleanupEvent);
	}

	private static void setupBeforeRenderFrameBufferBinding() {
		DhApiBeforeRenderSetupEvent beforeRenderPassEvent = new DhApiBeforeRenderSetupEvent() {
			@Override
			public void beforeSetup(DhApiEventParam<DhApiRenderParam> event) {
				DHCompatInternal instance = getInstance();

				OverrideInjector.INSTANCE.unbind(IDhApiShadowCullingFrustum.class, (IDhApiOverrideable) ShadowRenderer.FRUSTUM);
				OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, instance.getShadowFBWrapper());
				OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, instance.getSolidFBWrapper());

				if (instance.shouldOverride) {
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && instance.shouldOverrideShadow) {
						OverrideInjector.INSTANCE.bind(IDhApiFramebuffer.class, instance.getShadowFBWrapper());
						OverrideInjector.INSTANCE.bind(IDhApiShadowCullingFrustum.class, (IDhApiOverrideable) ShadowRenderer.FRUSTUM);
					} else {
						OverrideInjector.INSTANCE.bind(IDhApiFramebuffer.class, instance.getSolidFBWrapper());
					}
				}
			}
		};
		DhApi.events.bind(DhApiBeforeRenderSetupEvent.class, beforeRenderPassEvent);

	}

	private static void setupBeforeRenderPassEvent() {
		DhApiBeforeRenderPassEvent beforeCleanupEvent = new DhApiBeforeRenderPassEvent() {
			@Override
			public void beforeRender(DhApiEventParam<DhApiRenderParam> event) {
				DHCompatInternal instance = getInstance();

				// config overrides
				if (instance.shouldOverride) {
					DhApi.Delayed.configs.graphics().ambientOcclusion().enabled().setValue(false);
					DhApi.Delayed.configs.graphics().fog().drawMode().setValue(EFogDrawMode.FOG_DISABLED);

					if (event.value.renderPass == EDhApiRenderPass.OPAQUE_AND_TRANSPARENT) {
						Iris.logger.error("Unexpected; somehow the Opaque + Translucent pass ran with shaders on.");
					}
				} else {
					DhApi.Delayed.configs.graphics().ambientOcclusion().enabled().clearValue();
					DhApi.Delayed.configs.graphics().fog().drawMode().clearValue();
				}


				// cleanup
				if (event.value.renderPass == EDhApiRenderPass.OPAQUE) {
					if (instance.shouldOverride) {
						if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
							instance.getShadowShader().bind();
						} else {
							instance.getSolidShader().bind();
						}
						atTranslucent = false;
					}
				}


				// opaque
				if (event.value.renderPass == EDhApiRenderPass.OPAQUE) {
					float partialTicks = event.value.partialTicks;

					if (instance.shouldOverride) {
						if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
							instance.getShadowShader().fillUniformData(
								ShadowRenderer.PROJECTION, ShadowRenderer.MODELVIEW,
								-1000, //MC.getWrappedClientLevel().getMinHeight(),
								partialTicks);
						} else {
							Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
							//float nearClip = DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(partialTicks);
							//float farClip = (float) ((double) (DHCompatInternal.getDhBlockRenderDistance() + 512) * Math.sqrt(2.0));

							//Iris.logger.info("event near clip: "+event.value.nearClipPlane+" event far clip: "+event.value.farClipPlane+
							//	" \niris near clip: "+nearClip+" iris far clip: "+farClip);

							instance.getSolidShader().fillUniformData(
								new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), event.value.nearClipPlane, event.value.farClipPlane),
								CapturedRenderingState.INSTANCE.getGbufferModelView(),
								-1000, //MC.getWrappedClientLevel().getMinHeight(),
								partialTicks);
						}
					}
				}


				// transparent
				if (event.value.renderPass == EDhApiRenderPass.TRANSPARENT) {
					float partialTicks = event.value.partialTicks;
					int depthTextureId = DhApi.Delayed.renderProxy.getDhDepthTextureId().payload;

					if (instance.shouldOverrideShadow && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
						instance.getShadowShader().bind();
						instance.getShadowFB().bind();
						atTranslucent = true;

						return;
					}

					if (instance.shouldOverride && instance.getTranslucentFB() != null) {
						instance.copyTranslucents(textureWidth, textureHeight);
						instance.getTranslucentShader().bind();
						Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
						//float nearClip = DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(partialTicks);
						//float farClip = (float) ((double) (DHCompatInternal.getDhBlockRenderDistance() + 512) * Math.sqrt(2.0));
						GL46C.glDisable(GL46C.GL_CULL_FACE);
						//Iris.logger.info("event near clip: "+event.value.nearClipPlane+" event far clip: "+event.value.farClipPlane+
						//	" \niris near clip: "+nearClip+" iris far clip: "+farClip);

						instance.getTranslucentShader().fillUniformData(
							new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), event.value.nearClipPlane, event.value.farClipPlane),
							CapturedRenderingState.INSTANCE.getGbufferModelView(),
							-1000, //MC.getWrappedClientLevel().getMinHeight(),
							partialTicks);

						instance.getTranslucentFB().bind();
					}

					atTranslucent = true;
				}

			}
		};

		DhApi.events.bind(DhApiBeforeRenderPassEvent.class, beforeCleanupEvent);
	}

	private static void setupBeforeApplyShaderEvent() {
		DhApiBeforeApplyShaderRenderEvent beforeApplyShaderEvent = new DhApiBeforeApplyShaderRenderEvent() {
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
				if (IrisApi.getInstance().isShaderPackInUse()) {
					DHCompatInternal instance = getInstance();

					OverrideInjector.INSTANCE.unbind(IDhApiShadowCullingFrustum.class, (IDhApiOverrideable) ShadowRenderer.FRUSTUM);
					OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, instance.getShadowFBWrapper());
					OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, instance.getSolidFBWrapper());

					event.cancelEvent();
				}
			}
		};

		DhApi.events.bind(DhApiBeforeApplyShaderRenderEvent.class, beforeApplyShaderEvent);
	}


}
