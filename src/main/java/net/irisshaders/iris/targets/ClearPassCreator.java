package net.irisshaders.iris.targets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL21C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearPassCreator {
	public static ImmutableList<ClearPass> createClearPasses(RenderTargets renderTargets, boolean fullClear,
															 PackRenderTargetDirectives renderTargetDirectives) {
		final int maxDrawBuffers = GlStateManager._getInteger(GL21C.GL_MAX_DRAW_BUFFERS);

		// Sort buffers by their clear color so we can group up glClear calls.
		Map<Vector2i, Map<ClearPassInformation, IntList>> clearByColor = new HashMap<>();

		renderTargetDirectives.getRenderTargetSettings().forEach((bufferI, settings) -> {
			// unboxed
			final int buffer = bufferI;

			if (fullClear || settings.shouldClear()) {
				Vector4f defaultClearColor;

				if (buffer == 0) {
					// colortex0 is cleared to the fog color (with 1.0 alpha) by default.
					defaultClearColor = null;
				} else if (buffer == 1) {
					// colortex1 is cleared to solid white (with 1.0 alpha) by default.
					defaultClearColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
				} else {
					// all other buffers are cleared to solid black (with 0.0 alpha) by default.
					defaultClearColor = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
				}

				RenderTarget target = renderTargets.get(buffer);
				if (target == null) return;
				Vector4f clearColor = settings.getClearColor().orElse(defaultClearColor);
				clearByColor.computeIfAbsent(new Vector2i(target.getWidth(), target.getHeight()), size -> new HashMap<>()).computeIfAbsent(new ClearPassInformation(clearColor, target.getWidth(), target.getHeight()), color -> new IntArrayList()).add(buffer);
			}
		});

		List<ClearPass> clearPasses = new ArrayList<>();

		clearByColor.forEach((passSize, vector4fIntListMap) -> vector4fIntListMap.forEach((clearInfo, buffers) -> {
			int startIndex = 0;

			while (startIndex < buffers.size()) {
				// clear up to the maximum number of draw buffers per each clear pass.
				// This allows us to handle having more than 8 buffers with the same clear color on systems with
				// a max draw buffers of 8 (ie, most systems).
				int[] clearBuffers = new int[Math.min(buffers.size() - startIndex, maxDrawBuffers)];

				for (int i = 0; i < clearBuffers.length; i++) {
					clearBuffers[i] = buffers.getInt(startIndex);
					startIndex++;
				}

				// No need to clear the depth buffer, since we're using Minecraft's depth buffer.
				clearPasses.add(new ClearPass(clearInfo.getColor(), clearInfo::getWidth, clearInfo::getHeight,
					renderTargets.createClearFramebuffer(true, clearBuffers), GL21C.GL_COLOR_BUFFER_BIT));

				clearPasses.add(new ClearPass(clearInfo.getColor(), clearInfo::getWidth, clearInfo::getHeight,
					renderTargets.createClearFramebuffer(false, clearBuffers), GL21C.GL_COLOR_BUFFER_BIT));
			}
		}));

		return ImmutableList.copyOf(clearPasses);
	}

	public static ImmutableList<ClearPass> createShadowClearPasses(ShadowRenderTargets renderTargets, boolean fullClear,
																   PackShadowDirectives renderTargetDirectives) {
		if (renderTargets == null) {
			return ImmutableList.of();
		}

		final int maxDrawBuffers = GlStateManager._getInteger(GL21C.GL_MAX_DRAW_BUFFERS);

		// Sort buffers by their clear color so we can group up glClear calls.
		Map<Vector4f, IntList> clearByColor = new HashMap<>();

		for (int i = 0; i < renderTargets.getRenderTargetCount(); i++) {
			if (renderTargets.get(i) != null) {
				// unboxed
				PackShadowDirectives.SamplingSettings settings = renderTargetDirectives.getColorSamplingSettings().get(i);

				if (fullClear || settings.getClear()) {
					Vector4f clearColor = settings.getClearColor();
					clearByColor.computeIfAbsent(clearColor, color -> new IntArrayList()).add(i);
				}
			}
		}

		List<ClearPass> clearPasses = new ArrayList<>();


		clearByColor.forEach((clearColor, buffers) -> {
			int startIndex = 0;

			while (startIndex < buffers.size()) {
				// clear up to the maximum number of draw buffers per each clear pass.
				// This allows us to handle having more than 8 buffers with the same clear color on systems with
				// a max draw buffers of 8 (ie, most systems).
				int[] clearBuffers = new int[Math.min(buffers.size() - startIndex, maxDrawBuffers)];

				for (int i = 0; i < clearBuffers.length; i++) {
					clearBuffers[i] = buffers.getInt(startIndex);
					startIndex++;
				}

				// No need to clear the depth buffer, since we're using Minecraft's depth buffer.
				clearPasses.add(new ClearPass(clearColor, renderTargets::getResolution, renderTargets::getResolution,
					renderTargets.createFramebufferWritingToAlt(clearBuffers), GL21C.GL_COLOR_BUFFER_BIT));

				clearPasses.add(new ClearPass(clearColor, renderTargets::getResolution, renderTargets::getResolution,
					renderTargets.createFramebufferWritingToMain(clearBuffers), GL21C.GL_COLOR_BUFFER_BIT));
			}
		});

		return ImmutableList.copyOf(clearPasses);
	}
}
