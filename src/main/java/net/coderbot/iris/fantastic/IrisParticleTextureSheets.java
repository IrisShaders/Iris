package net.coderbot.iris.fantastic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class IrisParticleTextureSheets {
	public static final ParticleRenderType OPAQUE_TERRAIN_SHEET = new ParticleRenderType() {
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			RenderSystem.defaultAlphaFunc();
			textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
			bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
		}

		public void end(Tesselator tessellator) {
			tessellator.end();
		}

		public String toString() {
			return "OPAQUE_TERRAIN_SHEET";
		}
	};
}
