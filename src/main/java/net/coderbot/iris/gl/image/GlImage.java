package net.coderbot.iris.gl.image;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.gl.texture.TextureType;
import org.lwjgl.opengl.ARBClearTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

public class GlImage {
	protected int id;
	protected final String name;
	protected final String samplerName;
	protected final TextureType target;
	protected final PixelFormat format;
	protected final InternalTextureFormat internalTextureFormat;
	protected final PixelType pixelType;
	private final boolean clear;

	public GlImage(String name, String samplerName, TextureType target, PixelFormat format, InternalTextureFormat internalFormat, PixelType pixelType, boolean clear, int width, int height, int depth) {
		this.id = IrisRenderSystem.createTexture(target.getGlType());

		this.name = name;
		this.samplerName = samplerName;
		this.target = target;
		this.format = format;
		this.internalTextureFormat = internalFormat;
		this.pixelType = pixelType;
		this.clear = clear;

		IrisRenderSystem.bindTextureForSetup(target.getGlType(), id);
		target.apply(id, width, height, depth, internalFormat.getGlFormat(), format.getGlFormat(), pixelType.getGlFormat(), 0L);

		int texture = id;

		setup(texture, width, height, depth);

		IrisRenderSystem.bindTextureForSetup(target.getGlType(), 0);
	}

	protected void setup(int texture, int width, int height, int depth) {
		boolean isInteger = internalTextureFormat.getPixelFormat().isInteger();
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_MIN_FILTER, isInteger ? GL11C.GL_NEAREST : GL11C.GL_LINEAR);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_MAG_FILTER, isInteger ? GL11C.GL_NEAREST : GL11C.GL_LINEAR);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);

		if (height > 0) {
			IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		}

		if (depth > 0) {
			IrisRenderSystem.texParameteri(texture, GL30C.GL_TEXTURE_WRAP_R, GL13C.GL_CLAMP_TO_EDGE);
		}

		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_MAX_LEVEL, 0);
		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_MIN_LOD, 0);
		IrisRenderSystem.texParameteri(texture, GL20C.GL_TEXTURE_MAX_LOD,0);
		IrisRenderSystem.texParameterf(texture, GL20C.GL_TEXTURE_LOD_BIAS, 0.0F);

		ARBClearTexture.glClearTexImage(texture, 0, format.getGlFormat(), pixelType.getGlFormat(), (int[]) null);
	}

	public String getName() {
		return name;
	}

	public String getSamplerName() {
		return samplerName;
	}

	public TextureType getTarget() {
		return target;
	}

	public boolean shouldClear() {
		return clear;
	}

	public int getId() {
		return id;
	}

	/**
	 * This makes the image aware of a new render target. Depending on the image's properties, it may not follow these targets.
	 * @param width The width of the main render target.
	 * @param height The height of the main render target.
	 */
	public void updateNewSize(int width, int height) {

	}

	public void destroy() {
		GlStateManager._deleteTexture(id);
	}

	public InternalTextureFormat getInternalFormat() {
		return internalTextureFormat;
	}

	@Override
	public String toString() {
		return "GlImage name " + name + " format " + format + "internalformat " + internalTextureFormat + " pixeltype " + pixelType;
	}

	public PixelFormat getFormat() {
		return format;
	}

	public PixelType getPixelType() {
		return pixelType;
	}

	public static class Relative extends GlImage {

		private final float relativeHeight;
		private final float relativeWidth;

		public Relative(String name, String samplerName, PixelFormat format, InternalTextureFormat internalFormat, PixelType pixelType, boolean clear, float relativeWidth, float relativeHeight, int currentWidth, int currentHeight) {
			super(name, samplerName, TextureType.TEXTURE_2D, format, internalFormat, pixelType, clear, (int) (currentWidth * relativeWidth), (int) (currentHeight * relativeHeight), 0);

			this.relativeWidth = relativeWidth;
			this.relativeHeight = relativeHeight;
		}

		@Override
		public void updateNewSize(int width, int height) {
			GlStateManager._deleteTexture(id);
			this.id = IrisRenderSystem.createTexture(target.getGlType());

			IrisRenderSystem.bindTextureForSetup(target.getGlType(), id);
			target.apply(id, (int) (width * relativeWidth), (int) (height * relativeHeight), 0, internalTextureFormat.getGlFormat(), format.getGlFormat(), pixelType.getGlFormat(), 0L);

			int texture = id;

			setup(texture, width, height, 0);

			IrisRenderSystem.bindTextureForSetup(target.getGlType(), 0);
		}
	}
}
