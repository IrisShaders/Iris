package net.coderbot.iris.postprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.rendertarget.RenderTarget;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.CommonUniforms;
import org.lwjgl.opengl.GL15C;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Pair;

public class CompositeRenderer {
	private final Program baseline;
	public final RenderTargets renderTargets;

	// TODO: Make private
	public final GlFramebuffer clearEverythingBuffer;
	public final GlFramebuffer clearAltBuffers;
	private final ImmutableList<Pass> passes;

	private final FullScreenQuadRenderer quadRenderer;
	final CenterDepthSampler centerDepthSampler;

	public CompositeRenderer(ShaderPack pack) {
		centerDepthSampler = new CenterDepthSampler();
		baseline = createBaselineProgram(pack);

		final List<Pair<Program, int[]>> programs = new ArrayList<>();

		for (ShaderPack.ProgramSource source : pack.getComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			programs.add(createProgram(source));
		}

		pack.getCompositeFinal().map(this::createProgram).ifPresent(programs::add);

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

		this.renderTargets = new RenderTargets(main.textureWidth, main.textureHeight, pack.getRequestedBufferFormats());

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();

		boolean[] stageReadsFromAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		// TODO: Hardcoding for sildurs: should clear all buffers unless otherwise specified, but we skip buffer 7
		this.clearAltBuffers = createStageFramebuffer(renderTargets, stageReadsFromAlt, new int[]{0, 1, 2, 3, 4, 5, 6});

		// Hack to make a framebuffer that writes to the "main" buffers.
		Arrays.fill(stageReadsFromAlt, true);
		this.clearEverythingBuffer = createStageFramebuffer(renderTargets, stageReadsFromAlt, new int[]{0, 1, 2, 3, 4, 5, 6});
		this.clearEverythingBuffer.addDepthAttachment(renderTargets.getDepthTexture().getTextureId());

		Arrays.fill(stageReadsFromAlt, false);

		for (Pair<Program, int[]> programEntry : programs) {
			Pass pass = new Pass();

			pass.program = programEntry.getLeft();
			int[] drawBuffers = programEntry.getRight();

			System.out.println("Draw buffers: " + new IntArrayList(drawBuffers));

			GlFramebuffer framebuffer = createStageFramebuffer(renderTargets, stageReadsFromAlt, drawBuffers);

			pass.stageReadsFromAlt = Arrays.copyOf(stageReadsFromAlt, stageReadsFromAlt.length);
			pass.framebuffer = framebuffer;

			if (programEntry == programs.get(programs.size() - 1)) {
				pass.isLastPass = true;
			}

			passes.add(pass);

			// Flip the buffers that this shader wrote to
			for (int buffer : drawBuffers) {
				stageReadsFromAlt[buffer] = !stageReadsFromAlt[buffer];
			}
		}

		this.passes = passes.build();
		this.quadRenderer = new FullScreenQuadRenderer();
	}

	private static final class Pass {
		Program program;
		GlFramebuffer framebuffer;
		boolean[] stageReadsFromAlt;
		boolean isLastPass;
	}

	public static GlFramebuffer createMainFramebuffer(RenderTargets renderTargets, int[] drawBuffers) {
		boolean[] stageReadsFromAlt = new boolean[RenderTargets.MAX_RENDER_TARGETS];

		// Hack to make a framebuffer that writes to the "main" buffers.
		Arrays.fill(stageReadsFromAlt, true);

		GlFramebuffer framebuffer =  createStageFramebuffer(renderTargets, stageReadsFromAlt, drawBuffers);

		framebuffer.addDepthAttachment(renderTargets.getDepthTexture().getTextureId());

		return framebuffer;
	}

	private static GlFramebuffer createStageFramebuffer(RenderTargets renderTargets, boolean[] stageReadsFromAlt, int[] drawBuffers) {
		GlFramebuffer framebuffer = new GlFramebuffer();
		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();

		System.out.println("creating framebuffer: stageReadsFromAlt = " + new BooleanArrayList(stageReadsFromAlt));

		for (int i = 0; i < RenderTargets.MAX_RENDER_TARGETS; i++) {
			RenderTarget target = renderTargets.get(i);
			boolean stageWritesToAlt = !stageReadsFromAlt[i];

			int textureId = stageWritesToAlt ? target.getAltTexture() : target.getMainTexture();

			System.out.println("  attachment " + i + " -> texture" + textureId);

			framebuffer.addColorAttachment(i, textureId);
		}

		if (!framebuffer.isComplete()) {
			throw new IllegalStateException("Unexpected error while creating framebuffer");
		}

		framebuffer.drawBuffers(drawBuffers);

		return framebuffer;
	}

