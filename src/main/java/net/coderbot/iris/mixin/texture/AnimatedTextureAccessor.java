package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(targets = "net/minecraft/client/renderer/texture/SpriteContents$AnimatedTexture")
public interface AnimatedTextureAccessor {

	@Accessor("frames")
	List<SpriteContents.FrameInfo> getFrames();

	@Invoker("uploadFrame")
	void invokeUploadFrame(int i, int j, int k);
}
