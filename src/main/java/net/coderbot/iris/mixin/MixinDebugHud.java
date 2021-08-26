package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.Objects;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
	@Unique
	private static final List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);

	@Unique
	private static final BufferPoolMXBean directPool;

	static {
		BufferPoolMXBean found = null;

		for (BufferPoolMXBean pool : pools) {
			if (pool.getName().equals("direct")) {
				found = pool;
				break;
			}
		}

		directPool = Objects.requireNonNull(found);
	}

	@Inject(method = "getRightText", at = @At("RETURN"))
	private void appendShaderPackText(CallbackInfoReturnable<List<String>> cir) {
		List<String> messages = cir.getReturnValue();

		messages.add("");
		messages.add("[Iris] Version: " + Iris.getFormattedVersion());
		messages.add("");

		if (Iris.getIrisConfig().areShadersEnabled()) {
			messages.add("[Iris] Shaderpack: " + Iris.getCurrentPackName());
		} else {
			messages.add("[Iris] Shaders are disabled");
		}

		messages.add(3, "Direct Buffers: +" + humanReadableByteCountBin(directPool.getMemoryUsed()));

		if (!FabricLoader.getInstance().isModLoaded("sodium")) {
			messages.add(3, "Native Memory: +" + humanReadableByteCountBin(getNativeMemoryUsage()));
		}
	}

	@Inject(method = "getLeftText", at = @At("RETURN"))
	private void appendShadowDebugText(CallbackInfoReturnable<List<String>> cir) {
		List<String> messages = cir.getReturnValue();

		if (!FabricLoader.getInstance().isModLoaded("sodium") && Iris.getCurrentPack().isPresent()) {
			messages.add(1, Formatting.YELLOW + "[Iris] Sodium isn't installed; you will have poor performance.");
			messages.add(2, Formatting.YELLOW + "[Iris] Install the compatible Sodium fork if you want to run benchmarks or get higher FPS!");
		}

		Iris.getPipelineManager().getPipeline().addDebugText(messages);
	}

	// stackoverflow.com/a/3758880
	private static String humanReadableByteCountBin(long bytes) {
		long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		if (absB < 1024) {
			return bytes + " B";
		}
		long value = absB;
		CharacterIterator ci = new StringCharacterIterator("KMGTPE");
		for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
			value >>= 10;
			ci.next();
		}
		value *= Long.signum(bytes);
		return String.format("%.3f %ciB", value / 1024.0, ci.current());
	}

	// From Sodium
	private static long getNativeMemoryUsage() {
			return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
	}
}
