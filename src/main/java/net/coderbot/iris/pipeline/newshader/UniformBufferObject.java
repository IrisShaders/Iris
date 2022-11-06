package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.Pair;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UniformBufferObject {
	public final List<String> uniformList = new ArrayList<>();
	private int bufferSize;
	private long bufferBeginning;
	private ByteBuffer buffer;
	private int handle;
	private List<UniformInformation> uniformInfo = new ArrayList<>();


	private long lastTick;

	public UniformBufferObject(Map<UniformUpdateFrequency, List<Pair<String, Uniform>>> uniformMap) {
		handle = GlStateManager._glGenBuffers();

		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, handle);

		String lastType = "";
		for (List<Pair<String, Uniform>> uniformPair : uniformMap.values()) {
				for (Pair<String, Uniform> uniform : uniformPair) {
					uniformList.add(uniform.left());
					Uniform uniform2 = uniform.right();
					lastType = uniform2.getTypeName();
					bufferSize = uniform2.align(bufferSize);
					uniformInfo.add(new UniformInformation(uniform.key(), uniform2, bufferSize));
					bufferSize += uniform2.getStandardOffsetBytes();
				}
		}

		//uniformInfo.forEach(uniform -> Iris.logger.warn(uniform.name + " with buffer location " + uniform.byteOffset));

		bufferSize = align(bufferSize, 16);

		buffer = MemoryUtil.memAlloc(bufferSize);
		bufferBeginning = MemoryUtil.memAddress(buffer);

		GL45C.glBufferStorage(GL32C.GL_UNIFORM_BUFFER, bufferSize, 256);
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, 0);
		GL30C.glBindBufferBase(GL32C.GL_UNIFORM_BUFFER, 1, handle);

		//uniformInfo.forEach(uniformInformation -> uniformInformation.setUniform(bufferBeginning));
		//uniformInfo.computeIfAbsent(UniformUpdateFrequency.ONCE, list -> new ArrayList<>()).forEach(uniformInformation -> {
		//	uniformInformation.setUniform(bufferBeginning);
		//});
	}

	private int align(int bufferSize, int alignment) {
		return (((bufferSize - 1) + alignment) & -alignment);
	}

	public void update() {
		long currentTick = getCurrentTick();

		uniformInfo.forEach(uniformInformation -> uniformInformation.setUniform(bufferBeginning));

		GL30C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, handle);
		GL30C.glBufferSubData(GL32C.GL_UNIFORM_BUFFER, 0, buffer);
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, 0);
		GL30C.glBindBufferBase(GL32C.GL_UNIFORM_BUFFER, 1, handle);
	}

	public String getLayout() {
		StringBuilder builder = new StringBuilder();

		builder.append("layout (std140, binding = 1) uniform CommonUniforms {\n");
		uniformInfo.forEach(uniformInformation -> builder.append(uniformInformation.getUniformLayoutName()).append("\n"));


		builder.append("};");

		return builder.toString();
	}

	public boolean containsUniform(String uniform) {
		return uniformList.contains(uniform);
	}

	private static long getCurrentTick() {
		return Objects.requireNonNull(Minecraft.getInstance().level).getGameTime();
	}
}
