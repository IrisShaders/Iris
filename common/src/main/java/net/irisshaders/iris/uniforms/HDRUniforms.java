package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.GLFWAccess;
import net.irisshaders.iris.GLFWHDRConfig;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.joml.Vector2f;

public class HDRUniforms {
	public static void addHDRUniforms(UniformHolder holder) {
		GLFWHDRConfig config = GLFWAccess.conf;

		holder.uniform2f(UniformUpdateFrequency.ONCE, "redPrimary", () -> new Vector2f(config.getPrimaryRedX(),config.getPrimaryRedY()));
		holder.uniform2f(UniformUpdateFrequency.ONCE, "bluePrimary", () -> new Vector2f(config.getPrimaryBlueX(),config.getPrimaryBlueY()));
		holder.uniform2f(UniformUpdateFrequency.ONCE, "greenPrimary", () -> new Vector2f(config.getPrimaryGreenX(),config.getPrimaryGreenY()));
		holder.uniform2f(UniformUpdateFrequency.ONCE, "whitePoint", () -> new Vector2f(config.getWhitePointX(),config.getWhitePointY()));
		holder.uniform1f(UniformUpdateFrequency.ONCE, "maxLuminance", () -> config.getMaxLuminance());
		holder.uniform1f(UniformUpdateFrequency.ONCE, "maxFrameLuminance", () -> config.getMaxFrameLuminance());
		holder.uniform1f(UniformUpdateFrequency.ONCE, "minLuminance", () -> config.getMinLuminance());
	}
}
