package net.irisshaders.iris.pbr.texture;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.texture.SpriteContentsAnimatedTextureAccessor;
import net.irisshaders.iris.mixin.texture.SpriteContentsFrameInfoAccessor;
import net.irisshaders.iris.mixin.texture.SpriteContentsTickerAccessor;
import net.irisshaders.iris.pbr.loader.AtlasPBRLoader.PBRTextureAtlasSprite;
import net.irisshaders.iris.pbr.util.TextureManipulationUtil;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents.FrameInfo;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

public class PBRAtlasTexture extends AbstractTexture implements PBRDumpable {
	protected final TextureAtlas atlasTexture;
	protected final PBRType type;
	protected final Identifier location;
	private List<PBRTextureAtlasSprite> sprites = List.of();
	protected final Map<Identifier, PBRTextureAtlasSprite> texturesByNameToAdd = new HashMap<>();
	protected Map<Identifier, PBRTextureAtlasSprite> texturesByName = new HashMap<>();
	private List<SpriteContents.AnimationState> animatedTexturesStates = List.of();
	protected int width;
	protected int height;
	private GpuBuffer spriteUbos;
	private int mipLevelCount;
	private GpuTextureView[] mipViews = new GpuTextureView[0];
	private int maxMipLevel;
	private TextureAtlasSprite missingSprite;

	public PBRAtlasTexture(TextureAtlas atlasTexture, PBRType type) {
		this.atlasTexture = atlasTexture;
		this.type = type;
		location = Identifier.fromNamespaceAndPath(atlasTexture.location().getNamespace(), atlasTexture.location().getPath().replace(".png", "") + type.getSuffix() + ".png");
	}

	public static void syncAnimation(SpriteContents.AnimatedTexture source, SpriteContents.AnimationState target) {
		SpriteContentsTickerAccessor sourceAccessor = (SpriteContentsTickerAccessor) source;
		List<FrameInfo> sourceFrames = ((SpriteContentsAnimatedTextureAccessor) sourceAccessor.getAnimationInfo()).getFrames();

		int ticks = 0;
		for (int f = 0; f < sourceAccessor.getFrame(); f++) {
			ticks += ((SpriteContentsFrameInfoAccessor) (Object) sourceFrames.get(f)).getTime();
		}

		SpriteContentsTickerAccessor targetAccessor = (SpriteContentsTickerAccessor) target;
		List<FrameInfo> targetFrames = ((SpriteContentsAnimatedTextureAccessor) targetAccessor.getAnimationInfo()).getFrames();

		int cycleTime = 0;
		int frameCount = targetFrames.size();
		for (FrameInfo frame : targetFrames) {
			cycleTime += ((SpriteContentsFrameInfoAccessor) (Object) frame).getTime();
		}
		ticks %= cycleTime;

		int targetFrame = 0;
		while (true) {
			int time = ((SpriteContentsFrameInfoAccessor) (Object) targetFrames.get(targetFrame)).getTime();
			if (ticks >= time) {
				targetFrame++;
				ticks -= time;
			} else {
				break;
			}
		}

		targetAccessor.setFrame(targetFrame);
		targetAccessor.setSubFrame(ticks + sourceAccessor.getSubFrame());
	}

