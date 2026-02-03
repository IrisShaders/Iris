package net.irisshaders.iris.gl.sampler;

import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class GlSampler extends GlResource {
	public static final GlSampler MIPPED_LINEAR_HW = new GlSampler(true, true, true, true, true, true);
	public static final GlSampler LINEAR_HW = new GlSampler(true, true, false, true, true, true);
	public static final GlSampler MIPPED_NEAREST_HW = new GlSampler(false, true, true, true, true, true);
	public static final GlSampler NEAREST_HW = new GlSampler(false, true, false, true, true, true);
	public static final GlSampler MIPPED_LINEAR = new GlSampler(true, true, true, false, false, true);
	public static final GlSampler LINEAR = new GlSampler(true, true, false, false, false, true);
	public static final GlSampler MIPPED_NEAREST = new GlSampler(false, true, true, false, false, true);
	public static final GlSampler NEAREST = new GlSampler(false, true, false, false, false, true);
	public static final GlSampler NEAREST_REPEAT = new GlSampler(false, true, false, false, false, false);
	public static final GlSampler LINEAR_REPEAT = new GlSampler(true, true, false, false, false, false);
	public static final GlSampler MIPPED_NEAREST_NEAREST = new GlSampler(false, false, true, false, false, true);
	public static final GlSampler MIPPED_NEAREST_REPEAT = new GlSampler(false, true, true, false, false, false);

	public GlSampler(boolean linear, boolean linearMips, boolean mipmapped, boolean shadow, boolean hardwareShadow, boolean clamp) {
		super(IrisRenderSystem.genSampler());

		IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_MAG_FILTER, linear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_WRAP_S, clamp ? GL13C.GL_CLAMP_TO_EDGE : GL13C.GL_REPEAT);
		IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_WRAP_T, clamp ? GL13C.GL_CLAMP_TO_EDGE : GL13C.GL_REPEAT);

		if (mipmapped) {
			IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_MIN_FILTER, linearMips ? (
				linear ? GL11C.GL_LINEAR_MIPMAP_LINEAR : GL11C.GL_NEAREST_MIPMAP_LINEAR
				) : (
				linear ? GL11C.GL_LINEAR_MIPMAP_NEAREST : GL11C.GL_NEAREST_MIPMAP_NEAREST
				));
		} else {
			IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_MIN_FILTER, linear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		}

		if (hardwareShadow) {
			IrisRenderSystem.samplerParameteri(getId(), GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
		}
	}

	public GlSampler(int sampler) {
		super(sampler);
	}

	@Override
	protected void destroyInternal() {
		IrisRenderSystem.destroySampler(getGlId());
	}

	public int getId() {
		return getGlId();
	}
}
