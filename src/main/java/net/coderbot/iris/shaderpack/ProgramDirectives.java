package net.coderbot.iris.shaderpack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.coderbot.iris.gl.blending.AlphaTestOverride;
import org.jetbrains.annotations.Nullable;

public class ProgramDirectives {
	private static final ImmutableList<String> LEGACY_RENDER_TARGETS = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS;

	private int[] drawBuffers;
	private float viewportScale;
	@Nullable
	private AlphaTestOverride alphaTestOverride;
	private boolean disableBlend;
	private ImmutableSet<Integer> mipmappedBuffers;

	ProgramDirectives(ProgramSource source, ShaderProperties properties, Set<Integer> supportedRenderTargets) {
		// First try to find it in the fragment source, then in the vertex source.
		// If there's no explicit declaration, then by default /* DRAWBUFFERS:0 */ is inferred.
		// TODO: ShadersMod appears to default to all buffers? Investigate this more closely.
		drawBuffers = findDrawbuffersDirective(source.getFragmentSource())
			.orElseGet(() -> findDrawbuffersDirective(source.getVertexSource()).orElse(new int[]{0}));
		viewportScale = 1.0f;

		if (properties != null) {
			viewportScale = properties.viewportScaleOverrides.getOrDefault(source.getName(), 1.0f);
			alphaTestOverride = properties.alphaTestOverrides.get(source.getName());
			disableBlend = properties.blendDisabled.contains(source.getName());
		}

		HashSet<Integer> mipmappedBuffers = new HashSet<>();
		DispatchingDirectiveHolder directiveHolder = new DispatchingDirectiveHolder();

		supportedRenderTargets.forEach(index -> {
			BooleanConsumer mipmapHandler = shouldMipmap -> {
				if (shouldMipmap) {
					mipmappedBuffers.add(index);
				} else {
					mipmappedBuffers.remove(index);
				}
			};

			directiveHolder.acceptConstBooleanDirective("colortex" + index + "MipmapEnabled", mipmapHandler);

			if (index < LEGACY_RENDER_TARGETS.size()) {
				directiveHolder.acceptConstBooleanDirective(LEGACY_RENDER_TARGETS.get(index) + "MipmapEnabled", mipmapHandler);
			}
		});

		source.getFragmentSource().map(ConstDirectiveParser::findDirectives).ifPresent(directives -> {
			for (ConstDirectiveParser.ConstDirective directive : directives) {
				directiveHolder.processDirective(directive);
			}
		});

		this.mipmappedBuffers = ImmutableSet.copyOf(mipmappedBuffers);
	}

	private static Optional<int[]> findDrawbuffersDirective(Optional<String> stageSource) {
		return stageSource
			.flatMap(fragment -> CommentDirectiveParser.findDirective(fragment, "DRAWBUFFERS"))
			.map(String::toCharArray)
			.map(ProgramDirectives::parseDigits);
	}

	private static int[] parseDigits(char[] directiveChars) {
		int[] buffers = new int[directiveChars.length];
		int index = 0;

		for (char buffer : directiveChars) {
			buffers[index++] = Character.digit(buffer, 10);
		}

		return buffers;
	}

	public int[] getDrawBuffers() {
		return drawBuffers;
	}

	public float getViewportScale() {
		return viewportScale;
	}

	public Optional<AlphaTestOverride> getAlphaTestOverride() {
		return Optional.ofNullable(alphaTestOverride);
	}

	public boolean shouldDisableBlend() {
		return disableBlend;
	}

	public ImmutableSet<Integer> getMipmappedBuffers() {
		return mipmappedBuffers;
	}
}
