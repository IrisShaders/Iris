package net.coderbot.iris.shaderpack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.coderbot.iris.gl.blending.AlphaTest;
import org.jetbrains.annotations.Nullable;

public class ProgramDirectives {
	private static final ImmutableList<String> LEGACY_RENDER_TARGETS = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS;
	private static boolean isRenderTarget;

	private final int[] drawBuffers;
	private final float viewportScale;
	@Nullable
	private final AlphaTest alphaTestOverride;
	private final boolean disableBlend;
	private final ImmutableSet<Integer> mipmappedBuffers;

	ProgramDirectives(ProgramSource source, ShaderProperties properties, Set<Integer> supportedRenderTargets) {
		// DRAWBUFFERS is only detected in the fragment shader source code (.fsh).
		// If there's no explicit declaration, then by default /* DRAWBUFFERS:0 */ is inferred.
		// For SEUS v08 and SEUS v10 to work, this will need to be set to 01234567. However, doing this causes
		// TAA to break on Sildur's Vibrant Shaders, since gbuffers_skybasic lacks a DRAWBUFFERS directive, causing
		// undefined data to be written to colortex7.
		//
		// TODO: Figure out how to infer the DRAWBUFFERS directive when it is missing.
		if(findbuffersDirective(source.getFragmentSource())) {
			drawBuffers = findDrawbuffersDirective(source.getFragmentSource(), "RENDERTARGETS").orElse(new int[]{0});
			isRenderTarget = true;
		} else {
			drawBuffers = findDrawbuffersDirective(source.getFragmentSource(), "DRAWBUFFERS").orElse(new int[]{0});
			isRenderTarget = false;
		}

		if (properties != null) {
			viewportScale = properties.getViewportScaleOverrides().getOrDefault(source.getName(), 1.0f);
			alphaTestOverride = properties.getAlphaTestOverrides().get(source.getName());
			disableBlend = properties.getBlendDisabled().contains(source.getName());
		} else {
			viewportScale = 1.0f;
			alphaTestOverride = null;
			disableBlend = false;
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

	private static boolean findbuffersDirective(Optional<String> stageSource) {
		stageSource
				.flatMap(fragment -> CommentDirectiveParser.findDirectiveAndType(fragment, null, true))
				.map(String::toString);
		String mappedStageSource = stageSource.orElse("");

		if(mappedStageSource.contains("RENDERTARGETS")) {
			return true;
		}
		return false;
	}

	private static Optional<int[]> findDrawbuffersDirective(Optional<String> stageSource, String needle) {
		return stageSource
				.flatMap(fragment -> CommentDirectiveParser.findDirectiveAndType(fragment, needle, false))
				.map(String::toCharArray)
				.map(ProgramDirectives::parseNumbers);
	}

	private static int[] parseNumbers(char[] directiveChars) {
		int[] buffers = new int[directiveChars.length];
		int index = 0;

		if (isRenderTarget == true) {
			for (char buffer : directiveChars) {
				if (Character.isDigit(buffer)) {
					buffers[index] = Character.digit(buffer, 16);
				} else {
					index++;
					continue;
				}
			}
		} else {
			for (char buffer : directiveChars) {
				if (Character.isDigit(buffer)) {
					buffers[index++] = Character.digit(buffer, 16);
				}


			}
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

	public boolean shouldDisableBlend() {
		return disableBlend;
	}

	public ImmutableSet<Integer> getMipmappedBuffers() {
		return mipmappedBuffers;
	}

	// Test code for directive parsing. It's a bit homegrown but it works.
	@SuppressWarnings("unused")
	private static class Tests {
		private static <T> void test(String name, T expected, Supplier<T> testCase) {
			T actual;

			try {
				actual = testCase.get();
			} catch (Throwable e) {
				System.err.println("Test \"" + name + "\" failed with an exception:");
				e.printStackTrace();

				return;
			}

			if (!expected.equals(actual)) {
				System.err.println("Test \"" + name + "\" failed: Expected " + expected + ", got " + actual);
			} else {
				System.out.println("Test \"" + name + "\" passed");
			}
		}

		public static void main(String[] args) {
			test("normal text", Optional.empty(), () -> {
				String line = "01";

				return parseNumbers(line.toCharArray());
			});

			test("rendertarget text", Optional.empty(), () -> {
				String line = "12, 5, 15";

				return parseNumbers(line.toCharArray());
			});
		}
	}
}
