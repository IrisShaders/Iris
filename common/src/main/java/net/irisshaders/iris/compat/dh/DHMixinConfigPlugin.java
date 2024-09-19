package net.irisshaders.iris.compat.dh;

import com.sun.jna.NativeLibrary;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.system.SharedLibrary;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class DHMixinConfigPlugin implements IMixinConfigPlugin {

	private static SharedLibrary GLFW;

	static {
		Class<?> clas = GLFW.class;

		try {
			Field field = clas.getDeclaredField("GLFW");
			field.setAccessible(true);
			GLFW = (SharedLibrary) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {

	}
	@Override
	public void onLoad(String mixinPackage) {
		if (true) return;
		System.out.println("[Iris] Hijacking GLFW.");
		long base = GLFW.address();
		long maxSize = 0x100000;
		if (GLFW.getPath() != null) {
			try {
				maxSize = Files.size(new File(GLFW.getPath()).toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		long result = -1;
		for (long offset = 0; offset < maxSize; offset++) {
			if (MemoryUtil.memGetInt(base + offset) == 0x0000202b) {
				result = offset;
				break;
			}
		}

		try(var kernel = NativeLibrary.getInstance("Kernel32")) {
			long VirtualProtect = com.sun.jna.Pointer.nativeValue(kernel.getFunction("VirtualProtect"));
			long aa = MemoryUtil.nmemAlloc(4);
			JNI.invokePPPPP(base+result,4,0x40,aa, VirtualProtect);
			MemoryUtil.memPutInt(base+result, 0x000021A0);
			JNI.invokePPPPP(base+result,4,MemoryUtil.memGetInt(aa), aa, VirtualProtect);
			MemoryUtil.nmemFree(aa);
		}
	}

	@Override
	public String getRefMapperConfig() {
		return "";
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return IrisPlatformHelpers.getInstance().isModLoaded("distanthorizons");
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return List.of();
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
