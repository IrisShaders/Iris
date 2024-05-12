package net.irisshaders.iris.shaderpack.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendInformation;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.shaderpack.parsing.CommentDirective;
import net.irisshaders.iris.shaderpack.parsing.CommentDirectiveParser;
import net.irisshaders.iris.shaderpack.parsing.ConstDirectiveParser;
import net.irisshaders.iris.shaderpack.parsing.DispatchingDirectiveHolder;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ProgramDirectives {
	private static final ImmutableList<String> LEGACY_RENDER_TARGETS = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS;

	private final int[] drawBuffers;
	private final ViewportData viewportScale;
	@Nullable
	private final AlphaTest alphaTestOverride;

	private final Optional<BlendModeOverride> blendModeOverride;
	private final List<BufferBlendInformation> bufferBlendInformations;
	private final ImmutableSet<Integer> mipmappedBuffers;
	private final ImmutableMap<Integer, Boolean> explicitFlips;
	private boolean unknownDrawBuffers;

	private ProgramDirectives(int[] drawBuffers, ViewportData viewportScale, @Nullable AlphaTest alphaTestOverride,
							  Optional<BlendModeOverride> blendModeOverride, List<BufferBlendInformation> bufferBlendInformations, ImmutableSet<Integer> mipmappedBuffers,
							  ImmutableMap<Integer, Boolean> explicitFlips) {
		this.drawBuffers = drawBuffers;
		this.viewportScale = viewportScale;
		this.alphaTestOverride = alphaTestOverride;
		this.blendModeOverride = blendModeOverride;
		this.bufferBlendInformations = bufferBlendInformations;
		this.mipmappedBuffers = mipmappedBuffers;
		this.explicitFlips = explicitFlips;
		this.unknownDrawBuffers = false;
	}

	public ProgramDirectives(ProgramSource source, ShaderProperties properties, Set<Integer> supportedRenderTargets,
							 @Nullable BlendModeOverride defaultBlendOverride) {
		// DRAWBUFFERS is only detected in the fragment shader source code (.fsh).
		// If there's no explicit declaration, then by default /* DRAWBUFFERS:0 */ is inferred.
		// For SEUS v08 and SEUS v10 to work, this will need to be set to 01234567. However, doing this causes
		// TAA to break on Sildur's Vibrant Shaders, since gbuffers_skybasic lacks a DRAWBUFFERS directive, causing
		// undefined data to be written to colortex7.
		//
		// TODO: Figure out how to infer the DRAWBUFFERS directive when it is missing.
		Optional<CommentDirective> optionalDrawbuffersDirective = findDrawbuffersDirective(source.getFragmentSource());
		Optional<CommentDirective> optionalRendertargetsDirective = findRendertargetsDirective(source.getFragmentSource());

		Optional<CommentDirective> optionalCommentDirective = getAppliedDirective(optionalDrawbuffersDirective, optionalRendertargetsDirective);
		drawBuffers = optionalCommentDirective.map(commentDirective -> {
			if (commentDirective.getType() == CommentDirective.Type.DRAWBUFFERS) {
				return parseDigits(commentDirective.getDirective().toCharArray());
			} else if (commentDirective.getType() == CommentDirective.Type.RENDERTARGETS) {
				return parseDigitList(commentDirective.getDirective());
			} else {
				throw new IllegalStateException("Unhandled comment directive type!");
			}
		}).orElseGet(() -> {
			unknownDrawBuffers = true;
			return new int[]{0};
		});

		if (properties != null) {
			viewportScale = properties.getViewportScaleOverrides().getOrDefault(source.getName(), ViewportData.defaultValue());
			alphaTestOverride = properties.getAlphaTestOverrides().get(source.getName());

			BlendModeOverride blendModeOverride = properties.getBlendModeOverrides().get(source.getName());
			List<BufferBlendInformation> bufferBlendInformations = properties.getBufferBlendOverrides().get(source.getName());
			this.blendModeOverride = Optional.ofNullable(blendModeOverride != null ? blendModeOverride : defaultBlendOverride);
			this.bufferBlendInformations = bufferBlendInformations != null ? bufferBlendInformations : Collections.emptyList();

			explicitFlips = source.getParent().getPackDirectives().getExplicitFlips(source.getName());
		} else {
			viewportScale = ViewportData.defaultValue();
			alphaTestOverride = null;
			blendModeOverride = Optional.ofNullable(defaultBlendOverride);
			bufferBlendInformations = Collections.emptyList();
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

	private static Optional<CommentDirective> findDrawbuffersDirective(Optional<String> stageSource) {
		return stageSource.flatMap(fragment -> CommentDirectiveParser.findDirective(fragment, CommentDirective.Type.DRAWBUFFERS));
	}

	private static Optional<CommentDirective> findRendertargetsDirective(Optional<String> stageSource) {
		return stageSource.flatMap(fragment -> CommentDirectiveParser.findDirective(fragment, CommentDirective.Type.RENDERTARGETS));
	}

	private static int[] parseDigits(char[] directiveChars) {
		int[] buffers = new int[directiveChars.length];
		int index = 0;

		for (char buffer : directiveChars) {
			buffers[index++] = Character.digit(buffer, 10);
		}

		return buffers;
	}

	private static int[] parseDigitList(String digitListString) {
		return Arrays.stream(digitListString.split(","))
			.mapToInt(Integer::parseInt)
			.toArray();
	}

	private static Optional<CommentDirective> getAppliedDirective(Optional<CommentDirective> optionalDrawbuffersDirective, Optional<CommentDirective> optionalRendertargetsDirective) {
		if (optionalDrawbuffersDirective.isPresent() && optionalRendertargetsDirective.isPresent()) {
			if (optionalDrawbuffersDirective.get().getLocation() > optionalRendertargetsDirective.get().getLocation()) {
				return optionalDrawbuffersDirective;
			} else {
				return optionalRendertargetsDirective;
			}
		} else if (optionalDrawbuffersDirective.isPresent()) {
			return optionalDrawbuffersDirective;
		} else {
			return optionalRendertargetsDirective;
		}
	}

	public ProgramDirectives withOverriddenDrawBuffers(int[] drawBuffersOverride) {
		return new ProgramDirectives(drawBuffersOverride, viewportScale, alphaTestOverride, blendModeOverride, bufferBlendInformations,
			mipmappedBuffers, explicitFlips);
	}

	public int[] getDrawBuffers() {
		return drawBuffers;
	}

	public boolean hasUnknownDrawBuffers() {
		return unknownDrawBuffers;
	}

	public ViewportData getViewportScale() {
		return viewportScale;
	}

	public Optional<AlphaTest> getAlphaTestOverride() {
		return Optional.ofNullable(alphaTestOverride);
	}

	public Optional<BlendModeOverride> getBlendModeOverride() {
		return blendModeOverride;
	}

	public List<BufferBlendInformation> getBufferBlendOverrides() {
		return bufferBlendInformations;
	}

	public ImmutableSet<Integer> getMipmappedBuffers() {
		return mipmappedBuffers;
	}

	public ImmutableMap<Integer, Boolean> getExplicitFlips() {
		return explicitFlips;
	}
}
