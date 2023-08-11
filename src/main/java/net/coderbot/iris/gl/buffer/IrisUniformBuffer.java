package net.coderbot.iris.gl.buffer;

import it.unimi.dsi.fastutil.Pair;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL46C;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class IrisUniformBuffer implements LocationalUniformHolder {
	private Map<UniformUpdateFrequency, List<Uniform>> uniformList = new HashMap<>();
	private int size;
	private ArrayList<Pair<UniformUpdateFrequency, Uniform>> uniformOrder;
	private long lastTick;
	private UniformBuffer buffer;

	private int align(int bufferSize, int alignment) {
		return (((bufferSize - 1) + alignment) & -alignment);
	}

	public void finish() {
		this.uniformOrder = new ArrayList<>();
		for (UniformUpdateFrequency frequency : uniformList.keySet()) {
			for (Uniform uniform : uniformList.get(frequency)) {
				uniformOrder.add(Pair.of(frequency, uniform));
			}
		}

		uniformOrder.sort(Comparator.comparing(Pair::first));

		for (Pair<UniformUpdateFrequency, Uniform> uniform : uniformOrder) {
			size = align(size, uniform.second().getAlignment());
			uniform.second().setBufferIndex(size);
			Iris.logger.warn("Uniform " + uniform.second().getType().name() + " added at " + size);
			size += uniform.second().getByteSize();
		}

		Iris.logger.warn("Final size: " + size);

		Iris.logger.warn(getLayout());

		buffer = new UniformBuffer(size);
	}

	public String getLayout() {
		StringBuilder builder = new StringBuilder();

		builder.append("layout (std140, binding = 1) uniform CommonUniforms {\n");
		uniformOrder.forEach(uniformInformation -> builder.append(uniformInformation.second().getType().name().toLowerCase()).append(" ").append(uniformInformation.second().getName()).append(";").append("\n"));


		builder.append("};");

		return builder.toString();
	}

	private static long getCurrentTick() {
		if (Minecraft.getInstance().level == null) {
			return 0L;
		} else {
			return Minecraft.getInstance().level.getGameTime();
		}
	}

	private boolean hasUpdatedOnce = false;

	public void updateUniforms() {
		boolean updateOnce = false;
		boolean updateTickUniforms = false;

		if (!hasUpdatedOnce) {
			updateOnce = true;
			hasUpdatedOnce = true;
		}

		long currentTick = getCurrentTick();

		if (lastTick != currentTick) {
			lastTick = currentTick;
			updateTickUniforms = true;
		}

		boolean finalUpdateOnce = updateOnce;
		boolean finalUpdateTickUniforms = updateTickUniforms;
		uniformOrder.forEach((pair) -> {
			switch (pair.first()) {
				case ONCE -> {
					if (!finalUpdateOnce) return;
				}
				case PER_TICK -> {
					if (!finalUpdateTickUniforms) return;
				}
			}

			pair.second().updateBuffer(buffer.getWriteAddressForFrame());
		});

		buffer.updateFrame();

	}

	@Override
	public UniformHolder externallyManagedUniform(String name, UniformType type) {
		return this;
	}

	@Override
	public LocationalUniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
		uniformList.computeIfAbsent(updateFrequency, a -> new ArrayList<>()).add(uniform);
		return this;
	}

	@Override
	public OptionalInt location(String name, UniformType type) {
		return OptionalInt.of(1);
	}
}
