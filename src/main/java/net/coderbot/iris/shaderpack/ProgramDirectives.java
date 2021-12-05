package net.coderbot.iris.shaderpack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendMode;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ProgramDirectives {
	private static final ImmutableList<String> LEGACY_RENDER_TARGETS = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS;

	private final int[] drawBuffers;
	private final float viewportScale;
	@Nullable
	private final AlphaTest alphaTestOverride;
	@Nullable
	private final BlendModeOverride blendModeOverride;
	private final ImmutableSet<Integer> mipmappedBuffers;
	private final ImmutableMap<Integer, Boolean> explicitFlips;

	ProgramDirectives(ProgramSource source, ShaderProperties properties, Set<Integer> supportedRenderTargets) {
		// DRAWBUFFERS is only detected in the fragment shader source code (.fsh).
		// If there's no explicit declaration, then by default /* DRAWBUFFERS:0 */ is inferred.
		// For SEUS v08 and SEUS v10 to work, this will need to be set to 01234567. However, doing this causes
		// TAA to break on Sildur's Vibrant Shaders, since gbuffers_skybasic lacks a DRAWBUFFERS directive, causing
		// undefined data to be written to colortex7.
		//
		// TODO: Figure out how to infer the DRAWBUFFERS directive when it is missing.
		drawBuffers = findDrawbuffersDirective(source.getFragmentSource()).orElse(new int[] { 0 });

		if (properties != null) {
			viewportScale = properties.getViewportScaleOverrides().getOrDefault(source.getName(), 1.0f);
			alphaTestOverride = properties.getAlphaTestOverrides().get(source.getName());
			blendModeOverride = properties.getBlendModeOverrides().get(source.getName());
			explicitFlips = source.getParent().getPackDirectives().getExplicitFlips(source.getName());
		} else {
			viewportScale = 1.0f;
			alphaTestOverride = null;
			blendModeOverride = null;
			explicitFlips = ImmutableMap.of();
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

	public Optional<AlphaTest> getAlphaTestOverride() {
		return Optional.ofNullable(alphaTestOverride);
	}

	@Nullable
	public BlendModeOverride getBlendModeOverride() {
		return blendModeOverride;
	}

	public ImmutableSet<Integer> getMipmappedBuffers() {
		return mipmappedBuffers;
	}

	public ImmutableMap<Integer, Boolean> getExplicitFlips() {
		return explicitFlips;
	}
}
