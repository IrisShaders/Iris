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

	private final Description description;
	private int handle;

	public GlSampler(boolean linear, boolean linearMips, boolean mipmapped, boolean shadow, boolean hardwareShadow, boolean clamp) {
		super(0);
		this.description = new Description(linear, linearMips, mipmapped, hardwareShadow, clamp);
	}

	public GlSampler(int sampler) {
		super(0);

		if (sampler <= 0) {
			throw new IllegalArgumentException("invalid OpenGL sampler");
		}

		this.handle = sampler;
		this.description = null;
	}

	private void createGlSampler() {
		this.handle = IrisRenderSystem.genSampler();

		boolean linear = this.description.linear();
		boolean linearMips = this.description.linearMips();
		boolean mipmapped = this.description.mipmapped();
		boolean hardwareShadow = this.description.hardwareShadow();
		boolean clamp = this.description.clamp();

		IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_MAG_FILTER, linear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_WRAP_S, clamp ? GL13C.GL_CLAMP_TO_EDGE : GL13C.GL_REPEAT);
		IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_WRAP_T, clamp ? GL13C.GL_CLAMP_TO_EDGE : GL13C.GL_REPEAT);

		if (mipmapped) {
			IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_MIN_FILTER,
				linearMips ? (linear ? GL11C.GL_LINEAR_MIPMAP_LINEAR : GL11C.GL_NEAREST_MIPMAP_LINEAR) : (linear ? GL11C.GL_LINEAR_MIPMAP_NEAREST : GL11C.GL_NEAREST_MIPMAP_NEAREST));
		} else {
			IrisRenderSystem.samplerParameteri(getId(), GL11C.GL_TEXTURE_MIN_FILTER, linear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
		}

		if (hardwareShadow) {
			IrisRenderSystem.samplerParameteri(getId(), GL20C.GL_TEXTURE_COMPARE_MODE, GL30C.GL_COMPARE_REF_TO_TEXTURE);
			IrisRenderSystem.samplerParameteri(getId(), GL20C.GL_TEXTURE_COMPARE_FUNC, GL11C.GL_LEQUAL);
		}
	}

	@Override
	protected void destroyInternal() {
		if (this.handle != 0) {
			IrisRenderSystem.destroySampler(this.handle);
		}

		this.handle = 0;
	}

	@Override
	protected int getGlId() {
		this.assertValid();

		if (this.handle == 0) {
			this.createGlSampler();
		}

		return this.handle;
	}

	public Description description() {
		this.assertValid();

		if (this.description == null) {
			throw new IllegalStateException("cannot recreate an opaque OpenGL sampler in Vulkan");
		}

		return this.description;
	}

	public int getId() {
		return getGlId();
	}

	public record Description(boolean linear, boolean linearMips, boolean mipmapped, boolean hardwareShadow, boolean clamp) {
	}
}