	public void renderAll() {
		centerDepthSampler.endWorldRendering();

		// Make sure we're using texture unit 0
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		/*this.writesToMain.bind();

		RenderSystem.bindTexture(main.getColorAttachment());
		baseline.use();
		quadRenderer.render();*/

		int depthAttachment = renderTargets.getDepthTexture().getTextureId();

		for (Pass renderPass : passes) {
			if (!renderPass.isLastPass) {
				renderPass.framebuffer.bind();
			} else {
				MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
			}

			// TODO: Consider copying the depth texture content into a separate texture that won't be modified? Probably
			// isn't an issue though.
			bindTexture(PostProcessUniforms.DEPTH_TEX_0, depthAttachment);
			// TODO: No translucent objects
			bindTexture(PostProcessUniforms.DEPTH_TEX_1, depthAttachment);
			// Note: Since we haven't rendered the hand yet, this won't contain any handheld items.
			// Once we start rendering the hand before composite content, this will need to be addressed.
			bindTexture(PostProcessUniforms.DEPTH_TEX_2, depthAttachment);

			bindRenderTarget(PostProcessUniforms.COLOR_TEX_0, renderTargets.get(0), renderPass.stageReadsFromAlt[0]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_1, renderTargets.get(1), renderPass.stageReadsFromAlt[1]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_2, renderTargets.get(2), renderPass.stageReadsFromAlt[2]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_3, renderTargets.get(3), renderPass.stageReadsFromAlt[3]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_4, renderTargets.get(4), renderPass.stageReadsFromAlt[4]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_5, renderTargets.get(5), renderPass.stageReadsFromAlt[5]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_6, renderTargets.get(6), renderPass.stageReadsFromAlt[6]);
			bindRenderTarget(PostProcessUniforms.COLOR_TEX_7, renderTargets.get(7), renderPass.stageReadsFromAlt[7]);

			renderPass.program.use();
			quadRenderer.render();
		}

		// TODO: If there are no composite passes, we need to add a "fake" pass

		GlStateManager.useProgram(0);

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_DEPTH);
		RenderSystem.bindTexture(0);
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + PostProcessUniforms.DEFAULT_COLOR);
		RenderSystem.bindTexture(0);
	}

	private static void bindRenderTarget(int textureUnit, RenderTarget target, boolean readFromAlt) {
		bindTexture(textureUnit, readFromAlt ? target.getAltTexture() : target.getMainTexture());
	}

	private static void bindTexture(int textureUnit, int texture) {
		RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + textureUnit);
		RenderSystem.bindTexture(texture);
	}

	// TODO: Don't just copy this from ShaderPipeline
	private Pair<Program, int[]> createProgram(ShaderPack.ProgramSource source) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null),
				source.getFragmentSource().orElse(null));
		} catch (IOException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getIdMap());
		PostProcessUniforms.addPostProcessUniforms(builder, this);

		return new Pair<>(builder.build(), source.getDirectives().getDrawBuffers());
	}

	private Program createBaselineProgram(ShaderPack parent) {
		ShaderPack.ProgramSource source = new ShaderPack.ProgramSource("<iris builtin baseline composite>", BASELINE_COMPOSITE_VSH, BASELINE_COMPOSITE_FSH, parent);

		return createProgram(source).getLeft();
	}

	private static final String BASELINE_COMPOSITE_VSH =
		"#version 120\n" +
			"\n" +
			"varying vec2 texcoord;\n" +
			"\n" +
			"void main() {\n" +
			"\tgl_Position = ftransform();\n" +
			"\ttexcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;\n" +
			"}";

	private static final String BASELINE_COMPOSITE_FSH =
		"#version 120\n" +
			"\n" +
			"uniform sampler2D gcolor;\n" +
			"\n" +
			"varying vec2 texcoord;\n" +
			"\n" +
			"void main() {\n" +
			"\tvec3 color = texture2D(gcolor, texcoord).rgb;\n" +
			"\n" +
			"/* DRAWBUFFERS:0 */\n" +
			"\tgl_FragData[0] = vec4(color, 1.0); // gcolor\n" +
			"}";
}