	protected static void dumpSpriteNames(Path dir, String fileName, Map<Identifier, PBRTextureAtlasSprite> sprites) {
		Path path = dir.resolve(fileName + ".txt");
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for (Map.Entry<Identifier, PBRTextureAtlasSprite> entry : sprites.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
				PBRTextureAtlasSprite sprite = entry.getValue();
				writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), sprite.getX(), sprite.getY(), sprite.contents().width(), sprite.contents().height()));
			}
		} catch (IOException e) {
			Iris.logger.warn("Failed to write file {}", path, e);
		}
	}

	public PBRType getType() {
		return type;
	}

	public Identifier getAtlasId() {
		return location;
	}

	public void addSprite(PBRTextureAtlasSprite sprite) {
		texturesByNameToAdd.put(sprite.contents().name(), sprite);
	}

	@Nullable
	public PBRTextureAtlasSprite getSprite(Identifier id) {
		return texturesByName.get(id);
	}

	public boolean tryUpload(int atlasWidth, int atlasHeight, int mipLevel) {
		try {
			upload(atlasWidth, atlasHeight, mipLevel);
			return true;
		} catch (Throwable t) {
			if (IrisPlatformHelpers.getInstance().isDevelopmentEnvironment()) {
				t.printStackTrace();
			}
			return false;
		}
	}

	private void createTexture(int i, int j, int k) {
		Iris.logger.info("Created: {}x{}x{} {}-atlas", i, j, k, this.location);
		GpuDevice gpuDevice = RenderSystem.getDevice();
		this.close();
		this.texture = gpuDevice.createTexture(this.location::toString, 15, TextureFormat.RGBA8, i, j, 1, k + 1);
		this.textureView = gpuDevice.createTextureView(this.texture);
		this.width = i;
		this.height = j;
		this.maxMipLevel = k;
		this.mipLevelCount = k + 1;
		this.mipViews = new GpuTextureView[this.mipLevelCount];
		TextureManipulationUtil.fillWithColor(texture.iris$getGlId(), maxMipLevel, type.getDefaultValue());

		for (int l = 0; l <= this.maxMipLevel; l++) {
			this.mipViews[l] = gpuDevice.createTextureView(this.texture, l, 1);
		}
	}

	public void clearTextureData() {
		this.sprites.forEach(TextureAtlasSprite::close);
		this.sprites = List.of();
		this.animatedTexturesStates = List.of();
		this.texturesByName = Map.of();
		this.missingSprite = null;
	}

	public void upload(int atlasWidth, int atlasHeight, int mipLevel) {
		this.createTexture(atlasWidth, atlasHeight, mipLevel);
		this.clearTextureData();
		this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		this.texturesByName = Map.copyOf(texturesByNameToAdd);
		this.missingSprite = null;
		List<PBRTextureAtlasSprite> sprites = new ArrayList<>();
		List<SpriteContents.AnimationState> animationStates = new ArrayList<>();
		int animatedSpriteCount = (int) texturesByName.values().stream().filter(TextureAtlasSprite::isAnimated).count();
		int spriteUboSize = Mth.roundToward(SpriteContents.UBO_SIZE, RenderSystem.getDevice().getUniformOffsetAlignment());
		int uboBlockSize = spriteUboSize * this.mipLevelCount;
		ByteBuffer spriteUboBuffer = MemoryUtil.memAlloc(animatedSpriteCount * uboBlockSize);
		int animationIndex = 0;

		for (TextureAtlasSprite sprite : texturesByName.values()) {
			if (sprite.isAnimated()) {
				sprite.uploadSpriteUbo(spriteUboBuffer, animationIndex * uboBlockSize, this.maxMipLevel, this.width, this.height, spriteUboSize);
				animationIndex++;
			}
		}

		GpuBuffer spriteUbos = animationIndex > 0 ? RenderSystem.getDevice().createBuffer(() -> this.location + " sprite UBOs", 128, spriteUboBuffer) : null;
		animationIndex = 0;

		for (PBRTextureAtlasSprite spritex : texturesByName.values()) {
			sprites.add(spritex);
			if (spritex.isAnimated() && spriteUbos != null) {
				SpriteContents.AnimationState animationState = spritex.createAnimationState(spriteUbos.slice(animationIndex * uboBlockSize, uboBlockSize), spriteUboSize);
				animationIndex++;
				if (animationState != null) {
					animationStates.add(animationState);
				}
			}
		}

		this.spriteUbos = spriteUbos;
		this.sprites = sprites;
		this.animatedTexturesStates = List.copyOf(animationStates);
		this.uploadInitialContents();
		if (SharedConstants.DEBUG_DUMP_TEXTURE_ATLAS) {
			Path dumpDir = TextureUtil.getDebugTexturePath();

			try {
				Files.createDirectories(dumpDir);
				this.dumpContents(this.location, dumpDir);
			} catch (IOException var13) {
				Iris.logger.warn("Failed to dump atlas contents to {}", dumpDir);
			}
		}

		PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getOrCreatePBRHolder();

		switch (type) {
			case NORMAL:
				pbrHolder.setNormalAtlas(this);
				break;
			case SPECULAR:
				pbrHolder.setSpecularAtlas(this);
				break;
		}
	}

	private void uploadInitialContents() {
		GpuDevice gpuDevice = RenderSystem.getDevice();
		int spriteUboSize = Mth.roundToward(SpriteContents.UBO_SIZE, RenderSystem.getDevice().getUniformOffsetAlignment());
		int uboBlockSize = spriteUboSize * this.mipLevelCount;
		GpuSampler gpuSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		List<PBRTextureAtlasSprite> staticSprites = this.sprites.stream().filter(textureAtlasSprite -> !textureAtlasSprite.isAnimated()).toList();
		List<GpuTextureView[]> scratchTextures = new ArrayList<>();
		ByteBuffer byteBuffer = MemoryUtil.memAlloc(staticSprites.size() * uboBlockSize);

		for (int k = 0; k < staticSprites.size(); k++) {
			TextureAtlasSprite textureAtlasSprite = staticSprites.get(k);
			textureAtlasSprite.uploadSpriteUbo(byteBuffer, k * uboBlockSize, this.maxMipLevel, this.width, this.height, spriteUboSize);
			GpuTexture gpuTexture = gpuDevice.createTexture(
				() -> textureAtlasSprite.contents().name().toString(),
				5,
				TextureFormat.RGBA8,
				textureAtlasSprite.contents().width(),
				textureAtlasSprite.contents().height(),
				1,
				this.mipLevelCount
			);
			GpuTextureView[] gpuTextureViews = new GpuTextureView[this.mipLevelCount];

			for (int l = 0; l <= this.maxMipLevel; l++) {
				textureAtlasSprite.uploadFirstFrame(gpuTexture, l);
				gpuTextureViews[l] = gpuDevice.createTextureView(gpuTexture);
			}

			scratchTextures.add(gpuTextureViews);
		}

		try (GpuBuffer gpuBuffer = gpuDevice.createBuffer(() -> "SpriteAnimationInfo", 128, byteBuffer)) {
			for (int level = 0; level < this.mipLevelCount; level++) {
				try (RenderPass renderPass = RenderSystem.getDevice()
					.createCommandEncoder()
					.createRenderPass(() -> "Animate " + this.location, this.mipViews[level], OptionalInt.empty())) {
					renderPass.setPipeline(RenderPipelines.ANIMATE_SPRITE_BLIT);

					for (int n = 0; n < staticSprites.size(); n++) {
						renderPass.bindTexture("Sprite", scratchTextures.get(n)[level], gpuSampler);
						renderPass.setUniform("SpriteAnimationInfo", gpuBuffer.slice(n * uboBlockSize + level * spriteUboSize, SpriteContents.UBO_SIZE));
						renderPass.draw(0, 6);
					}
				}
			}
		}

		for (GpuTextureView[] views : scratchTextures) {
			for (GpuTextureView view : views) {
				view.close();
				view.texture().close();
			}
		}

		MemoryUtil.memFree(byteBuffer);
	}

	public void cycleAnimationFrames() {
		if (this.texture != null) {
			for (SpriteContents.AnimationState animationState : this.animatedTexturesStates) {
				animationState.tick();
			}

			if (this.animatedTexturesStates.stream().anyMatch(SpriteContents.AnimationState::needsToDraw)) {
				for (int i = 0; i <= this.maxMipLevel; i++) {
					try (RenderPass renderPass = RenderSystem.getDevice()
						.createCommandEncoder()
						.createRenderPass(() -> "Animate " + this.location, this.mipViews[i], OptionalInt.empty())) {
						for (SpriteContents.AnimationState animationState2 : this.animatedTexturesStates) {
							if (animationState2.needsToDraw()) {
								animationState2.drawToAtlas(renderPass, animationState2.getDrawUbo(i));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void close() {
		PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getPBRHolder();
		if (pbrHolder != null) {
			switch (type) {
				case NORMAL:
					pbrHolder.setNormalAtlas(null);
					break;
				case SPECULAR:
					pbrHolder.setSpecularAtlas(null);
					break;
			}
		}

		super.close();

		for (GpuTextureView gpuTextureView : this.mipViews) {
			gpuTextureView.close();
		}

		for (SpriteContents.AnimationState animationState : this.animatedTexturesStates) {
			animationState.close();
		}

		if (this.spriteUbos != null) {
			this.spriteUbos.close();
			this.spriteUbos = null;
		}
	}

	@Override
	public void dumpContents(Identifier id, Path path) {
		String string = id.toDebugFileName();
		TextureUtil.writeAsPNG(path, string, this.getTexture(), this.maxMipLevel, i -> i);
		dumpSpriteNames(path, string, this.texturesByName);
	}

	@Override
	public Identifier getDefaultDumpLocation() {
		return location;
	}
}
