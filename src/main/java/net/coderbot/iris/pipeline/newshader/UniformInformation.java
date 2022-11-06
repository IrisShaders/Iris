package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.uniform.Uniform;

import java.util.List;

public class UniformInformation {
	final String name;
	private final Uniform uniform;
	final int byteOffset;

	public UniformInformation(String name, Uniform uniform, int byteOffset) {
		this.name = name;
		this.uniform = uniform;
		this.byteOffset = byteOffset;
	}

	public String getUniformLayoutName() {
		return uniform.getTypeName() + " " + name + ";";
	}

	public void setUniform(long address) {
		uniform.putInBuffer(address + byteOffset);
	}
}
