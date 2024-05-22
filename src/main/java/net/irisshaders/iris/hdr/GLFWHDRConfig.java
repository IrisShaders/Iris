package net.irisshaders.iris.hdr;

import net.irisshaders.iris.Iris;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGammaRamp;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;

import java.nio.ByteBuffer;
import java.text.NumberFormat;

import static org.lwjgl.system.APIUtil.apiGetFunctionAddress;
import static org.lwjgl.system.Checks.CHECKS;
import static org.lwjgl.system.Checks.check;
import static org.lwjgl.system.JNI.invokePP;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWHDRConfig extends Struct<GLFWHDRConfig> {
	private static final long getHDRConfig = GLFW.getLibrary().getFunctionAddress("glfwGetHDRConfig");
	public static final int SIZEOF;

	/** The struct alignment in bytes. */
	public static final int ALIGNOF;

	/** The struct member offsets. */
	public static final int
		transfer_function,
		output_display_primary_red_x,
		output_display_primary_red_y,
		output_display_primary_green_x,
		output_display_primary_green_y,
		output_display_primary_blue_x,
		output_display_primary_blue_y,
		output_white_point_x,
		output_white_point_y,
		max_luminance,
		min_luminance,
		max_full_frame_luminance;

	static {
		Layout layout = __struct(
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES),
			__member(Integer.BYTES)
		);

		SIZEOF = layout.getSize();
		ALIGNOF = layout.getAlignment();

		transfer_function = layout.offsetof(0);
		output_display_primary_red_x = layout.offsetof(1);
		output_display_primary_red_y = layout.offsetof(2);
		output_display_primary_green_x = layout.offsetof(3);
		output_display_primary_green_y = layout.offsetof(4);
		output_display_primary_blue_x = layout.offsetof(5);
		output_display_primary_blue_y = layout.offsetof(6);
		output_white_point_x = layout.offsetof(7);
		output_white_point_y = layout.offsetof(8);
		max_luminance = layout.offsetof(9);
		min_luminance = layout.offsetof(10);
		max_full_frame_luminance = layout.offsetof(11);
	}

	protected GLFWHDRConfig(long address, @Nullable ByteBuffer container) {
		super(address, container);
	}

	@Override
	protected GLFWHDRConfig create(long address, @Nullable ByteBuffer container) {
		return new GLFWHDRConfig(address, container);
	}

	public int getTransferFunction() {
		return mgi(0);
	}

	public Vector2f getPrimaryRedCoord() {
		return new Vector2f(mgi(1) / 50000.0f, mgi(2) / 50000.0f);
	}

	public Vector2f getPrimaryGreenCoord() {
		return new Vector2f(mgi(3) / 50000.0f, mgi(4) / 50000.0f);
	}

	public Vector2f getPrimaryBlueCoord() {
		return new Vector2f(mgi(5) / 50000.0f, mgi(6) / 50000.0f);
	}

	public Vector2f getWhitePoint() {
		return new Vector2f(mgi(7) / 50000.0f, mgi(8) / 50000.0f);
	}

	public int getMaxLuminance() {
		return mgi(9);
	}

	public int getMinLuminance() {
		return mgi(10);
	}

	public int getMaxFullFrameLuminance() {
		return mgi(11);
	}

	@Override
	public String toString() {
		return "Max: " + getMaxLuminance() + " Min: " + getMinLuminance() + " White point: " + getWhitePoint().toString(NumberFormat.getNumberInstance());
	}

	private int mgi(int i) {
		return MemoryUtil.memGetInt(address() + (i * 4));
	}

	@Nullable
	@NativeType("GLFWhdrconfig const *")
	public static GLFWHDRConfig glfwGetHDRConfig(@NativeType("GLFWwindow *") long window) {
		if (getHDRConfig == 0L) {
			Iris.logger.fatal("Can't do this, no HDR in GLFW!");
			return null;
		}
		long __result = nglfwGetHDRConfig(window);
		return GLFWHDRConfig.createSafe(__result);
	}

	public static long nglfwGetHDRConfig(long window) {
		long __functionAddress = getHDRConfig;
		if (CHECKS) {
			check(window);
		}
		return invokePP(window, __functionAddress);
	}

	@Nullable
	public static GLFWHDRConfig createSafe(long address) {
		return address == NULL ? null : new GLFWHDRConfig(address, null);
	}

	@Override
	public int sizeof() {
		return SIZEOF;
	}
}
