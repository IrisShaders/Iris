package net.irisshaders.iris.shaderpack.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisLimits;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.shaderpack.parsing.DirectiveHolder;
import org.joml.Vector4f;

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

	// TODO: Instead of just passing this, the shader pack loader should try to figure out what color buffers are in
	//       use.
	public static final Set<Integer> BASELINE_SUPPORTED_RENDER_TARGETS;

	static {
		ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();

		for (int i = 0; i < IrisLimits.MAX_COLOR_BUFFERS; i++) {
			builder.add(i);
		}

		BASELINE_SUPPORTED_RENDER_TARGETS = builder.build();
	}

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

		// Note: This is still relevant even if shouldClear is false,
		// since this will be the initial color of the buffer.
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
			this.clearColor = null;
		}

		public InternalTextureFormat getInternalFormat() {
			return requestedFormat;
		}

		public boolean shouldClear() {
			return clear;
		}

		public Optional<Vector4f> getClearColor() {
			return Optional.ofNullable(clearColor);
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
