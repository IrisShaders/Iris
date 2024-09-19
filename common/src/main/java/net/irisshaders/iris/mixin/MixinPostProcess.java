package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.mixinterface.PostPassAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.IntSupplier;

@Mixin(PostPass.class)
public class MixinPostProcess implements PostPassAccess {
	@Shadow
	@Final
	private EffectInstance effect;

	@Shadow
	@Final
	public RenderTarget inTarget;

	@Shadow
	@Final
	public RenderTarget outTarget;

	@Shadow
	@Final
	private List<String> auxNames;

	@Shadow
	@Final
	private List<IntSupplier> auxAssets;

	@Shadow
	@Final
	private List<Integer> auxWidths;

	@Shadow
	@Final
	private List<Integer> auxHeights;

	@Shadow
	private Matrix4f shaderOrthoMatrix;

	@Override
	public void processIris(int inTarget, int outFB, float partialTicks) {
		this.inTarget.unbindWrite();
		float f = (float)this.outTarget.width;
		float g = (float)this.outTarget.height;
		RenderSystem.viewport(0, 0, (int)f, (int)g);
		this.effect.setSampler("DiffuseSampler", () -> inTarget);

		for (int i = 0; i < this.auxAssets.size(); i++) {
			this.effect.setSampler((String)this.auxNames.get(i), (IntSupplier)this.auxAssets.get(i));
			this.effect.safeGetUniform("AuxSize" + i).set((float)((Integer)this.auxWidths.get(i)).intValue(), (float)((Integer)this.auxHeights.get(i)).intValue());
		}

		this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
		this.effect.safeGetUniform("InSize").set((float)this.inTarget.width, (float)this.inTarget.height);
		this.effect.safeGetUniform("OutSize").set(f, g);
		this.effect.safeGetUniform("Time").set(partialTicks);
		Minecraft minecraft = Minecraft.getInstance();
		this.effect.safeGetUniform("ScreenSize").set((float)minecraft.getWindow().getWidth(), (float)minecraft.getWindow().getHeight());
		this.effect.apply();
		this.outTarget.clear(Minecraft.ON_OSX);
		GlStateManager._glBindFramebuffer(GL46C.GL_FRAMEBUFFER, outFB);
		RenderSystem.depthFunc(519);
		BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		bufferBuilder.addVertex(0.0F, 0.0F, 500.0F);
		bufferBuilder.addVertex(f, 0.0F, 500.0F);
		bufferBuilder.addVertex(f, g, 500.0F);
		bufferBuilder.addVertex(0.0F, g, 500.0F);
		BufferUploader.draw(bufferBuilder.buildOrThrow());
		RenderSystem.depthFunc(515);
		this.effect.clear();
		GlStateManager._glBindFramebuffer(GL46C.GL_FRAMEBUFFER, 0);
		GlStateManager._glBindFramebuffer(GL46C.GL_READ_FRAMEBUFFER, 0);

		for (Object object : this.auxAssets) {
			if (object instanceof RenderTarget) {
				((RenderTarget)object).unbindRead();
			}
		}
	}
}
