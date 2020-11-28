package net.coderbot.iris.postprocess;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class CompositeRenderer {
	private static final Matrix4f IDENTITY = new Matrix4f();

	static {
		IDENTITY.loadIdentity();
	}

	VertexBuffer quad;

	VertexBuffer getQuad() {
		if (quad == null) {
			createQuad();
		}

		return quad;
	}

	public void render() {
		begin();

		VertexBuffer quad = getQuad();

		RenderSystem.activeTexture(GL15.GL_TEXTURE0);
		MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/environment/moon_phases.png"));
		RenderSystem.enableTexture();

		renderQuad();

		end();
	}

	private void begin() {
		RenderSystem.disableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	}

	private void renderQuad() {
		quad.bind();
		VertexFormats.POSITION_COLOR_TEXTURE.startDrawing(0L);
		quad.draw(IDENTITY, GL11.GL_TRIANGLES);
		VertexFormats.POSITION_COLOR_TEXTURE.endDrawing();
		VertexBuffer.unbind();
	}

	private void end() {
		RenderSystem.enableDepthTest();

		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
	}

	private void createQuad() {
		quad = new VertexBuffer(VertexFormats.POSITION_COLOR_TEXTURE);

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION_COLOR_TEXTURE);

		// NB: Use counterclockwise order here! Otherwise these triangles will be invisible.

		// The first triangle
		vertex(buffer, true, true);
		vertex(buffer, false, true);
		vertex(buffer, true, false);

		// The second triangle
		vertex(buffer, true, false);
		vertex(buffer, false, true);
		vertex(buffer, false, false);

		buffer.end();
		quad.upload(buffer);
	}

	private static void vertex(BufferBuilder buffer, boolean plusX, boolean up) {
		// These coordinates are provided in clip space, and therefore range from -1.0 to 1.0 to fill the entire screen.
		buffer.vertex(plusX ? 1.0F : -1.0F, up ? 1.0F : -1.0F, 0.0);

		// Each vertex will always be white.
		buffer.color(1.0F, 1.0F, 1.0F, 1.0F);

		// Texture coordinates are in the range of 0.0 to 1.0
		buffer.texture(plusX ? 1.0F : 0.0F, up? 1.0F : 0.0F);

		// Move to the next vertex.
		buffer.next();
	}
}
