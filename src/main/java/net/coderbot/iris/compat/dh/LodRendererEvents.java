package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiRenderPass;
import com.seibel.distanthorizons.api.enums.rendering.EFogDrawMode;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.*;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL43C;

public class LodRendererEvents
{
	private static boolean eventHandlersBound = false;

	private static boolean atTranslucent = false;
	private static int textureWidth;
	private static int textureHeight;



	// constructor //

	public static void setupEventHandlers()
	{
		if (!eventHandlersBound)
		{
			eventHandlersBound = true;
			Iris.logger.info("Queuing DH event binding...");

			DhApiAfterDhInitEvent beforeCleanupEvent = new DhApiAfterDhInitEvent()
			{
				@Override
				public void afterDistantHorizonsInit(DhApiEventParam<Void> event)
				{
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

					Iris.logger.info("DH Iris events bound.");
				}
			};
			DhApi.events.bind(DhApiAfterDhInitEvent.class, beforeCleanupEvent);
		}
	}



	// setup event handlers //

	private static void setupSetDeferredBeforeRenderingEvent()
	{
		DhApiBeforeRenderEvent beforeRenderEvent = new DhApiBeforeRenderEvent()
		{
			// this event is called before DH starts any rendering prep
			// canceling it will prevent DH from rendering for that frame
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event)
			{
				DhApi.Delayed.renderProxy.setDeferTransparentRendering(IrisApi.getInstance().isShaderPackInUse());
			}
		};

