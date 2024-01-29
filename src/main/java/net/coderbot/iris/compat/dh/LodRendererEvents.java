package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EFogDrawMode;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.*;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.DepthCopyStrategy;
import net.coderbot.iris.pipeline.ShadowRenderer;
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
			@Override
			public void beforeRender(DhApiCancelableEventParam<EventParam> input)
			{
				DhApi.Delayed.renderProxy.setDeferTransparentRendering(IrisApi.getInstance().isShaderPackInUse());
			}
		};

		DhApi.events.bind(DhApiBeforeRenderEvent.class, beforeRenderEvent);
	}

	private static void setupReconnectDepthTextureEvent()
	{
		DhApiBeforeBufferClearEvent beforeRenderEvent = new DhApiBeforeBufferClearEvent()
		{
			@Override
			public void beforeClear(DhApiCancelableEventParam<Void> input)
			{
				var getResult = DhApi.Delayed.renderProxy.getDhDepthTextureId();
				if (getResult.success)
				{
					int depthTextureId = getResult.payload;
					DHCompatInternal.INSTANCE.reconnectDHTextures(depthTextureId);
				}
			}
		};

		DhApi.events.bind(DhApiBeforeBufferClearEvent.class, beforeRenderEvent);
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
			public void beforeRender(DhApiCancelableEventParam<EventParam> input)
			{
				if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && !DHCompatInternal.INSTANCE.shouldOverrideShadow)
				{
					input.cancelEvent();
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
			public void beforeCleanup()
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
		DhApiBeforeBufferClearEvent beforeCleanupEvent = new DhApiBeforeBufferClearEvent()
		{
			@Override
			public void beforeClear(DhApiCancelableEventParam<Void> input)
			{
				if (ShadowRenderingState.areShadowsCurrentlyBeingRendered())
				{
					input.cancelEvent();
				}
				else if (DHCompatInternal.INSTANCE.shouldOverride)
				{
					GL43C.glClear(GL43C.GL_DEPTH_BUFFER_BIT);
					input.cancelEvent();
				}
			}
		};

		DhApi.events.bind(DhApiBeforeBufferClearEvent.class, beforeCleanupEvent);
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
			public void beforeSetup(DhApiEventParam<EventParam> event)
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
			public void beforeRender(DhApiEventParam<EventParam> event)
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


				// cleanup
				if (event.value.renderPass == EDhApiRenderPass.Opaque)
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
				if (event.value.renderPass == EDhApiRenderPass.Opaque)
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
							float nearClip = RenderUtil.getNearClipPlaneDistanceInBlocks(partialTicks);
							float farClip = (float) ((double) (RenderUtil.getFarClipPlaneDistanceInBlocks() + 512) * Math.sqrt(2.0));

							DHCompatInternal.INSTANCE.getSolidShader().fillUniformData(
								new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), nearClip, farClip),
								CapturedRenderingState.INSTANCE.getGbufferModelView(),
								-1000, //MC.getWrappedClientLevel().getMinHeight(),
								partialTicks);
						}
					}
				}


				// transparent
				if (event.value.renderPass == EDhApiRenderPass.Transparent)
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
						DepthCopyStrategy.fastest(false).copy(DHCompatInternal.INSTANCE.getSolidFB(), depthTextureId, null, DHCompatInternal.INSTANCE.getDepthTexNoTranslucent(), textureWidth, textureHeight);
						DHCompatInternal.INSTANCE.getTranslucentShader().bind();
						Matrix4f projection = CapturedRenderingState.INSTANCE.getGbufferProjection();
						float nearClip = RenderUtil.getNearClipPlaneDistanceInBlocks(partialTicks);
						float farClip = (float) ((double) (RenderUtil.getFarClipPlaneDistanceInBlocks() + 512) * Math.sqrt(2.0));
						RenderSystem.disableCull();


						DHCompatInternal.INSTANCE.getTranslucentShader().fillUniformData(
							new Matrix4f().setPerspective(projection.perspectiveFov(), projection.m11() / projection.m00(), nearClip, farClip),
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



}
