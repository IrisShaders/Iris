package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.*;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PackRenderTargetDirectives {
	public static final ImmutableList<String> LEGACY_RENDER_TARGETS = ImmutableList.of(
		"gcolor",
		"gdepth",
		"gnormal",
		"composite",
		"gaux1",
		"gaux2",
		"gaux3",
		"gaux4"
	);

	// TODO: Support 16 render targets instead of just 8, we need other changes elsewhere first.
	public static final Set<Integer> BASELINE_SUPPORTED_RENDER_TARGETS = ImmutableSet.of(0, 1, 2, 3, 4, 5, 6, 7);

	private final Int2ObjectMap<RenderTargetSettings> renderTargetSettings;

	PackRenderTargetDirectives(Set<Integer> supportedRenderTargets) {
		this.renderTargetSettings = new Int2ObjectOpenHashMap<>();

		supportedRenderTargets.forEach(
				(index) -> renderTargetSettings.put(index.intValue(), new RenderTargetSettings()));
	}

	public IntList getBuffersToBeCleared() {
		IntList buffersToBeCleared = new IntArrayList();

		renderTargetSettings.forEach((index, settings) -> {
			if (settings.shouldClear()) {
				buffersToBeCleared.add(index.intValue());
			}
		});

		return buffersToBeCleared;
	}

	public Map<Integer, RenderTargetSettings> getRenderTargetSettings() {
		return Collections.unmodifiableMap(renderTargetSettings);
	}

	public void acceptDirectives(DirectiveHolder directives) {
		Optional.ofNullable(renderTargetSettings.get(7)).ifPresent(colortex7 -> {
			// Handle legacy GAUX4FORMAT directives

			directives.acceptCommentStringDirective("GAUX4FORMAT", format -> {
				if ("RGBA32F".equals(format)) {
					colortex7.requestedFormat = InternalTextureFormat.RGBA32F;
				} else if ("RGB32F".equals(format)) {
					colortex7.requestedFormat = InternalTextureFormat.RGB32F;
				} else if ("RGB16".equals(format)) {
					colortex7.requestedFormat = InternalTextureFormat.RGB16;
				} else {
					Iris.logger.warn("Ignoring GAUX4FORMAT directive /* GAUX4FORMAT:" + format + "*/ because " + format
						+ " must be RGBA32F, RGB32F, or RGB16. Use `const int colortex7Format = " + format + ";` + instead.");
				}
			});
		});

		// If a shaderpack declares a gdepth uniform (even if it is not actually sampled or even of the correct type),
		// we upgrade the format of gdepth / colortex1 to RGBA32F if it is currently RGBA.
		Optional.ofNullable(renderTargetSettings.get(1)).ifPresent(gdepth -> {
			directives.acceptUniformDirective("gdepth", () -> {
				if (gdepth.requestedFormat == InternalTextureFormat.RGBA) {
					gdepth.requestedFormat = InternalTextureFormat.RGBA32F;
				}
			});
		});

		renderTargetSettings.forEach((index, settings) -> {
			acceptBufferDirectives(directives, settings, "colortex" + index);

			if (index < LEGACY_RENDER_TARGETS.size()) {
				acceptBufferDirectives(directives, settings, LEGACY_RENDER_TARGETS.get(index));
			}
		});
	}

	private void acceptBufferDirectives(DirectiveHolder directives, RenderTargetSettings settings, String bufferName) {
		directives.acceptConstStringDirective(bufferName + "Format", format -> {
			Optional<InternalTextureFormat> internalFormat = InternalTextureFormat.fromString(format);

			if (internalFormat.isPresent()) {
				settings.requestedFormat = internalFormat.get();
			} else {
				Iris.logger.warn("Unrecognized internal texture format " + format + " specified for " + bufferName + "Format, ignoring.");
			}
		});

		// TODO: Only for composite and deferred
		directives.acceptConstBooleanDirective(bufferName + "Clear",
				shouldClear -> settings.clear = shouldClear);

		// TODO: Only for composite, deferred, and final
		// TODO: what happens if clear = false but clearColor is specified?
		directives.acceptConstVec4Directive(bufferName + "ClearColor",
				clearColor -> settings.clearColor = clearColor);
	}

	public static final class RenderTargetSettings {
		private InternalTextureFormat requestedFormat;
		private boolean clear;
		private Vector4f clearColor;

		public RenderTargetSettings() {
			this.requestedFormat = InternalTextureFormat.RGBA;
			this.clear = true;
			this.clearColor = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
		}

		public InternalTextureFormat getRequestedFormat() {
			return requestedFormat;
		}

		public boolean shouldClear() {
			return clear;
		}

		public Vector4f getClearColor() {
			return clearColor;
		}

		@Override
		public String toString() {
			return "RenderTargetSettings{" +
					"requestedFormat=" + requestedFormat +
					", clear=" + clear +
					", clearColor=" + clearColor +
					'}';
		}
	}
}