		DhApi.events.bind(DhApiBeforeRenderEvent.class, beforeRenderEvent);
	}

	private static void setupReconnectDepthTextureEvent()
	{
		DhApiBeforeTextureClearEvent beforeRenderEvent = new DhApiBeforeTextureClearEvent()
		{
			@Override
			public void beforeClear(DhApiCancelableEventParam<DhApiRenderParam> event)
			{
				var getResult = DhApi.Delayed.renderProxy.getDhDepthTextureId();
				if (getResult.success)
				{
					int depthTextureId = getResult.payload;
					DHCompatInternal.INSTANCE.reconnectDHTextures(depthTextureId);
				}
			}
		};

		DhApi.events.bind(DhApiBeforeTextureClearEvent.class, beforeRenderEvent);
	}

	private static void setupCreateDepthTextureEvent()
	{
		DhApiScreenResizeEvent beforeRenderEvent = new DhApiScreenResizeEvent()
		{
			@Override
			public void onResize(DhApiEventParam<EventParam> input)
			{
				textureWidth = input.value.newWidth;
				textureHeight = input.value.newHeight;
				DHCompatInternal.INSTANCE.createDepthTex(textureWidth, textureHeight);
			}
		};

		DhApi.events.bind(DhApiScreenResizeEvent.class, beforeRenderEvent);
	}

	private static void setupTransparentRendererEventCancling()
	{
		DhApiBeforeRenderEvent beforeRenderEvent = new DhApiBeforeRenderEvent()
		{
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event)
			{
				if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && !DHCompatInternal.INSTANCE.shouldOverrideShadow)
				{
					event.cancelEvent();
				}
			}
		};

		DhApi.events.bind(DhApiBeforeRenderEvent.class, beforeRenderEvent);
	}

	private static void setupBeforeRenderCleanupEvent()
	{
		DhApiBeforeRenderCleanupEvent beforeCleanupEvent = new DhApiBeforeRenderCleanupEvent()
		{
			@Override
			public void beforeCleanup(DhApiEventParam<DhApiRenderParam> event)
			{
				if (DHCompatInternal.INSTANCE.shouldOverride)
				{
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
					{
						DHCompatInternal.INSTANCE.getShadowShader().unbind();
					}
					else
					{
						DHCompatInternal.INSTANCE.getSolidShader().unbind();
					}
				}
			}
		};

		DhApi.events.bind(DhApiBeforeRenderCleanupEvent.class, beforeCleanupEvent);
	}

	private static void setupBeforeBufferClearEvent()
	{
		DhApiBeforeTextureClearEvent beforeCleanupEvent = new DhApiBeforeTextureClearEvent()
		{
			@Override
			public void beforeClear(DhApiCancelableEventParam<DhApiRenderParam> event)
			{
				if (event.value.renderPass == EDhApiRenderPass.OPAQUE)
				{
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
					{
						event.cancelEvent();
					}
					else if (DHCompatInternal.INSTANCE.shouldOverride)
					{
						GL43C.glClear(GL43C.GL_DEPTH_BUFFER_BIT);
						event.cancelEvent();
					}
				}
			}
		};

		DhApi.events.bind(DhApiBeforeTextureClearEvent.class, beforeCleanupEvent);
	}

	private static void beforeBufferRenderEvent()
	{
		DhApiBeforeBufferRenderEvent beforeCleanupEvent = new DhApiBeforeBufferRenderEvent()
		{
			@Override
			public void beforeRender(DhApiEventParam<EventParam> input)
			{
				if (DHCompatInternal.INSTANCE.shouldOverride)
				{
					Vec3f modelPos = input.value.modelPos;
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
					{
						DHCompatInternal.INSTANCE.getShadowShader().bind();
						DHCompatInternal.INSTANCE.getShadowShader().setModelPos(modelPos);
					}
					else if (atTranslucent)
					{
						DHCompatInternal.INSTANCE.getTranslucentShader().bind();
						DHCompatInternal.INSTANCE.getTranslucentShader().setModelPos(modelPos);
					}
					else
					{
						DHCompatInternal.INSTANCE.getSolidShader().bind();
						DHCompatInternal.INSTANCE.getSolidShader().setModelPos(modelPos);
					}
				}
			}
		};

		DhApi.events.bind(DhApiBeforeBufferRenderEvent.class, beforeCleanupEvent);
	}

	private static void setupBeforeRenderFrameBufferBinding()
	{
		DhApiBeforeRenderSetupEvent beforeRenderPassEvent = new DhApiBeforeRenderSetupEvent()
		{
			@Override
			public void beforeSetup(DhApiEventParam<DhApiRenderParam> event)
			{
				// doesn't unbind
				OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, DHCompatInternal.INSTANCE.getShadowFBWrapper());
				OverrideInjector.INSTANCE.unbind(IDhApiFramebuffer.class, DHCompatInternal.INSTANCE.getSolidFBWrapper());

				if (DHCompatInternal.INSTANCE.shouldOverride)
				{
					if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
					{
						OverrideInjector.INSTANCE.bind(IDhApiFramebuffer.class, DHCompatInternal.INSTANCE.getShadowFBWrapper());
					}
					else
					{
						OverrideInjector.INSTANCE.bind(IDhApiFramebuffer.class, DHCompatInternal.INSTANCE.getSolidFBWrapper());
					}
				}
			}
		};
		DhApi.events.bind(DhApiBeforeRenderSetupEvent.class, beforeRenderPassEvent);

	}

	private static void setupBeforeRenderPassEvent()
	{
		DhApiBeforeRenderPassEvent beforeCleanupEvent = new DhApiBeforeRenderPassEvent()
		{
			@Override
			public void beforeRender(DhApiEventParam<DhApiRenderParam> event)
			{
				// config overrides
				if (DHCompatInternal.INSTANCE.shouldOverride)
				{
					DhApi.Delayed.configs.graphics().ambientOcclusion().enabled().setValue(false);
					DhApi.Delayed.configs.graphics().fog().drawMode().setValue(EFogDrawMode.FOG_DISABLED);
				}
				else
				{
					DhApi.Delayed.configs.graphics().ambientOcclusion().enabled().clearValue();
					DhApi.Delayed.configs.graphics().fog().drawMode().clearValue();
				}



				if (event.value.renderPass == EDhApiRenderPass.OPAQUE_AND_TRANSPARENT)
				{
					Iris.logger.error("Unexpected ");
				}



				// cleanup
				if (event.value.renderPass == EDhApiRenderPass.OPAQUE)
				{
					if (DHCompatInternal.INSTANCE.shouldOverride)
					{
						if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
						{
							DHCompatInternal.INSTANCE.getShadowShader().bind();
						}
						else
						{
							DHCompatInternal.INSTANCE.getSolidShader().bind();
						}
						atTranslucent = false;
					}
				}


				// opaque
				if (event.value.renderPass == EDhApiRenderPass.OPAQUE)
				{
					float partialTicks = event.value.partialTicks;

					if (DHCompatInternal.INSTANCE.shouldOverride)
					{
						if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
						{
							DHCompatInternal.INSTANCE.getShadowShader().fillUniformData(
								ShadowRenderer.PROJECTION, ShadowRenderer.MODELVIEW,
								-1000, //MC.getWrappedClientLevel().getMinHeight(),
								partialTicks);
						}
						else
						{
							Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
							//float nearClip = DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(partialTicks);
							//float farClip = (float) ((double) (DHCompatInternal.getDhBlockRenderDistance() + 512) * Math.sqrt(2.0));

							//Iris.logger.info("event near clip: "+event.value.nearClipPlane+" event far clip: "+event.value.farClipPlane+
							//	" \niris near clip: "+nearClip+" iris far clip: "+farClip);

							DHCompatInternal.INSTANCE.getSolidShader().fillUniformData(
								new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), event.value.nearClipPlane, event.value.farClipPlane),
								CapturedRenderingState.INSTANCE.getGbufferModelView(),
								-1000, //MC.getWrappedClientLevel().getMinHeight(),
								partialTicks);
						}
					}
				}


				// transparent
				if (event.value.renderPass == EDhApiRenderPass.TRANSPARENT)
				{
					float partialTicks = event.value.partialTicks;
					int depthTextureId = DhApi.Delayed.renderProxy.getDhDepthTextureId().payload;

					if (DHCompatInternal.INSTANCE.shouldOverrideShadow && ShadowRenderingState.areShadowsCurrentlyBeingRendered())
					{
						DHCompatInternal.INSTANCE.getShadowShader().bind();
						DHCompatInternal.INSTANCE.getShadowFB().bind();
						atTranslucent = true;

						return;
					}

					if (DHCompatInternal.INSTANCE.shouldOverride && DHCompatInternal.INSTANCE.getTranslucentFB() != null)
					{
						DHCompatInternal.INSTANCE.copyTranslucents(textureWidth, textureHeight);
						DHCompatInternal.INSTANCE.getTranslucentShader().bind();
						Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
						//float nearClip = DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(partialTicks);
						//float farClip = (float) ((double) (DHCompatInternal.getDhBlockRenderDistance() + 512) * Math.sqrt(2.0));
						RenderSystem.disableCull();

						//Iris.logger.info("event near clip: "+event.value.nearClipPlane+" event far clip: "+event.value.farClipPlane+
						//	" \niris near clip: "+nearClip+" iris far clip: "+farClip);

						DHCompatInternal.INSTANCE.getTranslucentShader().fillUniformData(
							new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), event.value.nearClipPlane, event.value.farClipPlane),
							CapturedRenderingState.INSTANCE.getGbufferModelView(),
							-1000, //MC.getWrappedClientLevel().getMinHeight(),
							partialTicks);

						DHCompatInternal.INSTANCE.getTranslucentFB().bind();
					}

					atTranslucent = true;
				}

			}
		};

		DhApi.events.bind(DhApiBeforeRenderPassEvent.class, beforeCleanupEvent);
	}

	private static void setupBeforeApplyShaderEvent()
	{
		DhApiBeforeApplyShaderRenderEvent beforeApplyShaderEvent = new DhApiBeforeApplyShaderRenderEvent()
		{
			@Override
			public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event)
			{
				if (IrisApi.getInstance().isShaderPackInUse())
				{
					event.cancelEvent();
				}
			}
		};

		DhApi.events.bind(DhApiBeforeApplyShaderRenderEvent.class, beforeApplyShaderEvent);
	}


}
