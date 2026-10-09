package net.irisshaders.iris.mixin;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IrisMixinPlugin implements IMixinConfigPlugin {
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);

    public static boolean usingVulkan;

    static {
        BufferedReader reader = null;
        boolean check = true;
        try {
            reader = Files.newReader(IrisPlatformHelpers.getInstance().getGameDir().resolve("options.txt").toFile(), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            usingVulkan = false;
            check = false;
        }

        if (check) {
            Map<String, String> options = new HashMap<>();

            try {
                reader.lines().forEach(line -> {
                    try {
                        Iterator<String> iterator = OPTION_SPLITTER.split(line).iterator();
                        options.put((String) iterator.next(), (String) iterator.next());
                    } catch (Exception var3) {
                    }
                });
            } catch (Throwable var6) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (options.get("preferredGraphicsBackend") != null) {
                usingVulkan = options.get("preferredGraphicsBackend").toLowerCase(Locale.ROOT).contains("vulkan");
            } else {
                usingVulkan = false;
            }
        }
    }
	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("VKOnly")) return usingVulkan;
		return !usingVulkan;
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
		//if (targetClassName.contains("LevelRenderer")) {
		//	targetClass.methods.forEach(m -> System.out.println(m.name + m.desc));
		//}
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
